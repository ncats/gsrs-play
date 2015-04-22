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

public class ChemblRegistry implements Commons {
    static final TextIndexer INDEXER = 
        Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    static final StructureProcessorPlugin PROCESSOR =
        Play.application().plugin(StructureProcessorPlugin.class);
    static final PersistenceQueue PQ =
        Play.application().plugin(PersistenceQueue.class);
    

    DataSource chembl;
    Connection con;
    PreparedStatement pstm, pstm2, pstm3, pstm4, pstm5, pstm6, pstm7, pstm8;
    Map<String, Set<String>> uniprotMap;
    Keyword source;
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
                source = KeywordFactory.registerIfAbsent
                    (SOURCE, version, "https://www.ebi.ac.uk/chembl");
            }
            rset.close();
            Logger.debug("ChEBML version: "+source.term);
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
             // D - direct H - homolog.. ignore for now
             //+"and b.relationship_type = 'D' "
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

    public Set<Long> instruments (Target target)  throws SQLException {
        return target (target);
    }

    public void instruments (Set<Long> tids,
                             Target target, List<Ligand> ligands)
        throws SQLException {
        if (tids != null) {
            for (Ligand ligand : ligands) {
                try {
                    ligand (tids, target, ligand);
                }
                catch (Exception ex) {
                    Logger.error("Unable to process ligand "
                                 +ligand.getName()
                                 +" for target "+target.id, ex);
                    ex.printStackTrace();
                }
            }
        }
        else if (!ligands.isEmpty()) {
            Logger.warn("Target "+target.id+" ("+target.getName()+") has no "
                        +"uniprot mapping but yet has "
                        +ligands.size()+" associated ligands!");
        }
    }

    void ligand (final Set<Long> tids,
                 final Target target, final Ligand ligand)
        throws SQLException {
        // see if this ligand has a chembl_id..
        String chembl = null, drug = null;
        for (Keyword kw : ligand.synonyms) {
            if (ChEMBL_ID.equals(kw.label))
                chembl = kw.term;
            else if (IDG_DRUG.equals(kw.label))
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

        if (ids == null || ids.isEmpty()) {
            Logger.warn("Can't lookup ligand " +ligand.id+" in chembl!");
            return;
        }
        Logger.debug("Ligand: "+ligand.id+" "+ligand.getName()+"; chembl="
                     +chembl+" drug="+drug);
        
        try {        
            final XRef tref = new XRef (target);
            tref.properties.add(source);
            tref.properties.add
                (KeywordFactory.registerIfAbsent
                 (Target.IDG_FAMILY, target.idgFamily, null));
            tref.properties.add
                (KeywordFactory.registerIfAbsent
                 (Target.IDG_DEVELOPMENT, target.idgTDL.name, null));
            
            final XRef lref = new XRef (ligand);
            lref.properties.add(source);
            lref.properties.add
                (KeywordFactory.registerIfAbsent
                 ("Ligand", ligand.getName(), null));
            
            Logger.debug("Registering information for ligand "
                         +ligand.id+" "+ligand.getName()
                         +" <-> target "+target.id);
            for (Long no : ids) {
                int rows = mechanism (tids, no, ligand, tref, lref);
                Logger.debug("...."+rows+" mechanisms");
                rows = activity (tids, no, ligand, tref, lref);
                Logger.debug("...."+rows+" activities");
            }
            
            
            tref.save();
            ligand.links.add(tref); // ligand -> target
            lref.save();
            target.links.add(lref); // target -> ligand
        }
        catch (Exception ex) {
            Logger.error("Can't create target ("+target.id+") <-> ligand ("
                         +ligand.id+") relationship!", ex);
            ex.printStackTrace();
        }
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
        Set<Long> all = new HashSet<Long>();
        while (rset.next()) {
            Long id = rset.getLong("molregno");
            assert id != null: "molregno is null!";
            
            String syn = rset.getString("synonyms");
            if (syn != null) {
                Keyword kw = new Keyword (ChEMBL_SYNONYM, syn);
                String type = rset.getString("syn_type");
                kw.href = type;
                ligand.addIfAbsent(kw);
                syns.put(type, syn);
            }
            
            if (!molregno.contains(id)) {
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
                         "https://www.ebi.ac.uk/chembl/compound/inspect/"
                         +chemblId);
                    ligand.addIfAbsent(kw);
                }
                int rows = atc (id, ligand);
                Logger.debug("...."+rows+" ATC");
                
                newids.add(id);
                molregno.add(id);
            }
            all.add(id);
        }
        rset.close();

        // no synonym, so use the chembl_id as the name
        if (chemblId != null) {
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
        }
        
        if (newids.isEmpty()) {
            Logger.debug("Ligand "+ligand.name+" is already registered!");
        }
        else if (molfile != null) {
            if (!ligand.hasProperty(ChEMBL_MOLFILE)) {
                ligand.properties.add
                    (new Text (ChEMBL_MOLFILE, molfile));
            }
            if (!ligand.hasProperty(ChEMBL_INCHI)) {
                ligand.properties.add(new Text (ChEMBL_INCHI, inchi));
            }
            if (!ligand.hasProperty(ChEMBL_INCHI_KEY)) {
                ligand.properties.add
                    (new Text (ChEMBL_INCHI_KEY, inchiKey));
            }
            if (!ligand.hasProperty(ChEMBL_SMILES)) {
                ligand.properties.add(new Text (ChEMBL_SMILES, smiles));
            }
            ligand.save();
            
            // now standardize and index
            Logger.debug("submitting "+chemblId+" for processing...");
            StructureReceiver receiver =
                new TcrdRegistry.LigandStructureReceiver(source, ligand);
            PROCESSOR.submit(molfile, receiver);
        }
        else {
            Logger.warn("Ligand "+ligand.getName()+" ("+chembl
                        +") has empty molfile!");
            ligand.save();
        }
        
        return all;
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

                String comment = rset.getString("mechanism_comment");
                if (comment != null) {
                    tx = new Text (ChEMBL_MECHANISM_COMMENT, comment);
                    tx.save();
                    tref.properties.add(tx);
                    lref.properties.add(tx);
                }

                String mode = rset.getString("action_type");
                if (mode != null) {
                    Keyword kw = KeywordFactory.registerIfAbsent
                        (ChEMBL_MOA_MODE, mode, null);
                    lref.properties.add(kw);
                }
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
                    (WHO_ATC, l, null);
                ligand.addIfAbsent(kw);
                // now create a lookup from this atc node to its definition
                KeywordFactory.registerIfAbsent(WHO_ATC+" "+l, d, null);
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
        Set<Long> acts = new HashSet<Long>();
        while (rset.next()) {
            Long tid = rset.getLong("tid");
            if (!tids.contains(tid)) {
                continue;
            }
            
            Long actId = rset.getLong("activity_id");
            if (acts.contains(actId))
                continue;
            acts.add(actId);
            
            String type = rset.getString("standard_type");
            Double value = rset.getDouble("standard_value");
            String unit = rset.getString("standard_units");
            Value act = null;
            if (!rset.wasNull()) {
                VNum num = new VNum (type, value);
                num.unit = unit;
                num.save();
                Logger.debug("........activity "+num.id);
                lref.properties.add(num);
                tref.properties.add(num);
                act = num;
                ++rows;
            }

            Long pmid = rset.getLong("pubmed_id");
            if (!rset.wasNull()) {
                Publication pub = PublicationFactory.registerIfAbsent(pmid);
                if (pub != null) {
                    XRef ref = ligand.getLink(pub);
                    boolean isNew = false;
                    if (ref == null) {
                        ref = new XRef (pub);
                        ref.properties.add(source);
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
        }
        rset.close();
        return rows;
    }

    Set<Long> target (Target target) throws SQLException {
        String acc = null;
        Set<String> chemblIds = new HashSet<String>();
        for (Keyword kw : target.synonyms) {
            if (UNIPROT_ACCESSION.equals(kw.label)) {
                if (acc == null)
                    acc = kw.term;
                Set<String> accs = uniprotMap.get(acc);
                if (accs != null) {
                    chemblIds.addAll(accs);
                }
            }
        }
        
        if (acc == null) {
            Logger.warn
                ("Target "+target.id+" ("+target.name
                 +") has no "+UNIPROT_ACCESSION+" synonym!");
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
            if (chemblId == null)
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
