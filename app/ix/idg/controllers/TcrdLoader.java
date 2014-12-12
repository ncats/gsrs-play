package ix.idg.controllers;

import java.sql.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;

import ix.core.models.*;
import ix.idg.models.*;

public class TcrdLoader extends Controller {
    static final Model.Finder<Long, Target> targetDb = 
        new Model.Finder(Long.class, Target.class);
    static final Model.Finder<Long, Disease> diseaseDb = 
        new Model.Finder(Long.class, Disease.class);

    public static Result load () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String jdbcUrl = requestData.get("jdbcUrl");
        String jdbcUsername = requestData.get("jdbc-username");
        String jdbcPassword = requestData.get("jdbc-password");
        Logger.debug("JDBC: "+jdbcUrl);

        if (jdbcUrl == null || jdbcUrl.equals("")) {
            return badRequest ("No JDBC URL specified!");
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
        int count = 0;

        Namespace resource = Namespace.newPublic("IDGv2");
        resource.location = "https://pharos.nih.gov";
        try {
            ResultSet rset = stm.executeQuery
                ("select * from t2tc a, target b, protein c\n"+
                 "where a.target_id = b.id\n"+
                 "and a.protein_id = c.id limit 10");

            UniprotParser uni = new UniprotParser ();
            while (rset.next()) {
                long protId = rset.getLong("protein_id");
                if (rset.wasNull()) {
                    Logger.warn("Not a protein target: "
                                +rset.getLong("target_id"));
                    continue;
                }
                String fam = rset.getString("idgfam");
                String tdl = rset.getString("tdl");
                String acc = rset.getString("uniprot");
                List<Target> targets = targetDb
                    .where().eq("synonyms.term", acc).findList();
                if (targets.isEmpty()) {
                    try {
                        uni.parse("http://www.uniprot.org/uniprot/"
                                  +acc+".xml");
                        
                        Target target = uni.getTarget();
                        target.family = fam;
                        target.idgClass = tdl;
                        
                        target.save();
                        Logger.debug(target.id+": "+target.name);
                        ++count;
                    }
                    catch (Throwable t) {
                        Logger.trace("Can't parse "+acc, t);
                    }
                }
                else 
                    Logger.debug(acc+" is already loaded!");
            }
            rset.close();
            return count;
        }
        finally {
            stm.close();
        }
    }

    public static Result index () {
        return ok (ix.idg.views.html.tcrd.render("IDG TCRD Loader"));
    }
}
