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

public class TcrdLoader extends Controller {
    static final Model.Finder<Long, Target> targetDb = 
        new Model.Finder(Long.class, Target.class);
    static final Model.Finder<Long, Disease> diseaseDb = 
        new Model.Finder(Long.class, Disease.class);

    static class TcrdTarget implements Comparable<TcrdTarget> {
	String acc;
	String family;
	String tdl;
	Long id;

	TcrdTarget (String acc, String family, String tdl, Long id) {
	    this.acc = acc;
	    this.family = family;
	    this.tdl = tdl;
	    this.id = id;
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
        String jdbcUrl = requestData.get("jdbcUrl");
        String jdbcUsername = requestData.get("jdbc-username");
        String jdbcPassword = requestData.get("jdbc-password");
        Logger.debug("JDBC: "+jdbcUrl);
        if (jdbcUrl == null || jdbcUrl.equals("")) {
            return badRequest ("No JDBC URL specified!");
        }

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
            count = load (con);
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

    static int load (Connection con) throws SQLException {
        Statement stm = con.createStatement();
        PreparedStatement pstm = con.prepareStatement
            ("select * from target2disease where target_id = ?");
	PreparedStatement pstm2 = con.prepareStatement
	    ("select * from chembl_activity where target_id = ?");
	PreparedStatement pstm3 = con.prepareStatement
	    ("select * from drugdb_activity where target_id = ?");
	
        int count = 0;

        Namespace namespace = NamespaceFactory.registerIfAbsent
	    ("TCRDv090", "https://pharos.nih.gov");

        try {
            ResultSet rset = stm.executeQuery
                ("select * from t2tc a, target b, protein c\n"+
                 "where a.target_id = b.id\n"+
                 "and a.protein_id = c.id "
                 //+"limit 50"
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
		    TcrdTarget t = new TcrdTarget (acc, fam, tdl, id);
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
			(new Keyword ("TCRD Target", String.valueOf(t.id)));
		    
		    pstm.setLong(1, t.id);
		    addDiseaseRefs (target, namespace, pstm);
		    pstm2.setLong(1, t.id);
		    addChemblRefs (target, pstm2);
		    pstm3.setLong(1, t.id);
		    addDrugDbRefs (target, pstm3);
		    
		    UniprotRegistry uni = new UniprotRegistry ();
		    uni.register(target, t.acc);
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
				PreparedStatement pstm)	throws SQLException {
	ResultSet rs = pstm.executeQuery();
	try {
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
			xref.properties.add
			    (new Text ("TCRD Disease Inference",
				       d.name));
			xref.properties.add
			    (new VNum ("TCRD Z-score", zscore));
			xref.properties.add
			    (new VNum ("TCRD Confidence", conf));
			target.links.add(xref);
		    }
		}
	    }
	}
	finally {
	    rs.close();
	}
    }

    static void addChemblRefs (Target target, PreparedStatement pstm)
	throws SQLException {
	ResultSet rs = pstm.executeQuery();
	try {
	    /*
	    Namespace ns = NamespaceFactory.registerIfAbsent
		("ChEMBL", "https://www.ebi.ac.uk/chembl");
	    */
	    while (rs.next()) {
		String chemblId = rs.getString("cmpd_chemblid");
		Keyword kw = new Keyword ("ChEMBL Activity", chemblId);
		kw.url =
		    "https://www.ebi.ac.uk/chembl/compound/inspect/"+chemblId;
		target.properties.add(kw);
	    }
	}
	finally {
	    rs.close();
	}
    }

    static void addDrugDbRefs (Target target, PreparedStatement pstm)
	throws SQLException {
	ResultSet rs = pstm.executeQuery();
	try {
	    while (rs.next()) {
		String drug = rs.getString("drug");
		String ref = rs.getString("reference");
		Keyword kw = new Keyword ("Drug", drug);
		kw.url = ref;
		target.properties.add(kw);
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
