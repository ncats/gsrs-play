package ix.idg.controllers;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.*;

import ix.core.models.*;
import ix.idg.models.*;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.StructureReceiver;
import ix.core.plugins.StructureProcessorPlugin;
import ix.core.plugins.PersistenceQueue;
import ix.core.search.TextIndexer;
import ix.core.controllers.*;

import play.Logger;
import play.Play;
import play.db.DB;
import com.avaje.ebean.Expr;
import com.jolbox.bonecp.BoneCPDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChemblRegistry {
    public static final String WHO_ATC = "WHO ATC";
    public static final String ATC_ANCESTRY =  "ATC Ancestry";
    
    public static final String ChEMBL = "ChEMBL";
    public static final String ChEMBL_ID = "ChEMBL ID";
    public static final String ChEMBL_MECHANISM = "ChEMBL Mechanism";
    public static final String ChEMBL_SYNONYM = "ChEMBL Synonym";
    public static final String ChEMBL_MOLFILE = "ChEMBL Molfile";
    public static final String ChEMBL_INCHI = "ChEMBL InChI";
    public static final String ChEMBL_INCHI_KEY = "ChEMBL InChI Key";
    public static final String ChEMBL_SMILES = "ChEMBL Canonical SMILES";
    public static final String ChEMBL_PROTEIN_CLASS = "ChEMBL Protein Class";
    public static final String ChEMBL_PROTEIN_ANCESTRY =
        "ChEMBL Protein Ancestry";

    static final TextIndexer INDEXER = 
        Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    static final StructureProcessorPlugin PROCESSOR =
        Play.application().plugin(StructureProcessorPlugin.class);
    static final PersistenceQueue PQ =
        Play.application().plugin(PersistenceQueue.class);
    
    static class LigandStructureReceiver implements StructureReceiver {
        final Ligand ligand;
        final Namespace namespace;
        
        LigandStructureReceiver (Namespace namespace, Ligand ligand) {
            this.ligand = ligand;
            this.namespace = namespace;
        }

        public String getSource () { return namespace.name; }
        public void receive (Status status, String mesg, Structure struc) {
            //Logger.debug(status+": ligand "+ligand.getName());
            if (status == Status.OK) {
                try {
                    if (struc != null) {
                        struc.namespace = namespace;
                        struc.update();
                        
                        XRef xref = new XRef (struc);
                        xref.namespace = namespace;
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

    DataSource chembl;
    Connection con;
    PreparedStatement pstm, pstm2, pstm3, pstm4, pstm5, pstm6, pstm7, pstm8;
    Map<String, Set<String>> uniprotMap;
    Namespace namespace;
    Set<Long> molregno = new HashSet<Long>();
    
    public ChemblRegistry (Map<String, Set<String>> uniprotMap)
        throws SQLException {
        if (uniprotMap == null || uniprotMap.isEmpty())
            throw new IllegalArgumentException ("No UniProt mapping provided!");
        
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
                String version = rset.getString("name");
                namespace = NamespaceFactory.registerIfAbsent
                    (version, "https://www.ebi.ac.uk/chembl");
            }
            rset.close();
            Logger.debug("ChEBML version: "+namespace.name);
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
            
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
            ("select *  "
             +"from molecule_atc_classification a, atc_classification b "
             +"where molregno = ? and a.level5 = b.level5");
        pstm6 = con.prepareStatement
            ("select * from activities a, "
             +"assays b, target_dictionary c, docs d "
             +"where a.molregno = ? "
             +"and b.relationship_type = 'D' "
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
        pstm8 = con.prepareStatement
            ("select * from chembl_id_lookup where entity_type = ? "
             +"and chembl_id = ?");
        
        this.uniprotMap = uniprotMap;           
    }

    public void shutdown () throws SQLException {
        try {
            pstm.close();
            pstm2.close();
            pstm3.close();
            pstm4.close();
            pstm5.close();
            pstm6.close();
            pstm7.close();
            pstm8.close();
        }
        finally {
            con.close();
        }
    }

    public Namespace getNamespace () { return namespace; }

    public void instruments (Target target, List<Ligand> ligands)
        throws SQLException {
        Set<Long> tids = target (target);
        for (Ligand ligand : ligands) 
            ligand (tids, target, ligand);
    }

    void ligand (final Set<Long> tids,
                 final Target target, final Ligand ligand)
        throws SQLException {
        // see if this ligand has a chembl_id..
        String chembl = null, drug = null;
        for (Keyword kw : ligand.synonyms) {
            if (ChEMBL_ID.equals(kw.label))
                chembl = kw.term;
            else if (TcrdRegistry.DRUG.equals(kw.label))
                drug = kw.term;
        }

        Set<Long> ids = null;
        if (chembl != null) {
            pstm2.setString(1, chembl);
            ids = instrument (ligand, pstm2);
        }
        else if (drug != null) {
            pstm7.setString(1, drug);
            ids = instrument (ligand, pstm7);
        }
        else {
            Logger.warn("Neither chemblId nor drug name found!");
            return;
        }

        if (ids == null || ids.isEmpty())
            return;
        
        Logger.debug("Ligand: "+ligand.id+" "+ligand.getName()+"; chembl="
                     +chembl+" drug="+drug);

        final XRef tref = new XRef (target);
        tref.namespace = namespace;
        ligand.links.add(tref); // ligand -> target
        
        final XRef lref = new XRef (ligand);
        lref.namespace = namespace;
        target.links.add(lref); // target -> ligand

        Logger.debug("Registering information for ligand "
                     +ligand.id+" "+ligand.getName());
        for (Long no : ids) {
            int rows = mechanism (tids, no, ligand, tref, lref);
            Logger.debug("...."+rows+" mechanisms");
            rows = atc (no, ligand);
            Logger.debug("...."+rows+" ATC");
            rows = activity (tids, no, ligand, tref, lref);
            Logger.debug("...."+rows+" activities");
        }

        final EntityFactory.EntityMapper mapper =
            new EntityFactory.EntityMapper(BeanViews.Full.class);
        
        // queue this for update
        PQ.submit(new PersistenceQueue.AbstractPersistenceContext() {
                public void persists () throws Exception {
                    try {
                        tref.save();
                    }
                    catch (Exception ex) {
                        Logger.error("Can't save Target XRef", ex);
                        Logger.debug("Target XRef\n"+mapper.toJson(tref,true));
                    }
                    try {
                        lref.save();
                    }
                    catch (Exception ex) {
                        Logger.error("Can't save Ligand XRef", ex);
                        Logger.debug("Ligand XRef\n"+mapper.toJson(lref,true));
                    }
                    try {
                        ligand.update();
                        INDEXER.update(ligand);
                    }
                    catch (Exception ex) {
                        Logger.error("Can't update ligand", ex);
                        Logger.debug("Ligand\n"+mapper.toJson(ligand,true));
                    }
                    try {
                        target.update();
                        INDEXER.update(target);
                    }
                    catch (Exception ex) {
                        Logger.error("Can't update ligand", ex);
                        Logger.debug("Target\n"+mapper.toJson(target,true));
                    }
                }
            });
    }

    Long resolve (String chemblId, String type) throws SQLException {
        pstm8.setString(1, type);
        pstm8.setString(2, chemblId);
        ResultSet rset = null;
        try {
            rset = pstm8.executeQuery();
            if (rset.next()) {
                return rset.getLong("entity_id");
            }
            return null;
        }
        finally {
            if (rset != null)
                rset.close();
        }
    }

    Long getMolRegno (String chemblId) throws SQLException {
        return resolve (chemblId, "COMPOUND");
    }
    
    Long getTID (String chemblId) throws SQLException {
        return resolve (chemblId, "TARGET");
    }

    Set<Long> instrument (Ligand ligand, PreparedStatement pstm)
        throws SQLException {
        ResultSet rset = pstm.executeQuery();
        String molfile = null, inchi = null,
            inchiKey = null, smiles = null;
        String chemblId = null;
        
        Map<String, String> syns = new HashMap<String, String>();
        Set<Long> newids = new HashSet<Long>();
        while (rset.next()) {
            Long id = rset.getLong("molregno");
            assert id != null: "molregno is null!";
            if (molregno.contains(id))
                continue;
            newids.add(id);

            String syn = rset.getString("synonyms");
            if (syn != null) {
                Keyword kw = new Keyword (ChEMBL_SYNONYM, syn);
                String type = rset.getString("syn_type");
                kw.href = type;
                ligand.synonyms.add(kw);
                syns.put(type, syn);
            }

            String smi = rset.getString("canonical_smiles");
            if (molfile == null || smiles.length() > smi.length()) {
                molfile = rset.getString("molfile");
                inchi = rset.getString("standard_inchi");
                inchiKey = rset.getString("standard_inchi_key");
                smiles = smi;
            }
            
            if (chemblId == null) {
                chemblId = rset.getString("chembl_id");
                Keyword kw = KeywordFactory.registerIfAbsent
                    (ChEMBL_ID, chemblId,
                     "https://www.ebi.ac.uk/chembl/compound/inspect/"+chemblId);
                ligand.addIfAbsent(kw);
            }
        }
        rset.close();
        
        if (!newids.isEmpty()) {
            molregno.addAll(newids);
            
            // no synonym, so use the chembl_id as the name
            ligand.name = chemblId;
            for (String type : new String[]{
                    "INN","USAN","FDA","BAN","USP",
                    "TRADE_NAME","MERCK_INDEX",
                    "JAN","DCF","ATC",
                    "RESEARCH_CODE","SYSTEMATIC","OTHER"
                }) {
                String s = syns.get(type);
                if (s != null) {
                    ligand.name = s;
                    break;
                }
            }
            
            if (molfile != null) {
                ligand.properties.add
                    (new Text (ChEMBL_MOLFILE, molfile));
                ligand.properties.add(new Text (ChEMBL_INCHI, inchi));
                ligand.properties.add
                    (new Text (ChEMBL_INCHI_KEY, inchiKey));
                ligand.properties.add(new Text (ChEMBL_SMILES, smiles));
                ligand.save();
                
                // now standardize and index
                Logger.debug("submitting "+chemblId+" for processing...");
                StructureReceiver receiver = new LigandStructureReceiver
                    (namespace, ligand);
                PROCESSOR.submit(molfile, receiver);
            }
            else {
                Logger.warn("Ligand "+ligand.getName()+" ("+chembl
                        +") has empty molfile!");
                ligand.save();
            }
        }
        
        return newids;
    }
    
    // mechanism    
    int mechanism (Set<Long> tids, Long molregno,
                   Ligand ligand, XRef tref, XRef lref) throws SQLException {
        pstm4.setLong(1, molregno);
        int rows = 0;
        ResultSet rset = pstm4.executeQuery();
        while (rset.next()) {
            String action = rset.getString("mechanism_of_action");
            Long tid = rset.getLong("tid");
            if (action != null && tids.contains(tid)) {
                Text tx = new Text (ChEMBL_MECHANISM, action);
                tx.save();
                tref.properties.add(tx);
                lref.properties.add(tx);
            }
            ++rows;
        }
        rset.close();
        return rows;
    }

    int atc (Long molregno, Ligand ligand) throws SQLException {
        pstm5.setLong(1, molregno);
        ResultSet rset = pstm5.executeQuery();
        int rows = 0;
        while (rset.next()) {
            List<Keyword> path = new ArrayList<Keyword>();
            for (int i = 1; i <= 4; ++i) {
                String d = rset.getString("level"+i+"_description");
                assert d != null
                    : ("ATC description is null at level "
                       +i+" for molregno="+molregno);
                String l = rset.getString("level"+i);
                Keyword kw = KeywordFactory.registerIfAbsent
                    (WHO_ATC+" "+l, d, null);
                ligand.addIfAbsent(kw);
            }
            String d = rset.getString("level5");
            Keyword kw = new Keyword (WHO_ATC, d);
            ligand.addIfAbsent(kw);
            ++rows;
        }
        rset.close();
        return rows;
    }

    int activity (Set<Long> tids, Long molregno, Ligand ligand,
                  XRef tref, XRef lref) throws SQLException {
        pstm6.setLong(1, molregno);
        ResultSet rset = pstm6.executeQuery();
        int rows = 0;
        while (rset.next()) {
            Long tid = rset.getLong("tid");
            if (!tids.contains(tid)) {
                continue;
            }
            
            String type = rset.getString("standard_type");
            Double value = rset.getDouble("standard_value");
            String unit = rset.getString("standard_units");
            Value act = null;
            if (!rset.wasNull()) {
                VNum num = new VNum (type, value);
                num.unit = unit;
                num.save();
                lref.properties.add(num);
                tref.properties.add(num);
                act = num;
            }

            Long pmid = rset.getLong("pubmed_id");
            if (!rset.wasNull()) {
                Publication pub = PublicationFactory.registerIfAbsent(pmid);
                if (pub != null) {
                    XRef ref = ligand.getLink(pub);
                    boolean isNew = false;
                    if (ref == null) {
                        ref = new XRef (pub);
                        ref.namespace = namespace;
                        isNew = true;
                    }
                    if (act != null) {
                        ref.properties.add(act);
                    }
                    if (isNew) {
                        ref.save();
                        ligand.links.add(ref);
                    }
                    else {
                        ref.update();
                    }
                    ligand.addIfAbsent(pub); // a bit redundent
                }
            }
            ++rows;
        }
        rset.close();
        return rows;
    }

    Set<Long> target (Target target) throws SQLException {
        String acc = null;
        Set<String> chemblIds = null;
        for (Keyword kw : target.synonyms) {
            if (UniprotRegistry.ACCESSION.equals(kw.label)) {
                acc = kw.term;
                chemblIds = uniprotMap.get(acc);
                if (chemblIds != null) {
                    // just pick one for now
                    break;
                }
            }
        }
        
        if (acc == null) {
            Logger.warn
                ("Target "+target.id+" ("+target.name
                 +") has no "+UniprotRegistry.ACCESSION+" synonym!");
            return null;
        }
        else if (chemblIds == null || chemblIds.isEmpty()) {
            Logger.warn
                ("Target "+target.id+" ("+target.name
                 +") accession "+acc+" has no chembl_id mapping!");
            return null;
        }

        Set<Long> tids = new TreeSet<Long>();
        String chemblId = null;
        for (String id : chemblIds) {
            Long tid = getTID (id);
            if (tid != null)
                tids.add(tid);
            Logger.debug(acc +" => "+id+":"+tid);
            chemblId = id;
        }
        // pick the last one for now...
        pstm.setString(1, chemblId);

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
                    try {
                        Predicate pred = new Predicate
                            (ChEMBL_PROTEIN_ANCESTRY);
                        pred.subject = new XRef (node);
                        pred.subject.save();
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
        
        return tids;
    }
}
