package ix.idg.controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jolbox.bonecp.BoneCPDataSource;
import ix.core.controllers.KeywordFactory;
import ix.core.controllers.NamespaceFactory;
import ix.core.controllers.PredicateFactory;
import ix.core.models.Keyword;
import ix.core.models.Namespace;
import ix.core.models.Predicate;
import ix.core.models.Publication;
import ix.core.models.Text;
import ix.core.models.VNum;
import ix.core.models.XRef;
import ix.core.models.Structure;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.StructureReceiver;
import ix.core.plugins.StructureProcessorPlugin;
import ix.core.search.TextIndexer;
import ix.idg.models.Disease;
import ix.idg.models.Target;
import ix.idg.models.Ligand;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.db.DB;
import play.db.ebean.Model;
import play.db.ebean.Transactional;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
    
public class TcrdRegistry extends Controller {
    public static final String DISEASE = "IDG Disease";
    public static final String DEVELOPMENT = Target.IDG_DEVELOPMENT;
    public static final String FAMILY = Target.IDG_FAMILY;
    public static final String DRUG = "IDG Drug";
    public static final String ZSCORE = "IDG Z-score";
    public static final String CONF = "IDG Confidence";
    public static final String GENERIF = "IDG GeneRIF";
    public static final String TARGET = "IDG Target";
    public static final String ChEMBL = "ChEMBL";
    public static final String ChEMBL_SYNONYM = "ChEMBL Synonym";
    public static final String ChEMBL_MOLFILE = "ChEMBL Molfile";
    public static final String ChEMBL_INCHI = "ChEMBL InChI";
    public static final String ChEMBL_INCHI_KEY = "ChEMBL InChI Key";
    public static final String ChEMBL_SMILES = "ChEMBL Canonical SMILES";
    public static final String ChEMBL_PROTEIN_CLASS = "ChEMBL Protein Class";
    public static final String ChEMBL_PROTEIN_ANCESTRY =
        "ChEMBL Protein Ancestry";

    static final Model.Finder<Long, Target> targetDb = 
        new Model.Finder(Long.class, Target.class);
    static final Model.Finder<Long, Disease> diseaseDb = 
        new Model.Finder(Long.class, Disease.class);
    
    static final TextIndexer indexer = 
        Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    static final StructureProcessorPlugin processor =
        Play.application().plugin(StructureProcessorPlugin.class);

    static final ConcurrentMap<String, Disease> diseases =
        new ConcurrentHashMap<String, Disease>();

    static final DrugTargetOntology dto = new DrugTargetOntology();

    public static final Namespace namespace = NamespaceFactory.registerIfAbsent
        ("TCRDv096", "https://pharos.nih.gov");

    static class TcrdTarget implements Comparable<TcrdTarget> {
        String acc;
        String family;
        String tdl;
        Long id;
        Long protein;

