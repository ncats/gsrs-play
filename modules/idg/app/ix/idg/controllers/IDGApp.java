package ix.idg.controllers;

import com.avaje.ebean.Expr;
import com.avaje.ebean.QueryIterator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.KeywordFactory;
import ix.core.controllers.PredicateFactory;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.*;
import ix.core.plugins.IxCache;
import ix.core.plugins.PayloadPlugin;
import ix.core.search.SearchOptions;
import ix.core.search.TextIndexer;
import ix.idg.models.Disease;
import ix.idg.models.Ligand;
import ix.idg.models.Target;
import ix.ncats.controllers.App;
import ix.utils.Util;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.Play;
import play.cache.Cached;
import play.db.ebean.Model;
import play.mvc.Result;
import play.mvc.BodyParser;
import play.mvc.Call;
import java.util.ArrayList;
import tripod.chem.indexer.StructureIndexer;
import ix.seqaln.SequenceIndexer;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import chemaxon.struc.MolAtom;

import static ix.core.search.TextIndexer.Facet;
import static ix.core.search.TextIndexer.SearchResult;

public class IDGApp extends App implements Commons {
    static final int MAX_SEARCH_RESULTS = 1000;
    public static final String IDG_RESOLVER = "IDG Resolver";

    public interface Filter<T extends EntityModel> {
        boolean accept (T e);
    }

    static class IDGSearchResultProcessor
        extends SearchResultProcessor<StructureIndexer.Result> {
        final public Set<Long> processed = new HashSet<Long>();
        int count;

        IDGSearchResultProcessor () throws IOException {
        }

        @Override
        protected Object instrument (StructureIndexer.Result r)
            throws Exception {
            List<Ligand> ligands = LigandFactory.finder
                .where(Expr.and(Expr.eq("links.refid", r.getId()),
                                Expr.eq("links.kind",
                                        Structure.class.getName())))
                .findList();
            if (!ligands.isEmpty()) {
                Ligand lig = ligands.iterator().next();
                //Logger.debug("matched ligand: "+ligand.id+" "+r.getId());
                if (!processed.contains(lig.id)) {
                    processed.add(lig.id);
                    int[] amap = new int[r.getMol().getAtomCount()];
                    int i = 0, nmaps = 0;
                    for (MolAtom ma : r.getMol().getAtomArray()) {
                        amap[i] = ma.getAtomMap();
                        if (amap[i] > 0)
                            ++nmaps;
                        ++i;
                    }

                    if (nmaps > 0) {
                        IxCache.set("AtomMaps/"+getContext().getId()+"/"
                                    +r.getId(), amap);
                    }
                    
                    return lig;
                }
            }
            return null;
        }
    }

    public static class IDGSequenceResultProcessor
        extends SearchResultProcessor<SequenceIndexer.Result> {
        
        IDGSequenceResultProcessor() {
        }
            
        protected Object instrument(SequenceIndexer.Result r)
            throws Exception {
            List<Target> targets = TargetFactory.finder.where
                (Expr.and(Expr.eq("properties.label", UNIPROT_SEQUENCE),
                          Expr.eq("properties.id", r.id)))
                .findList();
            
            Target target = null;
            if (!targets.isEmpty()) {
                target = targets.iterator().next();
                // cache this alignment for show later
                Logger.debug("alignment "+getContext().getId()+" => "+r.id);
                IxCache.set("Alignment/"+getContext().getId()+"/"+r.id, r);
            }
            
            return target;
        }
    }

    public static class GeneRIF implements Comparable<GeneRIF> {
        public Long pmid;       
        public String text;

        GeneRIF (Long pmid, String text) {
            this.pmid = pmid;
            this.text = text;
        }

        public int compareTo (GeneRIF gene) {
            if (gene.pmid > pmid) return 1;
            if (gene.pmid < pmid) return -1;
            return 0;
        }
    }
    
    public static class DiseaseRelevance
        implements Comparable<DiseaseRelevance> {
        public Disease disease;
        public Double zscore;
        public Double conf;
        public Double tinxScore;
        public String comment;
        public Keyword omim;
        public Keyword uniprot;
        public List<DiseaseRelevance> lineage =
            new ArrayList<DiseaseRelevance>();

        DiseaseRelevance () {}
        public int compareTo (DiseaseRelevance dr) {
            double d = 0.;
            if (dr.zscore != null && zscore != null)
                d = dr.zscore - zscore;
            else if (dr.conf != null && conf != null)
                d = dr.conf - conf;
            if (d < 0) return -1;
            if (d > 0) return 1;
            return 0;
        }
    }

    public static class LigandActivity {
        public final Target target;
        public final List<VNum> activities = new ArrayList<VNum>();
        public String mechanism;

        LigandActivity (XRef ref) {
            for (Value v : ref.properties) {
                if (ChEMBL_MECHANISM.equals(v.label)) {
                    mechanism = ((Text)v).text;
                }
                else if (v instanceof VNum) {
                    activities.add((VNum)v);
                }
            }
            target = (Target)ref.deRef();
        }
    }

    public static class DataSource {
        final public String name;
        public Integer targets;
        public Integer diseases;
        public Integer ligands;
        public String href;

        DataSource (String name) {
            if (name.startsWith("ChEMBL"))
                href = "https://www.ebi.ac.uk/chembl/";
            else if (name.equalsIgnoreCase("iuphar"))
                href = "http://www.guidetopharmacology.org/";
            else if (name.startsWith("TCRD"))
                href = "http://habanero.health.unm.edu";
            else if (name.startsWith("DiseaseOntology"))
                href = "http://www.disease-ontology.org";
            else if (name.equalsIgnoreCase("uniprot"))
                href = "http://www.uniprot.org";
            else if (name.equalsIgnoreCase("scientific literature")
                     || name.equalsIgnoreCase("drug label")) {
                // do nothing for this
            }
            else {
                List<Keyword> sources = KeywordFactory.finder.where
                    (Expr.and(Expr.eq("label", SOURCE),
                              Expr.eq("term", name))).findList();
                if (!sources.isEmpty()) {
                    Keyword source = sources.iterator().next();
                    href = source.href;
                }
            }
            this.name = name;
        }
    }

    static class IDGFacetDecorator extends FacetDecorator {
        IDGFacetDecorator (Facet facet) {
            super (facet, true, 6);
        }

        @Override
        public String name () {
            return super.name().replaceAll("IDG", "")
                .replaceAll("UniProt","").trim();
        }
        
        @Override
        public String label (final int i) {
            final String label = super.label(i);
            final String name = super.name();
            if (name.equals(IDG_DEVELOPMENT)) {
                Target.TDL tdl = Target.TDL.fromString(label);
                if (tdl != null) {
                    return "<span class=\"label label-"+tdl.label+"\""
                        +" data-toggle=\"tooltip\" data-placement=\"right\""
                        +" data-html=\"true\" title=\"<p align='left'>"
                        +tdl.desc+"</p>\">"+tdl.name+"</span>";
                }
                assert false: "Unknown TDL label: "+label;
            }
            else if (name.equals(WHO_ATC)) {
                final String key = WHO_ATC+":"+label;
                try {
                    Keyword kw = getOrElse (0l, key, new Callable<Keyword>() {
                            public Keyword call () {
                                List<Keyword> kws = KeywordFactory.finder
                                .where().eq("label",name+" "+label)
                                .findList();
                                if (!kws.isEmpty()) {
                                    return kws.iterator().next();
                                }
                                return null;
                            }
                        });
                    if (kw != null)
                        return kw.term.toLowerCase()+" ("+label+")";
                }
                catch (Exception ex) {
                    Logger.error("Can't retrieve key "+key+" from cache", ex);
                    ex.printStackTrace();
                }
            }
            else if (name.equals(Target.IDG_FAMILY)) {
                if (label.equalsIgnoreCase("ogpcr")) {
                    return "<a href='https://en.wikipedia.org/wiki/Olfactory_receptor'>oGPCR</a>";
                }
                
                if (label.equalsIgnoreCase("gpcr")) {
                    return "<a href=\"http://en.wikipedia.org/wiki/G_protein%E2%80%93coupled_receptor\">"+label+"</a>";
                }
                if (label.equalsIgnoreCase("kinase")) {
                    return "<a href=\"http://en.wikipedia.org/wiki/Kinase\">"+label+"</a>";
                }
                if (label.equalsIgnoreCase("ion channel")) {
                    return "<a href=\"http://en.wikipedia.org/wiki/Ion_channel\">"+label+"</a>";
                }
                if (label.equalsIgnoreCase("nuclear receptor")) {
                    return "<a href=\"http://en.wikipedia.org/wiki/Nuclear_receptor\">"+label+"</a>";
                }
            }
            return label;
        }
    }

    static abstract class GetResult<T extends EntityModel> {
        final Model.Finder<Long, T> finder;
        final Class<T> cls;
        GetResult (Class<T> cls, Model.Finder<Long, T> finder) {
            this.cls = cls;
            this.finder = finder;
        }

        public List<T> find (final String name) throws Exception {
            long start = System.currentTimeMillis();
            final String key = cls.getName()+"/"+name;
            List<T> e = getOrElse
                (key, new Callable<List<T>> () {
                        public List<T> call () throws Exception {
                            List<T> values = finder.where()
                            .eq("synonyms.term", name).findList();
                            if (values.size() > 1) {
                                Logger.warn("\""+name+"\" yields "
                                            +values.size()+" matches!");
                            }
                            
                            // also cache all the synonyms
                            for (T v : values) {
                                for (Keyword kw : v.getSynonyms()) {
                                    if (!kw.term.equals(name))
                                        IxCache.set(cls.getName()+"/"
                                                    +kw.term, values);
                                    if (!kw.term.toUpperCase().equals(name))
                                        IxCache.set(cls.getName()+"/"
                                                    +kw.term.toUpperCase(),
                                                    values);
                                    if (!kw.term.toLowerCase().equals(name))
                                        IxCache.set(cls.getName()+"/"
                                                    +kw.term.toLowerCase(),
                                                    values);
                                }
                            }
                            return values;
                        }
                    });
            double elapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug("Elapsed time "+String.format("%1$.3fs", elapsed)
                         +" to retrieve "+e.size()+" matches for "+name);
            return e;
        }
        
