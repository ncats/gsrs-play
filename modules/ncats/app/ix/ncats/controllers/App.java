package ix.ncats.controllers;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Call;
import play.mvc.BodyParser;
import play.libs.ws.*;
import play.libs.F;
import play.libs.Akka;
import play.mvc.Http;

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
import ix.core.plugins.IxContext;
import ix.core.plugins.IxCache;
import ix.core.plugins.PersistenceQueue;
import ix.core.controllers.search.SearchFactory;
import ix.core.chem.StructureProcessor;
import ix.core.models.Structure;
import ix.core.controllers.StructureFactory;
import ix.core.controllers.EntityFactory;
import ix.utils.Util;
import ix.utils.Global;

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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.ehcache.Element;

import ix.ncats.controllers.auth.*;

/**
 * Basic plumbing for an App
 */
public class App extends Authentication {
    static final String APP_CACHE = App.class.getName();
    
    static final String RENDERER_URL =
        play.Play.application()
        .configuration().getString("ix.structure.renderer.url");
    
    static final String RENDERER_FORMAT =
        play.Play.application()
        .configuration().getString("ix.structure.renderer.format");
    
    public static final int FACET_DIM = 20;
    public static final int MAX_SEARCH_RESULTS = 1000;

    public static final TextIndexer _textIndexer = 
        play.Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    public static final StructureIndexer _strucIndexer =
        play.Play.application().plugin(StructureIndexerPlugin.class).getIndexer();
    public static final IxContext _ix =
        play.Play.application().plugin(IxContext.class);

    public static final PersistenceQueue _pq =
        play.Play.application().plugin(PersistenceQueue.class);

    /**
     * interface for rendering a result page
     */
    public interface ResultRenderer<T> {
        Result render (int page, int rows, int total, int[] pages,
                       List<TextIndexer.Facet> facets, List<T> results);
        int getFacetDim ();
    }