        TcrdTarget () {}
        TcrdTarget (String acc, String family, String tdl,
                    Long id, Long protein) {
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

    static class ChemblStructureReceiver implements StructureReceiver {
        final Ligand ligand;
        final Target target;
        final Namespace namespace;
        
        ChemblStructureReceiver
            (Namespace namespace, Target target, Ligand ligand) {
            this.ligand = ligand;
            this.target = target;
            this.namespace = namespace;
        }

        public String getSource () { return namespace.name; }
        public void receive (Status status, String mesg, Structure struc) {
            Logger.debug(status+": ligand "+ligand.getName());
            if (status == Status.OK) {
                try {
                    if (struc != null) {
                        XRef xref = new XRef (struc);
                        xref.namespace = namespace;
                        xref.save();
                        ligand.links.add(xref);
                    }
                    XRef xref = new XRef (target);
                    xref.namespace = namespace;
                    xref.save();
                    ligand.links.add(xref);
                    ligand.save();

                    xref = new XRef (ligand);
                    xref.namespace = namespace;
                    xref.save();
                    target.links.add(xref);
                    target.update();
                    
                    indexer.update(target);
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

    static class ChemblResolver {
        DataSource chembl;
        Connection con;
        PreparedStatement pstm, pstm2, pstm3, pstm4, pstm5, pstm6, pstm7;
        Map<String, String> uniprotMap;
        String version;
        Namespace namespace;
        
        ChemblResolver (Map<String, String> uniprotMap)
            throws SQLException {
            chembl = DB.getDataSource("chembl");
            if (chembl == null) {
                throw new IllegalStateException
                    ("No \"chembl\" datasource found!");
            }
            con = chembl.getConnection();

            try {
                Statement stm = con.createStatement();
                ResultSet rset = stm.executeQuery("select * from version");
                if (rset.next()) {
                    version = rset.getString("name");
                    namespace = NamespaceFactory.registerIfAbsent
                        (version, "https://www.ebi.ac.uk/chembl");
                }
                rset.close();
                Logger.debug("ChEBML version: "+version);
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            if (uniprotMap == null || uniprotMap.isEmpty()) {
                con.prepareStatement
                    ("select distinct e.* "+
                     "from target_components a, "+
                     "component_synonyms b, "+
                     "component_class c, "+
                     "protein_classification d, "+
                     "protein_family_classification e "+
                     "where component_synonym = ? "+
                     "and syn_type = 'GENE_SYMBOL' "+
                     "and a.component_id = b.component_id "+
                     "and a.component_id = c.component_id "+
                     "and c.protein_class_id = d.protein_class_id "+
                     "and c.protein_class_id = e.protein_class_id "+
                     "order by class_level desc, e.protein_class_id");
                Logger.warn("No uniprot <-> chembl mapping provided!");
            }
            else {
                pstm = con.prepareStatement
                    ("select distinct e.* "+
                     "from target_components a, "+
                     "component_synonyms b, "+
                     "component_class c, "+
                     "protein_classification d, "+
                     "protein_family_classification e , "+
                     "chembl_id_lookup f "+
                     "where f.entity_type = 'TARGET' "+
                     "and f.chembl_id = ? "+
                     "and f.entity_id = a.tid "+
                     "and a.component_id = b.component_id "+
                     "and a.component_id = c.component_id "+
                     "and c.protein_class_id = d.protein_class_id "+
                     "and c.protein_class_id = e.protein_class_id "+
                     "order by class_level desc, e.protein_class_id"
                     );
            }
            pstm2 = con.prepareStatement
                ("select * from chembl_id_lookup a right join "
                 +"compound_structures b on b.molregno = a.entity_id "
                 +"left join molecule_synonyms c on c.molregno = a.entity_id "
                 +"where a.entity_type = 'COMPOUND' "
                 +"and a.chembl_id = ? "
                 );
            pstm3 = con.prepareStatement
                ("select * from molecule_synonyms where molregno = ?");
            pstm4 = con.prepareStatement
                ("select * from drug_mechanism where molregno = ?");
            pstm5 = con.prepareStatement
                ("select * from molecule_atc_classification "
                 +"where molregno = ?");
            pstm6 = con.prepareStatement
                ("select * from activities a, "
                 +"assays b, target_dictionary c, docs d "
                 +"where a.molregno = ? "
                 +"and a.assay_id = b.assay_id "
                 +"and b.tid = c.tid "
                 +"and a.doc_id = d.doc_id");
            pstm7 = con.prepareStatement
                ("select * from chembl_id_lookup a right join "
                 +"compound_structures b on b.molregno = a.entity_id "
                 +"left join molecule_synonyms c on c.molregno = a.entity_id "
                 +"where a.entity_type = 'COMPOUND' "
                 +"and c.synonyms = ? "
                 );
            this.uniprotMap = uniprotMap;           
        }

        void shutdown () throws SQLException {
            con.close();
        }

        public String version () { return version; }
        void instruments (Target target, List<Ligand> ligands)
            throws SQLException {
            target (target);
            for (Ligand ligand : ligands) 
                ligand (target, ligand);
        }

        void ligand (Target target, Ligand ligand) throws SQLException {
            // see if this ligand has a chembl_id..
            String chembl = null, drug = null;
            for (Keyword kw : ligand.synonyms) {
                if (ChEMBL_SYNONYM.equals(kw.label))
                    chembl = kw.term;
                else if (DRUG.equals(kw.label))
                    drug = kw.term;
            }

            Logger.debug("Ligand: "+ligand.getName()+"; chembl="
                         +chembl+" drug="+drug);
            if (chembl != null) {
                List<Ligand> ligands = LigandFactory.finder
                    .where(Expr.and(Expr.eq("synonyms.label", ChEMBL_SYNONYM),
                                    Expr.eq("synonyms.term", chembl)))
                    .findList();
                if (ligands.isEmpty()) {
                    pstm2.setString(1, chembl);
                    ResultSet rset = pstm2.executeQuery();
                    String molfile = null, inchi = null,
                        inchiKey = null, smiles = null;
                    Map<String, String> syns = new HashMap<String, String>();
                    while (rset.next()) {
                        String syn = rset.getString("synonyms");
                        if (syn != null) {
                            Keyword kw = KeywordFactory.registerIfAbsent
                                (ChEMBL_SYNONYM, syn, null);
                            ligand.synonyms.add(kw);
                            String type = rset.getString("syn_type");
                            syns.put(type, syn);
                        }
                        
                        if (molfile == null) {
                            molfile = rset.getString("molfile");
                            inchi = rset.getString("standard_inchi");
                            inchiKey = rset.getString("standard_inchi_key");
                            smiles = rset.getString("canonical_smiles");
                        }
                    }
                    rset.close();

                    if (syns.isEmpty()) {
                        // no synonym, so use the chembl_id as the name
                        ligand.name = chembl;
                    }
                    else {
                        for (String type : new String[]{
                                "INN","USAN","FDA","BAN","USP",
                                "TRADE_NAME","MERCK_INDEX",
                                "JAN","DCF","ATC",
                                "RESEARCH_CODE","SYSTEMATIC"
                            }) {
                            String s = syns.get(type);
                            if (s != null) {
                                ligand.name = s;
                                break;
                            }
                        }
                    }

                    if (molfile != null) {
                        ligand.properties.add
                            (new Text (ChEMBL_MOLFILE, molfile));
                        ligand.properties.add(new Text (ChEMBL_INCHI, inchi));
                        ligand.properties.add
                            (new Text (ChEMBL_INCHI_KEY, inchiKey));
                        ligand.properties.add(new Text (ChEMBL_SMILES, smiles));

                        // now standardize and index
                        Logger.debug("submitting "+chembl+" for processing...");
                        StructureReceiver receiver = new ChemblStructureReceiver
                            (namespace, target, ligand);
                        processor.submit(molfile, receiver);
                    }
                    else {
                        Logger.warn("Ligand "+ligand.getName()+" ("+chembl
                                    +") has empty molfile!");
                        //ligand.save();
                    }
                }
                else {
                    for (Ligand lig : ligands) {
                        StructureReceiver receiver =
                            new ChemblStructureReceiver (namespace, target, lig);
                        processor.submit(receiver);
                    }
                }
            }
            else if (drug != null) {
                pstm7.setString(1, drug);
            }
        }

        void target (Target target) throws SQLException {
            if (uniprotMap == null || uniprotMap.isEmpty()) {
                String gene = null;
                for (Keyword kw : target.synonyms) {
                    if (UniprotRegistry.GENE.equals(kw.label)) {
                        gene = kw.term;
                        break;
                    }
                }
                
                if (gene == null)
                    throw new IllegalArgumentException
                        ("Target "+target.id+" ("+target.name
                         +") has no gene synonym!");
                pstm.setString(1, gene);
            }
            else {
                String acc = null, chemblId = null;
                for (Keyword kw : target.synonyms) {
                    if (UniprotRegistry.ACCESSION.equals(kw.label)) {
                        acc = kw.term;
                        chemblId = uniprotMap.get(acc);
                        if (chemblId != null) {
                            break;
                        }
                    }
                }
                
                if (acc == null)
                    throw new IllegalArgumentException
                        ("Target "+target.id+" ("+target.name
                         +") has no "+UniprotRegistry.ACCESSION+" synonym!");
                else if (chemblId == null)
                    throw new IllegalArgumentException
                        ("Target "+target.id+" ("+target.name
                         +") accession "+acc+" has not chembl_id mapping!");
                
                Logger.debug(acc +" => "+chemblId);
                pstm.setString(1, chemblId);
            }
            
            ResultSet rset = pstm.executeQuery();
            if (rset.next()) {
                int i = 1;
                List<Keyword> path = new ArrayList<Keyword>();
                for (; i <= 8; ++i) {
                    String l = rset.getString("l"+i);
                    if (l != null) {
                        Keyword kw = KeywordFactory.registerIfAbsent
                            (ChEMBL_PROTEIN_CLASS+" ("+i+")", l, null);
                        path.add(kw);
                        target.properties.add(kw);
                    }
                    else {
                        break;
                    }
                }
                for (int k = path.size(); --k >= 0; ) {
                    Keyword node = path.get(k);
                    List<Predicate> predicates = PredicateFactory.finder.where
                        (Expr.and(Expr.eq("subject.refid", node.id),
                                  Expr.eq("predicate",
                                          ChEMBL_PROTEIN_ANCESTRY)))
                        .findList();
                    if (predicates.isEmpty()) {
                        Transaction tx = Ebean.beginTransaction();
                        try {
                            Predicate pred = new Predicate
                                (ChEMBL_PROTEIN_ANCESTRY);
                            pred.subject = new XRef (node);
                            pred.subject.save();
                            for (int j = k; --j >= 0; ) {
                                pred.objects.add(new XRef (path.get(j)));
                            }
                            pred.save();
                            tx.commit();
                        }
                        catch (Throwable t) {
                            t.printStackTrace();
                        }
                        finally {
                            Ebean.endTransaction();
                        }
                    }
                }
                Logger.debug("Protein "+target.id+" ("+target.name+") has "
                             +i+" family level(s)!");
                if (rset.next()) {
                    Logger.warn("Protein "+target.id+" ("+target.name
                                +") has multiple family classes!");
                }
            }
            else {
                Logger.warn("Target "+target.id+" ("+target.name+") has "
                            +"no protein family classification!");
            }
            rset.close();
        }
    }

    static TcrdTarget EMPTY = new TcrdTarget ();

    static class RegistrationWorker implements Callable<Integer> {
        BlockingQueue<TcrdTarget> queue;
        Connection con;
        PreparedStatement pstm, pstm2, pstm3, pstm4, pstm5, pstm6;
        Http.Context ctx;
        ChemblResolver chembl;
        
        RegistrationWorker (Connection con, Http.Context ctx,
                            BlockingQueue<TcrdTarget> queue,
                            Map<String, String> uniprotMap)
            throws SQLException {
            this.con = con;
            this.queue = queue;
            this.ctx = ctx;
            pstm = con.prepareStatement
                ("select * from target2disease where target_id = ?");
            pstm2 = con.prepareStatement
                ("select * from chembl_activity where target_id = ?");
            pstm3 = con.prepareStatement
                ("select * from drugdb_activity where target_id = ?");
            pstm4 = con.prepareStatement
                ("select * from generif where protein_id = ?");
            chembl = new ChemblResolver (uniprotMap);
        }

        public Integer call () throws Exception {
            Http.Context.current.set(ctx);
            Logger.debug("Thread "+Thread.currentThread()+" initialized!");
            int count = 0;
            for (TcrdTarget t; (t = queue.take()) != EMPTY; ) {
                try {
                    register (t);
                    ++count;
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.trace("Can't register target "+t.acc, ex);
                }
            }
            Logger.debug(Thread.currentThread().getName()+": finished..."
                         +count+" target(s) processed!");

            try {
                pstm.close();
                pstm2.close();
                pstm3.close();
                pstm4.close();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            con.close();
            
            return count;
        }

        void register (TcrdTarget t) throws Exception {
            Logger.debug(Thread.currentThread().getName()
                         +": "+t.family+" "+t.tdl+" "+t.acc+" "+t.id);
            Target target = new Target ();
            target.idgFamily = t.family;
            for (Target.TDL tdl : EnumSet.allOf(Target.TDL.class)) {
                if (t.tdl.equals(tdl.name)) 
                    target.idgTDL = tdl;
            }
            assert target.idgTDL != null
                : "Unknown TDL "+t.tdl;
            
            target.synonyms.add
                (new Keyword (TARGET, String.valueOf(t.id)));
            
            Logger.debug("...uniprot registration");
            UniprotRegistry uni = new UniprotRegistry ();
            uni.register(target, t.acc);
            
            Logger.debug("...retrieving disease links");
            pstm.setLong(1, t.id);
            addDiseaseRefs (target, namespace, pstm);
            
            //Logger.debug("...retrieving ChEMBL links");
            pstm2.setLong(1, t.id);
            List<Ligand> ligands = addChemblRefs (target, pstm2);
            
            //Logger.debug("...retrieving Drug links");
            pstm3.setLong(1, t.id);
            ligands.addAll(addDrugDbRefs (target, pstm3));
            Logger.debug("..."+ligands.size()+" ligands");
            
            Logger.debug("...retrieving GeneRIF links");
            pstm4.setLong(1, t.protein);
            addGeneRIF (target, pstm4);

            Logger.debug("...retrieving DTO links");
            addDTO(target);

            chembl.instruments(target, ligands);

            /*
            Transaction tx = Ebean.beginTransaction();
            try {
                target.save();
                tx.commit();
            }
            catch (Exception ex) {
                Logger.trace("Can't update target "+target.id, ex);
            }
            finally {
                Ebean.endTransaction();
            }
            
            // reindex this entity; we have to do this since
            // the target.update doesn't trigger the postUpdate
            // event.. need to figure exactly why.
            indexer.update(target);
            */
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
                diseases.putAll(obo.register(new FileInputStream (file)));
            }
            catch (IOException ex) {
                Logger.trace("Can't load obo file: "+file, ex);
            }
        }

        part = body.getFile("load-dto");
        if (part != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                dto.setRoot(mapper.readTree(part.getFile()));
                Logger.debug("Loaded DTO from "+part.getFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, String> uniprotMap = new HashMap<String, String>();
        part = body.getFile("uniprot-map");
        if (part != null) {
            String name = part.getFilename();
            String content = part.getContentType();
            File file = part.getFile();
            try {
                BufferedReader br = new BufferedReader (new FileReader (file));
                for (String line; (line = br.readLine()) != null; ) {
                    if (line.charAt(0) == '#')
                        continue;
                    String[] toks = line.split("[\\s\t]+");
                    if (2 == toks.length) {
                        uniprotMap.put(toks[0], toks[1]);
                    }
                }
                br.close();
            }
            catch (IOException ex) {
                Logger.trace("Can't load uniprot mapping file: "+file, ex);
            }
            Logger.debug("uniprot-map: file="+name+" content="
                         +content+" count="+uniprotMap.size());
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
                     Map<String, String> uniprotMap) throws Exception {

        Set<TcrdTarget> targets = new HashSet<TcrdTarget>();    

        Connection con = ds.getConnection();
        Statement stm = con.createStatement();
        int count = 0;
        try {
            ResultSet rset = stm.executeQuery
                ("select * from t2tc a, target b, protein c\n"+
                 "where a.target_id = b.id\n"+
                 "and a.protein_id = c.id "
                 +" order by c.id, c.uniprot "           
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
                List<Target> tlist = targetDb
                    .where().eq("synonyms.term", acc).findList();
                
                if (tlist.isEmpty()) {
                    TcrdTarget t =
                        new TcrdTarget (acc, fam, tdl, id, protId);
                    targets.add(t);
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
        
        if (!targets.isEmpty()) {
            threads = Math.max(threads, 1);
            Logger.debug("Preparing to register "
                         +targets.size()+" targets over "
                         +threads+" thread(s)!");
            ExecutorService pool = Executors.newCachedThreadPool();
            
            ArrayBlockingQueue<TcrdTarget> queue =
                new ArrayBlockingQueue<TcrdTarget>(targets.size());
            for (TcrdTarget t : targets) {
                try {
                    queue.put(t); // shouldn't block
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            List<Future<Integer>> jobs = new ArrayList<Future<Integer>>();
            for (int i = 0; i < threads; ++i) {
                try {
                    jobs.add(pool.submit
                             (new RegistrationWorker
                              (ds.getConnection(), Http.Context.current(),
                               queue, uniprotMap)));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            for (int i = 0; i < threads; ++i) {
                try {
                    queue.put(EMPTY); // add as many as there are threads
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
            Logger.debug("## waiting for threads to finish...");
            for (Future<Integer> f : jobs) {
                try {
                    count += f.get();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            pool.shutdownNow();
        }
        return count;
    }

    static void addDiseaseRefs (Target target, Namespace namespace,
                                PreparedStatement pstm) throws Exception {
        ResultSet rs = pstm.executeQuery();
        try {
            XRef self = null;
            Keyword family = KeywordFactory.registerIfAbsent
                (Target.IDG_FAMILY, target.idgFamily, null);
            Keyword clazz = KeywordFactory.registerIfAbsent
                (Target.IDG_DEVELOPMENT, target.idgTDL.name, null);

            Keyword name = KeywordFactory.registerIfAbsent
                (UniprotRegistry.TARGET, target.name, target.getSelf());

            int count = 0;
            Map<Long, Disease> neighbors = new HashMap<Long, Disease>();
            while (rs.next()) {
                String doid = rs.getString("doid");
                Disease disease = diseases.get(doid);
                if (disease == null) {
                    List<Disease> dl = DiseaseFactory.finder
                        .where(Expr.and(Expr.eq("synonyms.label",
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
                    diseases.putIfAbsent(doid, disease);
                }

                double zscore = rs.getDouble("zscore");
                double conf = rs.getDouble("conf");
                
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
                    xref.namespace = namespace;
                    Keyword kw = KeywordFactory.registerIfAbsent
                        (DISEASE, disease.name, xref.getHRef());
                    xref.properties.add(kw);
                    xref.properties.add(new VNum (ZSCORE, zscore));
                    xref.properties.add(new VNum (CONF, conf));
                    target.links.add(xref);
                    
                    // now add all the unique parents of this disease node
                    getNeighbors (neighbors, disease.links);
                    
                    if (self == null) {
                        Transaction tx = Ebean.beginTransaction();
                        try {
                            self = new XRef (target);
                            self.namespace = namespace;
                            self.properties.add(family);
                            self.properties.add(clazz);
                            self.properties.add(name);
                            self.save();
                            tx.commit();
                        }
                        catch (Exception ex) {
                            Logger.trace("Can't persist XRef for target "
                                         +target.id, ex);
                        }
                        finally {
                            Ebean.endTransaction();
                        }
                    }
                    
                    // link the other way
                    try {
                        disease.links.add(self);
                        disease.update();
                        indexer.update(disease);
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
            }

            // TODO: fix this to properly add parent/neighbor relationships
            /*
            for (Disease neighbor : neighbors.values()) {
                neighbor.links.add(self);
                neighbor.update();
                indexer.update(neighbor);
                
                Keyword kw = KeywordFactory.registerIfAbsent
                    (DISEASE, neighbor.name, null);
                XRef xref = new XRef (neighbor);
                xref.namespace = namespace;
                xref.properties.add(kw);
                target.links.add(xref);
            }
            */
            Logger.debug("...."+count+" disease xref(s) added!");           
        }
        finally {
            rs.close();
        }
    }

    static void getNeighbors (Map<Long, Disease> neighbors, List<XRef> links) {
        for (XRef xr : links) {
            if (Disease.class.getName().equals(xr.kind)) {
                Disease neighbor = (Disease)xr.deRef();
                neighbors.put(neighbor.id, neighbor);
                // recurse
                getNeighbors (neighbors, neighbor.links);
            }
        }
    }

    /**
     * TODO: this should be using the Ligand class!
     */
    @Transactional
    static List<Ligand> addChemblRefs (Target target, PreparedStatement pstm)
        throws SQLException {
        ResultSet rs = pstm.executeQuery();
        try {
            /*
            Namespace ns = NamespaceFactory.registerIfAbsent
                ("ChEMBL", "https://www.ebi.ac.uk/chembl");
            */
            List<Ligand> ligands = new ArrayList<Ligand>();
            while (rs.next()) {
                String chemblId = rs.getString("cmpd_chemblid");
                Keyword kw = KeywordFactory.registerIfAbsent
                    (ChEMBL_SYNONYM, chemblId,
                     "https://www.ebi.ac.uk/chembl/compound/inspect/"+chemblId);
                Ligand ligand = new Ligand (rs.getString("cmpd_name_in_ref"));
                ligand.synonyms.add(kw);
                ligands.add(ligand);
                
                //target.properties.add(kw);
            }
            Logger.debug("Target "+target.id+": "+target.name
                         +" has "+ ligands.size()+" ChEMBL ligands!");
            
            return ligands;
        }
        finally {
            rs.close();
        }
    }
    
    /**
     * TODO: this should be using the Ligand class!
     */
    @Transactional
    static List<Ligand> addDrugDbRefs (Target target, PreparedStatement pstm)
        throws SQLException {
        ResultSet rs = pstm.executeQuery();
        try {
            List<Ligand> ligands = new ArrayList<Ligand>();
            while (rs.next()) {
                String drug = rs.getString("drug");
                String ref = rs.getString("reference");
                Keyword kw = KeywordFactory.registerIfAbsent
                    (DRUG, drug, ref != null
                     && ref.startsWith("http") ? ref : null);
                Ligand ligand = new Ligand (drug);
                ligands.add(ligand);
                //target.properties.add(kw);
            }
            Logger.debug("Target "+target.id+": "+target.name
                         +" has "+ligands.size()+" TCRD Drug references!");
            return ligands;
        }
        finally {
            rs.close();
        }
    }

    @Transactional
    static void addDTO(Target target) {

    }


    @Transactional
    static void addGeneRIF (Target target, PreparedStatement pstm)
        throws SQLException {
        ResultSet rs = pstm.executeQuery();
        try {
            int count = 0;
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
                            xref.properties.add(new Text (GENERIF, text));
                            xref.update();
                            ++updates;
                        }
                    }
                }
                
                if (updates > 0) {
                    ++count;
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
    
    public static Result index () {
        return ok (ix.idg.views.html.tcrd.render("IDG TCRD Loader"));
    }
}
