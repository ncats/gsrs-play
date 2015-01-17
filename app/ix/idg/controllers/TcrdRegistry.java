package ix.idg.controllers;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;

import ix.core.models.*;
import ix.idg.models.*;
import ix.core.controllers.NamespaceFactory;
import ix.core.controllers.KeywordFactory;
import ix.utils.Global;
import ix.core.plugins.*;

public class TcrdRegistry extends Controller {
    public static final String DISEASE = "IDG Disease";
    public static final String DRUG = "IDG Drug";
    public static final String CLASSIFICATION = "IDG Target Classification";
    public static final String FAMILY = "IDG Target Family";
    public static final String ZSCORE = "IDG Z-score";
    public static final String CONF = "IDG Confidence";
    public static final String ChEMBL = "IDG ChEMBL";
    public static final String GENERIF = "IDG GeneRIF";
    public static final String TARGET = "IDG Target";
        
    static final Model.Finder<Long, Target> targetDb = 
        new Model.Finder(Long.class, Target.class);
    static final Model.Finder<Long, Disease> diseaseDb = 
        new Model.Finder(Long.class, Disease.class);
    
    static final TextIndexerPlugin indexer = 
        Play.application().plugin(TextIndexerPlugin.class);

    public static final Namespace namespace = NamespaceFactory.registerIfAbsent
        ("TCRDv093", "https://pharos.nih.gov");

    static class TcrdTarget implements Comparable<TcrdTarget> {
        String acc;
        String family;
        String tdl;
        Long id;
        Long protein;

        TcrdTarget (String acc, String family, String tdl,
                    Long id, Long protein) {
            this.acc = acc;
            this.family = family;
            this.tdl = tdl;
            this.id = id;
            this.protein = protein;
        }

        public int hashCode () { return acc.hashCode(); }
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
        if (jdbcUrl == null || jdbcUrl.equals("")) {
            return badRequest ("No JDBC URL specified!");
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
                obo.register(new FileInputStream (file));
            }
            catch (IOException ex) {
                Logger.trace("Can't load obo file: "+file, ex);
            }
        }

        Connection con = null;
        int count = 0;
        try {
            con = DriverManager.getConnection
                (jdbcUrl, jdbcUsername, jdbcPassword);
            
            int rows = 0;
            if (maxRows != null && maxRows.length() > 0) {
                try {
                    rows = Integer.parseInt(maxRows);
                }
                catch (NumberFormatException ex) {
                    Logger.warn("Bogus maxRows \""+maxRows+"\"; default to 0!");
                }
            }
            count = load (con, rows);
        }
        catch (SQLException ex) {
            return internalServerError (ex.getMessage());
        }
        finally {
            try {
                if (con != null) con.close();
            }
            catch (SQLException ex) {}
        }

