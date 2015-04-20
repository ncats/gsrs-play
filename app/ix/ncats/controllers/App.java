package ix.ncats.controllers;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.Callable;

import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Call;
import play.libs.ws.*;
import play.libs.F;
import play.libs.Akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Inbox;
import akka.actor.Terminated;
import akka.routing.Broadcast;
import akka.routing.RouterConfig;
import akka.routing.FromConfig;
import akka.routing.RoundRobinRouter;
import akka.routing.SmallestMailboxRouter;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import ix.core.search.TextIndexer;
import static ix.core.search.TextIndexer.*;
import tripod.chem.indexer.StructureIndexer;
import static tripod.chem.indexer.StructureIndexer.*;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.controllers.search.SearchFactory;
import ix.core.chem.StructureProcessor;
import ix.core.models.Structure;
import ix.core.controllers.StructureFactory;
import ix.utils.Util;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.util.MolHandler;

import java.awt.Dimension;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.freehep.graphicsio.svg.SVGGraphics2D;

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalAtom;
import gov.nih.ncgc.chemical.ChemicalFactory;
import gov.nih.ncgc.chemical.ChemicalRenderer;
import gov.nih.ncgc.chemical.DisplayParams;
import gov.nih.ncgc.nchemical.NchemicalRenderer;
import gov.nih.ncgc.jchemical.Jchemical;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Basic plumbing for an App
 */
public class App extends Controller {
    public static final int CACHE_TIMEOUT =
        play.Play.application()
        .configuration().getInt("ix.cache.time", 24*60*60); // 1 day

    static final String RENDERER_URL =
        play.Play.application()
        .configuration().getString("ix.structure.renderer.url");
    
