package ix.ncats.controllers;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
import java.util.concurrent.Callable;

import play.Logger;
import play.cache.Cache;
import play.libs.ws.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Call;

import ix.core.search.TextIndexer;
import static ix.core.search.TextIndexer.*;
import tripod.chem.indexer.StructureIndexer;
import static tripod.chem.indexer.StructureIndexer.*;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.controllers.search.SearchFactory;
import ix.utils.Util;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.util.MolHandler;


/**
 * Basic plumbing for an App
 */
public class App extends Controller {
    public static final int CACHE_TIMEOUT =
        play.Play.application()
        .configuration().getInt("ix.cache.time", 24*60*60); // 1 day
    
    public static final int FACET_DIM = 20;
    public static final int MAX_SEARCH_RESULTS = 1000;

    public static final TextIndexer textIndexer = 
        play.Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    public static final StructureIndexer strucIndexer =
        play.Play.application().plugin(StructureIndexerPlugin.class).getIndexer();

    public static class FacetDecorator {
        final public Facet facet;
        final public int max;
        final public boolean raw;

        public FacetDecorator (Facet facet) {
            this (facet, false, 6);
        }
        public FacetDecorator (Facet facet, boolean raw, int max) {
            this.facet = facet;
            this.raw = raw;
            this.max = max;
        }