        public Result get (final String name) {
            try {
                List<T> e = find (name);
                if (e.isEmpty()) {
                    return _notFound ("Unknown name: "+name);
                }
                return result (e);
            }
            catch (Exception ex) {
                Logger.error("Unable to generate Result for \""+name+"\"", ex);
                return _internalServerError (ex);
            }
        }
        
        public Result result (final List<T> e) {
            try {
                final String key = cls.getName()
                    +"/result/"+Util.sha1(request ());
                return getOrElse (key, new Callable<Result> () {
                            public Result call () throws Exception {
                                long start = System.currentTimeMillis();
                                Result r = getResult (e);
                                Logger.debug("Cache missed: "+key+"..."
                                             +(System.currentTimeMillis()-start)
                                             +"ms");
                                return r;
                            }
                        });
            }
            catch (Exception ex) {
                return _internalServerError (ex);
            }
        }

        abstract Result getResult (List<T> e) throws Exception;
    }

    static class TargetCacheWarmer implements Runnable {
        final AtomicLong start = new AtomicLong ();
        final AtomicLong stop = new AtomicLong ();
        
        @JsonIgnore
        final ExecutorService threadPool;
        @JsonIgnore
        final Future task;

        @JsonIgnore
        final List<String> targets;
        final Map<String, Long> complete =
            new ConcurrentHashMap<String, Long>();
        
        TargetCacheWarmer (List<String> targets) {
            this.targets = targets;
            threadPool = Executors.newSingleThreadExecutor();
            task = threadPool.submit(this);
        }

        public void run () {
            Logger.debug
                (Thread.currentThread().getName()
                 +": preparing to warm cache for "+targets.size()+" targets!");
            start.set(System.currentTimeMillis());
            for (String name : targets) {
                long s = System.currentTimeMillis();
                try {
                    List<Target> tar = TargetResult.find(name);
                    for (Target t : tar) {
                        List<DiseaseRelevance> dr = getDiseases (t); // cache
                        Logger.debug(name+": id="+t.id
                                     +" #syns="+t.synonyms.size()
                                     +" #pubs="+t.publications.size()
                                     +" #links="+t.links.size()
                                     +" #props="+t.properties.size()
                                     +" #diseases="+dr.size()
                                     );
                    }
                    complete.put(name, System.currentTimeMillis()-s);
                }
                catch (Exception ex) {
                    Logger.debug(name+"...failed: "+ex.getMessage());
                }
            }
            stop.set(System.currentTimeMillis());
            Logger.debug
                (Thread.currentThread().getName()
                 +": target cache warmer complete..."+new java.util.Date());
            threadPool.shutdown();
        }

        public Map<String, Long> getComplete () { return complete; }
        public boolean isDone () { return task.isDone(); }
        public int getTotal () { return targets.size(); }
        public int getCount () { return complete.size(); }
        public long getStart () { return start.get(); }
        public long getStop () { return stop.get(); }
    }
    
    public static final String[] TARGET_FACETS = {
        IDG_DEVELOPMENT,
        IDG_FAMILY,
        IDG_DISEASE,
        IDG_TISSUE,
        GTEx_TISSUE,
        HPM_TISSUE,
        HPA_RNA_TISSUE
    };

    public static final String[] DISEASE_FACETS = {
        IDG_DEVELOPMENT,
        IDG_FAMILY,
        UNIPROT_TARGET
    };

    public static final String[] LIGAND_FACETS = {
        WHO_ATC,
        //IDG_DRUG,
        IDG_DEVELOPMENT,
        IDG_FAMILY,
        PHARMALOGICAL_ACTION
        //UNIPROT_TARGET
    };

    public static final String[] ALL_FACETS = {
        IDG_DEVELOPMENT,
        IDG_FAMILY,
        IDG_DISEASE,
        UNIPROT_TARGET,
        "Ligand"
    };