        return ok (count+" target(s) loaded!");
    }

    static int load (Connection con, int rows) throws SQLException {
        Statement stm = con.createStatement();
        PreparedStatement pstm = con.prepareStatement
            ("select * from target2disease where target_id = ?");
        PreparedStatement pstm2 = con.prepareStatement
            ("select * from chembl_activity where target_id = ?");
        PreparedStatement pstm3 = con.prepareStatement
            ("select * from drugdb_activity where target_id = ?");
        PreparedStatement pstm4 = con.prepareStatement
            ("select * from generif where protein_id = ?");
        
        int count = 0;
                
        try {
            ResultSet rset = stm.executeQuery
                ("select * from t2tc a, target b, protein c\n"+
                 "where a.target_id = b.id\n"+
                 "and a.protein_id = c.id "
                 +(rows > 0 ? ("limit "+rows) : "")
                 //+"limit 20"
                 );

            Set<TcrdTarget> targets = new HashSet<TcrdTarget>();
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
                    TcrdTarget t = new TcrdTarget (acc, fam, tdl, id, protId);
                    targets.add(t);
                }
            }
            rset.close();

            Logger.debug("Preparing to register "+targets.size()+" targets!");
            for (TcrdTarget t : targets) {
                Logger.debug(t.family+" "+t.tdl+" "+t.acc+" "+t.id);
                try {
                    Target target = new Target ();
                    target.idgFamily = t.family;
                    target.idgClass = t.tdl;
                    target.synonyms.add
                        (new Keyword (TARGET, String.valueOf(t.id)));
                    UniprotRegistry uni = new UniprotRegistry ();
                    uni.register(target, t.acc);
                    
                    pstm.setLong(1, t.id);
                    addDiseaseRefs (target, namespace, pstm);

                    pstm2.setLong(1, t.id);
                    addChemblRefs (target, pstm2);

                    pstm3.setLong(1, t.id);
                    addDrugDbRefs (target, pstm3);
                    
                    pstm4.setLong(1, t.protein);
                    addGeneRIF (target, pstm4);

                    // reindex this entity; we have to do this since
                    // the target.update doesn't trigger the postUpdate
                    // event.. need to figure exactly why.
                    indexer.getIndexer().update(target);
                    
                    ++count;
                }
                catch (Throwable e) {
                    Logger.trace("Can't parse "+t.acc, e);
                }
            }
            
            return count;
        }
        finally {
            stm.close();            
            pstm.close();
            pstm2.close();
            pstm3.close();
        }
    }

    static void addDiseaseRefs (Target target, Namespace namespace,
                                PreparedStatement pstm) throws Exception {
        ResultSet rs = pstm.executeQuery();
        try {
            XRef self = null;
            String label = FAMILY;
            Keyword family = KeywordFactory.registerIfAbsent
                (label, target.idgFamily,
                 Global.getNamespace()
                 +"/targets/search?facet="
                 +URLEncoder.encode(label, "utf-8")+"/"
                 +URLEncoder.encode(target.idgFamily, "utf-8"));

            label = CLASSIFICATION;
            Keyword clazz = KeywordFactory.registerIfAbsent
                (label, target.idgClass,
                 Global.getNamespace()
                 +"/targets/search?facet="
                 +URLEncoder.encode(label,"utf-8")+"/"
                 +URLEncoder.encode(target.idgClass, "utf-8"));

            Keyword name = KeywordFactory.registerIfAbsent
                (UniprotRegistry.TARGET, target.name, target.getSelf());
            
            while (rs.next()) {
                String doid = rs.getString("doid");
                List<Disease> diseases = DiseaseFactory.finder
                    .where(Expr.and(Expr.eq("synonyms.label", "DOID"),
                                    Expr.eq("synonyms.term", doid)))
                    .findList();
                if (diseases.isEmpty()) {
                    Logger.warn("Target "+target.id+" references "
                                +"unknown disease "+doid);
                }
                else {
                    double zscore = rs.getDouble("zscore");
                    double conf = rs.getDouble("conf");
                    for (Disease d : diseases) {
                        XRef xref = new XRef (d);
                        xref.namespace = namespace;
                        Keyword kw = KeywordFactory.registerIfAbsent
                            (DISEASE, d.name, xref.getHRef());
                        xref.properties.add(kw);
                        xref.properties.add(new VNum (ZSCORE, zscore));
                        xref.properties.add(new VNum (CONF, conf));
                        target.links.add(xref);

                        // now add all the parent of this disease node
                        addXRefs (target, namespace, d.links);
                        
                        if (self == null) {
                            self = new XRef (target);
                            self.namespace = namespace;
                            self.properties.add(family);
                            self.properties.add(clazz);
                            self.properties.add(name);
                            self.save();
                        }

                        // link the other way
                        d.links.add(self);
                        d.update();
                        indexer.getIndexer().update(d);
                    }
                }
            }
            target.update();
        }
        finally {
            rs.close();
        }
    }

    static void addXRefs (Target target,
                          Namespace namespace, List<XRef> links) {
        for (XRef xr : links) {
            if (Disease.class.getName().equals(xr.kind)) {
                Disease neighbor = (Disease)xr.deRef();
                Keyword kw = KeywordFactory.registerIfAbsent
                    (DISEASE, neighbor.name, xr.getHRef());
                XRef xref = new XRef (neighbor);
                xref.namespace = namespace;
                xref.properties.add(kw);
                target.links.add(xref);
                // recurse
                addXRefs (target, namespace, neighbor.links);
            }
        }
    }

    /**
     * TODO: this should be using the Ligand class!
     */
    static void addChemblRefs (Target target, PreparedStatement pstm)
        throws SQLException {
        ResultSet rs = pstm.executeQuery();
        try {
            /*
            Namespace ns = NamespaceFactory.registerIfAbsent
                ("ChEMBL", "https://www.ebi.ac.uk/chembl");
            */
            int count = 0;
            while (rs.next()) {
                String chemblId = rs.getString("cmpd_chemblid");
                Keyword kw = KeywordFactory.registerIfAbsent
                    (ChEMBL, chemblId,
                     "https://www.ebi.ac.uk/chembl/compound/inspect/"+chemblId);
                target.properties.add(kw);
                ++count;
            }
            if (count > 0) {
                Logger.debug("Updated target "+target.id+": "+target.name
                             +" with "+count+" ChEMBL references!");
                target.update();
            }       
        }
        finally {
            rs.close();
        }
    }
    
    /**
     * TODO: this should be using the Ligand class!
     */
    static void addDrugDbRefs (Target target, PreparedStatement pstm)
        throws SQLException {
        ResultSet rs = pstm.executeQuery();
        try {
            int count = 0;
            while (rs.next()) {
                String drug = rs.getString("drug");
                String ref = rs.getString("reference");
                Keyword kw = KeywordFactory.registerIfAbsent
                    (DRUG, drug, ref != null
                     && ref.startsWith("http") ? ref : null);
                target.properties.add(kw);
                ++count;
            }
            if (count > 0) {
                Logger.debug("Updated target "+target.id+": "+target.name
                             +" with "+count+" TCRD Drug references!");
                target.update();
            }
        }
        finally {
            rs.close();
        }
    }

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
                target.update();
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