        public String name () { return facet.getName(); }
        public int size () { return facet.getValues().size(); }
        public String label (int i) {
            return facet.getValues().get(i).getLabel();
        }
        public String value (int i) {
            return facet.getValues().get(i).getCount().toString();
        }
    }
    
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
            pages[0] = 1;
            pages[1] = 2;
            pages[2] = 0;
            for (int i = pages.length; --i > 2; )
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
                pages[0] = 1;
                pages[1] = 2;
                pages[2] = 0;
            }
            pages[8] = max-1;
            pages[9] = max;
        }
        return pages;
    }

    public static String sha1 (Facet facet, int value) {
        return Util.sha1(facet.getName(),
                         facet.getValues().get(value).getLabel());
    }

    /**
     * make sure if the argument doesn't have quote then add them
     */
    static Pattern regex = Pattern.compile("\"([^\"]+)");
    public static String quote (String s) {
        Matcher m = regex.matcher(s);
        if (m.find())
            return s; // nothing to do.. already have quote
        return "\""+s+"\"";
    }
    
    public static String decode (String s) {
        try {
            return URLDecoder.decode(s, "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+s, ex);
        }
        return null;
    }

    public static String encode (Facet facet) {
        try {
            return URLEncoder.encode(facet.getName(), "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+facet.getName(), ex);
        }
        return null;
    }
    
    public static String encode (Facet facet, int i) {
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
        //params.remove("rows");
        params.remove("page");
        StringBuilder uri = new StringBuilder ("?page="+page);
        for (Map.Entry<String, Collection<String>> me : params.entrySet()) {
            for (String v : me.getValue())
                uri.append("&"+me.getKey()+"="+v);
        }
        
        return uri.toString();
    }

    public static String truncate (String str, int size) {
        if (str.length() <= size) return str;
        return str.substring(0, size)+"...";
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

    /**
     * more specific version that only remove parameters based on 
     * given facets
     */
    public static String url (FacetDecorator[] facets, String... others) {
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
            if (me.getKey().equals("facet")) {
                for (String v : me.getValue())
                    if (v != null) {
                        String s = decode (v);
                        boolean matched = false;
                        for (FacetDecorator f : facets) {
                            if (s.startsWith(f.name())) {
                                matched = true;
                                break;
                            }
                        }
                        
                        if (!matched)
                            uri.append(me.getKey()+"="+v+"&");
                    }
            }
            else {
                boolean matched = false;
                for (String s : others) {
                    if (s.equals(me.getKey())) {
                        matched = true;
                        break;
                    }
                }
                
                if (!matched)
                    for (String v : me.getValue())
                        if (v != null)
                            uri.append(me.getKey()+"="+v+"&");
            }
        }
        
        Logger.debug(">> "+uri);
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

    public static boolean hasFacet (Facet facet, int i) {
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

    public static List<Facet> getFacets (final Class kind, final int fdim) {
        try {
            SearchResult result =
                SearchFactory.search(kind, null, 0, 0, fdim, null);
            return result.getFacets();
        }
        catch (IOException ex) {
            Logger.trace("Can't retrieve facets for "+kind, ex);
        }
        return new ArrayList<Facet>();
    }

    public static Facet[] filter (List<Facet> facets, String... names) {
        if (names == null || names.length == 0)
            return facets.toArray(new Facet[0]);
        
        List<Facet> filtered = new ArrayList<Facet>();
        for (String n : names) {
            for (Facet f : facets)
                if (n.equals(f.getName()))
                    filtered.add(f);
        }
        return filtered.toArray(new Facet[0]);
    }

    public static String randvar (int size) {
        Random rand = new Random ();
        char[] alpha = {'a','b','c','x','y','z'};
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < size; ++i)
            sb.append(alpha[rand.nextInt(alpha.length)]);
        return sb.toString();
    }
    
    public static String randvar () {
        return randvar (5);
    }

    public static SearchResult getSearchResult
        (final Class kind, final String q, final int total) {
        return getSearchResult (textIndexer, kind, q, total);
    }
    
    public static SearchResult getSearchResult
        (final TextIndexer indexer, final Class kind,
         final String q, final int total) {
        
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
            SearchResult result;
            if (indexer != textIndexer) {
                // if it's an ad-hoc indexer, then we don't bother caching
                //  the results
                result = SearchFactory.search
                    (indexer, kind, hasFacets ? null : q,
                     total, 0, FACET_DIM, query);
            }
            else {
                String sha1 = Util.sha1(args.toArray(new String[0]));
                result = getOrElse
                    (sha1, new Callable<SearchResult>() {
                            public SearchResult call () throws Exception {
                                return SearchFactory.search
                                (kind, hasFacets ? null : q,
                                 total, 0, FACET_DIM, query);
                            }
                        });
                
                if (hasFacets && result.count() == 0) {
                    Logger.debug("No results found for facet; "
                                 +"retry as just query: "+q);
                    // empty result.. perhaps the query contains /'s
                    Cache.remove(sha1); // clear cache
                    result = getOrElse
                        (sha1, new Callable<SearchResult>() {
                                public SearchResult call ()
                                    throws Exception {
                                    return SearchFactory.search
                                    (kind, q, total, 0, FACET_DIM,
                                     request().queryString());
                                }
                            });
                }
            }
            double ellapsed = (System.currentTimeMillis() - start)*1e-3;
            Logger.debug(String.format("Ellapsed %1$.3fs to retrieve "
                                       +"search %2$d results...",
                                       ellapsed, result.size()));
            
            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.trace("Unable to perform search", ex);
        }
        return null;
    }

    public static <T> T getOrElse (String key, Callable<T> callable)
        throws Exception {
        return getOrElse (textIndexer.lastModified(), key, callable);
    }
    
    public static <T> T getOrElse (long modified,
                                   String key, Callable<T> callable)
        throws Exception {
        if (System.currentTimeMillis() <= (modified + CACHE_TIMEOUT))
            Cache.remove(key);
        return Cache.getOrElse(key, callable, CACHE_TIMEOUT);
    }

    public static Result marvin () {
        return ok (ix.ncats.views.html.marvin.render());
    }

    public static Result smiles () {
        String data = request().body().asText();
        Molecule mol = null;
        try {
            MolHandler mh = new MolHandler (data, true);
            mol = mh.getMolecule();
            for (MolAtom a : mol.getAtomArray()) {
                if (a.getAtno() == 114) {
                    a.setAtno(MolAtom.ANY);
                }
            }
            //String q = URLEncoder.encode(mol.toFormat("smarts"), "utf8");
            return ok (mol.toFormat("smarts"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.debug("** Unable to convert structure\n"+data);
            if (mol != null) {
                for (MolAtom a : mol.getAtomArray()) {
                    Logger.debug(mol.indexOf(a)+": pseudo="+a.isPseudo()
                                 +" query="+a.isQuery()
                                 +" querylabel="+a.getQueryLabel()
                                 +" querystr="+a.getQuerystr()
                                 +" atno="+a.getAtno());
                }
            }
            return badRequest (data);
        }
    }
}