    static FacetDecorator[] decorate (Class kind, Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new IDGFacetDecorator (facets[i]));
        }
        // now add hidden facet so as to not have them shown in the alert
        // box
        for (int i = 1; i <= 8; ++i) {
            IDGFacetDecorator f = new IDGFacetDecorator
                (new TextIndexer.Facet
                 (ChEMBL_PROTEIN_CLASS+" ("+i+")"));
            f.hidden = true;
            decors.add(f);
        }

        // at most the dto is only 5 deep
        for (int i = 0; i < 6; ++i) {
            IDGFacetDecorator f = new IDGFacetDecorator
                (new TextIndexer.Facet
                 (DTO_PROTEIN_CLASS+" ("+i+")"));
            f.hidden = true;
            decors.add(f);
        }

        // panther
        for (int i = 0; i < 6; ++i) {
            IDGFacetDecorator f = new IDGFacetDecorator
                (new TextIndexer.Facet
                 (PANTHER_PROTEIN_CLASS+" ("+i+")"));
            f.hidden = true;
            decors.add(f);
        }
        
        { IDGFacetDecorator f = new IDGFacetDecorator
                (new TextIndexer.Facet(DiseaseOntologyRegistry.CLASS));
            f.hidden = true;
            decors.add(f);
        }

        if (kind != null) {
            SearchResult result = getSearchFacets (kind);
            Logger.debug("+++");
            for (FacetDecorator f : decors) {
                if (!f.hidden) {
                    Logger.debug("Facet "+f.facet.getName());
                    Facet full = result.getFacet(f.facet.getName());
                    for (int i = 0; i < f.facet.size(); ++i) {
                        TextIndexer.FV fv = f.facet.getValue(i);
                        f.total[i] = full.getCount(fv.getLabel());
                        Logger.debug("  + "+fv.getLabel()+" "
                                     +fv.getCount()
                                     +"/"+f.total[i]);
                    }
                }
            }
        }
        
        return decors.toArray(new FacetDecorator[0]);
    }

    static FacetDecorator[] decorate (Facet... facets) {
        return decorate (null, facets);
    }
    
    @Cached(key="_help", duration= Integer.MAX_VALUE)
    public static Result help() {
        return ok (ix.idg.views.html.help.render
                ("Pharos: Illuminating the Druggable Genome"));
    }

    @Cached(key="_about", duration = Integer.MAX_VALUE)
    public static Result about() {
        final String key = "idg/about";
        try {
            return getOrElse (key, new Callable<Result> () {
                    public Result call () throws Exception {
                        TextIndexer.Facet[] target =
                            getFacets (Target.class, "Namespace");
                        TextIndexer.Facet[] disease =
                            getFacets (Disease.class, "Namespace");
                        TextIndexer.Facet[] ligand =
                            getFacets (Ligand.class, "Namespace");
                        return ok (ix.idg.views.html.about.render
                                   ("Pharos: Illuminating the Druggable Genome",
                                    target.length > 0 ? target[0] : null,
                                    disease.length > 0 ? disease[0] : null,
                                    ligand.length > 0 ? ligand[0]: null));
                    }
                });
        }
        catch (Exception ex) {
            Logger.error("Can't get about page", ex);
            return error (500, "Unable to fulfil request");
        }
    }

    @Cached(key="_index", duration = Integer.MAX_VALUE)
    public static Result index () {
        return ok (ix.idg.views.html.index2.render
                   ("Pharos: Illuminating the Druggable Genome",
                    DiseaseFactory.finder.findRowCount(),
                    TargetFactory.finder.findRowCount(),
                    LigandFactory.finder.findRowCount()));
    }

    public static Result home () {
        return redirect (routes.IDGApp.index());
    }

    @Cached(key="_kinome", duration = Integer.MAX_VALUE)
    public static Result kinome () {
        return ok (ix.idg.views.html.kinome.render());
    }

    public static Result error (int code, String mesg) {
        return ok (ix.idg.views.html.error.render(code, mesg));
    }

    public static Result _notFound (String mesg) {
        return notFound (ix.idg.views.html.error.render(404, mesg));
    }

    public static Result _badRequest (String mesg) {
        return badRequest (ix.idg.views.html.error.render(400, mesg));
    }

    public static Result _internalServerError (Throwable t) {
        t.printStackTrace();
        return internalServerError
            (ix.idg.views.html.error.render
             (500, "Internal server error: "+t.getMessage()));
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
                            return;
                        }
                    }
                }
            }
        }
    }

    public static String novelty (Value value) {
        if (value != null) {
            VNum val = (VNum)value;
            if (val.numval < 0.)
                return String.format("%1$.5f", val.numval);
            if (val.numval < 10.)
                return String.format("%1$.3f", val.numval);
            return String.format("%1$.1f", val.numval);
        }
        return "";
    }

    public static String format (Double value) {
        if (value != null) {
            if (value < 0.)
                return String.format("%1$.5f", value);
            if (value < 0.001)
                return String.format("%1$.5f", value);
            if (value < 10.)
                return String.format("%1$.1f", value);
            return String.format("%1$.0f", value);
        }
        return "";
    }
    
    public static String getId (Target t) {
        Keyword kw = t.getSynonym(UNIPROT_ACCESSION);
        return kw != null ? kw.term : null;
    }

    /**
     * return a list of all data sources
     */
    static DataSource[] _getDataSources () throws Exception {
        SearchOptions opts = new SearchOptions (null, 1, 0, 10);           
        TextIndexer.SearchResult results = _textIndexer.search(opts, null);
        Set<String> labels = new TreeSet<String>();
        for (TextIndexer.Facet f : results.getFacets()) {
            if (f.getName().equals(SOURCE)) {
                for (TextIndexer.FV fv : f.getValues())
                    labels.add(fv.getLabel());
            }
        }

        Class[] entities = new Class[]{
            Disease.class, Target.class, Ligand.class
        };

        List<DataSource> sources = new ArrayList<DataSource>();
        for (String la : labels ) {
            DataSource ds = new DataSource (la);
            for (Class cls : entities) {
                opts = new SearchOptions (cls, 1, 0, 10);
                results = _textIndexer.search(opts, null);
                for (TextIndexer.Facet f : results.getFacets()) {
                    if (f.getName().equals(SOURCE)) {
                        for (TextIndexer.FV fv : f.getValues())
                            if (la.equals(fv.getLabel())) {
                                if (cls == Target.class)
                                    ds.targets = fv.getCount();
                                else if (cls == Disease.class)
                                    ds.diseases = fv.getCount();
                                else
                                    ds.ligands = fv.getCount();
                            }
                    }
                }
            }
            sources.add(ds);
        }

        return sources.toArray(new DataSource[0]);
    }
    
    public static DataSource[] getDataSources () {
        final String key = "IDGApp/datasources";
        try {
            return getOrElse (key, new Callable<DataSource[]> () {
                    public DataSource[] call () throws Exception {
                        return _getDataSources ();
                    }
                });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new DataSource[0];
    }
    
    static final GetResult<Target> TargetResult =
        new GetResult<Target>(Target.class, TargetFactory.finder) {
            public Result getResult (List<Target> targets) throws Exception {
                return _getTargetResult (targets);
            }
        };

    public static Result target (final String name) {
        return TargetResult.get(name);
    }

    static List<DiseaseRelevance> getDiseases (final Target t)
        throws Exception {
        final String key = "targets/"+t.id+"/diseases";
        return getOrElse
            (key, new Callable<List<DiseaseRelevance>> () {
                    public List<DiseaseRelevance> call () throws Exception {
                        return getDiseaseRelevances (t);
                    }
                });
    }
    
    static Result _getTargetResult (final List<Target> targets)
        throws Exception {
        final Target t = targets.iterator().next(); // guarantee not empty
        List<DiseaseRelevance> diseases = getDiseases (t);
        List<Keyword> breadcrumb = getBreadcrumb (t);
        
        return ok (ix.idg.views.html
                   .targetdetails.render(t, diseases, breadcrumb));
    }

    public static Result targetWarmCache (String secret) {
        if (secret == null || secret.length() == 0
            || !secret.equals(Play.application()
                              .configuration().getString("ix.idg.secret"))) {
            return unauthorized
                ("You do not have permission to access this resource!");
        }

        try {
            TargetCacheWarmer cache = getOrElse
                ("IDGApp.targetWarmCache", new Callable<TargetCacheWarmer> () {
                        public TargetCacheWarmer call () throws Exception {
                            Logger.debug("Warming up target cache...");
                            QueryIterator<Keyword> kiter = KeywordFactory
                            .finder.where()
                            .eq("label", UNIPROT_ACCESSION)
                            .findIterate();
                            
                            List<String> targets = new ArrayList<String>();
                            while (kiter.hasNext()) {
                                Keyword kw = kiter.next();
                                targets.add(kw.term);
                            }
                            
                            return new TargetCacheWarmer (targets);
                        }
                    });
            ObjectMapper mapper = new ObjectMapper ();
            return ok (mapper.valueToTree(cache));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't retrieve target cache", ex);
            return _internalServerError (ex);
        }
    }

    static List<Keyword> getBreadcrumb (Target t) {
        List<Keyword> breadcrumb = new ArrayList<Keyword>();
        for (Value v : t.properties) {
            if (v.label != null && v.label.startsWith(DTO_PROTEIN_CLASS)) {
                try {
                    Keyword kw = (Keyword)v;
                    String url = ix.idg.controllers
                        .routes.IDGApp.targets(null, 10, 1).url();
                    kw.href = url + (url.indexOf('?') > 0 ? "&":"?")
                        +"facet="+kw.label+"/"
                        +URLEncoder.encode(kw.term, "utf8");
                    breadcrumb.add(kw);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.error("Can't generate breadcrumb for "
                                 +getId (t), ex);
                }
            }
        }
        // just make sure the order is correct
        Collections.sort(breadcrumb, new Comparator<Keyword>() {
                public int compare (Keyword kw1, Keyword kw2) {
                    return kw1.label.compareTo(kw2.label);
                }
            });
        return breadcrumb;
    }

    public static List<Ligand> getLigands (EntityModel e) {
        List<Ligand> ligands = new ArrayList<Ligand>();
        for (XRef xref : e.getLinks()) {
            try {
                Class cls = Class.forName(xref.kind);
                if (Ligand.class.isAssignableFrom(cls))
                    ligands.add((Ligand)xref.deRef());
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't resolve XRef "
                             +xref.kind+":"+xref.refid, ex);
            }
        }
        return ligands;
    }

    public static List<Value> getProperties (EntityModel e, String label) {
        return getProperties (e, label, 0);
    }

    static Comparator<Value> CompareValues = new Comparator<Value>() {
            public int compare (Value v1, Value v2) {
                return v1.label.compareTo(v2.label);
            }
        };
    public static List<Value> getProperties
        (EntityModel e, String label, int dir) {
        List<Value> props = new ArrayList<Value>();
        
        if (dir < 0) { // prefix
            for (Value v : e.getProperties())
                if (v.label.startsWith(label))
                    props.add(v);
            Collections.sort(props, CompareValues);
        }
        else if (dir > 0) { // suffix
            for (Value v : e.getProperties())
                if (v.label.endsWith(label)) {
                    if (!v.label.equals("TM Count")) {
                        props.add(v);
                    }
                }
            Collections.sort(props, CompareValues);
        }
        else {
            for (Value v : e.getProperties()) 
                if (label.equalsIgnoreCase(v.label))
                    props.add(v);
        }
        
        return props;
    }

    public static List<Mesh> getMesh (EntityModel e) {
        Map<String, Mesh> mesh = new TreeMap<String, Mesh>();
        for (Publication p : e.getPublications()) {
            for (Mesh m : p.mesh)
                mesh.put(m.heading, m);
        }
        return new ArrayList<Mesh>(mesh.values());
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
                long s = System.currentTimeMillis();
                getLineage (lineage, dr.disease);
                Logger.debug("Retrieve lineage for disease "+dr.disease.id+"..."
                             +String.format("%1$dms", (System.currentTimeMillis()-s)));
                
                /*
                { Disease d = dr.disease;
                    for (Disease parent : getLineage (d)) {
                        lineage.put(d.id, parent);
                        d = parent;
                    }
                }
                */
                for (Value p : xref.properties) {
                    if (IDG_ZSCORE.equals(p.label))
                        dr.zscore = (Double)p.getValue();
                    else if (IDG_CONF.equals(p.label))
                        dr.conf = (Double)p.getValue();
                    else if (TINX_IMPORTANCE.equals(p.label))
                        dr.tinxScore = (Double)p.getValue();
                    else if (UNIPROT_DISEASE_RELEVANCE.equals(p.label)
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
        double elapsed = (System.currentTimeMillis()-start)*1e-3;
        Logger.debug("Elapsed time "+String.format("%1$.3fs", elapsed)
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

    static Result createTargetResult
            (TextIndexer.SearchResult result, int rows, int page) {
        TextIndexer.Facet[] facets = filter
                (result.getFacets(), TARGET_FACETS);

        List<Target> targets = new ArrayList<Target>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging (rows, page, result.count());
            result.copyTo(targets, (page-1)*rows, rows);
        }

        return ok(ix.idg.views.html.targets.render
                  (page, rows, result.count(),
                   pages, decorate (Target.class, facets),
                   targets, result.getKey()));

    }

    static Result createLigandResult
            (TextIndexer.SearchResult result, int rows, int page) {
        TextIndexer.Facet[] facets = filter
                (result.getFacets(), LIGAND_FACETS);

        List<Ligand> ligands = new ArrayList<Ligand>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging (rows, page, result.count());
            result.copyTo(ligands, (page-1)*rows, rows);
        }

        return ok(ix.idg.views.html.ligandsmedia.render
                  (page, rows, result.count(),
                   pages, decorate (facets), ligands, null));
    }
    
    static Result createDiseaseResult
            (TextIndexer.SearchResult result, int rows, int page) {
        TextIndexer.Facet[] facets = filter
                (result.getFacets(), DISEASE_FACETS);

        List<Disease> diseases = new ArrayList<Disease>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging (rows, page, result.count());
            result.copyTo(diseases, (page-1)*rows, rows);
        }

        return ok(ix.idg.views.html.diseases.render
                  (page, rows, result.count(),
                   pages, decorate (facets), diseases));
    }

    public static Result targets (String q, final int rows, final int page) {
        try {
            if (q != null && q.trim().length() == 0)
                q = null;
            
            String type = request().getQueryString("type");
            if (q != null && type != null) {
                if (type.equalsIgnoreCase("sequence")) {
                    return sequences (q, rows, page);
                }
                else if (type.equalsIgnoreCase("batch")) {
                    return batchSearch (q, rows, page);
                }
            }
            
            return _targets (q, rows, page);
        }
        catch (Exception ex) {
            Logger.error("Can't retrieve targets", ex);
            ex.printStackTrace();
            return _internalServerError (ex);
        }
    }

    static String csvFromTarget(Target t) {
        Object novelty = "";
        Object function = "";

        StringBuilder sb2 = new StringBuilder();
        String delimiter = "";
        for (Publication pub : t.getPublications()) {
            sb2.append(delimiter).append(pub.pmid);
            delimiter = "|";
        }

        List<Value> props = t.getProperties();
        for (Value v : props) {
            if (v.label.equals("TINX Novelty")) novelty = v.getValue();
            else if (v.label.equals("function")) function = v.getValue();
        }

        // get classifications
        String chemblClass = "";
        String dtoClass = "";
        String pantherClass = "";
        for (Value v : t.properties) {
            if (v.label == null) continue;
            if (v.label.startsWith(DTO_PROTEIN_CLASS))
                dtoClass = ((Keyword) v).getValue();
            else if (v.label.startsWith(PANTHER_PROTEIN_CLASS))
                pantherClass = ((Keyword) v).getValue();
            else if (v.label.startsWith(ChEMBL_PROTEIN_CLASS))
                chemblClass = ((Keyword) v).getValue();
        }


        StringBuilder sb = new StringBuilder();
        sb.append(routes.IDGApp.target(getId(t))).append(",").
                append(getId(t)).append(",").
                append(t.getName()).append(",").
                append(csvQuote(t.getDescription())).append(",").
                append(t.idgTDL.toString()).append(",").
                append(dtoClass).append(",").
                append(pantherClass).append(",").
                append(chemblClass).append(",").
                append(novelty).append(",").
                append(t.idgFamily).append(",").
                append(csvQuote(function.toString())).append(",").
                append(sb2.toString());
        return sb.toString();
    }

    static String csvQuote(String s) {
        if (s == null) return s;
        if (s.contains("\"")) s = s.replace("\"", "\\\"");
        return "\""+s+"\"";
    }

    // check to see if q is format like a range
    final static Pattern RangeRe = Pattern.compile
        ("([^:]+):\\[([^,]*),([^\\]]*)\\]");
    static SearchResult getRangeSearchResult (Class kind, String q, final int total,
                                              Map<String, String[]> params) {
        if (q != null) {
            try {
                Matcher m = RangeRe.matcher(q);
                if (m.find()) {
                    final String field = m.group(1);
                    final String min = m.group(2);
                    final String max = m.group(3);
                    
                    Logger.debug("range: field="+field+" min="+min+" max="+max);
                    final String sha1 = signature (q, request().queryString());
                    return getOrElse (sha1, new Callable<SearchResult> () {
                            public SearchResult call () throws Exception {
                                SearchOptions options =
                                    new SearchOptions (request().queryString());
                                options.top = total;
                                SearchResult result = _textIndexer.range
                                    (options, field, min.equals("")
                                     ? null : Integer.parseInt(min),
                                     max.equals("") ? null : Integer.parseInt(max));
                                result.setKey(sha1);
                                return result;
                            }
                        });
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't perform range search", ex);
            }
        }
        return getSearchResult (kind, q, total, params);
    }

    static Result _targets (final String q, final int rows, final int page)
        throws Exception {
        final String key = "targets/"+Util.sha1(request ());
        Logger.debug("Targets: q="+q+" rows="+rows+" page="+page+" key="+key);
        
        final int total = TargetFactory.finder.findRowCount();
        if (request().queryString().containsKey("facet") || q != null) {
            Map<String, String[]> query = getRequestQuery ();
            if (!query.containsKey("order") && q == null) {
                // only implicitly order based on novelty if it's not a
                // search
                query.put("order", new String[]{"$novelty"});
            }
            
            final SearchResult result =
                getRangeSearchResult (Target.class, q, total, query);
            
            String action = request().getQueryString("action");
            if (action == null) action = "";

            if (action.toLowerCase().equals("download")) {
                StringBuilder sb = new StringBuilder();
                sb.append("URL,Uniprot ID,Name,Description,Development Level,DTOClass,PantherClass,ChemblClass,Novelty,Target Family,Function,PMIDs\n");
                if (result.count() > 0) {
                    for (int i = 0; i < result.count(); i++) {
                        Target t = (Target) result.getMatches().get(i);
                        sb.append(csvFromTarget(t)).append("\n");
                    }
                }
                return ok(sb.toString().getBytes()).as("text/csv");
            }

            if (result.finished()) {
                // now we can cache the result
                return getOrElse
                    (key+"/result", new Callable<Result> () {
                            public Result call () throws Exception {
                                return createTargetResult
                                    (result, rows, page);
                            }
                        });
            }

            return createTargetResult (result, rows, page);
        }
        else {
            final SearchResult result = getSearchFacets (Target.class);
            return getOrElse (key+"/result", new Callable<Result> () {
                    public Result call () throws Exception {
                        TextIndexer.Facet[] facets = filter
                            (result.getFacets(), TARGET_FACETS);
                        int _rows = Math.min(total, Math.max(1, rows));
                        int[] pages = paging (_rows, page, total);
                        
                        List<Target> targets = TargetFactory.getTargets
                            (_rows, (page-1)*_rows, null);

                        long start = System.currentTimeMillis();
                        Result r = ok (ix.idg.views.html.targets.render
                                       (page, _rows, total, pages,
                                        decorate (facets),
                                        targets, result.getKey()));
                        Logger.debug("rendering "+key+" in "
                                     + (System.currentTimeMillis()-start)+"ms...");
                        return r;
                    }
                });
        }
    }

    
    public static Result sequences (final String q,
                                    final int rows, final int page) {
        String param = request().getQueryString("identity");
        double identity = 0.5;
        if (param != null) {
            try {
                identity = Double.parseDouble(param);
            }
            catch (NumberFormatException ex) {
                Logger.error("Bogus identity value: "+param);
            }
        }
        
        String seq = App.getSequence(q);
        if (seq != null) {
            Logger.debug("sequence: "
                         +seq.substring(0, Math.min(seq.length(), 20))
                         +"; identity="+identity);
            return _sequences (seq, identity, rows, page);
        }
        
        return internalServerError ("Unable to retrieve sequence for "+q);
    }

    public static Result _sequences (final String seq, final double identity,
                                     final int rows, final int page) {
        try {
            SearchResultContext context = sequence
                (seq, identity, rows,
                 page, new IDGSequenceResultProcessor ());
            
            return App.fetchResult
                (context, rows, page, new DefaultResultRenderer<Target> () {
                        public Result render (SearchResultContext context,
                                              int page, int rows,
                                              int total, int[] pages,
                                              List<Facet> facets,
                                              List<Target> targets) {
                            return ok (ix.idg.views.html.targets.render
                                       (page, rows, total,
                                        pages, decorate
                                        (Target.class,
                                         filter (facets, TARGET_FACETS)),
                                        targets, context.getId()));
                        }
                    });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform sequence search", ex);
            return _internalServerError (ex);
        }
    }

    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class,
                   maxLength = 20000)
    public static Result sequence () {
        if (request().body().isMaxSizeExceeded()) {
            return badRequest ("Sequence is too large!");
        }
        
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        String[] values = params.get("sequence");
        if (values != null && values.length > 0) {
            String seq = values[0];
            try {
                Payload payload = _payloader.createPayload
                    ("Sequence Search", "text/plain", seq);
                Call call = routes.IDGApp.targets(payload.id.toString(), 10, 1);
                return redirect (call.url()+"&type=sequence");
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return _internalServerError (ex);
            }
        }
        
        return badRequest ("Invalid \"sequence\" parameter specified!");
    }

    public static Keyword[] getAncestry (final String facet,
                                         final String predicate) {
        try {
            final String key = predicate+"/"+facet
                +"/"+signature (null, request().queryString());
            return getOrElse
                (key, new Callable<Keyword[]> () {
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
                    .add(Expr.eq("subject.refid", anchor.id.toString()))
                    .add(Expr.eq("subject.kind", anchor.getClass().getName()))
                    .add(Expr.eq("predicate", predicate))
                    .findList();
                if (!pred.isEmpty()) {
                    for (XRef ref : pred.iterator().next().objects) {
                        if (ref.kind.equals(anchor.getClass().getName())) {
                            Keyword kw = (Keyword)ref.deRef();
                            /*
                            String url = ix.idg.controllers
                                .routes.IDGApp.targets(null, 20, 1).url();
                            */
                            String url = App.url(App.removeIfMatch("facet", facet));
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
        return getAncestry (facet,
                            //ChEMBL_PROTEIN_ANCESTRY
                            DTO_PROTEIN_ANCESTRY
                            );
    }

    public static Keyword[] getATCAncestry (String facet) {
        List<Keyword> ancestry = new ArrayList<Keyword>();
        String[] toks = facet.split("/");
        if (toks[0].equals(WHO_ATC)) {
            String atc = toks[1];
            try {
                int len = atc.length();
                switch (len) {
                case 1:
                    break;
                    
                case 7:
                    ancestry.add(getATC (atc.substring(0,5)));
                    // fall through
                case 5:
                    ancestry.add(getATC (atc.substring(0,4)));
                    // fall through
                case 4:
                    ancestry.add(getATC (atc.substring(0,3)));
                    // fall through
                case 3:
                    ancestry.add(getATC (atc.substring(0,1)));
                    Collections.sort(ancestry, new Comparator<Keyword>() {
                            public int compare (Keyword kw1, Keyword kw2) {
                                return kw1.label.compareTo(kw2.label);
                            }
                        });
                    break;
                default:
                    Logger.warn("Not a valid ATC facet value: "+ atc);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't get ATC", ex);
            }
        }
        return ancestry.toArray(new Keyword[0]);
    }

    public static Result search (String kind) {
        try {
            String q = request().getQueryString("q");
            String t = request().getQueryString("type");

            if (kind != null && !"".equals(kind)) {
                if (Target.class.getName().equals(kind))
                    return redirect (routes.IDGApp.targets(q, 10, 1));
                else if (Disease.class.getName().equals(kind))
                    return redirect (routes.IDGApp.diseases(q, 10, 1));
                else if (Ligand.class.getName().equals(kind))
                    return redirect (routes.IDGApp.ligands(q, 8, 1));
            }
            else if ("substructure".equalsIgnoreCase(t)) {
                String url = routes.IDGApp.ligands(q, 8, 1).url()
                    +"&type="+t;
                return redirect (url);
            }
            else if ("similarity".equalsIgnoreCase(t)) {
                String cutoff = request().getQueryString("cutoff");
                if (cutoff == null) {
                    cutoff = "0.8";
                }
                String url = routes.IDGApp.ligands(q, 8, 1).url()
                    +"&type="+t+"&cutoff="+cutoff;
                return redirect (url);
            }
            else if ("sequence".equalsIgnoreCase(t)) {
                String iden = request().getQueryString("identity");
                if (iden == null) {
                    iden = "0.5";
                }
                String url = routes.IDGApp.targets(q, 10, 1).url()
                    +"&type="+t+"&identity="+iden;
                return redirect (url);
            }
            
            // generic entity search..
            return search (5);
        }
        catch (Exception ex) {
            Logger.debug("Can't resolve class: "+kind, ex);
        }
            
        return _badRequest("Invalid request: " + request().uri());
    }

    static <T> List<T> filter (Class<T> cls, List values, int max) {
        List<T> fv = new ArrayList<T>();
        for (Object v : values) {
            if (cls.isAssignableFrom(v.getClass())) {
                fv.add((T)v);
                if (fv.size() >= max)
                    break;
            }
        }
        return fv;
    }

    public static Result search (final int rows) {
        try {
            final String key = "search/"+Util.sha1(request ());
            return getOrElse (key, new Callable<Result> () {
                    public Result call () throws Exception {
                        return _search (rows);
                    }
                });
        }
        catch (Exception ex) {
            return _internalServerError (ex);
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
            final String key =
                "search/facet/"+Util.sha1(queryString.get("facet")); 
            result = getOrElse
                (key, new Callable<TextIndexer.SearchResult>() {
                        public TextIndexer.SearchResult
                            call ()  throws Exception {
                            return SearchFactory.search
                            (_textIndexer, null, null, MAX_SEARCH_RESULTS,
                             0, FACET_DIM, queryString);
                        }
                    });
            double ellapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug
                ("1. Ellapsed time "+String.format("%1$.3fs", ellapsed));
        }

        if (result == null || result.count() == 0) {
            long start = System.currentTimeMillis();
            final String key =
                "search/facet/q/"+Util.sha1(request(), "facet", "q");
            result = getOrElse
                (key, new Callable<TextIndexer.SearchResult>() {
                        public TextIndexer.SearchResult
                            call () throws Exception {
                            return SearchFactory.search
                            (_textIndexer, null, query, MAX_SEARCH_RESULTS, 0,
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
        int total = 0, totalTargets = 0, totalDiseases = 0, totalLigands = 0, totalPubs = 0;
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
                    else if (Ligand.class.getName().equals(fv.getLabel())) {
                        totalLigands = fv.getCount();
                        total += totalLigands;
                    }
                    else if (Publication.class.getName().equals(fv.getLabel())) {
                        totalPubs = fv.getCount();
                        total += totalPubs;
                    }
                }
            }
        }
        
        List<Target> targets =
            filter (Target.class, result.getMatches(), max);
        List<Disease> diseases =
            filter (Disease.class, result.getMatches(), max);
        List<Ligand> ligands = filter (Ligand.class, result.getMatches(), max);
        List<Publication> publications = filter
            (Publication.class, result.getMatches(), max);

        return ok(ix.idg.views.html.search.render
                (query, total, decorate(facets),
                        targets, totalTargets,
                        ligands, totalLigands,
                        diseases, totalDiseases,
                        publications, totalPubs));
    }

    public static Keyword getATC (final String term) throws Exception {
        final String key = WHO_ATC+" "+term;
        return getOrElse (0l, key, new Callable<Keyword>() {
                public Keyword call () {
                    List<Keyword> kws = KeywordFactory.finder.where()
                        .eq("label", key).findList();
                    if (!kws.isEmpty()) {
                        Keyword n = kws.iterator().next();
                        String url = routes.IDGApp.ligands(null, 8, 1).url();
                        n.term = n.term.toLowerCase();
                        n.href = url + (url.indexOf('?') > 0?"&":"?")
                            +"facet="+WHO_ATC+"/"+term;
                        return n;
                    }
                    return null;
                }
            });
    }

    public static Keyword getATC (final Keyword kw) throws Exception {
        if (kw.label.equals(WHO_ATC))
            return getATC (kw.term);
        Logger.warn("Not a valid ATC label: "+kw.label);
        return null;
    }
    
    public static Result ligands (String q, final int rows, final int page) {
        String type = request().getQueryString("type");
        if (q != null && q.trim().length() == 0)
            q = null;
        
        long start = System.currentTimeMillis();
        try {
            if (type != null && (type.equalsIgnoreCase("substructure")
                                 || type.equalsIgnoreCase("similarity"))) {
                // structure search
                String cutoff = request().getQueryString("cutoff");
                Logger.debug("Search: q="+q+" type="+type+" cutoff="+cutoff);
                if (type.equalsIgnoreCase("substructure")) {
                    return substructure (q, rows, page);
                }
                else {
                    return similarity
                        (q, Double.parseDouble(cutoff), rows, page);
                }
            }
            else {
                return _ligands (q, rows, page);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError (ex);
        }
        finally {
            Logger.debug("ligands: q="+q+" rows="+rows+" page="+page
                         +"..."+String.format
                         ("%1$dms", System.currentTimeMillis()-start));
        }
    }

    static String csvFromLigand(Ligand l) throws ClassNotFoundException {

        String inchiKey = "";
        String canSmi = "";

        for (Value v : l.getProperties()) {
            if (ChEMBL_INCHI_KEY.equals(v.label))
                inchiKey = (String) v.getValue();
            else if (ChEMBL_SMILES.equals(v.label))
                canSmi = (String) v.getValue();
        }

        StringBuilder sb2 = new StringBuilder();
        String delimiter = "";
        List<XRef> links = l.getLinks();
        for (XRef xref : links) {
            if (Target.class.isAssignableFrom(Class.forName(xref.kind))) {
                sb2.append(delimiter).append(getId((Target) xref.deRef()));
                delimiter = "|";
            }
        }


        StringBuilder sb = new StringBuilder();
        sb.append(routes.IDGApp.ligand(getId(l))).append(",").
                append(getId(l)).append(",").
                append(csvQuote(l.getName())).append(",").
                append(csvQuote(l.getDescription())).append(",").
                append(canSmi).append(",").
                append(inchiKey).append(",").
                append(sb2.toString());
        return sb.toString();
    }


    static Result _ligands (final String q, final int rows, final int page)
        throws Exception {
        final String key = "ligands/"+Util.sha1(request ());
        Logger.debug("ligands: q="+q+" rows="+rows+" page="+page+" key="+key);
        
        final int total = LigandFactory.finder.findRowCount();
        if (request().queryString().containsKey("facet") || q != null) {
            final TextIndexer.SearchResult result =
                getSearchResult (Ligand.class, q, total, getRequestQuery ());
            
            String action = request().getQueryString("action");
            if (action == null) action = "";

            if (action.toLowerCase().equals("download")) {
                StringBuilder sb = new StringBuilder();
                sb.append("URL,ID,Name,Description,SMILES,InChI Key,Targets\n");
                if (result.count() > 0) {
                    for (int i = 0; i < result.count(); i++) {
                        Ligand d = (Ligand) result.getMatches().get(i);
                        sb.append(csvFromLigand(d)).append("\n");
                    }
                }
                return ok(sb.toString().getBytes()).as("text/csv");
            }

            if (result.finished()) {
                // now we can cache the result
                return getOrElse
                        (key+"/result", new Callable<Result> () {
                            public Result call () throws Exception {
                                return createLigandResult
                                    (result, rows, page);
                            }
                        });
            }

            return createLigandResult (result, rows, page);
        }
        else {
            return getOrElse (key, new Callable<Result> () {
                    public Result call () throws Exception {
                        TextIndexer.Facet[] facets =
                            filter (getFacets (Ligand.class, FACET_DIM),
                                    LIGAND_FACETS);
            
                        int _rows = Math.min(total, Math.max(1, rows));
                        int[] pages = paging (_rows, page, total);
            
                        List<Ligand> ligands = LigandFactory.getLigands
                            (_rows, (page-1)*_rows, null);
            
                        return ok (ix.idg.views.html.ligandsmedia.render
                                   (page, _rows, total, pages,
                                    decorate (facets), ligands, null));
                    }
                });
        }
    }

    static final GetResult<Ligand> LigandResult =
        new GetResult<Ligand>(Ligand.class, LigandFactory.finder) {
            public Result getResult (List<Ligand> ligands) throws Exception {
                return _getLigandResult (ligands);
            }
        };

    static Result _getLigandResult (List<Ligand> ligands) throws Exception {
        // force it to show only one since it's possible that the provided
        // name isn't unique
        if (true || ligands.size() == 1) {
            Ligand ligand = ligands.iterator().next();
            
            List<Keyword> breadcrumb = new ArrayList<Keyword>();
            for (Keyword kw : ligand.synonyms) {
                if (kw.label.equals(WHO_ATC)
                    // don't include the leaf node
                    && kw.term.length() < 7) {
                    breadcrumb.add(kw);
                }
            }
            
            if (!breadcrumb.isEmpty()) {
                Collections.sort(breadcrumb, new Comparator<Keyword>() {
                        public int compare (Keyword kw1, Keyword kw2) {
                            return kw1.term.compareTo(kw2.term);
                        }
                    });
                for (Keyword kw : breadcrumb) {
                    try {
                        Keyword atc = getATC (kw);
                        if (atc != null) {
                            kw.term = atc.term;
                            kw.href = atc.href;
                        }
                    }
                    catch (Exception ex) {
                        Logger.error("Can't retreive ATC "+kw.term, ex);
                        ex.printStackTrace();
                    }
                }
            }
            
            List<LigandActivity> acts = new ArrayList<LigandActivity>();
            for (XRef ref : ligand.getLinks()) {
                if (ref.kind.equals(Target.class.getName())) {
                    acts.add(new LigandActivity (ref));
                }
            }
            
            return ok (ix.idg.views.html
                       .liganddetails.render(ligand, acts, breadcrumb));
        }
        else {
            TextIndexer indexer = _textIndexer.createEmptyInstance();
            for (Ligand lig : ligands)
                indexer.add(lig);
            
            TextIndexer.SearchResult result = SearchFactory.search
                (indexer, Ligand.class, null, indexer.size(), 0, FACET_DIM,
                 request().queryString());
            if (result.count() < ligands.size()) {
                ligands.clear();
                for (int i = 0; i < result.count(); ++i) {
                    ligands.add((Ligand)result.getMatches().get(i));
                }
            }
            TextIndexer.Facet[] facets = filter
                (result.getFacets(), LIGAND_FACETS);
            indexer.shutdown();
        
            return ok (ix.idg.views.html.ligands.render
                       (1, result.count(), result.count(),
                        new int[0], decorate (facets), ligands));
        }
    }
    
    public static Result ligand (String name) {
        return LigandResult.get(name);
    }

    /**
     * return the canonical/default ligand id
     */
    public static String getId (Ligand ligand) {
        return ligand.getName();
    }
    public static Structure getStructure (Ligand ligand) {
        for (XRef xref : ligand.getLinks()) {
            if (xref.kind.equals(Structure.class.getName())) {
                return (Structure)xref.deRef();
            }
        }
        return null;
    }
    
    public static Set<Target.TDL> getTDL (EntityModel model) {
        Set<Target.TDL> tdls = EnumSet.noneOf(Target.TDL.class);
        for (XRef ref : model.getLinks()) {
            if (ref.kind.equals(Target.class.getName())) {
                for (Value v : ref.properties) {
                    if (IDG_DEVELOPMENT.equals(v.label)) {
                        tdls.add(Target.TDL.fromString(((Keyword)v).term));
                    }
                }
            }
        }
        return tdls;
    }

    public static Set<String> getMechanisms (Ligand lig) {
        Set<String> moa = new TreeSet<String>();
        for (XRef ref : lig.links) {
            if (ref.kind.equals(Target.class.getName())) {
                for (Value v : ref.properties) {
                    if (ChEMBL_MECHANISM.equals(v.label))
                        moa.add(((Text)v).text);
                }
            }
        }
        return moa;
    }

    public static List<Mesh> getMajorTopics (EntityModel model) {
        List<Mesh> topics = new ArrayList<Mesh>();
        for (Publication pub : model.getPublications()) {
            for (Mesh m : pub.mesh) {
                if (m.majorTopic)
                    topics.add(m);
            }
        }
        return topics;
    }

    public static Result fetchResult
        (final SearchResultContext context, int rows, int page)
        throws Exception {
        return App.fetchResult
            (context, rows, page, new DefaultResultRenderer<Ligand> () {
                    public Result render (SearchResultContext context,
                                          int page, int rows,
                                          int total, int[] pages,
                                          List<TextIndexer.Facet> facets,
                                          List<Ligand> ligands) {
                        return ok (ix.idg.views.html.ligandsmedia.render
                                   (page, rows, total,
                                    pages, decorate (filter
                                                     (facets, LIGAND_FACETS)),
                                    ligands, context.getId()));
                    }
            });
    }

    public static Result similarity (final String query,
                                     final double threshold,
                                     final int rows,
                                     final int page) {
        try {
            SearchResultContext context = similarity
                (query, threshold, rows, page, new IDGSearchResultProcessor ());
            if (context != null) {
                return fetchResult (context, rows, page);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform similarity search", ex);
        }
        return internalServerError
            (ix.idg.views.html.error.render
             (500, "Unable to perform similarity search: "+query));
    }
    
    public static Result substructure
        (final String query, final int rows, int page) {
        try {
            SearchResultContext context = substructure
                (query, rows, page, new IDGSearchResultProcessor ());
            if (context != null) {
                return fetchResult (context, rows, page);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform substructure search", ex);
        }
        return internalServerError
            (ix.idg.views.html.error.render
             (500, "Unable to perform substructure search: "+query));
    }
    
    public static String getId (Disease d) {
        Keyword kw = d.getSynonym(DiseaseOntologyRegistry.DOID,
                                  UNIPROT_DISEASE);
        return kw != null ? kw.term : null;
    }
    
    static final GetResult<Disease> DiseaseResult =
        new GetResult<Disease>(Disease.class, DiseaseFactory.finder) {
            public Result getResult (List<Disease> diseases) throws Exception {
                return _getDiseaseResult (diseases);
            }
        };

    public static Result disease (final String name) {
        return DiseaseResult.get(name);
    }

    static Result _getDiseaseResult (final List<Disease> diseases)
        throws Exception {
        final Disease d = diseases.iterator().next();
        // resolve the targets for this disease
        final String key = "diseases/"+d.id+"/targets";
        List<Target> targets = getOrElse
            (key, new Callable<List<Target>> () {
                    public List<Target> call () throws Exception {
                        List<Target> targets = new ArrayList<Target>();
                        for (XRef ref : d.links) {
                            if (Target.class.isAssignableFrom
                                (Class.forName(ref.kind))) {
                                Target t = (Target) ref.deRef();
                                targets.add(t);
                            }
                        }
                        return targets;
                    }
                });

        
        return ok(ix.idg.views.html.diseasedetails.render
                  (d, targets.toArray(new Target[0]), getBreadcrumb (d)));
    }

    public static List<Keyword> getBreadcrumb (Disease d) {
        List<Keyword> breadcrumb = new ArrayList<Keyword>();
        for (Value prop : d.properties) {
            if (DiseaseOntologyRegistry.PATH.equals(prop.label)) {
                String[] path = ((Text)prop).text.split("/");
                for (String p : path) {
                    if (p.length() > 0) {
                        Keyword node = new Keyword (prop.label, p);
                        String url = ix.idg.controllers
                            .routes.IDGApp.diseases(null, 10, 1).url();
                        node.href = url + (url.indexOf('?') > 0 ? "&":"?")
                            +"facet="+DiseaseOntologyRegistry.CLASS+"/"+p;
                        breadcrumb.add(node);
                    }
                }
            }
        }
        return breadcrumb;
    }

    public static List<Keyword> getDiseaseAncestry (String name) {
        List<Disease> diseases =
            // probably should be more exact here?
            DiseaseFactory.finder.where().eq("name", name).findList();
        if (!diseases.isEmpty()) {
            if (diseases.size() > 1) {
                Logger.warn("Name \""+name+"\" maps to "+diseases.size()+
                            "diseases!");
            }
            return getBreadcrumb (diseases.iterator().next());
        }
        return new ArrayList<Keyword>();
    }
    
    public static Result diseases (String q, final int rows, final int page) {
        try {
            if (q != null && q.trim().length() == 0)
                q = null;
            return _diseases (q, rows, page);
        }
        catch (Exception ex) {
            return _internalServerError (ex);
        }
    }

    static String csvFromDisease(Disease d) throws ClassNotFoundException {

        StringBuilder sb2 = new StringBuilder();
        String delimiter = "";
        List<XRef> links = d.getLinks();
        for (XRef xref : links) {
            if (Target.class.isAssignableFrom(Class.forName(xref.kind))) {
                sb2.append(delimiter).append(getId((Target) xref.deRef()));
                delimiter = "|";
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(routes.IDGApp.disease(getId(d))).append(",").
                append(getId(d)).append(",").
                append(csvQuote(d.getName())).append(",").
                append(csvQuote(d.getDescription())).append(",").
                append(sb2.toString());
        return sb.toString();
    }

    static Result _diseases (final String q, final int rows, final int page)
        throws Exception {
        final int total = DiseaseFactory.finder.findRowCount();
        final String key = "diseases/"+Util.sha1(request ());
        Logger.debug("Diseases: rows=" + rows + " page=" + page+" key="+key);
        
        if (request().queryString().containsKey("facet") || q != null) {
            final TextIndexer.SearchResult result =
                getSearchResult (Disease.class, q, total, getRequestQuery ());
            
            String action = request().getQueryString("action");
            if (action == null) action = "";

            if (action.toLowerCase().equals("download")) {
                StringBuilder sb = new StringBuilder();
                sb.append("URL,DOID,Name,Description,Targets\n");
                if (result.count() > 0) {
                    for (int i = 0; i < result.count(); i++) {
                        Disease d = (Disease) result.getMatches().get(i);
                        sb.append(csvFromDisease(d)).append("\n");
                    }
                }
                return ok(sb.toString().getBytes()).as("text/csv");
            }

            if (result.finished()) {
                // now we can cache the result
                return getOrElse
                        (key+"/result", new Callable<Result> () {
                            public Result call () throws Exception {
                                return createDiseaseResult
                                    (result, rows, page);
                            }
                        });
            }

            return createDiseaseResult (result, rows, page);
        }
        else {
            return getOrElse (key, new Callable<Result> () {
                    public Result call () throws Exception {
                        TextIndexer.Facet[] facets = filter
                            (getFacets (Disease.class, FACET_DIM),
                             DISEASE_FACETS);
                        int _rows = Math.min(total, Math.max(1, rows));
                        int[] pages = paging(_rows, page, total);
                        
                        List<Disease> diseases = DiseaseFactory.getDiseases
                            (_rows, (page - 1) * _rows, null);
            
                        return ok(ix.idg.views.html.diseases.render
                                  (page, _rows, total, pages,
                                   decorate (facets), diseases));
                    }
                });
        }
    }

    public static Result lastUnicorn (String url) {
        return _notFound ("Unknown resource: "+url);
    }

    public static Result getHierarchy (final String ctx, final String facet) {
        final SearchResult result = getSearchContext (ctx);
        if (result != null) {
            try {
                return getOrElse (ctx+"/hierarchy", new Callable<Result> () {
                        public Result call () throws Exception {
                            return ok (getHierarchyAsJson (result, facet));
                        }
                    });
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.trace("Can't retrieve hierarchy "+ctx+" for "+facet, ex);
                return _internalServerError (ex);
            }
        }
        return notFound ("Unknown search context "+ctx);
    }
    
    public static JsonNode getHierarchyAsJson
        (SearchResult result, String facet) {
        List<Facet> facets = new ArrayList<Facet>();
        for (Facet f : result.getFacets()) {
            if (f.getName().startsWith(facet)) {
                facets.add(f);
            }
        }

        if (facets.isEmpty()) {
            return null;
        }
        
        // order the facets from child to parent
        Collections.sort(facets, new Comparator<Facet>() {
                public int compare (Facet f1, Facet f2) {
                    return f2.getName().compareTo(f1.getName());
                }
            });

        /*
        for (Iterator<Facet> it = facets.iterator(); it.hasNext(); ) {
            Facet f = it.next();
            Logger.info("++ "+f.getName());
            for (TextIndexer.FV fv : f.getValues()) {
                Logger.info("++++ "+fv.getLabel()+" ("+fv.getCount()+")");
            }
        }
        */

        Iterator<Facet> nodeIter = facets.iterator();
        String predicate = "";
        if (DTO_PROTEIN_CLASS.equalsIgnoreCase(facet)) {
            predicate = DTO_PROTEIN_ANCESTRY;
            // should really fix this when we pull in the tcrd..
            nodeIter.next(); // skip level (4)
        }
        else if (PANTHER_PROTEIN_CLASS.equalsIgnoreCase(facet))
            predicate = PANTHER_PROTEIN_ANCESTRY;
        else if (ChEMBL_PROTEIN_CLASS.equalsIgnoreCase(facet))
            predicate = ChEMBL_PROTEIN_ANCESTRY;
        
        Map root = new TreeMap ();
        root.put("name", facet);
        root.put("children", new ArrayList<Map>());

        while (nodeIter.hasNext()) {
            Facet leaf = nodeIter.next();
            for (TextIndexer.FV fv : leaf.getValues()) {
                Keyword[] ancestors = getAncestry
                    (leaf.getName()+"/"+fv.getLabel(), predicate);
                
                Map node = root;
                for (int i = 0; i < ancestors.length; ++i) {
                    String name = ancestors[i].term;
                    List<Map> children = (List<Map>)node.get("children");
                    
                    Map child = null;
                    for (Map c : children) {
                        if (name.equalsIgnoreCase((String)c.get("name"))) {
                            child = c;
                            break;
                        }
                    }
                    
                    if (child == null) {
                        child = new HashMap ();
                        child.put("name", name);
                        child.put("children", new ArrayList<Map>());
                        children.add(child);
                    }
                    node = child;
                }
                
                // leaf
                List<Map> children = (List<Map>)node.get("children");
                Map child = null;
                for (Map c : children) {
                    if (fv.getLabel().equalsIgnoreCase((String)c.get("name"))) {
                        child = c;
                        break;
                    }
                }
                
                if (child == null) {
                    child = new HashMap ();
                    child.put("name", fv.getLabel());
                    child.put("size", fv.getCount());
                    children.add(child);
                }
            }
        }
        
        //Logger.debug(">>> "+ix.core.controllers.EntityFactory.getEntityMapper().toJson(root, true));
        
        ObjectMapper mapper = new ObjectMapper ();
        return mapper.valueToTree(root);
    }

    public static String getSequence (Target target) {
        return getSequence (target, 80);
    }
    
    public static String getSequence (Target target, int wrap) {
        Value val = target.getProperty(UNIPROT_SEQUENCE);
        if (val == null) {
            return null;
        }
        
        String text = ((Text)val).text;
        return formatSequence (text, wrap);
    }

    public static SequenceIndexer.Result
        getSeqAlignment (String context, Target target) {
        Value seq = target.getProperty(UNIPROT_SEQUENCE);
        SequenceIndexer.Result r = null;
        if (seq != null) {
            r = (SequenceIndexer.Result)IxCache.get
                ("Alignment/"+context+"/"+seq.id);
        }
        //Logger.debug("retrieving alignment "+context+" "+seq.id+" => "+r);
        return r;
    }
    

    public static String formatSequence (String text, int wrap) {
        StringBuilder seq = new StringBuilder ();
        for (int len = text.length(), i = 1, j = 1; i <= len; ++i) {
            seq.append(text.charAt(i-1));           
            if (i % wrap == 0) {
                seq.append(String.format("%1$7d - %2$d\n", j, i));
                j = i+1;
            }
        }
        return seq.toString();
    }

    public static String getTargetTableHeader (String name, String field) {
        String order = request().getQueryString("order");
        String sort = "";
        if (order != null && field.equalsIgnoreCase(order.substring(1))) {
            char dir = order.charAt(0);
            if (dir == '^') { // ascending
                order = "$"+field;
                sort = "-asc";
            }
            else if (dir == '$') {
                order = "^"+field;
                sort = "-desc";
            }
            else {
                // default to descending
                order = "$"+field;
                sort = "-desc";
            }
        }
        else {
            // since novelty is the default..
            order = (order == null && field.equalsIgnoreCase("novelty")
                     ? "^":"$")+field;
        }
        String url = url ("order");
        if (url.indexOf('?') > 0) url += '&';
        else url += '?';
        
        return "<th><a href='"+url+"order="+order+"'>"+name
            +"</a>&nbsp;<i class='fa fa-sort"+sort+"'></i></th>";
    }

    public static <T extends EntityModel> List<T> filter
        (SearchResult results, Filter<T> filter) {
        List<T> matches = new ArrayList<T>();
        for (Object obj : results.getMatches()) {
            T e = (T)obj;
            if (filter.accept(e)) {
                matches.add(e);
            }
        }
        return matches;
    }

    static JsonNode getKinases (SearchResult result) {
        ObjectMapper mapper = new ObjectMapper ();      
        ArrayNode node = mapper.createArrayNode();
        for (Target t : filter (result, new Filter<Target> () {
                public boolean accept (Target t) {
                    //Logger.debug(t.getName()+" \""+t.idgFamily+"\"");
                    return "kinase".equalsIgnoreCase(t.idgFamily);
                }
            })) {
            //Logger.debug("Kinase: "+t.getName());
            for (Keyword kw : t.synonyms) {
                if (UNIPROT_GENE.equalsIgnoreCase(kw.label)) {
                    ObjectNode n = mapper.createObjectNode();
                    n.put("name", kw.term);
                    n.put("tdl", t.idgTDL.toString());
                    node.add(n);
                    break;
                }
            }
        }
        
        return node;
    }
    
    public static JsonNode _getKinases (final String q) throws Exception {
        final Map<String, String[]> query = getRequestQuery ();
        Logger.debug("** _getKinases: request");
        for (Map.Entry<String, String[]> me : query.entrySet()) {
            Logger.debug("["+me.getKey()+"]");
            String[] values = me.getValue();
            if (values != null) {
                for (String v : values)
                    Logger.debug(" "+v);
            }
        }
        
        String[] facets = query.get("facet");
        if (facets != null) {
            boolean hasFamily = false;
            for (String f : facets) {
                if (f.startsWith(IDG_FAMILY)) {
                    hasFamily = true;
                    break;
                }
            }
            
            if (!hasFamily) {
                // add kinase facet
                List<String> values = new ArrayList<String>();
                for (String f : facets)
                    values.add(f);
                values.add(IDG_FAMILY+"/Kinase");
                query.put("facet", values.toArray(new String[0]));
            }
            else {
                // leave it alone
            }
        }
        else {
            query.put("facet", new String[]{IDG_FAMILY+"/Kinase"});
        }
        List<String> args = new ArrayList<String>();
        facets = query.get("facet");
        if (facets != null) {
            for (String f : facets)
                args.add(f);
            Collections.sort(args);
        }
        if (q != null) args.add(q);
                
        final String key = "kinases/"+Util.sha1(args.toArray(new String[0]));
        final SearchResult result =
            getOrElse (key, new Callable<SearchResult> () {
                    public SearchResult call () throws Exception {
                        int total = TargetFactory.finder.findRowCount();        
                        return getRangeSearchResult (Target.class, q,
                                                     total, query);
                    }
                });
        for (String s : args) {
            Logger.debug(" ++ "+s);
        }
        Logger.debug("_getKinases: q="+q+" key="+key+" result="+result);
        
        if (result.finished()) {
            return getOrElse (key+"/json", new Callable<JsonNode>() {
                    public JsonNode call () throws Exception {
                        return getKinases (result);
                    }
                });
        }

        // not cached because it hasn't finished fetching..
        return getKinases (result);
    }

    public static Result getKinases (String q) {
        try {
            return ok (_getKinases (q));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError (ex);
        }
    }

    static Payload getBatchPayload () throws Exception {
        Payload payload = null;
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        String[] values = params.get("q");
        if (values != null && values.length > 0) {
            String content = values[0];
            payload = _payloader.createPayload
                ("Resolver Query", "text/plain", content);
            values = params.get("kind");
            String kind = null;
            if (values != null) {
                kind = values[0];
                if (Target.class.getName().equalsIgnoreCase(kind)
                    || Ligand.class.getName().equalsIgnoreCase(kind)
                    || Disease.class.getName().equalsIgnoreCase(kind)) {
                    payload.addIfAbsent
                        (KeywordFactory.registerIfAbsent
                         (IDG_RESOLVER, kind, null));
                    payload.update();
                }
                else {
                    Logger.debug("Bogus kind: "+kind);
                }
            }
            Logger.debug("batchResolver: kind="+kind
                         +" => payload "+payload.id);
        }
        return payload;
    }

    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class, 
                   maxLength = 100000)
    public static Result resolveBatch () {
        if (request().body().isMaxSizeExceeded()) {
            return badRequest ("Input is too large!");
        }

        try {
            Payload payload = getBatchPayload ();
            if (payload != null) {
                String kind = null;
                for (Value v : payload.properties)
                    if (v.label.equals(IDG_RESOLVER)) {
                        kind = ((Keyword)v).term;
                        break;
                    }
                return resolve (payload.id.toString(), kind);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError (ex);
        }
        return badRequest ("No \"q\" parameter specified!");
    }

    public static ArrayNode _resolveAsJson (String q, String kind) {
        ObjectMapper mapper = EntityFactory.getEntityMapper();
        ArrayNode nodes = mapper.createArrayNode();
        if (Target.class.getName().equalsIgnoreCase(kind)) {
            Map<Long, Target> found = new HashMap<Long, Target>();
            for (String tok : q.split("[\\s;,\n\t]")) {
                List<Target> targets = TargetFactory.finder.where().eq
                    ("synonyms.term", tok).findList();
                for (Target t : targets)
                    found.put(t.id, t);
            }
            Logger.debug("_resolve: "+found.size()+" unique entries resolved!");
            for (Target t : found.values()) {
                nodes.add(mapper.valueToTree(t));
            }
        }
        else if (Ligand.class.getName().equalsIgnoreCase(kind)) {
        }
        else if (Disease.class.getName().equalsIgnoreCase(kind)) {
        }
        return nodes;
    }
    
    public static ArrayNode resolveAsJson (final String q, final String kind)
        throws Exception {
        String view = request().getQueryString("view");
        final String key = "resolve/"+Util.sha1(q)+"/"+kind
            +(view != null ? "/"+view : "");
        return getOrElse (key, new Callable<ArrayNode>() {
                public ArrayNode call () throws Exception {
                    return _resolveAsJson (q, kind);
                }
            });
    }

    public static Result resolve (String q, String kind) {
        Logger.debug("resolve: q="+q+" kind="+kind);
        try {
            String content = PayloadFactory.getString(q);
            if (content != null) {
                ArrayNode nodes = resolveAsJson (content, kind);
                if (nodes.size() > 0)
                    return ok (nodes);
            }
        }
        catch (IllegalArgumentException ex) {
            // not a payload id
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError (ex);
        }
        
        try {
            ArrayNode nodes = resolveAsJson (q, kind);
            if (nodes.size() > 0)
                return ok (nodes);

            return notFound (nodes);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError (ex);
        }
    }

    @BodyParser.Of(value = BodyParser.FormUrlEncoded.class, 
                   maxLength = 100000)
    public static Result batch () {
        if (request().body().isMaxSizeExceeded()) {
            return badRequest ("Input is too large!");
        }

        try {
            Payload payload = getBatchPayload ();
            Call call = routes.IDGApp.targets(payload.id.toString(), 10, 1);
            return redirect (call.url()+"&type=batch");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError (ex);
        }
    }

    static class PayloadTokenizer extends DefaultTokenizer {
        @Override
        public Enumeration<String> tokenize (String q) {
            String payload = PayloadFactory.getString(q);
            if (payload != null) {
                return super.tokenize(payload);
            }
            return Collections.emptyEnumeration();
        }
    }

    public static Result batchSearch (final String q,
                                      final int rows, final int page) {
        try {
            SearchResultContext context = batch
                (q, rows, new PayloadTokenizer (),
                 new SearchResultProcessor<String> () {
                        Map<Long, Target> found = new HashMap<Long, Target>();
                        protected Object instrument (String token)
                            throws Exception {
                            // assume for now we're only batch search on
                            // targets.. will need to generalize it to other
                            // entities.
                            List<Target> targets =
                                TargetFactory.finder.where().eq
                                ("synonyms.term", token).findList();
                            if (!targets.isEmpty()) {
                                Target t = targets.iterator().next();
                                if (!found.containsKey(t.id)) {
                                    found.put(t.id, t);
                                    return t;
                                }
                                // dup.. so ignore..
                            }
                            return null;
                        }
                    });
            
            return App.fetchResult
                (context, rows, page, new DefaultResultRenderer<Target> () {
                        public Result render (SearchResultContext context,
                                              int page, int rows,
                                              int total, int[] pages,
                                              List<Facet> facets,
                                              List<Target> targets) {
                            return ok (ix.idg.views.html.targets.render
                                       (page, rows, total,
                                        pages, decorate
                                        (Target.class,
                                         filter (facets, TARGET_FACETS)),
                                        targets, context.getId()));
                        }
                    });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform batch search", ex);
        }
        return internalServerError
            (ix.idg.views.html.error.render
             (500, "Unable to perform batch search: "+q));
    }

    static final String[] TISSUES  = new String[] {
        "GTEx Tissue Specificity Index",
        "HPM Protein Tissue Specificity Index",
        "HPA RNA Tissue Specificity Index",
        //"HPA Protein Tissue Specificity Index"
    };
    static JsonNode _targetTissue (final String name) throws Exception {
        ObjectMapper mapper = new ObjectMapper ();
        ArrayNode nodes = mapper.createArrayNode();

        List<Target> targets = TargetResult.find(name);
        for (Target tar: targets) {
            ArrayNode axes = mapper.createArrayNode();
            for (String t: TISSUES) {
                ObjectNode n = mapper.createObjectNode();
                n.put("axis", t.replaceAll("Tissue Specificity Index",""));
                Value p = tar.getProperty(t);
                if (p != null) {
                    if (p instanceof VNum)
                        n.put("value", ((VNum)p).numval);
                    else if (p instanceof VInt)
                        n.put("value", ((VInt)p).intval);
                    else {
                        Logger.warn("Unknown tissue index property: "+p);
                        n.put("value", 0);
                    }
                }
                else {
                    n.put("value", 0);
                }
                axes.add(n);
            }

            ObjectNode node = mapper.createObjectNode();
            node.put("className", name);
            node.put("axes", axes);
            nodes.add(node);
        }
        
        return nodes;
    }
    
    public static Result targetTissue (final String name) {
        try {
            final String key = Util.sha1(name);
            return getOrElse (key, new Callable<Result> () {
                    public Result call () throws Exception {
                        return ok (_targetTissue (name));
                    }
                });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return _internalServerError (ex);
        }
    }

    public static Result structure (final String id,
                                    final String format, final int size,
                                    final String context) {
        //Logger.debug("Fetching structure");
        String atomMap = "";
        if (context != null) {
            int[] amap = (int[])IxCache.get("AtomMaps/"+context+"/"+id);
            //Logger.debug("AtomMaps/"+context+" => "+amap);
            if (amap != null && amap.length > 0) {
                StringBuilder sb = new StringBuilder ();
                sb.append(amap[0]);
                for (int i = 1; i < amap.length; ++i)
                    sb.append(","+amap[i]);
                atomMap = sb.toString();
            }
            else {
                atomMap = context;
            }
        }
        return App.structure(id, format, size, atomMap);        
    }

    public static List<GeneRIF> getGeneRIFs (Target target) {
        List<GeneRIF> generifs = new ArrayList<GeneRIF>();
        for (XRef ref : target.links) {
            if (Text.class.getName().equals(ref.kind)) {
                try {
                    Text text = (Text)ref.deRef();
                    if (IDG_GENERIF.equals(text.label)) {
                        for (Value val : ref.properties) {
                            if (PUBMED_ID.equals(val.label)) {
                                VInt pmid = (VInt)val;
                                generifs.add(new GeneRIF
                                             (pmid.intval, text.getValue()));
                            }
                        }
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.debug("Can't dereference "+ref.kind+":"+ref.refid);
                }
            }
        }
        
        Collections.sort(generifs);
        return generifs;
    }

    public static List<Long> getPMIDs (Target target) {
        List<Long> pmids = new ArrayList<Long>();
        for (Value val : target.properties) {
            if (PUBMED_ID.equals(val.label)) {
                pmids.add(((VInt)val).intval);
            }
        }
        return pmids;
    }

    public static JsonNode getPatents (Target target) {
        ObjectMapper mapper = new ObjectMapper ();
        ArrayNode nodes = mapper.createArrayNode();
        for (XRef ref : target.links) {
            if (Timeline.class.getName().equals(ref.kind)) {
                try {
                    Timeline tl = (Timeline)ref.deRef();
                    if ("Patent Count".equals(tl.name)) {
                        for (Event e : tl.events) {
                            ObjectNode n = mapper.createObjectNode();
                            n.put("year", e.start.toString());
                            n.put("count", e.end.toString());
                            nodes.add(n);
                        }
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.error("Can't dereference link "
                                 +ref.kind+":"+ref.refid);
                }
            }
        }
        return nodes;
    }

    public static String getPatentInfo(Target target) {
        ArrayList<Long> ent = new ArrayList<Long>();
        for (XRef ref : target.links) {
            if (Timeline.class.getName().equals(ref.kind)) {
                try {
                    Timeline tl = (Timeline)ref.deRef();
                    if ("Patent Count".equals(tl.name)) {
                        for (Event e : tl.events) {
                            ent.add(e.end);
                        }
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.error("Can't dereference link "
                            +ref.kind+":"+ref.refid);
                }
            }
        }

        //strip the brackets '[', ']' for sparkline
        String res = StringUtils.join(ent, ",");
        return res;
    }
}