    public static abstract class DefaultResultRenderer<T>
        implements ResultRenderer<T> {
        public int getFacetDim () { return FACET_DIM; }
    }
    
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
    /**
     * This returns links to up to 10 pages of interest.
     * 
     * The first few are always 1-3
     * 
     * The last 2 pages are always the last 2 possible
     * 
     * The middle pages are the pages around the current page
     * 
     * @param rowsPerPage
     * @param page
     * @param total
     * @return
     */
    public static int[] paging (int rowsPerPage, int page, int total) {
    	
    	//last page
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
            //0-7 set to +1
            for (; i < 7; ++i)
                pages[i] = i+1;
            //if the page is larger than 7 (last 3 page)
            //
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
            Logger.trace("Can't decode string "+s, ex);
        }
        return s;
    }

    public static String encode (String s) {
        try {
            return URLEncoder.encode(s, "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+s, ex);
        }
        return s;
    }

    public static String encode (Facet facet) {
        try {
            return URLEncoder.encode(facet.getName(), "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+facet.getName(), ex);
        }
        return facet.getName();
    }
    
    public static String encode (Facet facet, int i) {
        String value = facet.getValues().get(i).getLabel();
        try {
            return URLEncoder.encode(value, "utf8");
        }
        catch (Exception ex) {
            Logger.trace("Can't encode string "+value, ex);
        }
        return value;
    }

    public static String page (int rows, int page) {
        //Logger.debug(">> page(rows="+rows+",page="+page+") uri: "+request().uri());

        Map<String, Collection<String>> params = getQueryParameters ();
        
        // remove these
        //params.remove("rows");
        params.remove("page");
        StringBuilder uri = new StringBuilder (request().path()+"?page="+page);
        for (Map.Entry<String, Collection<String>> me : params.entrySet()) {
            for (String v : me.getValue()) {
                //Logger.debug(v+" => "+decode(v));
                uri.append("&"+me.getKey()+"="+v);
            }
        }

        //Logger.debug("<< "+uri);
        
        return uri.toString();
    }

    public static String truncate (String str, int size) {
        if (str.length() <= size) return str;
        return str.substring(0, size)+"...";
    }

    public static String url (String... remove) {
        //Logger.debug(">> uri="+request().uri());

        StringBuilder uri = new StringBuilder ("?");
        Map<String, Collection<String>> params = getQueryParameters ();
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
        //Logger.debug("<< "+uri);
        
        return uri.substring(0, uri.length()-1);
    }

    static Map<String, Collection<String>> getQueryParameters () {
        Map<String, Collection<String>> params =
            new TreeMap<String, Collection<String>>();
        String uri = request().uri();
        int pos = uri.indexOf('?');
        if (pos >= 0) {
            for (String p : uri.substring(pos+1).split("&")) {
                pos = p.indexOf('=');
                if (pos > 0) {
                    String key = p.substring(0, pos);
                    String value = p.substring(pos+1);
                    Collection<String> values = params.get(key);
                    if (values == null) {
                        params.put(key, values = new ArrayList<String>());
                    }
                    values.add(value);
                }
                else {
                    Logger.error("Bad parameter: "+p);
                }
            }
        }
        return params;
    }

    /**
     * more specific version that only remove parameters based on 
     * given facets
     */
    public static String url (FacetDecorator[] facets, String... others) {
        Logger.debug(">> uri="+request().uri());

        StringBuilder uri = new StringBuilder (request().path()+"?");
        Map<String, Collection<String>> params = getQueryParameters ();
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
                            if (!f.hidden && s.startsWith(f.facet.getName())) {
                                matched = true;
                                break;
                            }
                        }
                        
                        if (!matched) {
                            uri.append(me.getKey()+"="+v+"&");
                        }
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
        
        Logger.debug("<< uri="+uri);
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
        return getSearchResult (_textIndexer, kind, q, total);
    }

    public static SearchResult getSearchResult
        (final Class kind, final String q, final int total,
         Map<String, String[]> query) {
        return getSearchResult (_textIndexer, kind, q, total, query);
    }
    
    public static SearchResult getSearchResult
        (final TextIndexer indexer, final Class kind,
         final String q, final int total) {

        Map<String, String[]> query =  new HashMap<String, String[]>();
        query.putAll(request().queryString());
        return getSearchResult (indexer, kind, q, total, query);
    }

    public static String signature (String q, Map<String, String[]> query) {
        List<String> qfacets = new ArrayList<String>();
        if (query.get("facet") != null) {
            for (String f : query.get("facet"))
                qfacets.add(f);
        }
        
        final boolean hasFacets = q != null
            && q.indexOf('/') > 0 && q.indexOf("\"") < 0;
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
        
        return Util.sha1(args.toArray(new String[0]));
    }

    public static SearchResult getSearchResult
        (final TextIndexer indexer, final Class kind,
         final String q, final int total, final Map<String, String[]> query) {
        
        final String sha1 = signature (q, query);
        final boolean hasFacets = q != null
            && q.indexOf('/') > 0 && q.indexOf("\"") < 0;
        
        try {       
            long start = System.currentTimeMillis();
            SearchResult result;
            if (indexer != _textIndexer) {
                // if it's an ad-hoc indexer, then we don't bother caching
                //  the results
                result = SearchFactory.search
                    (indexer, kind, null, hasFacets ? null : q,
                     total, 0, FACET_DIM, query);
            }
            else {
                Logger.debug("request sha1: "+sha1+" cached? "
                             +IxCache.contains(sha1));

                result = getOrElse
                    (sha1, new Callable<SearchResult>() {
                            public SearchResult call () throws Exception {
                                return SearchFactory.search
                                (kind, hasFacets ? null : q,
                                 total, 0, FACET_DIM, query);
                            }
                        });

                /*
                if (hasFacets && result.count() == 0) {
                    Logger.debug("No results found for facet; "
                                 +"retry as just query: "+q);
                    // empty result.. perhaps the query contains /'s
                    IxCache.remove(sha1); // clear cache
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
                */
                Logger.debug(sha1+" => "+result);
            }
            double ellapsed = (System.currentTimeMillis() - start)*1e-3;
            Logger.debug(String.format("Ellapsed %1$.3fs to retrieve "
                                       +"search %2$d/%3$d results...",
                                       ellapsed, result.size(),
                                       result.count()));
            
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
        return getOrElse (_textIndexer.lastModified(), key, callable);
    }
    
    public static <T> T getOrElse (long modified,
                                   String key, Callable<T> callable)
        throws Exception {
        if (System.currentTimeMillis() <= (modified + CACHE_TIMEOUT)) {
            IxCache.remove(key);
        }
        return IxCache.getOrElse(key, callable);
    }

    public static Result marvin () {
        response().setHeader("X-Frame-Options", "SAMEORIGIN");
        return ok (ix.ncats.views.html.marvin.render());
    }

    @BodyParser.Of(value = BodyParser.Text.class, maxLength = 1024 * 10)
    public static Result smiles () {
        String data = request().body().asText();
        Logger.info(data);
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

    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 1024*10)
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
            result = getOrElse (key, new Callable<Result> () {
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
                });
            
            if (result == null)
                IxCache.remove(key);
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
                        Logger.info("ok");
                        return ok (render (mol, "svg", size, null));
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


     public static byte[] render (Molecule mol, String format, int size, int[] amap)
        throws Exception {
        Chemical chem = new Jchemical (mol);
        DisplayParams dp = DisplayParams.DEFAULT();
        
        if(amap!=null){
	        ChemicalAtom[] atoms = chem.getAtomArray();
	        for (int i = 0; i < Math.min(atoms.length, amap.length); ++i) {
	            atoms[i].setAtomMap(amap[i]);
	            if(amap[i]!=0){
	            	dp = dp.withSubstructureHighlight();
	            }
	        }
        }

        /*
        DisplayParams displayParams = new DisplayParams ();
        displayParams.changeProperty
            (DisplayParams.PROP_KEY_DRAW_STEREO_LABELS_AS_ATOMS, true);
        
        ChemicalRenderer render = new NchemicalRenderer (displayParams);
        */
       
        ChemicalRenderer render = new NchemicalRenderer ();
        render.setDisplayParams(dp);
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
    
    public static byte[] render (Structure struc, String format, int size, int[] amap)
        throws Exception {
        MolHandler mh = new MolHandler
            (struc.molfile != null ? struc.molfile : struc.smiles);
        Molecule mol = mh.getMolecule();
        if (mol.getDim() < 2) {
            mol.clean(2, null);
        }
        return render (mol, format, size, amap);
    }

    public static int[] stringToIntArray(String amapString){
    	int[] amap=null;
    	if(amapString!=null){
    		String[] amapb = null;
    		amapb = amapString.split(",");
    		amap = new int[amapb.length];
    		for(int i=0;i<amap.length;i++){
    			try{
    				amap[i]=Integer.parseInt(amapb[i]);
    			}catch(Exception e){
    				
    			}
    		}
    	}
    	return amap;
    }
    
    /**
     * Renders a chemical structure from structure ID
     * atom map can be provided for highlighting
     * 
     * @param id
     * @param format
     * @param size
     * @param atomMap
     * @return
     */
    public static Result structure (final String id,
                                    final String format, final int size, final String atomMap) {
    	
    	final int[] amap = stringToIntArray(atomMap);
        if (format.equals("svg") || format.equals("png")) {
            final String key =
                Structure.class.getName()+"/"+size+"/"+id+"."+format;
            String mime = format.equals("svg") ? "image/svg+xml" : "image/png";
            try {
                Result result = getOrElse (key, new Callable<Result> () {
                        public Result call () throws Exception {
                            Structure struc = StructureFactory.getStructure(id);
                            if (struc != null) {
                                return ok (render (struc, format, size, amap));
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
            final String key = Structure.class.getName()+"/"+id+"."+format;
            try {
                return getOrElse (key, new Callable<Result> () {
                        public Result call () throws Exception {
                            Structure struc = StructureFactory.getStructure(id);
                            if (struc != null) {
                                response().setContentType("text/plain");
                                if (format.equals("mrv")) {
                                    MolHandler mh =
                                        new MolHandler (struc.molfile);
                                    if (mh.getMolecule().getDim() < 2) {
                                        mh.getMolecule().clean(2, null);
                                    }
                                    return ok (mh.getMolecule()
                                               .toFormat("mrv"));
                                }
                                else if (format.equals("mol")
                                         || format.equals("sdf")) {
                                    return struc.molfile != null
                                        ? ok (struc.molfile) : noContent ();
                                }
                                else {
                                    return struc.smiles != null
                                        ?  ok (struc.smiles) : noContent ();
                                }
                            }
                            else {
                                Logger.warn("Unknown structure: "+id);
                            }
                            return noContent ();
                        }
                    });
            }
            catch (Exception ex) {
                Logger.error("Can't convert format "+format+" for structure "
                             +id, ex);
                ex.printStackTrace();
                return internalServerError
                    ("Unable to convert structure "+id+" to format "+format);
            }
        }
        return notFound ("Not a valid structure "+id);
    }

    /**
     * Structure searching
     */
    public static abstract class SearchResultProcessor {
        protected ResultEnumeration results;
        final SearchResultContext context = new SearchResultContext ();
        
        public SearchResultProcessor () {
        }

        public void setResults (int rows, ResultEnumeration results)
            throws Exception {
            this.results = results;
            // the idea is to generate enough results for 1.5 pages (enough
            // to show pagination) and return immediately. as the user pages,
            // the background job will fill in the rest of the results.
            int count = process (rows+1);
            
            // while we continue to fetch the rest of the results in the
            // background
            ActorRef handler = Akka.system().actorOf
                (Props.create(SearchResultHandler.class));
            handler.tell(this, ActorRef.noSender());
            Logger.debug("## search results submitted: "+handler);
        }
        
        public SearchResultContext getContext () { return context; }
        public boolean isDone () { return false; }

        public int process () throws Exception {
            return process (0);
        }
        
        public int process (int max) throws Exception {
            while (results.hasMoreElements()
                   && !isDone () && (max <= 0 || context.getCount() < max)) {
                StructureIndexer.Result r = results.nextElement();
                try {
                    long start = System.currentTimeMillis();
                    Object obj = instrument (r);
                    if (obj != null) {
                        context.add(obj);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.error("Can't process structure search result "
                                 +r.getId(), ex);
                }
            }
            return context.getCount();
        }
        
        //public abstract int process (int max) throws Exception;
        protected abstract Object instrument (StructureIndexer.Result r)
            throws Exception;
    }

    public static class SearchResultContext {
        public enum Status {
            Pending,
            Running,
            Done,
            Failed
        }

        Status status = Status.Pending;
        String mesg;
        Long start;
        Long stop;
        List results = new CopyOnWriteArrayList ();
        String id = randvar (10);
        Integer total;

        SearchResultContext () {
        }

        SearchResultContext (SearchResult result) {
            start = result.getTimestamp();          
            if (result.finished()) {
                status = Status.Done;
                stop = start+result.ellapsed();
            }
            else if (result.size() > 0) status = Status.Running;
            if (status != Status.Done) {
                mesg = String.format
                    ("Loading...%1$d%%",
                     (int)(100.*result.size()/result.count()+0.5));
            }
            results = result.getMatches();
            total = result.count();
        }

        public String getId () { return id; }
        public Status getStatus () { return status; }
        public void setStatus (Status status) { this.status = status; }
        public String getMessage () { return mesg; }
        public void setMessage (String mesg) { this.mesg = mesg; }
        public Integer getCount () { return results.size(); }
        public Integer getTotal () { return total; }
        public Long getStart () { return start; }
        public Long getStop () { return stop; }
        public boolean finished () {
            return status == Status.Done || status == Status.Failed;
        }
        
        @com.fasterxml.jackson.annotation.JsonIgnore
        public List getResults () { return results; }
        protected void add (Object obj) { results.add(obj); }
    }
    
    static class SearchResultHandler extends UntypedActor {
        @Override
        public void onReceive (Object obj) {
            if (obj instanceof SearchResultProcessor) {
                SearchResultProcessor processor = (SearchResultProcessor)obj;
                SearchResultContext ctx = processor.getContext();               
                try {
                    ctx.setStatus(SearchResultContext.Status.Running);
                    ctx.start = System.currentTimeMillis();            
                    int count = processor.process();
                    ctx.setStatus(SearchResultContext.Status.Done);
                    ctx.stop = System.currentTimeMillis();
                    Logger.debug("Actor "+self()+" finished; "+count
                                 +" search result(s) instrumented!");
                    context().stop(self ());
                }
                catch (Exception ex) {
                    ctx.status = SearchResultContext.Status.Failed;
                    ctx.setMessage(ex.getMessage());
                    ex.printStackTrace();
                    Logger.error("Unable to process search results", ex);
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

        public void preStart () {
        }
        
        @Override
        public void postStop () {
            Logger.debug(getClass().getName()+" "+self ()+" stopped!");
        }
    }

    /**
     * This method will return a proper Call only if the query isn't already
     * finished in one way or another
     */
    public static Call checkStatus () {
        String query = request().getQueryString("q");
        String type = request().getQueryString("type");
        Logger.debug("checkStatus: q="+query+" type="+type);
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

                Logger.debug("status: key="+key);
                Object value = IxCache.get(key);
                if (value != null) {
                    SearchResultContext context = (SearchResultContext)value;
                    Logger.debug("checkStatus: status="+context.getStatus()
                                 +" count="+context.getCount());
                    switch (context.getStatus()) {
                    case Done:
                    case Failed:
                        break;
                        
                    default:
                        return routes.App.status(key);
                    }
                    //return routes.App.status(type.toLowerCase(), query);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else {
            String key = signature (query, request().queryString());
            Object value = IxCache.get(key);
            if (value != null) {
                SearchResultContext ctx
                    = new SearchResultContext ((SearchResult)value);
                
                if (ctx.finished())
                    return null;
            }
            Logger.debug("status: key="+key);
            return routes.App.status(key);
        }
        return null;
    }

    public static Result status (String key) {
        Object value = IxCache.get(key);
        //Logger.debug("status["+key+"] => "+value);
        if (value != null) {
            if (value instanceof SearchResult) {
                // wrap SearchResult into SearchResultContext..
                SearchResultContext ctx
                    = new SearchResultContext ((SearchResult)value);
                
                ctx.id = key;
                value = ctx;
            }
            ObjectMapper mapper = new ObjectMapper ();
            return ok (mapper.valueToTree(value));
        }

        return notFound ("No key found: "+key+"!");
    }

    public static SearchResultContext substructure
        (final String query, final int rows,
         final int page, final SearchResultProcessor processor) {
        try {
            final String key = "substructure/"+Util.sha1(query);
            Logger.debug("substructure: query="+query
                         +" rows="+rows+" page="+page+" key="+key);
            return getOrElse
                (_strucIndexer.lastModified(),
                 key, new Callable<SearchResultContext> () {
                         public SearchResultContext call () throws Exception {
                             processor.setResults
                                 (rows, _strucIndexer.substructure(query, 0));
                             return processor.getContext();
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
                (_strucIndexer.lastModified(),
                 key, new Callable<SearchResultContext> () {
                         public SearchResultContext call () throws Exception {
                             processor.setResults
                                 (rows, _strucIndexer.similarity
                                  (query, threshold, 0));
                             return processor.getContext();
                         }
                     });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't execute similarity search", ex);
        }
        return null;
    }

    public static <T> Result structureResult
        (final SearchResultContext context, int rows,
         int page, final ResultRenderer<T> renderer) throws Exception {

        final String key = "structureResult/"+context.getId()
            +"/"+Util.sha1(request (), "facet");
        
        final TextIndexer.SearchResult result = getOrElse
            (key, new Callable<TextIndexer.SearchResult> () {
                    public TextIndexer.SearchResult call () throws Exception {
                        List results = context.getResults();
                        return results.isEmpty() ? null : SearchFactory.search
                        (results, null, results.size(), 0,
                         renderer.getFacetDim(),
                         request().queryString());
                    }
                });

        final List<T> results = new ArrayList<T>();
        
        int[] pages = new int[0];
        int count = 0;
        if (result != null) {
            Long stop = context.getStop();
            if (!context.finished() || 
                (stop != null && stop >= result.getTimestamp()))
                IxCache.remove(key);
            
            count = result.size();
            Logger.debug(key+": "+count+"/"+result.count()
                         +" finished? "+context.finished()
                         +" stop="+stop);
            
            rows = Math.min(count, Math.max(1, rows));
            int i = (page - 1) * rows;
            if (i < 0 || i >= count) {
                page = 1;
                i = 0;
            }
            pages = paging (rows, page, count);

            for (int j = 0; j < rows && i < count; ++j, ++i) 
                results.add((T)result.get(i));
        }

        final List<TextIndexer.Facet> facets = result != null
            ? result.getFacets() : new ArrayList<TextIndexer.Facet>();

        if (IxCache.contains(key)) {
            final String k = "structureResult/"
                +context.getId()+"/"+Util.sha1(request());
            final int _page = page;
            final int _rows = rows;
            final int _count = count;
            final int[] _pages = pages;
            
            // result is cached
            return getOrElse (k, new Callable<Result> () {
                    public Result call () throws Exception {
                        return renderer.render
                            (_page, _rows, _count, _pages,
                             facets, results);
                    }
                });
        }
        
        return renderer.render(page, rows, count, pages, facets, results);
    }

    static ObjectNode toJson (Element elm) {
        return toJson (new ObjectMapper (), elm);
    }
    
    static ObjectNode toJson (ObjectMapper mapper, Element elm) {
        return toJson (mapper.createObjectNode(), elm);
    }

    static ObjectNode toJson (ObjectNode node, Element elm) {
        node.put("class", elm.getObjectValue().getClass().getName()
                 +"@"+String.format
                 ("%1$x", System.identityHashCode(elm.getObjectValue())));
        node.put("key", elm.getObjectKey().toString());
        node.put("creation", new Date (elm.getCreationTime()).toString());
        node.put("expiration", new Date (elm.getExpirationTime()).toString());
        node.put("lastAccess", new Date (elm.getLastAccessTime()).toString());
        node.put("lastUpdate", new Date (elm.getLastUpdateTime()).toString());
        node.put("timeToIdle", elm.getTimeToIdle());
        node.put("timeToLive", elm.getTimeToLive());
        node.put("isEternal", elm.isEternal());
        node.put("isExpired", elm.isExpired());
        return node;
    }

    public static Result cache (String key) {
        try {
            Element elm = IxCache.getElm(key);
            if (elm == null) {
                return notFound ("Unknown cache: "+key);
            }

            return ok (toJson (elm));
        }
        catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }
    }

    public static Result cacheSummary () {
        return ok (ix.ncats.views.html.cachestats.render
                   (IxCache.getStatistics()));
    }

    public static Result cacheList (int top, int skip) {
        List keys = IxCache.getKeys(top, skip);
        if (keys != null) {
            ObjectMapper mapper = new ObjectMapper ();
            ArrayNode nodes = mapper.createArrayNode();
            for (Iterator it = keys.iterator(); it.hasNext(); ) {
                Object key = it.next();
                try {
                    Element elm = IxCache.getElm(key.toString());
                    if (elm != null)
                        nodes.add(toJson (mapper, elm));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            return ok (nodes);
        }
        return ok ("No cache available!");
    }

    public static Result cacheDelete (String key) {
        try {
            Element elm = IxCache.getElm(key);
            if (elm == null) {
                return notFound ("Unknown cache: "+key);
            }
                
            if (IxCache.remove(key)) {
                return ok (toJson (elm));
            }
            
            return ok ("Can't remove cache element: "+key);
        }
        catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }
    }
    
    public static Result statistics (String kind) {
        if (kind.equalsIgnoreCase("cache")) {
            return ok (ix.ncats.views.html.cachestats.render
                       (IxCache.getStatistics()));
        }
        return badRequest ("Unknown statistics: "+kind);
    }

    public static int[] uptime () {
        int[] ups = null;
        if (Global.epoch != null) {
            ups = new int[3];
            // epoch in seconds
            long u = (new java.util.Date().getTime()
                      - Global.epoch.getTime())/1000;
            ups[0] = (int)(u/3600); // hour
            ups[1] = (int)((u/60) % 60); // min
            ups[2] = (int)(u%60); // sec
        }
        return ups;
    }

    @BodyParser.Of(value = BodyParser.Text.class, maxLength = 1024 * 10)
    public static Result molinstrument () {
        //String mime = request().getHeader("Content-Type");
        //Logger.debug("molinstrument: content-type: "+mime);
        
        ObjectMapper mapper = EntityFactory.getEntityMapper();
        ObjectNode node = mapper.createObjectNode();
        try {
            String payload = request().body().asText();
            if (payload != null) {
                List<Structure> moieties = new ArrayList<Structure>();
                Structure struc = StructureProcessor.instrument
                    (payload, moieties);
                // we should be really use the PersistenceQueue to do this
                // so that it doesn't block
                struc.save();
                for (Structure m : moieties)
                    m.save();
                node.put("structure", mapper.valueToTree(struc));
                node.put("moieties", mapper.valueToTree(moieties));
            }
        }
        catch (Exception ex) {
            Logger.error("Can't process payload", ex);
            return internalServerError ("Can't process mol payload");       
        }
        return ok (node);
    }
}
