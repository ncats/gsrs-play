package ix.idg.controllers;

import akka.routing.Router;
import com.avaje.ebean.Expr;
import com.avaje.ebean.QueryIterator;
import ix.core.controllers.KeywordFactory;
import ix.core.controllers.PredicateFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.EntityModel;
import ix.core.models.Keyword;
import ix.core.models.Mesh;
import ix.core.models.Predicate;
import ix.core.models.Publication;
import ix.core.models.Structure;
import ix.core.models.Text;
import ix.core.models.VNum;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.core.search.TextIndexer;
import ix.core.search.SearchOptions;
import ix.core.plugins.IxCache;
import ix.idg.models.Disease;
import ix.idg.models.Ligand;
import ix.idg.models.Target;
import ix.ncats.controllers.App;
import ix.utils.Util;

import play.Play;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;
import play.cache.Cached;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import tripod.chem.indexer.StructureIndexer;

import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.URLEncoder;

import static ix.core.search.TextIndexer.Facet;

public class IDGApp extends App implements Commons {
    static final int MAX_SEARCH_RESULTS = 1000;

    static class IDGSearchResultProcessor extends SearchResultProcessor {
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
                    return lig;
                }
            }
            return null;
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
            double d = dr.zscore - zscore;
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
            List<T> e = IxCache.getOrElse
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
                    }, 0);
            double ellapsed = (System.currentTimeMillis()-start)*1e-3;
            Logger.debug("Ellapsed time "+String.format("%1$.3fs", ellapsed)
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
                    long dif = System.currentTimeMillis()-s;
                    complete.put(name, dif);
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
        UNIPROT_TISSUE,
        "Ligand"        
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
        UNIPROT_TARGET
    };

    public static final String[] ALL_FACETS = {
        IDG_DEVELOPMENT,
        IDG_FAMILY,
        IDG_DISEASE,
        UNIPROT_TARGET,
        "Ligand"
    };

    static FacetDecorator[] decorate (Facet... facets) {
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
        
        IDGFacetDecorator f = new IDGFacetDecorator
            (new TextIndexer.Facet(DiseaseOntologyRegistry.CLASS));
        f.hidden = true;
        decors.add(f);
        
        return decors.toArray(new FacetDecorator[0]);
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
                return String.format("%1$.3f", value);
            return String.format("%1$.1f", value);
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
        TextIndexer.SearchResult results = textIndexer.search(opts, null);
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
                results = textIndexer.search(opts, null);
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
        return IxCache.getOrElse
            (key, new Callable<List<DiseaseRelevance>> () {
                    public List<DiseaseRelevance> call () throws Exception {
                        return getDiseaseRelevances (t);
                    }
                }, 0);
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
            TargetCacheWarmer cache = IxCache.getOrElse
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
                    }, 0);
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
                        .routes.IDGApp.targets(null, 30, 1).url();
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

    static Result createTargetResult
            (TextIndexer.SearchResult result, int rows, int page) {
        TextIndexer.Facet[] facets = filter
                (result.getFacets(), TARGET_FACETS);

        List<Target> targets = new ArrayList<Target>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging (rows, page, result.count());
            for (int i = (page - 1) * rows, j = 0; j < rows
                    && i < result.size(); ++j, ++i) {
                targets.add((Target) result.get(i));
            }
        }

        return ok(ix.idg.views.html.targets.render
                  (page, rows, result.count(),
                   pages, decorate (facets), targets));

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
            for (int i = (page - 1) * rows, j = 0; j < rows
                    && i < result.size(); ++j, ++i) {
                ligands.add((Ligand) result.get(i));
            }
        }

        return ok(ix.idg.views.html.ligandsmedia.render
                  (page, rows, result.count(),
                   pages, decorate (facets), ligands));
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
            for (int i = (page - 1) * rows, j = 0; j < rows
                    && i < result.size(); ++j, ++i) {
                diseases.add((Disease) result.get(i));
            }
        }

        return ok(ix.idg.views.html.diseases.render
                  (page, rows, result.count(),
                   pages, decorate (facets), diseases));
    }

    public static Result targets (final String q,
                                  final int rows, final int page) {
        try {
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

        StringBuilder sb = new StringBuilder();
        sb.append(routes.IDGApp.target(getId(t))).append(",").
                append(getId(t)).append(",").
                append(t.getName()).append(",").
                append(csvQuote(t.getDescription())).append(",").
                append(t.idgTDL.toString()).append(",").
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

    static Map<String, String[]> getRequestQuery () {
        Map<String, String[]> query = new HashMap<String, String[]>();
        query.putAll(request().queryString());
        // force to fetch everything at once
        //query.put("fetch", new String[]{"0"});
        return query;
    }

    static Result _targets (final String q, final int rows, final int page)
        throws Exception {
        final String key = "targets/"+Util.sha1(request ());
        Logger.debug("Targets: q="+q+" rows="+rows+" page="+page+" key="+key);
        
        final int total = TargetFactory.finder.findRowCount();
        if (request().queryString().containsKey("facet") || q != null) {
            Map<String, String[]> query = getRequestQuery ();
            if (!query.containsKey("order")) {
                query.put("order", new String[]{"$novelty"});
            }
            
            final TextIndexer.SearchResult result =
                getSearchResult (Target.class, q, total, query);
            
            String action = request().getQueryString("action");
            if (action == null) action = "";

            if (action.toLowerCase().equals("download")) {
                StringBuilder sb = new StringBuilder();
                sb.append("URL,Uniprot ID,Name,Description,Development Level,Novelty,Target Family,Function,PMIDs\n");
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
            return getOrElse (key, new Callable<Result> () {
                    public Result call () throws Exception {
                        TextIndexer.Facet[] facets = filter
                            (getFacets(Target.class, FACET_DIM),
                             TARGET_FACETS);
                        int _rows = Math.min(total, Math.max(1, rows));
                        int[] pages = paging (_rows, page, total);
                        
                        List<Target> targets = TargetFactory.getTargets
                            (_rows, (page-1)*_rows, null);
                        
                        return ok (ix.idg.views.html.targets.render
                                   (page, _rows, total, pages,
                                    decorate (facets), targets));
                    }
                });
        }
    }

    public static Keyword[] getAncestry (final String facet,
                                         final String predicate) {
        try {
            final String key = predicate+"/"+facet;
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
                    return redirect (routes.IDGApp.targets(q, 30, 1));
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
            return getOrElse(key, new Callable<Result> () {
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
                            (textIndexer, null, null, MAX_SEARCH_RESULTS,
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
                            (textIndexer, null, query, MAX_SEARCH_RESULTS, 0,
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
        int total = 0, totalTargets = 0, totalDiseases = 0, totalLigands = 0;
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
                }
            }
        }
        
        List<Target> targets =
            filter (Target.class, result.getMatches(), max);
        List<Disease> diseases =
            filter (Disease.class, result.getMatches(), max);
        List<Ligand> ligands = filter (Ligand.class, result.getMatches(), max);
        
        return ok (ix.idg.views.html.search.render
                   (query, total, decorate (facets),
                    targets, totalTargets,
                    ligands, totalLigands,
                    diseases, totalDiseases));
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
    
    public static Result ligands (final String q,
                                  final int rows, final int page) {
        String type = request().getQueryString("type"); 
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
                                    decorate (facets), ligands));
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
            TextIndexer indexer = textIndexer.createEmptyInstance();
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

    public static Result structureResult
        (final SearchResultContext context, int rows, int page)
        throws Exception {
        return structureResult
            (context, rows, page, new DefaultResultRenderer<Ligand> () {
                    public Result render (int page, int rows,
                                          int total, int[] pages,
                                          List<TextIndexer.Facet> facets,
                                          List<Ligand> ligands) {
                        return ok (ix.idg.views.html.ligandsmedia.render
                                   (page, rows, total,
                                    pages, decorate (filter
                                                     (facets, LIGAND_FACETS)),
                                    ligands));
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
                return structureResult (context, rows, page);
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
                return structureResult (context, rows, page);
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
    
    public static Result diseases (final String q,
                                   final int rows, final int page) {
        try {
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
}
