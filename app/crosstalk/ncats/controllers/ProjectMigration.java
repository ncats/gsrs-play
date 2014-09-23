package crosstalk.ncats.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.sql.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import crosstalk.ncats.models.Project;
import crosstalk.core.models.Event;

public class ProjectMigration extends Controller {
    static final Model.Finder<Long, Project> finder = 
        new Model.Finder(Long.class, Project.class);

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Result migrate () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String jdbcUrl = requestData.get("jdbcUrl");
        String username = requestData.get("username");
        String password = requestData.get("password");
        Logger.debug("JDBC: "+jdbcUrl);

        if (jdbcUrl == null || jdbcUrl.equals("")) {
            return badRequest ("No JDBC URL specified!");
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection(jdbcUrl, username, password);
            Statement stm = con.createStatement();
            ResultSet rset = stm.executeQuery
                ("select * from project_summary");
            int count = 0;
            while (rset.next()) {
                Project proj = new Project ();
                proj.title = rset.getString("title");
                proj.objective = rset.getString("objective");
                proj.scope = rset.getString("scope");
                Event ev = new Event ();
                ev.title = rset.getString("status");
                proj.milestones.add(ev);
                proj.save();
                
                Logger.debug("Project "+proj.id+": "+proj.title);
                ++count;
            }
            rset.close();
            return ok (count+" projects migrated!");
        }
        catch (SQLException ex) {
            Logger.trace("Database exception", ex);
            return internalServerError (ex.getMessage());
        }
        finally {
            if (con != null) {
                try { con.close(); }
                catch (Exception ex) {
                    Logger.trace("Can't close db connection", ex);
                }
            }
        }
    }

    public static Result index () {
        return ok (crosstalk.ncats.views.html.migration.render("Project Migration"));
    }
}
