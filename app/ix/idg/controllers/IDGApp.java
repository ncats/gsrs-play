package ix.idg.controllers;

import ix.core.controllers.search.SearchFactory;
import ix.core.controllers.KeywordFactory;
import ix.core.controllers.PredicateFactory;
import ix.core.models.Keyword;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.core.models.Predicate;
import ix.core.search.TextIndexer;
import static ix.core.search.TextIndexer.*;
import ix.idg.models.Disease;
import ix.idg.models.Target;
import ix.utils.Util;
import ix.core.plugins.TextIndexerPlugin;
import ix.ncats.controllers.App;

import play.Logger;
import play.cache.Cache;
import play.libs.ws.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Call;
import com.avaje.ebean.Expr;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class IDGApp extends App {
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

    static class IDGFacetDecorator extends FacetDecorator {
        IDGFacetDecorator (Facet facet) {
            super (facet, true, 6);
        }

        @Override
        public String name () {
            return super.name().replaceAll("IDG", "").trim();
        }
        
        @Override
        public String label (int i) {
            String label = super.label(i);
            if (super.name().equals(Target.IDG_DEVELOPMENT)) {
                for (Target.TDL tdl : EnumSet.allOf(Target.TDL.class)) {
                    if (label.equalsIgnoreCase(tdl.name)) {
                        return "<span class=\"label label-"+tdl.label+"\""
                            +" data-toggle=\"tooltip\" data-placement=\"right\""
                            +" data-html=\"true\" title=\""+tdl.desc+"\">"
                            +tdl.name+"</span>";
                    }
                }
                assert false: "Unknown TDL label: "+label;
            }
            return label;
        }
    }
    
    public static final String[] TARGET_FACETS = {
        TcrdRegistry.DEVELOPMENT,
        TcrdRegistry.FAMILY,
        TcrdRegistry.DISEASE,
        TcrdRegistry.DRUG
    };

    public static final String[] DISEASE_FACETS = {
        TcrdRegistry.DEVELOPMENT,
        TcrdRegistry.FAMILY,
        UniprotRegistry.TARGET
    };

    public static final String[] ALL_FACETS = {
        TcrdRegistry.DEVELOPMENT,
        TcrdRegistry.FAMILY,
        TcrdRegistry.DISEASE,
        UniprotRegistry.TARGET,
        TcrdRegistry.DRUG
    };

    static FacetDecorator[] decorate (Facet... facets) {
        FacetDecorator[] decors = new FacetDecorator[facets.length];
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors[i] = new IDGFacetDecorator (facets[i]);
        }
        return decors;
    }
    
    public static Result about() {
        return ok (ix.idg.views.html.about.render
                           ("Pharos: Illuminating the Druggable Genome"));
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
        if (!lineage.containsKey(d.id)) {
            for (XRef ref : d.links) {
                if (Disease.class.getName().equals(ref.kind)) {
                    for (Value prop : ref.properties) {
                        if (prop.label.equals(DiseaseOntologyRegistry.IS_A)) {
                            Disease p = (Disease)ref.deRef();
                            lineage.put(d.id, p);
                            getLineage (lineage, p);
                        }
                    }
                }
            }
        }
    }

    static List<Disease> getLineage (Disease d) {
        Vector<Disease> lineage = new Vector<Disease>();
        for (XRef ref: d.links) {
            if (Disease.class.getName().equals(ref.kind)) {
                for (Value prop : ref.properties) {
                    if (prop.label.startsWith
                        (DiseaseOntologyRegistry.LINEAGE)) {
                        int pos = prop.label.indexOf(':');
                        if (pos > 0) {
                            int index = Integer.parseInt
                                (prop.label.substring(pos+1));
                            Disease p = (Disease)ref.deRef();
                            if (index >= lineage.size())
                                lineage.setSize(index+1);
                            lineage.setElementAt(p, index);
                        }
                    }
                }
            }
        }
        return lineage;
    }

    public static Result target (final String name) {
        try {
            long start = System.currentTimeMillis();        
            Target target = getOrElse (name, new Callable<Target> () {
                    public Target call () throws Exception {
                        List<Target> targets = TargetFactory.finder.where()
                           .eq("synonyms.term", name).findList();
                        if (!targets.isEmpty()) {
                            if (targets.size() > 1) {
                                Logger.warn("Request name \""+name+"\" maps "
                                            +"to "+targets.size()+" targets; "
                                            +"return the first one!");
                            }
                            return targets.iterator().next();
                        }
                        return null;
                    }
                });
            double ellapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug("Ellapsed time "+String.format("%1$.3fs", ellapsed)
                         +" to retrieve target "+name+" "+target);
            
            if (target == null) {
                return notFound (ix.idg.views.html.error.render
                                 (400, "Unknown target name: "+name));
            }
            return target (target);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.idg.views.html.error.render
                 (500, "Internal server error: "+ex.getMessage()));
        }
    }

    public static String getTargetId (Target t) {
        Keyword kw = t.getSynonym(UniprotRegistry.ACCESSION);
        return kw != null ? kw.term : null;
    }
    
    static Result target (final Target t) throws Exception {
        try {
            Result result = getOrElse
                (Util.sha1(request ()), new Callable<Result> () {
                        public Result call () throws Exception {
                            return getTargetResult (t);
                        }
                    });
            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static Result getTargetResult (final Target t) throws Exception {
        List<DiseaseRelevance> diseases = getOrElse
            ("/target/"+t.id+"/diseases",
             new Callable<List<DiseaseRelevance>> () {
                 public List<DiseaseRelevance> call () throws Exception {
                     return getDiseaseRelevances (t);
                 }
             });
        
        List<Keyword> breadcrumb = new ArrayList<Keyword>();
        for (Value v : t.properties) {
            if (v.label.startsWith(TcrdRegistry.ChEMBL_PROTEIN_CLASS)) {
                Keyword kw = (Keyword)v;
                String url = ix.idg.controllers
                    .routes.IDGApp.targets(null, 30, 1).url();
                kw.href = url + (url.indexOf('?') > 0 ? "&":"?")
                    +"facet="+kw.label+"/"+kw.term;
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
                   .targetdetails.render(t, diseases, breadcrumb));
    }

    static List<DiseaseRelevance>
        getDiseaseRelevances (Target t) throws Exception {
        List<DiseaseRelevance> diseases = new ArrayList<DiseaseRelevance>();
        List<DiseaseRelevance> uniprot = new ArrayList<DiseaseRelevance>();
        Map<Long, Disease> lineage = new HashMap<Long, Disease>();
        Map<Long, DiseaseRelevance> diseaseRel =
            new HashMap<Long, DiseaseRelevance>();
        long start = System.currentTimeMillis();
        for (XRef xref : t.links) {
            if (Disease.class.getName().equals(xref.kind)) {
                DiseaseRelevance dr = new DiseaseRelevance ();
                dr.disease = (Disease)xref.deRef();
                diseaseRel.put(dr.disease.id, dr);
                getLineage (lineage, dr.disease);
                /*
                { Disease d = dr.disease;
                    for (Disease parent : getLineage (d)) {
                        lineage.put(d.id, parent);
                        d = parent;
                    }
                }
                */
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
        double ellapsed = (System.currentTimeMillis()-start)*1e-3;
        Logger.debug("Ellapsed time "+String.format("%1$.3fs", ellapsed)
                     +" to retrieve disease relevance for target "+t.id);

        List<DiseaseRelevance> prune = new ArrayList<DiseaseRelevance>();       
        Set<Long> hasChildren = new HashSet<Long>();
        for (Disease d : lineage.values())
            hasChildren.add(d.id);

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

        return prune;
    }

    public static Result targets (final String q,
                                  final int rows, final int page) {
        try {
            String sha1 = Util.sha1(request ());
            return getOrElse (sha1, new Callable<Result>() {
                    public Result call () throws Exception {
                        return _targets (q, rows, page);
                    }
                });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return badRequest (ix.idg.views.html.error.render
                               (404, "Invalid page requested: "+ex));
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
                        pages, decorate (facets), targets));
        }
        else {
            String cache = Target.class.getName()+".facets";
            if (System.currentTimeMillis() - CACHE_TIMEOUT
                <= indexer.lastModified())
                Cache.remove(cache);
            TextIndexer.Facet[] facets = Cache.getOrElse
                (cache, new Callable<TextIndexer.Facet[]>() {
                        public TextIndexer.Facet[] call () {
                            return filter (getFacets (Target.class, FACET_DIM),
                                           TARGET_FACETS);
                        }
                    }, CACHE_TIMEOUT);
            
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = paging (rows, page, total);               
            
            List<Target> targets =
                TargetFactory.getTargets(rows, (page-1)*rows, null);
            
            return ok (ix.idg.views.html.targets.render
                       (page, rows, total, pages, decorate (facets), targets));
        }
    }

    public static Keyword[] getAncestry (final String facet,
                                         final String predicate) {
        try {
            return getOrElse
                (predicate+"/"+facet, new Callable<Keyword[]> () {
                        public Keyword[] call () throws Exception {
                            return _getAncestry (facet, predicate);
                        }
                    });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Keyword[0];
    }
    
    public static Keyword[] _getAncestry (String facet, String predicate) {
        List<Keyword> ancestry = new ArrayList<Keyword>();
        String[] toks = facet.split("/");
        if (toks.length == 2) {
            List<Keyword> terms = KeywordFactory.finder.where
                (Expr.and(Expr.eq("label", toks[0]),
                          Expr.eq("term", toks[1]))).
                findList();
            if (!terms.isEmpty()) {
                Keyword anchor = terms.iterator().next();
                List<Predicate> pred = PredicateFactory.finder
                    .where().conjunction()
                    .add(Expr.eq("subject.refid", anchor.id))
                    .add(Expr.eq("subject.kind", anchor.getClass().getName()))
                    .add(Expr.eq("predicate", predicate))
                    .findList();
                if (!pred.isEmpty()) {
                    for (XRef ref : pred.iterator().next().objects) {
                        if (ref.kind.equals(anchor.getClass().getName())) {
                            Keyword kw = (Keyword)ref.deRef();
                            String url = ix.idg.controllers
                                .routes.IDGApp.targets(null, 30, 1).url();
                            kw.href = url + (url.indexOf('?') > 0 ? "&":"?")
                                +"facet="+kw.label+"/"+kw.term;
                            ancestry.add(kw);
                        }
                    }
                    /* This sort is necessary to ensure the correct
                     * ordering of the nodes. This only works because
                     * the node labels have proper encoding for the level
                     * embedded in the label.
                     */
                    Collections.sort(ancestry, new Comparator<Keyword>() {
                            public int compare (Keyword kw1, Keyword kw2) {
                                return kw1.label.compareTo(kw2.label);
                            }
                        });
                }
                //ancestry.add(anchor);
            }
            else {
                Logger.warn("Uknown Keyword: label=\""+toks[0]+"\" term=\""
                            +toks[1]+"\"");
            }
        }
        return ancestry.toArray(new Keyword[0]);
    }

    public static Keyword[] getProteinAncestry (String facet) {
        return getAncestry (facet, TcrdRegistry.ChEMBL_PROTEIN_ANCESTRY);
    }

    public static Result search (String kind) {
        try {
            String q = request().getQueryString("q");
            if (kind != null && !"".equals(kind)) {
                if (Target.class.isAssignableFrom(Class.forName(kind)))
                    return redirect (routes.IDGApp.targets(q, 30, 1));
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

    public static Result search (final int rows) {
        try {
            String sha1 = Util.sha1(request ());
            return getOrElse(sha1, new Callable<Result> () {
                    public Result call () throws Exception {
                        return _search (rows);
                    }
                });
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
            result = getOrElse
                (Util.sha1(queryString.get("facet")),
                 new Callable<TextIndexer.SearchResult>() {
                     public TextIndexer.SearchResult
                         call ()  throws Exception {
                         return SearchFactory.search
                         (null, null, MAX_SEARCH_RESULTS,
                          0, FACET_DIM, queryString);
                     }
                 });
            double ellapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug
                ("1. Ellapsed time "+String.format("%1$.3fs", ellapsed));
        }

        if (result == null || result.count() == 0) {
            long start = System.currentTimeMillis();                
            result = getOrElse
                (Util.sha1(request(), "facet", "q"),
                 new Callable<TextIndexer.SearchResult>() {
                     public TextIndexer.SearchResult
                                call () throws Exception {
                         return SearchFactory.search
                         (null, quote (query), MAX_SEARCH_RESULTS, 0,
                          FACET_DIM, request().queryString());
                     }
                 });
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
                   (query, total, decorate (facets),
                    targets, totalTargets, diseases, totalDiseases));
    }

    public static Result ligands () {
        return ok (ix.idg.views.html.ligands.render());
    }

    public static Result disease (final long id) {
        try {
            String sha1 = Util.sha1(request ());
            return getOrElse (sha1, new Callable<Result>() {
                    public Result call () throws Exception {
                        return _disease (id);
                    }
                });
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
                       pages, decorate (facets), diseases));
        }
        else {
            TextIndexer.Facet[] facets = getOrElse
                (Disease.class.getName()+".facets",
                 new Callable<TextIndexer.Facet[]>() {
                     public TextIndexer.Facet[] call() {
                         return filter(getFacets(Disease.class, 30),
                                       DISEASE_FACETS);
                     }
                 });
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = paging(rows, page, total);
            
            List<Disease> diseases =
                DiseaseFactory.getDiseases(rows, (page - 1) * rows, null);
            
            return ok(ix.idg.views.html.diseases.render
                      (page, rows, total, pages, decorate (facets), diseases));
        }
    }
}
