package ix.idg.controllers;

import com.avaje.ebean.Expr;
import com.jolbox.bonecp.BoneCPDataSource;
import ix.core.controllers.KeywordFactory;
import ix.core.controllers.PredicateFactory;
import ix.core.controllers.PublicationFactory;
import ix.core.models.*;
import ix.core.plugins.PersistenceQueue;
import ix.core.plugins.StructureProcessorPlugin;
import ix.core.plugins.StructureReceiver;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.TextIndexer;
import ix.idg.models.Disease;
import ix.idg.models.Ligand;
import ix.idg.models.TINX;
import ix.idg.models.Target;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.db.DB;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
    
public class TcrdRegistry extends Controller implements Commons {

    static final Model.Finder<Long, Target> targetDb = 
        new Model.Finder(Long.class, Target.class);
    static final Model.Finder<Long, Disease> diseaseDb = 
        new Model.Finder(Long.class, Disease.class);
    
    static final TextIndexer INDEXER = 
        Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    static final StructureProcessorPlugin PROCESSOR =
        Play.application().plugin(StructureProcessorPlugin.class);
    static final PersistenceQueue PQ =
        Play.application().plugin(PersistenceQueue.class);

    static final ConcurrentMap<String, Disease> DISEASES =
        new ConcurrentHashMap<String, Disease>();
    static final List<Target> TARGETS = new ArrayList<Target>();
    static final ConcurrentMap<Long, Ligand> LIGANDS =
        new ConcurrentHashMap<Long, Ligand>();

    static final DrugTargetOntology dto = new DrugTargetOntology();

    public static Namespace namespace;
    static public class LigandStructureReceiver implements StructureReceiver {
        final Ligand ligand;
        final Keyword source;
        
        public LigandStructureReceiver (Keyword source, Ligand ligand) {
            this.ligand = ligand;
            this.source = source;
        }