    static final String RENDERER_FORMAT =
        play.Play.application()
        .configuration().getString("ix.structure.renderer.format");
    
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
        public boolean hidden;

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
                            // use the real name.. f.name() is a decoration
                            // that might not be the same as the actual
                            // facet name
                            if (s.startsWith(f.facet.getName())) {
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

    public static List<String> getUnspecifiedFacets
        (final FacetDecorator[] decors) {
        String[] facets = request().queryString().get("facet");
        List<String> unspec = new ArrayList<String>();
        if (facets != null && facets.length > 0) {
            for (String f : facets) {
                int matches = 0;
                for (FacetDecorator d : decors) {
                    //Logger.debug(f+" <=> "+d.facet.getName());              
                    if (f.startsWith(d.facet.getName())) {
                        ++matches;
                    }
                }
                if (matches == 0)
                    unspec.add(f);
            }
        }
        return unspec;
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

    public static TextIndexer.Facet[] getFacets (final Class<?> cls,
                                                 final String... filters) {
        StringBuilder key = new StringBuilder (cls.getName()+".facets");
        for (String f : filters)
            key.append("."+f);
        try {
            TextIndexer.Facet[] facets = getOrElse
                (key.toString(), new Callable<TextIndexer.Facet[]>() {
                        public TextIndexer.Facet[] call () {
                            return filter (getFacets (cls, FACET_DIM), filters);
                        }
                    });
            return facets;
        }
        catch (Exception ex) {
            Logger.error("Can't get facets for "+cls, ex);
            ex.printStackTrace();
        }
        return new TextIndexer.Facet[0];
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
        if (query.get("facet") != null) {
            for (String f : query.get("facet"))
                qfacets.add(f);
        }
        
        final boolean hasFacets = q != null && q.indexOf('/') > 0;
        if (hasFacets) {
            // treat this as facet
            qfacets.add("MeSH/"+q);
            query.put("facet", qfacets.toArray(new String[0]));
        }
        //query.put("drill", new String[]{"down"});
        
        List<String> args = new ArrayList<String>();
        args.add(request().path());
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
                /*
                Logger.debug("request sha1: "+sha1);
                for (String a : args) {
                    Logger.debug("** "+a);
                }
                */
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
        response().setHeader("X-Frame-Options", "SAMEORIGIN");
        return ok (ix.ncats.views.html.marvin.render());
    }

    public static Result smiles () {
        String data = request().body().asText();
        try {
            //String q = URLEncoder.encode(mol.toFormat("smarts"), "utf8");
            return ok (StructureProcessor.createQuery(data));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.debug("** Unable to convert structure\n"+data);
            return badRequest (data);
        }
    }

    public static Result molconvert () {
        JsonNode json = request().body().asJson();        
        try {
            final String format = json.get("parameters").asText();
            final String mol = json.get("structure").asText();

            String sha1 = Util.sha1(mol);
            Logger.debug("MOLCONVERT: format="+format+" mol="
                         +mol+" sha1="+sha1);
            
            response().setContentType("application/json");
            return getOrElse (0l, sha1, new Callable<Result>() {
                    public Result call () {
                        try {
                            MolHandler mh = new MolHandler (mol);
                            if (mh.getMolecule().getDim() < 2) {
                                mh.getMolecule().clean(2, null);
                            }
                            String out = mh.getMolecule().toFormat(format);
                            //Logger.debug("MOLCONVERT: output="+out);
                            ObjectMapper mapper = new ObjectMapper ();
                            ObjectNode node = mapper.createObjectNode();
                            node.put("structure", out);
                            node.put("format", format);
                            node.put("contentUrl", "");
                           
                            return ok (node);
                        }
                        catch (Exception ex) {
                            return badRequest ("Invalid molecule: "+mol);
                        }
                    }
                });
        }
        catch (Exception ex) {
            Logger.error("Can't parse request", ex);
            ex.printStackTrace();
            
            return internalServerError ("Unable to convert input molecule");
        }
    }

    public static Result renderOld (final String value, final int size) {
        String key = Util.sha1(value)+"::"+size;
        Result result = null;
        try {
            result = Cache.getOrElse(key, new Callable<Result> () {
                    public Result call () throws Exception {
                        WSRequestHolder ws = WS.url(RENDERER_URL)
                        .setFollowRedirects(true)
                        .setQueryParameter("structure", value)
                        .setQueryParameter("format", RENDERER_FORMAT)
                        .setQueryParameter("size", String.valueOf(size));
                        WSResponse res = ws.get().get(5000);
                        byte[] data = res.asByteArray();
                        if (data.length > 0) {
                            return ok (data);
                        }
                        return null;
                    }
                }, CACHE_TIMEOUT);
            
            if (result == null)
                Cache.remove(key);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.trace("Can't render "+value, ex);
        }
        response().setContentType("image/svg+xml");
        return result;
    }

    public static Result render (final String value, final int size) {
        String key = Util.sha1(value)+"::"+size;
        try {
            response().setContentType("image/svg+xml");
            return getOrElse (0l, key, new Callable<Result>() {
                    public Result call () throws Exception {
                        MolHandler mh = new MolHandler (value);
                        Molecule mol = mh.getMolecule();
                        if (mol.getDim() < 2) {
                            mol.clean(2, null);
                        }
                        return ok (render (mol, "svg", size));
                    }
                });
        }
        catch (Exception ex) {
            Logger.error("Not a valid molecule:\n"+value, ex);
            ex.printStackTrace();
            return badRequest ("Not a valid molecule: "+value);
        }
    }

    public static Result rendertest () {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream ();
            int size = 400;
            SVGGraphics2D svg = new SVGGraphics2D
                (bos, new Dimension (size, size));
            svg.startExport();
            Chemical chem = new Jchemical ();
            chem.load("c1ccncc1", Chemical.FORMAT_SMILES);
            chem.clean2D();
            
            ChemicalRenderer cr = new NchemicalRenderer();

            BufferedImage bi = cr.createImage(chem, 200);
            //ImageIO.write(bi, "png", bos); 

            cr.renderChem(svg, chem, size, size, false);
            svg.endExport();
            svg.dispose();
            
            response().setContentType("image/svg+xml");
            //response().setContentType("image/png");
            return ok(bos.toByteArray());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError (ex.getMessage());
        }
    }

    public static byte[] render (Molecule mol, String format, int size)
        throws Exception {
        Chemical chem = new Jchemical ();
        chem.load(mol.toFormat("mol"), Chemical.FORMAT_SDF);
        ChemicalAtom[] atoms = chem.getAtomArray();
        for (int i = 0; i < Math.min(atoms.length, 10); ++i) {
            atoms[i].setAtomMap(i+1);
        }

        /*
        DisplayParams displayParams = new DisplayParams ();
        displayParams.changeProperty
            (DisplayParams.PROP_KEY_DRAW_STEREO_LABELS_AS_ATOMS, true);
        
        ChemicalRenderer render = new NchemicalRenderer (displayParams);
        */
        ChemicalRenderer render = new NchemicalRenderer ();
        ByteArrayOutputStream bos = new ByteArrayOutputStream ();       
        if (format.equals("svg")) {
            SVGGraphics2D svg = new SVGGraphics2D
                (bos, new Dimension (size, size));
            svg.startExport();
            render.renderChem(svg, chem, size, size, false);
            svg.endExport();
            svg.dispose();
        }
        else {
            BufferedImage bi = render.createImage(chem, size);
            ImageIO.write(bi, "png", bos); 
        }
        
        return bos.toByteArray();
    }
    
    public static byte[] render (Structure struc, String format, int size)
        throws Exception {
        MolHandler mh = new MolHandler
            (struc.molfile != null ? struc.molfile : struc.smiles);
        Molecule mol = mh.getMolecule();
        if (mol.getDim() < 2) {
            mol.clean(2, null);
        }
        return render (mol, format, size);
    }

    public static Result structure (final long id,
                                    final String format, final int size) {
        if (format.equals("svg") || format.equals("png")) {
            String key = Structure.class.getName()+"."+id+"."+size+"."+format;
            String mime = format.equals("svg") ? "image/svg+xml" : "image/png";
            try {
                Result result = getOrElse (0l, key, new Callable<Result> () {
                        public Result call () throws Exception {
                            Structure struc = StructureFactory.getStructure(id);
                            if (struc != null) {
                                return ok (render (struc, format, size));
                            }
                            return null;
                        }
                    });
                if (result != null) {
                    response().setContentType(mime);
                    return result;
                }
            }
            catch (Exception ex) {
                Logger.error("Can't generate image for structure "
                             +id+" format="+format+" size="+size, ex);
                ex.printStackTrace();
                return internalServerError
                    ("Unable to retrieve image for structure "+id);
            }
        }
        else {
            Structure struc = StructureFactory.getStructure(id);
            if (struc != null) {
                response().setContentType("text/plain");
                if (format.equals("mrv")) {
                    try {
                        MolHandler mh = new MolHandler (struc.molfile);
                        if (mh.getMolecule().getDim() < 2) {
                            mh.getMolecule().clean(2, null);
                        }
                        return ok (mh.getMolecule().toFormat("mrv"));
                    }
                    catch (Exception ex) {
                        return internalServerError
                            ("Structure "+id+" can't coverted to MRV format");
                    }
                }
                else if (format.equals("mol") || format.equals("sdf")) {
                    return struc.molfile != null
                        ? ok (struc.molfile) : noContent ();
                }
                else {
                    return struc.smiles != null
                        ?  ok (struc.smiles) : noContent ();
                }
            }
        }
        return notFound ("Not a valid structure "+id);
    }

    /**
     * Structure searching
     */
    public static abstract class SearchResultProcessor {
        protected ResultEnumeration results;
        final SearchResultContext context;
        
        public SearchResultProcessor () throws IOException {
            context = new SearchResultContext
                (textIndexer.createEmptyInstance());
        }

        public void setResults (ResultEnumeration results) {
            this.results = results;
        }
        public SearchResultContext getContext () { return context; }
        public TextIndexer getIndexer () { return context.getIndexer(); }

        public int process () throws Exception {
            return process (0);
        }
        
        public abstract int process (int max) throws Exception;
    }

    public static class SearchResultContext {
        public enum Status {
            Running,
            Done,
            Failed
        }

        Status status;
        String mesg;
        Long start;
        Long stop;
        final TextIndexer indexer;

        SearchResultContext (TextIndexer indexer) {
            this.indexer = indexer;
        }

        public Status getStatus () { return status; }
        public void setStatus (Status status) { this.status = status; }
        public String getMessage () { return mesg; }
        public void setMessage (String mesg) { this.mesg = mesg; }
        public Integer getCount () { return indexer.size(); }
        public Long getStart () { return start; }
        public Long getStop () { return stop; }
        
        @com.fasterxml.jackson.annotation.JsonIgnore
        public TextIndexer getIndexer () { return indexer; }
    }

    static class SearchResultHandler extends UntypedActor {
        @Override
        public void onReceive (Object obj) {
            if (obj instanceof SearchResultProcessor) {
                SearchResultProcessor processor = (SearchResultProcessor)obj;
                SearchResultContext ctx = processor.getContext();               
                try {
                    ctx.start = System.currentTimeMillis();
                    ctx.status = SearchResultContext.Status.Running;
                    int count = processor.process();
                    ctx.stop = System.currentTimeMillis();
                    ctx.status = SearchResultContext.Status.Done;
                    double ellapsed = (ctx.stop - ctx.start)*1e-3;
                    Logger.debug("Actor "+self()+" finished; "
                                 +String.format("Ellapsed %1$.3fs to retrieve "
                                                +"%2$d structures...",
                                                ellapsed, count));
                    context().stop(self ());
                }
                catch (Exception ex) {
                    ctx.status = SearchResultContext.Status.Failed;
                    ctx.setMessage(ex.getMessage());
                    ex.printStackTrace();
                    Logger.error("Unable process search results", ex);
                }
            }
            else if (obj instanceof Terminated) {
                ActorRef actor = ((Terminated)obj).actor();
                Logger.debug("Terminating actor "+actor);
            }
            else {
                unhandled (obj);
            }
        }
        
        @Override
        public void postStop () {
            Logger.debug("Actor "+self ()+" is stopped!");
        }
    }

    static protected SearchResultContext submit
        (final int rows, SearchResultProcessor processor)  throws Exception {
        
        long start = System.currentTimeMillis();        
        // the idea is to generate enough results for 1.5 pages (enough
        // to show pagination) and return immediately. as the user pages,
        // the background job will fill in the rest of the results.
        int count = processor.process(rows+rows/2);

        // while we continue to fetch the rest of the results in the
        // background
        ActorRef handler = Akka.system().actorOf
            (Props.create(SearchResultHandler.class));
        handler.tell(processor, ActorRef.noSender());
        
        double ellapsed = (System.currentTimeMillis() - start)*1e-3;
        Logger.debug(String.format("Ellapsed %1$.3fs to retrieve "
                                   +"%2$d structures...",
                                   ellapsed, count));
        return processor.getContext();
    }

    /**
     * This method will return a proper Call only if the query isn't already
     * finished in one way or another
     */
    public static Call checkStatus () {
        String query = request().getQueryString("q");
        String type = request().getQueryString("type");
        if (type != null && query != null) {
            try {
                String key = null;
                if (type.equalsIgnoreCase("substructure")) {
                    key = "substructure/"+Util.sha1(query);
                }
                else if (type.equalsIgnoreCase("similarity")) {
                    String c = request().getQueryString("cutoff");
                    key = "similarity/"+getKey (query, Double.parseDouble(c));
                }
                else {
                }

                Object value = Cache.get(key);
                if (value != null) {
                    SearchResultContext context = (SearchResultContext)value;
                    switch (context.getStatus()) {
                    case Done:
                    case Failed:
                        break;
                        
                    default:
                        return routes.App.status(type.toLowerCase(), query);
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static Result status (String type, String query) {
        String key = null;
        if (type.equalsIgnoreCase("substructure")) {
            key = "substructure/"+Util.sha1(query);
        }
        else if (type.equalsIgnoreCase("similarity")) {
            String c = request().getQueryString("cutoff");
            if (c == null)
                return badRequest ("No \"cutoff\" parameter "
                                   +"specified for query of type "+type);
            try {
                key = "similarity/"+getKey (query, Double.parseDouble(c));
            }
            catch (Exception ex) {
                return badRequest ("Bogus cutoff value: "+c);
            }
        }
        else {
            return badRequest ("Unknown type: \""+type+"\"");
        }

        Object value = Cache.get(key);
        if (value != null) {
            SearchResultContext context = (SearchResultContext)value;
            ObjectMapper mapper = new ObjectMapper ();
            return ok (mapper.valueToTree(context));
        }

        return notFound ("No query "+query+" of type "+type+" found!");
    }

    public static SearchResultContext substructure
        (final String query, final int rows,
         final int page, final SearchResultProcessor processor) {
        try {
            final String key = "substructure/"+Util.sha1(query);
            Logger.debug("substructure: query="+query
                         +" rows="+rows+" page="+page+" key="+key);
            final int size = (page+1)*rows;
            return getOrElse
                (strucIndexer.lastModified(),
                 key, new Callable<SearchResultContext> () {
                         public SearchResultContext call () throws Exception {
                             Logger.debug("Cache missed: "+key);
                             processor.setResults
                                 (strucIndexer.substructure(query, 0));
                             return submit (size, processor);
                         }
                     });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform substructure search", ex);
        }
        return null;
    }

    static String getKey (String q, double t) {
        return Util.sha1(q) + "/"+String.format("%1$d", (int)(1000*t+.5));
    }
    
    public static SearchResultContext similarity
        (final String query, final double threshold,
         final int rows, final int page,
         final SearchResultProcessor processor) {
        try {
            final String key = "similarity/"+getKey (query, threshold);
            final int size = (page+1)*rows;
            return getOrElse
                (strucIndexer.lastModified(),
                 key, new Callable<SearchResultContext> () {
                         public SearchResultContext call () throws Exception {
                             Logger.debug("Cache missed: "+key);
                             processor.setResults
                                 (strucIndexer.similarity(query, threshold, 0));
                             return submit (size, processor);
                         }
                     });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't execute similarity search", ex);
        }
        return null;
    }
}
