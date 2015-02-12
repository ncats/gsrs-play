package ix.idg.controllers;

import ix.core.controllers.search.SearchFactory;
import ix.core.models.Keyword;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.core.search.TextIndexer;
import ix.idg.models.Disease;
import ix.idg.models.Target;
import ix.utils.Util;
import ix.core.plugins.TextIndexerPlugin;

import play.Logger;
import play.cache.Cache;
import play.libs.ws.WS;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class IDGApp extends Controller {
    static public final int CACHE_TIMEOUT =
        play.Play.application()
        .configuration().getInt("ix.cache.time", 24*60*60); // 1 day
    
    static public final int MAX_FACETS = 6;
    static final int FACET_DIM = 20;
    static final int MAX_SEARCH_RESULTS = 1000;

    static final TextIndexer indexer = 
        play.Play.application().plugin(TextIndexerPlugin.class).getIndexer();


    public static class DiseaseRelevance
        implements Comparable<DiseaseRelevance> {
        public Disease disease;
        public Double zscore;
        public Double conf;
        public String comment;
        public Keyword omim;
        public Keyword uniprot;
        public List<DiseaseRelevance> lineage =
            new ArrayList<DiseaseRelevance>();

        DiseaseRelevance () {}
        public int compareTo (DiseaseRelevance dr) {
            double d = dr.zscore - zscore;
            if (d < 0) return -1;
            if (d > 0) return 1;
            return 0;
        }
    }
    
    public static final String[] TARGET_FACETS = {
        TcrdRegistry.CLASSIFICATION,
        TcrdRegistry.FAMILY,
        TcrdRegistry.DISEASE,
        TcrdRegistry.DRUG
    };

    public static final String[] DISEASE_FACETS = {
        TcrdRegistry.CLASSIFICATION,
        TcrdRegistry.FAMILY,
        UniprotRegistry.TARGET
    };

    public static final String[] ALL_FACETS = {
        TcrdRegistry.CLASSIFICATION,
        TcrdRegistry.FAMILY,
        TcrdRegistry.DISEASE,
        UniprotRegistry.TARGET,
        TcrdRegistry.DRUG
    };

    public static int[] paging (int rowsPerPage, int page, int total) {
        int max = (total+ rowsPerPage-1)/rowsPerPage;
        if (page < 0 || page > max) {
            throw new IllegalArgumentException ("Bogus page "+page);
        }
        
        int[] pages;
        if (max <= 10) {
            pages = new int[max];
            for (int i = 0; i < pages.length; ++i)
                pages[i] = i+1;
        }
        else if (page >= max-3) {
            pages = new int[10];
            for (int i = pages.length; --i >= 0; )
                pages[i] = max--;
        }
        else {
            pages = new int[10];
            int i = 0;
            for (; i < 7; ++i)
                pages[i] = i+1;
            if (page >= pages[i-1]) {
                // now shift
                pages[--i] = page;
                while (i-- > 0)
                    pages[i] = pages[i+1]-1;
            }
            pages[8] = max-1;
            pages[9] = max;
        }
        return pages;
    }
    
    public static Result index () {
        return ok (ix.idg.views.html.index2.render
                   ("Pharos: Illuminating the Druggable Genome",
                    DiseaseFactory.finder.findRowCount(),
                    TargetFactory.finder.findRowCount(),
                    LigandFactory.finder.findRowCount()));
    }

    public static Result error (int code, String mesg) {
        return ok (ix.idg.views.html.error.render(code, mesg));
    }

    static void getLineage (Map<Long, Disease> lineage, Disease d) {
        for (XRef ref : d.links) {
            if (Disease.class.getName().equals(ref.kind)) {
                for (Value prop : ref.properties) {
                    if (prop.label.equals("is_a")) {
                        Disease p = (Disease)ref.deRef();
                        lineage.put(d.id, p);
                        getLineage (lineage, p);
                    }
                }
            }
        }
    }

    public static Result target (final long id) {
        try {
            String sha1 = Util.sha1(request ());
            return Cache.getOrElse(sha1, new Callable<Result> () {
                    public Result call () throws Exception {
                        return _target (id);
                    }
                }, CACHE_TIMEOUT);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.idg.views.html.error.render(400, "Invalid target id: "+id));
        }
    }

    static Result _target (final long id) throws Exception {
        long start = System.currentTimeMillis();
        Target t = Cache.getOrElse
            (Target.class.getName()+":"+id, new Callable<Target> () {
                    public Target call () {
                        return TargetFactory.getTarget(id);
                    }
                }, CACHE_TIMEOUT);
        double ellapsed = (System.currentTimeMillis()-start)*1e-3;
        Logger.debug("Ellapsed time "+String.format("%1$.3fs", ellapsed)
                     +" to retrieve target "+id);
        
        List<DiseaseRelevance> diseases = new ArrayList<DiseaseRelevance>();
        List<DiseaseRelevance> uniprot = new ArrayList<DiseaseRelevance>();
        Map<Long, Disease> lineage = new HashMap<Long, Disease>();
        Map<Long, DiseaseRelevance> diseaseRel =
            new HashMap<Long, DiseaseRelevance>();
        for (XRef xref : t.links) {
            if (Disease.class.getName().equals(xref.kind)) {
                DiseaseRelevance dr = new DiseaseRelevance ();
                dr.disease = (Disease)xref.deRef();
                diseaseRel.put(dr.disease.id, dr);
                getLineage (lineage, dr.disease);
                for (Value p : xref.properties) {
                    if (TcrdRegistry.ZSCORE.equals(p.label))
                        dr.zscore = (Double)p.getValue();
                    else if (TcrdRegistry.CONF.equals(p.label))
                        dr.conf = (Double)p.getValue();
                    else if (UniprotRegistry
                             .DISEASE_RELEVANCE.equals(p.label)
                             || p.label.equals(dr.disease.name)) {
                        dr.comment = ((Text)p).text;
                    }
                }
                if (dr.zscore != null || dr.conf != null)
                    diseases.add(dr);
                else if (dr.comment != null) {
                    for (Keyword kw : dr.disease.synonyms) {
                        if ("MIM".equals(kw.label)) {
                            dr.omim = kw;
                        }
                        else if ("UniProt".equals(kw.label))
                            dr.uniprot = kw;
                    }
                    uniprot.add(dr);
                }
            }
        }
        Collections.sort(diseases);
        
        Set<Long> hasChildren = new HashSet<Long>();
        for (Disease d : lineage.values())
            hasChildren.add(d.id);
        
        List<DiseaseRelevance> prune = new ArrayList<DiseaseRelevance>();
        for (DiseaseRelevance dr : diseases) {
            if (!hasChildren.contains(dr.disease.id)) {
                prune.add(dr);
                for (Disease p = lineage.get(dr.disease.id); p != null; ) {
                    DiseaseRelevance parent = diseaseRel.get(p.id);
                    if (parent == null) {
                        parent = new DiseaseRelevance ();
                        parent.disease = p;
                    }
                    dr.lineage.add(parent);
                    p = lineage.get(p.id);
                }
                Logger.debug("Disease "+dr.disease.id+" ["+dr.disease.name
                             +"] has "+dr.lineage.size()+" lineage!");
            }
        }
        prune.addAll(uniprot); // append uniprot diseases
        List<Keyword> breadcrumb = new ArrayList<Keyword>();
        for (Value v : t.properties) {
            if (v.label.startsWith(TcrdRegistry.ChEMBL_PROTEIN_CLASS)) {
                Keyword kw = (Keyword)v;
                kw.href = ix.idg.controllers.routes.IDGApp.targets(null, 20, 1)
                    +"&facet="+kw.label+"/"+kw.term;
                breadcrumb.add(kw);
            }
        }
        // just make sure the order is correct
        Collections.sort(breadcrumb, new Comparator<Keyword>() {
                public int compare (Keyword kw1, Keyword kw2) {
                    return kw1.label.compareTo(kw2.label);
                }
            });
        
        return ok (ix.idg.views.html
                   .targetdetails.render(t, prune, breadcrumb));
    }

    public static String sha1 (TextIndexer.Facet facet, int value) {
        return Util.sha1(facet.getName(),
                         facet.getValues().get(value).getLabel());
    }
    
    public static String encode (TextIndexer.Facet facet) {
        try {
            return URLEncoder.encode(facet.getName(), "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+facet.getName(), ex);
        }
        return null;
    }
    
    public static String encode (TextIndexer.Facet facet, int i) {
        String value = facet.getValues().get(i).getLabel();
        try {
            return URLEncoder.encode(value, "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+value, ex);
        }
        return null;
    }

    public static String page (int rows, int page) {
        String url = "http"+ (request().secure() ? "s" : "") + "://"
            +request().host()
            +request().uri();
        if (url.charAt(url.length() -1) == '?') {
            url = url.substring(0, url.length()-1);
        }
        //Logger.debug(url);

        Map<String, Collection<String>> params =
            WS.url(url).getQueryParameters();
        
        // remove these
        params.remove("rows");
        params.remove("page");
        StringBuilder uri = new StringBuilder ("?rows="+rows+"&page="+page);
        for (Map.Entry<String, Collection<String>> me : params.entrySet()) {
            for (String v : me.getValue())
                uri.append("&"+me.getKey()+"="+v);
        }
        
        return uri.toString();
    }

    public static String url (String... remove) {
        String url = "http"+ (request().secure() ? "s" : "") + "://"
            +request().host()
            +request().uri();
        if (url.charAt(url.length()-1) == '?') {
            url = url.substring(0, url.length()-1);
        }
        //Logger.debug(">> uri="+request().uri());

        StringBuilder uri = new StringBuilder ("?");
        Map<String, Collection<String>> params =
            WS.url(url).getQueryParameters();
        for (Map.Entry<String, Collection<String>> me : params.entrySet()) {
            boolean matched = false;
            for (String s : remove)
                if (s.equals(me.getKey())) {
                    matched = true;
                    break;
                }
            
            if (!matched) {
                for (String v : me.getValue())
                    if (v != null)
                        uri.append(me.getKey()+"="+v+"&");
            }
        }
        //Logger.debug(">> "+uri);
        return uri.substring(0, uri.length()-1);
    }

    public static String queryString (String... params) {
        Map<String, String[]> query = new HashMap<String, String[]>();
        for (String p : params) {
            String[] values = request().queryString().get(p);
            if (values != null)
                query.put(p, values);
        }
        
        return query.isEmpty() ? "" : "?"+queryString (query);
    }
    
    public static String queryString (Map<String, String[]> queryString) {
        //Logger.debug("QueryString: "+queryString);
        StringBuilder q = new StringBuilder ();
        for (Map.Entry<String, String[]> me : queryString.entrySet()) {
            for (String s : me.getValue()) {
                if (q.length() > 0)
                    q.append('&');
                q.append(me.getKey()+"="
                         + ("q".equals(me.getKey()) ? quote (s) : s));
            }
        }
        return q.toString();
    }

    public static boolean hasFacet (TextIndexer.Facet facet, int i) {
        String[] facets = request().queryString().get("facet");
        if (facets != null) {
            for (String f : facets) {
                String[] toks = f.split("/");
                if (toks.length == 2) {
                    try {
                        String name = toks[0];
                        String value = toks[1];
                        /*
                        Logger.debug("Searching facet "+name+"/"+value+"..."
                                     +facet.getName()+"/"
                                     +facet.getValues().get(i).getLabel());
                        */
                        boolean matched = name.equals(facet.getName())
                            && value.equals(facet.getValues()
                                            .get(i).getLabel());
                        
                        if (matched)
                            return matched;
                    }
                    catch (Exception ex) {
                        Logger.trace("Can't URL decode string", ex);
                    }
                }
            }
        }
        
        return false;
    }

    static List<TextIndexer.Facet> getFacets
        (final Class kind, final int fdim) {
        try {
            TextIndexer.SearchResult result =
                SearchFactory.search(kind, null, 0, 0, fdim, null);
            return result.getFacets();
        }
        catch (IOException ex) {
            Logger.trace("Can't retrieve facets for "+kind, ex);
        }
        return new ArrayList<TextIndexer.Facet>();
    }

    static TextIndexer.Facet[] filter (List<TextIndexer.Facet> facets,
                                       String... names) {
        if (names == null || names.length == 0)
            return facets.toArray(new TextIndexer.Facet[0]);
        
        List<TextIndexer.Facet> filtered = new ArrayList<TextIndexer.Facet>();
        for (String n : names) {
            for (TextIndexer.Facet f : facets)
                if (n.equals(f.getName()))
                    filtered.add(f);
        }
        return filtered.toArray(new TextIndexer.Facet[0]);
    }

    static TextIndexer.SearchResult getSearchResult
        (final Class kind, final String q, final int total) {
        
        final Map<String, String[]> query =  new HashMap<String, String[]>();
        query.putAll(request().queryString());
                
        List<String> qfacets = new ArrayList<String>();
        final boolean hasFacets = q != null && q.indexOf('/') > 0;
        if (hasFacets) {
            // treat this as facet
            if (query.get("facet") != null) {
                for (String f : query.get("facet"))
                    qfacets.add(f);
            }
            qfacets.add("MeSH/"+q);
            query.put("facet", qfacets.toArray(new String[0]));
        }
        //query.put("drill", new String[]{"down"});
        
        List<String> args = new ArrayList<String>();
        args.add(request().uri());
        if (q != null)
            args.add(q);
        for (String f : qfacets)
            args.add(f);
        Collections.sort(args);
        
        // filtering
        try {
            long start = System.currentTimeMillis();
            String sha1 = Util.sha1(args.toArray(new String[0]));
            TextIndexer.SearchResult result = Cache.getOrElse
                (sha1, new Callable<TextIndexer.SearchResult>() {
                     public TextIndexer.SearchResult call () throws Exception {
                         return SearchFactory.search
                         (kind, hasFacets ? null : q,
                          total, 0, FACET_DIM, query);
                     }
                 }, CACHE_TIMEOUT);
            
            double ellapsed = (System.currentTimeMillis() - start)*1e-3;
            Logger.debug(String.format("Ellapsed %1$.3fs to retrieve "
                                       +"results for "
                                       +sha1.substring(0, 8)+"...",
                                       ellapsed));
            
            if (hasFacets && result.count() == 0) {
                start = System.currentTimeMillis();
                // empty result.. perhaps the query contains /'s
                Cache.remove(sha1); // clear cache
                result = Cache.getOrElse
                    (sha1, new Callable<TextIndexer.SearchResult>() {
                            public TextIndexer.SearchResult call ()
                                throws Exception {
                                return SearchFactory.search
                                (kind, q, total, 0, FACET_DIM,
                                 request().queryString());
                            }
                        }, CACHE_TIMEOUT);
                ellapsed = (System.currentTimeMillis() - start)*1e-3;
                Logger.debug(String.format("Retry as query; "
                                           +"ellapsed %1$.3fs to retrieve "
                                           +"results for "
                                           +sha1.substring(0, 8)+"...",
                                           ellapsed));
            }
            
            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.trace("Unable to perform search", ex);
        }
        return null;
    }

    public static Result targets (final String q,
                                  final int rows, final int page) {
        try {
            String sha1 = Util.sha1(request ());
            return Cache.getOrElse(sha1, new Callable<Result>() {
                    public Result call () throws Exception {
                        return _targets (q, rows, page);
                    }
                }, CACHE_TIMEOUT);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return badRequest (ix.idg.views.html.error.render
                               (404, "Invalid page requested: "+page+ex));
        }
    }
    
    static Result _targets (final String q, int rows, final int page)
        throws Exception {
        Logger.debug("Targets: q="+q+" rows="+rows+" page="+page);
        final int total = TargetFactory.finder.findRowCount();
        if (request().queryString().containsKey("facet") || q != null) {
            TextIndexer.SearchResult result =
                getSearchResult (Target.class, q, total);
            
            TextIndexer.Facet[] facets = filter
                (result.getFacets(), TARGET_FACETS);
            List<Target> targets = new ArrayList<Target>();
            int[] pages = new int[0];
            if (result.count() > 0) {
                rows = Math.min(result.count(), Math.max(1, rows));
                pages = paging (rows, page, result.count());
                
                for (int i = (page-1)*rows, j = 0; j < rows
                         && i < result.count(); ++j, ++i) {
                    targets.add((Target)result.getMatches().get(i));
                }
            }
            
            return ok (ix.idg.views.html.targets.render
                       (page, rows, result.count(),
                        pages, facets, targets));
        }
        else {
            String cache = Target.class.getName()+".facets";
            if (System.currentTimeMillis() - CACHE_TIMEOUT
                <= indexer.lastModified())
                Cache.remove(cache);
            TextIndexer.Facet[] facets = Cache.getOrElse
                (cache, new Callable<TextIndexer.Facet[]>() {
                        public TextIndexer.Facet[] call () {
                            return filter (getFacets (Target.class, 20),
                                           TARGET_FACETS);
                        }
                    }, CACHE_TIMEOUT);
            
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = paging (rows, page, total);               
            
            List<Target> targets =
                TargetFactory.getTargets(rows, (page-1)*rows, null);
            
            return ok (ix.idg.views.html.targets.render
                       (page, rows, total, pages, facets, targets));
        }
    }

    public static Result search (String kind) {
        try {
            String q = request().getQueryString("q");
            if (kind != null && !"".equals(kind)) {
                if (Target.class.isAssignableFrom(Class.forName(kind)))
                    return redirect (routes.IDGApp.targets(q, 20, 1));
                if (Disease.class.isAssignableFrom(Class.forName(kind)))
                    return redirect (routes.IDGApp.diseases(q, 10, 1));
            }
            
            // generic entity search..
            return search (5);
        }
        catch (Exception ex) {
            Logger.debug("Can't resolve class: "+kind, ex);
        }
            
        return badRequest (ix.idg.views.html.error.render
                           (400, "Invalid request: "+request().uri()));
    }

    static <T> List<T> filter (Class<T> cls, List values, int max) {
        List<T> fv = new ArrayList<T>();
        for (Object v : values) {
            if (cls.isAssignableFrom(v.getClass()) && fv.size() < max) {
                fv.add((T)v);
            }
        }
        return fv;
    }

    /**
     * make sure if the argument doesn't have quote then add them
     */
    static Pattern regex = Pattern.compile("\"([^\"]+)");
    static String quote (String s) {
        Matcher m = regex.matcher(s);
        if (m.find())
            return s; // nothing to do.. already have quote
        return "\""+s+"\"";
    }

    public static Result search (final int rows) {
        try {
            String sha1 = Util.sha1(request ());
            return Cache.getOrElse(sha1, new Callable<Result> () {
                    public Result call () throws Exception {
                        return _search (rows);
                    }
                }, CACHE_TIMEOUT);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError (ix.idg.views.html.error.render
                                        (500, "Unable to fullfil request"));
        }
    }

    static Result _search (int rows) throws Exception {
        final String query = request().getQueryString("q");
        Logger.debug("Query: \""+query+"\"");

        TextIndexer.SearchResult result = null;            
        if (query.indexOf('/') > 0) { // use mesh facet
            final Map<String, String[]> queryString =
                new HashMap<String, String[]>();
            queryString.putAll(request().queryString());
            
            // append this facet to the list 
            List<String> f = new ArrayList<String>();
            f.add("MeSH/"+query);
            String[] ff = queryString.get("facet");
            if (ff != null) {
                for (String fv : ff)
                    f.add(fv);
            }
            queryString.put("facet", f.toArray(new String[0]));
            long start = System.currentTimeMillis();
            result = Cache.getOrElse
                (Util.sha1(queryString.get("facet")),
                 new Callable<TextIndexer.SearchResult>() {
                     public TextIndexer.SearchResult
                         call ()  throws Exception {
                         return SearchFactory.search
                         (null, null, MAX_SEARCH_RESULTS,
                          0, FACET_DIM, queryString);
                     }
                 }, CACHE_TIMEOUT);
            double ellapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug
                ("1. Ellapsed time "+String.format("%1$.3fs", ellapsed));
        }

        if (result == null || result.count() == 0) {
            long start = System.currentTimeMillis();                
            result = Cache.getOrElse
                (Util.sha1(request(), "facet", "q"),
                 new Callable<TextIndexer.SearchResult>() {
                     public TextIndexer.SearchResult
                                call () throws Exception {
                         return SearchFactory.search
                         (null, quote (query), MAX_SEARCH_RESULTS, 0,
                          FACET_DIM, request().queryString());
                     }
                 }, CACHE_TIMEOUT);
            double ellapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug
                ("2. Ellapsed time "+String.format("%1$.3fs", ellapsed));
        }
        
        TextIndexer.Facet[] facets = filter
            (result.getFacets(), ALL_FACETS);
        
        int max = Math.min(rows, Math.max(1,result.count()));
        int total = 0, totalTargets = 0, totalDiseases = 0;
        for (TextIndexer.Facet f : result.getFacets()) {
            if (f.getName().equals("ix.Class")) {
                for (TextIndexer.FV fv : f.getValues()) {
                    if (Target.class.getName().equals(fv.getLabel())) {
                        totalTargets = fv.getCount();
                        total += totalTargets;
                    }
                    else if (Disease.class.getName()
                             .equals(fv.getLabel())) {
                        totalDiseases = fv.getCount();
                        total += totalDiseases;
                    }
                }
            }
        }
        
        List<Target> targets =
            filter (Target.class, result.getMatches(), max);
        List<Disease> diseases =
            filter (Disease.class, result.getMatches(), max);
        
        return ok (ix.idg.views.html.search.render
                   (query, total, facets,
                    targets, totalTargets, diseases, totalDiseases));
    }

    public static Result ligands () {
        return ok (ix.idg.views.html.ligands.render());
    }

    public static Result disease (final long id) {
        try {
            String sha1 = Util.sha1(request ());
            return Cache.getOrElse(sha1, new Callable<Result>() {
                    public Result call () throws Exception {
                        return _disease (id);
                    }
                }, CACHE_TIMEOUT);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.idg.views.html.error.render(500, "Internal server error"));
        }           
    }

    static Result _disease (long id) throws Exception {
        Disease d = DiseaseFactory.getDisease(id);
        for (XRef xref : d.links) {
            //Logger.debug(xref.refid + "/" + xref.kind + "/" + xref.deRef());
            List<Value> props = xref.properties;
            for (Value prop: props) {
                //Logger.debug("prop = " + prop);
                if (prop instanceof Text) {
                    Text text = (Text) prop;
                    //Logger.debug("\ttext = " + text.text +"/"+text.label);
                } else if (prop instanceof Keyword) {
                    Keyword kw = (Keyword) prop;
                    //Logger.debug("\tkw = " + kw.term +"/"+kw.label);
                }
            }
            //System.out.println();
        }
        
        // resolve the targets for this disease
        List<Target> targets = new ArrayList<Target>();
        for (XRef ref : d.links) {
            if (Target.class.isAssignableFrom(Class.forName(ref.kind))) {
                Target t = (Target) ref.deRef();
                targets.add(t);
            }
        }
        return ok(ix.idg.views.html.diseasedetails.render
                  (d, targets.toArray(new Target[]{})));
    }

    public static Result diseases (final String q,
                                   final int rows, final int page) {
        try {
            String sha1 = Util.sha1(request ());
            return Cache.getOrElse(sha1, new Callable<Result>() {
                    public Result call () throws Exception {
                        return _diseases (q, rows, page);
                    }
                }, CACHE_TIMEOUT);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return badRequest(ix.idg.views.html.error.render
                              (404, "Invalid page requested: " + page));
        }
    }
    
    static Result _diseases (String q, int rows, int page) throws Exception {
        Logger.debug("Diseases: rows=" + rows + " page=" + page);
        final int total = DiseaseFactory.finder.findRowCount();
        if (request().queryString().containsKey("facet") || q != null) {
            TextIndexer.SearchResult result =
                getSearchResult (Disease.class, q, total);
            
            TextIndexer.Facet[] facets = filter
                (result.getFacets(), DISEASE_FACETS);
            
            List<Disease> diseases = new ArrayList<Disease>();
            int[] pages = new int[0];
            if (result.count() > 0) {
                rows = Math.min(result.count(), Math.max(1, rows));
                pages = paging (rows, page, result.count());
                for (int i = (page - 1) * rows, j = 0; j < rows
                         && i < result.count(); ++j, ++i) {
                    diseases.add((Disease) result.getMatches().get(i));
                }
                }
            
            return ok(ix.idg.views.html.diseases.render
                      (page, rows, result.count(),
                       pages, facets, diseases));
        }
        else {
            TextIndexer.Facet[] facets = Cache.getOrElse
                (Disease.class.getName()+".facets",
                 new Callable<TextIndexer.Facet[]>() {
                     public TextIndexer.Facet[] call() {
                         return filter(getFacets(Disease.class, 20),
                                       DISEASE_FACETS);
                     }
                 }, CACHE_TIMEOUT);
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = paging(rows, page, total);
            
            List<Disease> diseases =
                DiseaseFactory.getDiseases(rows, (page - 1) * rows, null);
            
            return ok(ix.idg.views.html.diseases.render
                      (page, rows, total, pages, facets, diseases));
        }
    }
}