        public String getSource () { return source.term; }
        public void receive (Status status, String mesg, Structure struc) {
            //Logger.debug(status+": ligand "+ligand.getName()+" struc "+struc);
            if (status == Status.OK) {
                try {
                    if (struc != null) {
                        struc.properties.add(source);
                        //struc.save();
                        
                        XRef xref = new XRef (struc);
                        xref.properties.add(source);
                        xref.save();
                        ligand.links.add(xref);
                        ligand.update();
                        INDEXER.update(ligand);
                    }
                    Logger.debug
                        (status+": Ligand "+ligand.id+" "+ligand.getName());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else {
                Logger.error(status+": "+ligand.getName()+": "+mesg);
            }
        }
    }
    
    static class TcrdTarget implements Comparable<TcrdTarget> {
        String acc;
        String family;
        String tdl;
        Long id;
        Long protein;
        Double novelty;
        Keyword source;

        TcrdTarget () {}
        TcrdTarget (String acc, String family, String tdl,
                    Long id, Long protein, Double novelty,
                    Keyword source) {
            this.acc = acc;
            if ("nr".equalsIgnoreCase(family))
                this.family = "Nuclear Receptor";
            else if ("ic".equalsIgnoreCase(family))
                this.family = "Ion Channel";
            else 
                this.family = family;
            this.tdl = tdl;
            this.id = id;
            this.protein = protein;
            this.novelty = novelty;
            this.source = source;
        }

        public int hashCode () {
            return acc == null ? 1 : acc.hashCode();
        }
        public boolean equals (Object obj) {
            if (obj instanceof TcrdTarget) {
                return acc.equals(((TcrdTarget)obj).acc);
            }
            return false;
        }
        public int compareTo (TcrdTarget t) {
            return acc.compareTo(t.acc);
        }
    }


    static class PersistRegistration
        extends PersistenceQueue.AbstractPersistenceContext {
        final Connection con;
        final Http.Context ctx;
        final ChemblRegistry chembl;
        final Collection<TcrdTarget> targets;
        PreparedStatement pstm, pstm2, pstm3, pstm4,
            pstm5, pstm6, pstm7, pstm8, pstm9, pstm10, pstm11;
        Map<String, Keyword> phenotypeSource = new HashMap<String, Keyword>();
        
        PersistRegistration (Connection con, Http.Context ctx,
                             Collection<TcrdTarget> targets,
                             ChemblRegistry chembl)
            throws SQLException {
            this.con = con;
            this.ctx = ctx;
            this.targets = targets;
            pstm = con.prepareStatement
                ("select a.*,c.score,b.protein_id "
                 +"from target2disease a, t2tc b, tinx_importance c, "
                 +"tinx_disease d "
                 +"where a.target_id = ? "
                 +"and a.target_id = b.target_id "
                 +"and b.protein_id = c.protein_id "
                 +"and d.id = c.disease_id "
                 +"and a.doid = d.doid");
            pstm2 = con.prepareStatement
                ("select * from chembl_activity where target_id = ?");
            pstm3 = con.prepareStatement
                ("select * from drugdb_activity where target_id = ?");
            pstm4 = con.prepareStatement
                ("select * from generif where protein_id = ?");
            pstm5 = con.prepareStatement
                ("select * from dto_classification "
                 +"where protein_id = ? order by id");
            pstm6 = con.prepareStatement
                ("select * from tdl_info where protein_id = ?");
            pstm7 = con.prepareStatement
                ("select * from phenotype where protein_id = ?");
            pstm8 = con.prepareStatement
                ("select * from expression where protein_id = ? "
                 +"and evidence = 'CURATED'");
            pstm9 = con.prepareStatement
                ("select * from goa where protein_id = ?");
            pstm10 = con.prepareStatement
                ("select * from panther_class a, p2pc b "
                 +"where a.id = b.panther_class_id and b.protein_id = ?");
            pstm11 = con.prepareStatement
                ("select * from target2pathway where target_id = ?");
            this.chembl = chembl;
        }

        public void persists () throws Exception {
            for (TcrdTarget t : targets)
                persists (t);
            
            for (Target t : TARGETS) {
                t.update();
                INDEXER.update(t);
            }
            
            // the ligands are handled by the
            //  ChemblRegistry.LigandStructureReceiver callback
        }

        public void shutdown () throws SQLException {
            pstm.close();
            pstm2.close();
            pstm3.close();
            pstm4.close();
            pstm5.close();
            pstm6.close();
            pstm7.close();
            pstm8.close();
            pstm9.close();
            pstm10.close();
            pstm11.close();
            chembl.shutdown();
        }

        void addPathway (Target target, long tid) throws Exception {
            pstm11.setLong(1, tid);
            ResultSet rset = pstm11.executeQuery();
            while (rset.next()) {
                String source = rset.getString("source");
                String name = rset.getString("name");
                Keyword term = KeywordFactory.registerIfAbsent
                    (source+" Pathway", name, null);
                if (!target.properties.contains(term)) {
                    target.properties.add(term);
                    Logger.debug("Target "+target.id+" pathway: "+term);
                }
            }
            rset.close();
        }
        
        void addGO (Target target, long protein) throws Exception {
            pstm9.setLong(1, protein);
            ResultSet rset = pstm9.executeQuery();
            while (rset.next()) {
                String term = rset.getString("go_term");
                String id = rset.getString("go_id");
                
                Keyword go = null;
                char kind = term.charAt(0);
                term = term.substring(term.indexOf(':')+1);
                String href = "http://amigo.geneontology.org/amigo/term/"+id;
                
                switch (kind) {
                case 'C': // component
                    go = KeywordFactory.registerIfAbsent
                        (GO_COMPONENT, term, href);
                    break;
                    
                case 'F': // function
                    go = KeywordFactory.registerIfAbsent
                        (GO_FUNCTION, term, href);
                    break;
                    
                case 'P': // process
                    go = KeywordFactory.registerIfAbsent
                        (GO_PROCESS, term, href);
                    break;
                    
                default:
                    Logger.warn("Unknown GO term \""+term+"\"!");
                }

                if (go != null && !target.properties.contains(go)) {
                    target.properties.add(go);
                    Logger.debug("Target "+target.id+" GO: "+term);
                }
            }
            rset.close();
        }
        
        void addTissue (Target target, long protein) throws Exception {
            pstm8.setLong(1, protein);
            ResultSet rset = pstm8.executeQuery();
            while (rset.next()) {
                String tissue = rset.getString("tissue");
                Keyword t = KeywordFactory.registerIfAbsent
                    (IDG_TISSUE, tissue, null);
                if (!target.properties.contains(t)) {
                    target.properties.add(t);
                    Logger.debug("Target "+target.id+" tissue: "+tissue);
                }
            }
            rset.close();
        }
        
        void addPhenotype (Target target, long protein) throws Exception {
            pstm7.setLong(1, protein);
            ResultSet rset = pstm7.executeQuery();
            Set<String> terms = new TreeSet<String>();
            Map<String, Keyword> sources = new TreeMap<String, Keyword>();
            while (rset.next()) {
                String type = rset.getString("ptype");
                if ("impc".equalsIgnoreCase(type)) {
                    Keyword source = phenotypeSource.get(type);
                    if (source == null) {
                        source = KeywordFactory.registerIfAbsent
                            (SOURCE, type,
                             "http://www.mousephenotype.org/data/secondaryproject/idg");
                        phenotypeSource.put(type, source);
                    }
                    sources.put(type, source);
                    String term = rset.getString("term_name");
                    if (term != null) {
                        for (String t : term.split(",")) {
                            terms.add(t);
                        }
                    }
                }
                else if ("gwas catalog".equalsIgnoreCase(type)) {
                    Keyword source = phenotypeSource.get(type);
                    if (source == null) {
                        source = KeywordFactory.registerIfAbsent
                            (SOURCE, type,
                             "https://www.genome.gov/26525384");
                        phenotypeSource.put(type, source);
                    }
                    sources.put(type, source);
                    String trait = rset.getString("trait");
                    if (trait != null) {
                        long pmid = rset.getLong("pmid");
                        if (!rset.wasNull()) {
                            Publication pub =
                                PublicationFactory.registerIfAbsent(pmid);
                            if (pub != null) {
                                XRef ref = target.getLink(pub);
                                Keyword t = KeywordFactory.registerIfAbsent
                                    (GWAS_TRAIT, trait, null);
                                if (ref == null) {
                                    ref = new XRef (pub);
                                    ref.properties.add(source);
                                    ref.properties.add(t);
                                    ref.save();
                                    target.links.add(ref);
                                }
                                else {
                                    boolean add = true;
                                    if (!ref.properties.contains(t)) {
                                        ref.properties.add(t);
                                        ref.update();
                                    }
                                }
                                target.addIfAbsent(pub);
                            }
                            else {
                                Logger.warn("Can't retrieve publication "+pmid+
                                            "for target "+target.id);
                            }
                        }
                    }
                }
                else {
                    Logger.warn("Unknown phenotype \""+type
                                +"\" for target "+target.id);
                }
            }
            rset.close();

            if (!terms.isEmpty()) {
                for (String term : terms) {
                    Keyword t = KeywordFactory.registerIfAbsent
                        (IMPC_TERM, term, null);
                    target.properties.add(t);
                }
                Logger.debug("Target "+target.id+" has "+terms.size()
                             +" phenotype(s)!");
            }
            
            for (Keyword source: sources.values()) {
                target.properties.add(source);
            }
        }
        
        void addTDL (Target target, long protein) throws Exception {
            pstm6.setLong(1, protein);
            ResultSet rset = pstm6.executeQuery();
            while (rset.next()) {
                String type = rset.getString("itype");
                Value val = null;
                for (String field : new String[]{"string_value",
                                                 "number_value",
                                                 "integer_value",
                                                 "date_value",
                                                 "boolean_value"}) {
                    if (field.equals("string_value")) {
                        String str = rset.getString(field);
                        if (str != null)
                            val = new Text (type, str);
                    }
                    else if (field.equals("number_value")) {
                        double num = rset.getDouble(field);
                        if (!rset.wasNull())
                            val = new VNum (type, num);
                    }
                    else {
                        long num = rset.getLong(field);
                        if (!rset.wasNull())
                            val = new VInt (type, num);
                    }
                }
                
                if (val != null) {
                    Logger.debug("Target "+target.id+": "+type);
                    target.properties.add(val);
                }
                else {
                    Logger.warn("TDL info \""+type+"\" for target "+target.id
                                +" is null!");
                }
            }
            rset.close();
        }

        void addPanther (Target target,long protein) throws Exception {
            pstm10.setLong(1, protein);
            Map<String, String> parents = new HashMap<String, String>();
            Map<String, String> panther = new HashMap<String, String>();
            ResultSet rset = pstm10.executeQuery();
            while (rset.next()) {
                String pcid = rset.getString("pcid");
                String name = rset.getString("name");
                String ancestor = rset.getString("parent_pcids");
                for (String p : ancestor.split("\\|")) 
                    if (!"PC00000".equals(p)) {
                        String old = parents.put(pcid, p);
                        if (old != null && !old.equals(p)) {
                            Logger.warn("Target "+target.id
                                        +" has two Panther parents: "
                                        +p+" and "+old+"; keeping "+p+"!");
                        }
                    }
                panther.put(pcid, name);
            }
            rset.close();

            Keyword[] path = new Keyword[panther.size()];
            for (Map.Entry<String, String> me : panther.entrySet()) {
                int d = 0;
                for (String p = parents.get(me.getKey()); p != null; ++d)
                    p = parents.get(p);
                //Logger.debug("PANTHER "+me.getKey()+" "+d);

                if (path[d] != null) {
                    // ignore this lineage!
                }
                else {
                    Keyword kw = KeywordFactory.registerIfAbsent
                        (PANTHER_PROTEIN_CLASS + " ("+d+")",
                         me.getValue(), null);
                    target.properties.add(kw);
                    path[d] = kw;
                }
            }
            
            for (int k = path.length; --k >= 0; ) {
                Keyword node = path[k];
                if (node != null) {
                    List<Predicate> predicates = PredicateFactory.finder.where
                        (Expr.and(Expr.eq("subject.refid", node.id),
                                  Expr.eq("predicate",
                                          PANTHER_PROTEIN_ANCESTRY)))
                        .findList();
                    if (predicates.isEmpty()) {
                        try {
                            Predicate pred = new Predicate
                                (PANTHER_PROTEIN_ANCESTRY);
                            pred.subject = new XRef (node);
                            for (int j = k; --j >= 0; ) {
                                pred.objects.add(new XRef (path[j]));
                            }
                            pred.save();
                        }
                        catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        }
        
        void addDTO (Target target, long protein) throws Exception {
            pstm5.setLong(1, protein);
            ResultSet rset = pstm5.executeQuery();
            List<Keyword> path = new ArrayList<Keyword>();
            Keyword kw = KeywordFactory.registerIfAbsent
                (DTO_PROTEIN_CLASS + " (0)", target.idgFamily, null);
            target.properties.add(kw);
            path.add(kw);
            Logger.debug("Target "+IDGApp.getId(target)+" "
                         +target.idgFamily+" DTO");
            while (rset.next()) {
                String label = rset.getString("name").trim();
                if (target.idgFamily.equals("GPCR")) {
                    if (label.equalsIgnoreCase("Ligand Type"))
                        break; // we're done
                }
                else if (target.idgFamily.equals("Ion Channel")) {
                    if (label.equalsIgnoreCase("Transporter Protein Type"))
                        break;
                }
                else if (target.idgFamily.equals("Kinase")) {
                    if (label.equalsIgnoreCase("Pseudokinase"))
                        break;
                }
                else if (target.idgFamily.equals("Nuclear Receptor")) {
                    // nothing to check
                }

                String value = rset.getString("value");
                if (value.equals(""))
                    break; // we're done
                value = value.replaceAll("/", "-");
                Logger.debug("  name=\""+label+"\" value="+value);
                
                kw = KeywordFactory.registerIfAbsent
                    (DTO_PROTEIN_CLASS+" ("+path.size()+")", value, null);
                target.properties.add(kw);
                path.add(kw);
            }
            rset.close();
            
            for (int k = path.size(); --k >= 0; ) {
                Keyword node = path.get(k);
                List<Predicate> predicates = PredicateFactory.finder.where
                    (Expr.and(Expr.eq("subject.refid", node.id),
                              Expr.eq("predicate",
                                      DTO_PROTEIN_ANCESTRY)))
                    .findList();
                if (predicates.isEmpty()) {
                    try {
                        Predicate pred = new Predicate
                            (DTO_PROTEIN_ANCESTRY);
                        pred.subject = new XRef (node);
                        for (int j = k; --j >= 0; ) {
                            pred.objects.add(new XRef (path.get(j)));
                        }
                        pred.save();
                    }
                    catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }

        void persists (TcrdTarget t) throws Exception {
            Http.Context.current.set(ctx);
            Logger.debug(t.family+" "+t.tdl+" "+t.acc+" "+t.id);
            
            final Target target = new Target ();
            target.idgFamily = t.family;
            for (Target.TDL tdl : EnumSet.allOf(Target.TDL.class)) {
                if (t.tdl.equals(tdl.name)) 
                    target.idgTDL = tdl;
            }
            assert target.idgTDL != null
                : "Unknown TDL "+t.tdl;
            
            target.synonyms.add(new Keyword (IDG_TARGET, "TCRD:"+t.id));
            target.properties.add(t.source);

            Logger.debug("...uniprot registration");
            UniprotRegistry uni = new UniprotRegistry ();
            uni.register(target, t.acc);
            
            addDTO (target, t.protein);
            addTDL (target, t.protein);
            addPhenotype (target, t.protein);
            addTissue (target, t.protein);
            addGO (target, t.protein);
            addPathway (target, t.id);
            addPanther (target, t.protein);
            
            TARGETS.add(target);
            
            if (t.novelty != null) {
                target.novelty = t.novelty;
            }

            Logger.debug("...disease linking");
            pstm.setLong(1, t.id);
            long start = System.currentTimeMillis();
            new RegisterDiseaseRefs (target, t, pstm).persists();
            long end = System.currentTimeMillis();
            Logger.debug("..."+(end-start)+"ms to resolve diseases");

            Logger.debug("...gene RIF linking");
            pstm4.setLong(1, t.protein);
            new RegisterGeneRIFs (target, pstm4).persists();

            Logger.debug("...ligand linking");
            pstm2.setLong(1, t.id);
            pstm3.setLong(1, t.id);
            RegisterLigands reglig = new RegisterLigands
                (chembl, target, pstm2, pstm3, t.source);
            reglig.persists();

            for (Ligand lig : reglig.getLigands())
                if (lig.id != null)
                    LIGANDS.put(lig.id, lig);
            try {
                target.update();
            }
            catch (Exception ex) {
                Logger.error("Can't update target "+target.id+" ("
                             +IDGApp.getId(target)+")", ex);
                ex.printStackTrace();
            }
        }
    }

    static class RegisterDiseaseRefs
        extends PersistenceQueue.AbstractPersistenceContext {
        final Target target;
        final TcrdTarget tcrdTarget;
        final Keyword source;
        final PreparedStatement pstm;

        RegisterDiseaseRefs (Target target, TcrdTarget tcrdTarget,
                             PreparedStatement pstm) {
            this.target = target;
            this.source = tcrdTarget.source;
            this.pstm = pstm;
            this.tcrdTarget = tcrdTarget;
        }

        public void persists () throws Exception {
            ResultSet rs = pstm.executeQuery();
            try {               
                Keyword family = KeywordFactory.registerIfAbsent
                    (IDG_FAMILY, target.idgFamily, null);
                Keyword clazz = KeywordFactory.registerIfAbsent
                    (IDG_DEVELOPMENT, target.idgTDL.name, null);
                Keyword name = KeywordFactory.registerIfAbsent
                    (UNIPROT_TARGET, target.name, target.getSelf());

                XRef self = new XRef (target);
                self.properties.add(family);
                self.properties.add(clazz);
                self.properties.add(name);
                self.save();

                int count = 0;
                Map<Long, Disease> neighbors = new HashMap<Long, Disease>();
                List<Disease> updates = new ArrayList<Disease>();
                while (rs.next()) {
                    String doid = rs.getString("doid");
                    Disease disease = DISEASES.get(doid);
                    long start = System.currentTimeMillis();
                    if (disease == null) {
                        List<Disease> dl = DiseaseFactory.finder
                            .where(Expr.and
                                   (Expr.eq("synonyms.label",
                                            DiseaseOntologyRegistry.DOID),
                                    Expr.eq("synonyms.term", doid)))
                            .findList();
                    
                        if (dl.isEmpty()) {
                            Logger.warn("Target "+target.id+" references "
                                        +"unknown disease "+doid);
                            continue;
                        }
                        else if (dl.size() > 1) {
                            Logger.warn("Disease "+doid+" maps to "+dl.size()
                                        +" entries!");
                            for (Disease d : dl)
                                Logger.warn("..."+d.id+" "+d.name);
                            
                        }
                        disease = dl.iterator().next();
                        DISEASES.putIfAbsent(doid, disease);
                    }

                    { Value val = null;
                        for (Value v : disease.properties) {
                            if (v == source) {
                                val = v;
                                break;
                            }
                            else if (v instanceof Keyword) {
                                Keyword kw = (Keyword)v;
                                if (kw.label.equals(source.label)
                                    && kw.term.equals(source.term)) {
                                    val = kw;
                                    break;
                                }
                            }
                        }
                        
                        if (val == null) {
                            disease.properties.add(source);
                            disease.update();
                        }
                    }
                    
                    double zscore = rs.getDouble("zscore");
                    double conf = rs.getDouble("conf");
                    double tinx = rs.getDouble("score");

                    /**
                     * TODO: tinx should reference disease and target 
                     * directly instead of just the uniprot and doid!
                     */
                    TINX tinxe = new TINX
                        (tcrdTarget.acc, doid, tcrdTarget.novelty, tinx);
                    tinxe.save();

                    XRef xref = null;
                    for (XRef ref : target.links) {
                        if (ref.referenceOf(disease)) {
                            xref = ref;
                            break;
                        }
                    }
                    
                    if (xref != null) {
                        Logger.warn("Disease "+disease.id+" ("
                                    +disease.name+") is "
                                    +"already linked with target "
                                    +target.id+" ("+target.name+")");
                    }
                    else {
                        xref = new XRef (disease);
                        Keyword kw = KeywordFactory.registerIfAbsent
                            (IDG_DISEASE, disease.name, xref.getHRef());
                        xref.properties.add(kw);
                        xref.properties.add(new VNum (IDG_ZSCORE, zscore));
                        xref.properties.add(new VNum (IDG_CONF, conf));
                        xref.properties.add(new VNum (TINX_IMPORTANCE, tinx));
                        xref.save();
                        target.links.add(xref);

                        // now add all the unique parents of this disease node
                        getNeighbors(neighbors, disease.links);
                    
                        // link the other way
                        try {
                            disease.links.add(self);
                            updates.add(disease);
                            ++count;
                        }
                        catch (Exception ex) {
                            Logger.warn("Disease "+disease.id+" ("
                                        +disease.name+")"
                                        +" is already link with target "
                                        +target.id+" ("+target.name+"): "
                                        +ex.getMessage());
                        }
                    }
                    long end = System.currentTimeMillis();
                    Logger.debug("......."+(end-start)+"ms linking target "
                                 +target.id+" and disease "+disease.id
                                 +" ("+doid+")");  
                }
                Logger.debug(".....updating "+updates.size()+" diseases");
                for (Disease d : updates) {
                    try {
                        d.update();
                        INDEXER.update(d);
                    }
                    catch (Exception ex) {
                        Logger.error("Can't update disease "
                                     +d.id+" "+d.name, ex);
                        ex.printStackTrace();
                    }
                }
                Logger.debug("....."+count+" disease xref(s) added!");
            }
            finally {
                rs.close();
            }
        }

        void getNeighbors (Map<Long, Disease> neighbors, List<XRef> links) {
            for (XRef xr : links) {
                if (Disease.class.getName().equals(xr.kind)) {
                    final XRef ref = xr;
                    try {
                        Disease neighbor =
                            Cache.getOrElse
                            (Disease.class.getName()+"."+xr.refid,
                             new Callable<Disease> () {
                                 public Disease call () {
                                     return (Disease)ref.deRef();
                                 }
                             }, Integer.MAX_VALUE);
                        neighbors.put(neighbor.id, neighbor);
                        // recurse
                        getNeighbors (neighbors, neighbor.links);
                    }
                    catch (Exception ex) {
                        Logger.error("Can't retrieve neighbor for XRef "
                                     +ref.kind+" "+ref.refid, ex);
                        ex.printStackTrace();
                    }
                }
            }
        }
    } // RegisterDiseaseRefs ()

    static class RegisterGeneRIFs 
        extends PersistenceQueue.AbstractPersistenceContext {
        final Target target;
        final PreparedStatement pstm;
        
        RegisterGeneRIFs (Target target, PreparedStatement pstm) {
            this.target = target;
            this.pstm = pstm;
        }

        public void persists () throws Exception {
            ResultSet rs = pstm.executeQuery();
            try {
                int count = 0;
                Map<Long, String> generifs = new HashMap<Long, String>();
                while (rs.next()) {
                    long pmid = rs.getLong("pubmed_ids");
                    String text = rs.getString("text");
                    int updates = 0;
                    for (XRef xref : target.links) {
                        if (Publication.class.getName().equals(xref.kind)) {
                            Publication pub = (Publication)xref.deRef();
                            if (pub == null) {
                                Logger.error("XRef "+xref.id+" reference a "
                                             +"bogus publication!");
                            }
                            else if (pmid == pub.pmid) {
                                xref.properties.add
                                    (new Text (IDG_GENERIF, text));
                                xref.update();
                                ++updates;
                            }
                        }
                    }
                    
                    if (updates > 0) {
                        ++count;
                    }
                    else {
                        generifs.put(pmid, text);
                    }
                }

                for (Map.Entry<Long, String> me : generifs.entrySet()) {
                    long pmid = me.getKey();
                    Publication pub = PublicationFactory.registerIfAbsent(pmid);
                    if (pub != null) {
                        try {
                            target.publications.add(pub);
                            XRef xref = new XRef (pub);
                            xref.properties.add
                                (new Text (IDG_GENERIF, me.getValue()));
                            xref.save();
                            target.links.add(xref);
                            
                            ++count;
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                            Logger.error("Can't save XRef for publication "
                                         +pub.id, ex);
                        }
                    }
                    else {
                        Logger.warn("Bogus PMID: "+pmid+"!");
                    }
                }
                
                if (count > 0) {
                    Logger.debug("Updated target "+target.id+": "+target.name
                                 +" with "+count+" GeneRIF references!");
                }
            }
            finally {
                rs.close();
            }
        }
    } // RegisterGeneRIFs

    static class RegisterLigands
        extends PersistenceQueue.AbstractPersistenceContext {
        final ChemblRegistry registry;
        final Target target;
        final PreparedStatement chembl;
        final PreparedStatement drug;
        final List<Ligand> allligands = new ArrayList<Ligand>();
        final Keyword source;

        RegisterLigands (ChemblRegistry registry,
                         Target target, PreparedStatement chembl,
                         PreparedStatement drug, Keyword source)
            throws SQLException {
            this.registry = registry;
            this.target = target;
            this.chembl = chembl;
            this.drug = drug;
            this.source = source;
        }

        List<Ligand> loadChembl () throws SQLException {
            final List<Ligand> ligands = new ArrayList<Ligand>();
            ResultSet rs = chembl.executeQuery();
            while (rs.next()) {
                String chemblId = rs.getString("cmpd_chemblid");
                List<Ligand> ligs = LigandFactory.finder
                    .where(Expr.and
                           (Expr.eq("synonyms.label", ChEMBL_ID),
                            Expr.eq("synonyms.term", chemblId)))
                    .findList();
                if (ligs.isEmpty()) {
                    Keyword kw = KeywordFactory.registerIfAbsent
                        (ChEMBL_ID, chemblId,
                         "https://www.ebi.ac.uk/chembl/compound/inspect/"
                         +chemblId);
                    Ligand ligand = new Ligand (chemblId);
                    ligand.synonyms.add(kw);
                    ligand.properties.add(source);
                    ligands.add(ligand);
                }
                else {
                    for (Ligand l : ligs) {
                        Ligand lig = LIGANDS.get(l.id);
                        if (lig == null) {
                            Logger.warn("Ligand "+l.id+" ("+l.getName()
                                        +") isn't cached!");
                        }
                        else {
                            ligands.add(lig);
                        }
                    }
                }
            }
            rs.close();
            return ligands;
        }

        List<Ligand> loadDrugs () throws SQLException {
            List<Ligand> ligands = new ArrayList<Ligand>();
            ResultSet rs = drug.executeQuery();
            while (rs.next()) {
                String drug = rs.getString("drug");
                String ref = rs.getString("reference");
                List<Ligand> ligs;
                if (ref != null && ref.indexOf("CHEMBL") > 0) {
                    String chemblId = ref.substring(ref.indexOf("CHEMBL"));
                    ligs = LigandFactory.finder
                        .where(Expr.and
                               (Expr.eq("synonyms.label", ChEMBL_ID),
                                Expr.eq("synonyms.term", chemblId)))
                        .findList();
                }
                else {
                    ligs = LigandFactory.finder
                        .where(Expr.and
                               (Expr.eq("synonyms.label", ChEMBL_SYNONYM),
                                Expr.eq("synonyms.term", drug)))
                        .findList();
                }
                if (ligs.isEmpty()) {
                    Keyword kw = KeywordFactory.registerIfAbsent
                        (IDG_DRUG, drug, ref != null
                         && ref.startsWith("http") ? ref : null);
                    Ligand ligand = new Ligand (drug);
                    String desc = rs.getString("nlm_drug_info");
                    if (desc != null) {
                        int pos = desc.lastIndexOf("Check for");
                        if (pos > 0)
                            desc = desc.substring(0, pos);
                    }
                    ligand.description = desc;
                    ligand.synonyms.add(kw);
                    String src = rs.getString("source");
                    if (!src.equalsIgnoreCase("chembl")) {
                        kw = KeywordFactory.registerIfAbsent
                            (SOURCE, src, ref);
                        ligand.properties.add(kw);
                    }
                    ligand.properties.add(source);
                    ligands.add(ligand);
                }
                else {
                    for (Ligand l : ligs) {
                        Ligand lig = LIGANDS.get(l.id);
                        if (lig == null) {
                            Logger.warn("Ligand "+l.id+" ("+l.getName()
                                        +") isn't cached!");
                        }
                        else {
                            ligands.add(lig);
                        }
                    }
                }
            }
            rs.close();
            return ligands;
        }
        
        /**
         * This is to register the ligands direct instead of going through
         * chembl. This is for those ligands/targets that can't be resolved
         * through chembl.
         */
        List<Ligand> registerDrugLigands () throws SQLException {
            List<Ligand> ligands = new ArrayList<Ligand>();
            ResultSet rs = drug.executeQuery();
            while (rs.next()) {
                String drug = rs.getString("drug");
                String ref = rs.getString("reference");
                String src = rs.getString("source");
                
                List<Ligand> ligs = LigandFactory.finder
                    .where(Expr.and
                           (Expr.eq("synonyms.label", ChEMBL_SYNONYM),
                            Expr.eq("synonyms.term", drug)))
                    .findList();
                
                if (ligs.isEmpty()) {
                    Keyword kw = KeywordFactory.registerIfAbsent
                        (IDG_DRUG, drug, ref != null
                         && ref.startsWith("http") ? ref : null);
                    Ligand ligand = new Ligand (drug);
                    String desc = rs.getString("nlm_drug_info");
                    if (desc != null) {
                        int pos = desc.lastIndexOf("Check for");
                        if (pos > 0)
                            desc = desc.substring(0, pos);
                    }
                    ligand.description = desc;
                    ligand.synonyms.add(kw);
                    
                    if (!src.equalsIgnoreCase("chembl")) {
                        kw = KeywordFactory.registerIfAbsent(SOURCE, src, ref);
                        ligand.properties.add(kw);
                    }
                    else if (src.equalsIgnoreCase("iuphar")) {
                        kw = KeywordFactory.registerIfAbsent
                            (SOURCE, src, "http://www.guidetopharmacology.org/");
                        ligand.properties.add(kw);
                    }
                    ligand.properties.add(source);

                    if (ref != null && ref.indexOf("CHEMBL") > 0) {
                        String chemblId = ref.substring(ref.indexOf("CHEMBL"));
                        kw = KeywordFactory.registerIfAbsent
                            (ChEMBL_ID, chemblId,
                             "https://www.ebi.ac.uk/chembl/compound/inspect/"
                             +chemblId);
                        ligand.synonyms.add(kw);
                    }
                    
                    if (!registry.instrument(ligand)) {
                        // if can't resolve ligand via chembl, then use
                        // whatever information available
                        String smiles = rs.getString("smiles");
                        if (smiles != null) {
                            ligand.properties.add
                                (new Text (IDG_SMILES, smiles));
                            Logger.debug("submitting "+drug
                                         +" for processing...");
                            StructureReceiver receiver =
                                new LigandStructureReceiver (source, ligand);
                            PROCESSOR.submit(smiles, receiver);
                        }
                        ligand.save();
                    }
                    
                    ligs.add(ligand);
                }
                else {
                    List<Ligand> temp = new ArrayList<Ligand>();
                    for (Ligand l : ligs) {
                        Ligand lig = LIGANDS.get(l.id);
                        if (lig == null) {
                            Logger.warn("Ligand "+l.id+" ("+l.getName()
                                        +") isn't cached!");
                        }
                        else {
                            //ligands.add(lig);
                            temp.add(lig);
                        }
                    }
                    ligs = temp;
                }
                
                if (ref != null && ref.indexOf("CHEMBL") > 0) {
                    Logger.warn("Skipping ChEMBL reference "
                                +ref+" for drug "+drug);
                }
                else {
                    String type = rs.getString("act_type");
                    Double value = rs.getDouble("act_value");
                    if (rs.wasNull())
                        value = null;
                    
                    VNum act = new VNum (type, value);
                    act.save();
                    for (Ligand l : ligs) {
                        XRef tref = new XRef (target);
                        tref.properties.add(source);
                        tref.properties.add
                            (KeywordFactory.registerIfAbsent
                             (Target.IDG_FAMILY, target.idgFamily, null));
                        tref.properties.add
                            (KeywordFactory.registerIfAbsent
                             (Target.IDG_DEVELOPMENT,
                              target.idgTDL.name, null));
                        tref.properties.add(act);
                        
                        XRef lref = new XRef (l);
                        lref.properties.add(source);
                        lref.properties.add
                            (KeywordFactory.registerIfAbsent
                             ("Ligand", l.getName(), null));
                        lref.properties.add(act);
                        
                        tref.save();
                        lref.save();
                        l.links.add(tref);
                        target.links.add(lref);
                    }
                }
                ligands.addAll(ligs);
            }
            rs.close();
            
            return ligands;
        }

        public void persists () throws Exception {
            Set<Long> tids = registry.instruments(target);
            if (tids == null || tids.isEmpty()) {
                // not in chembl, so we only have to look at drug
                List<Ligand> ligands = registerDrugLigands ();
                Logger.debug("Registering "+ligands.size()+" drug ligand(s) "
                             +"for target "+target.id+": "+target.name);
                allligands.addAll(ligands);
            }
            else {
                List<Ligand> ligands = loadChembl();
                Logger.debug("Registering "+ligands.size()+" Chembl ligand(s) "
                             +"for target "+target.id+": "+target.name);
                registry.instruments(tids, target, ligands);
                allligands.addAll(ligands);
                
                //ligands = loadDrugs ();
                ligands = registerDrugLigands ();
                Logger.debug("Registering "+ligands.size()+" drug ligand(s) "
                             +"for target "+target.id+": "+target.name);
                registry.instruments(tids, target, ligands);
                allligands.addAll(ligands);
            }
        }

        public List<Ligand> getLigands () { return allligands; }
    } // RegisterLigands

    static void loadChemblUniprotMapping
        (Map<String, Set<String>> uniprotMap, File file) {
        try {
            BufferedReader br = new BufferedReader (new FileReader (file));
            for (String line; (line = br.readLine()) != null; ) {
                if (line.charAt(0) == '#')
                    continue;
                String[] toks = line.split("[\\s\t]+");
                if (2 == toks.length) {
                    Set<String> set = uniprotMap.get(toks[0]);
                    if (set == null) {
                        uniprotMap.put
                            (toks[0], set = new TreeSet<String>());
                    }
                    set.add(toks[1]);
                }
            }
            br.close();
        }
        catch (IOException ex) {
            Logger.trace("Can't load uniprot mapping file: "+file, ex);
        }
    }
    
    public static Result load () {
        DynamicForm requestData = Form.form().bindFromRequest();
        if (Play.isProd()) { // production..
            String secret = requestData.get("secret-code");
            if (secret == null || secret.length() == 0
                || !secret.equals(Play.application()
                                  .configuration().getString("ix.secret"))) {
                return unauthorized
                    ("You do not have permission to access resource!");
            }
        }
        
        String jdbcUrl = requestData.get("jdbcUrl");
        String jdbcUsername = requestData.get("jdbc-username");
        String jdbcPassword = requestData.get("jdbc-password");
        Logger.debug("JDBC: "+jdbcUrl);

        DataSource ds = null;
        if (jdbcUrl == null || jdbcUrl.equals("")) {
            //return badRequest ("No JDBC URL specified!");
            ds = DB.getDataSource("tcrd");
        }
        else {
            BoneCPDataSource bone = new BoneCPDataSource ();
            bone.setJdbcUrl(jdbcUrl);
            bone.setUsername(jdbcUsername);
            bone.setPassword(jdbcPassword);
            ds = bone;
        }

        if (ds == null) {
            return badRequest ("Neither DataSource \"tcrd\" found "
                               +"nor jdbc url is specified!");
        }

        String maxRows = requestData.get("max-rows");
        Logger.debug("Max Rows: "+maxRows);

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("load-do-obo");
        if (part != null) {
            String name = part.getFilename();
            String content = part.getContentType();
            Logger.debug("file="+name+" content="+content);
            File file = part.getFile();
            DiseaseOntologyRegistry obo = new DiseaseOntologyRegistry ();
            try {
                DISEASES.putAll(obo.register(new FileInputStream (file)));
            }
            catch (IOException ex) {
                Logger.trace("Can't load obo file: "+file, ex);
            }
        }

        Map<String, Set<String>> uniprotMap =
            new HashMap<String, Set<String>>();
        part = body.getFile("uniprot-map");
        if (part != null) {
            String name = part.getFilename();
            String content = part.getContentType();
            File file = part.getFile();
            loadChemblUniprotMapping (uniprotMap, file);
            Logger.debug("uniprot-map: file="+name+" content="
                         +content+" count="+uniprotMap.size());
        }
        else {
            // check the config
            String file = Play.application()
                .configuration().getString("ix.pharos.chembl_uniprot_mapping");
            if (file != null) {
                loadChemblUniprotMapping (uniprotMap, new File (file));
            }
            else {
                Logger.warn("No Chembl to UniProt mapping file provided!");
            }
        }

        int count = 0;
        try {
            int rows = 0;
            if (maxRows != null && maxRows.length() > 0) {
                try {
                    rows = Integer.parseInt(maxRows);
                }
                catch (NumberFormatException ex) {
                    Logger.warn("Bogus maxRows \""+maxRows+"\"; default to 0!");
                }
            }
            count = load (ds, 1, rows, uniprotMap);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError (ex.getMessage());
        }

        return redirect (routes.IDGApp.index());
    }

    static int load (DataSource ds, int threads, int rows,
                     Map<String, Set<String>> uniprotMap) throws Exception {

        Set<TcrdTarget> targets = new HashSet<TcrdTarget>();    
        Keyword source = null;
        Connection con = ds.getConnection();
        Statement stm = con.createStatement();
        int count = 0;
        try {
            ResultSet rset = stm.executeQuery("select * from dbinfo");
            if (rset.next()) {
                source = KeywordFactory.registerIfAbsent
                    (SOURCE, "TCRDv"+rset.getString("data_ver"),
                     "http://habanero.health.unm.edu");
            }
            rset.close();
            
            rset = stm.executeQuery
                ("select *\n"
                 +"from t2tc a "
                 +"     join (target b, protein c)\n"
                 +"on (a.target_id = b.id and a.protein_id = c.id)\n"
                 +"left join tinx_novelty d\n"
                 +"    on d.protein_id = a.protein_id \n"
                 //+"where b.tdl = 'Tclin'\n"
                 +" where c.uniprot = 'P25089'\n"
                 //+"where c.uniprot in ('P42685')\n"
                 //+"where c.uniprot in ('Q6PIU1')\n"
                 //+"where c.uniprot in ('A5X5Y0')\n"
                 //+"where c.uniprot in ('Q7RTX7')\n"
                 //+"where c.uniprot in ('Q00537','Q8WXA8')\n"
                 //+"where c.uniprot in ('O94921','Q96Q40','Q00536','Q00537','Q00526','P50613','P49761','P20794')\n"
                 //+"where c.uniprot in ('Q8WXA8')\n"
                 //+"where c.uniprot in ('Q7RTX7','Q86YV6','P07333','P07949')\n"
                 +"order by d.score desc, c.id\n"
                 +(rows > 0 ? ("limit "+rows) : "")
                 );
                 
            while (rset.next()) {
                long protId = rset.getLong("protein_id");
                if (rset.wasNull()) {
                    Logger.warn("Not a protein target: "
                                +rset.getLong("target_id"));
                    continue;
                }
                
                long id = rset.getLong("target_id");
                String fam = rset.getString("idgfam");
                String tdl = rset.getString("tdl");
                String acc = rset.getString("uniprot");
                Double novelty = rset.getDouble("d.score");
                if (rset.wasNull())
                    novelty = null;
                List<Target> tlist = targetDb
                    .where().eq("synonyms.term", acc).findList();
                
                if (tlist.isEmpty()) {
                    //Logger.debug("Adding "+acc);
                    TcrdTarget t =
                        new TcrdTarget (acc, fam, tdl, id, protId,
                                        novelty, source);
                    targets.add(t);
                }
                else {
                    Logger.debug("Skipping "+acc);
                }
            }
            rset.close();
            stm.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            con.close();
        }

        Logger.debug("Preparing to process "+targets.size()+" targets...");
        ChemblRegistry chembl = new ChemblRegistry (uniprotMap);
        PersistRegistration regis = new PersistRegistration
            (ds.getConnection(), Http.Context.current(),
             targets, chembl);
        PQ.submit(regis);
        //regis.persists();
        
        return count;
    }
    
    public static Result index () {
        return ok (ix.idg.views.html.tcrd.render("IDG TCRD Loader"));
    }
}
