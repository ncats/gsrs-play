package ix.ncats.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.sql.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;

import ix.utils.Global;
import ix.utils.Eutils;

import ix.core.search.TextIndexer;
import ix.core.models.Event;
import ix.core.models.Publication;
import ix.core.models.Author;
import ix.core.models.PubAuthor;
import ix.ncats.models.Project;
import ix.ncats.models.Employee;

import ix.core.controllers.PublicationFactory;

public class Migration extends Controller {
    static final Model.Finder<Long, Project> projFinder = 
        new Model.Finder(Long.class, Project.class);
    static final Model.Finder<Long, Publication> pubFinder =
        new Model.Finder(Long.class, Publication.class);
    static final Model.Finder<Long, Employee> emplFinder =
        new Model.Finder(Long.class, Employee.class);

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
        String jdbcUsername = requestData.get("jdbc-username");
        String jdbcPassword = requestData.get("jdbc-password");
        Logger.debug("JDBC: "+jdbcUrl);

        String ldapUsername = requestData.get("ldap-username");
        String ldapPassword = requestData.get("ldap-password");
        Logger.debug("LDAP: "+ldapUsername);

        if (jdbcUrl == null || jdbcUrl.equals("")) {
            return badRequest ("No JDBC URL specified!");
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection
                (jdbcUrl, jdbcUsername, jdbcPassword);

            //int projects = migrateProjects (con);
            //Logger.debug("Migrating projects..."+projects);
            int count = EmployeeFactory.createIfEmpty
                (ldapUsername, ldapPassword);
            Logger.debug(count+ " employees retrieved!");

            int pubs = migratePublications (con);

            return ok (pubs+" publications migrated");
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
    }

    public static int migrateProjects (Connection con) throws SQLException {
        Statement stm = con.createStatement();
        int count = 0;
        try {
            ResultSet rset = stm.executeQuery
                ("select * from project_summary");
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
        }
        catch (SQLException ex) {
            Logger.trace("Database exception", ex);
        }
        finally {
            stm.close();
        }
        return count;
    }

    public static int migratePubAuthors (Connection con) throws SQLException {
        Statement stm = con.createStatement();
        try {
            // migrate authors
            ResultSet rset = stm.executeQuery
                ("select * from pub_author where first_name is not null");
            int authors = 0;
            while (rset.next()) {
                String first = rset.getString("first_name");
                String last = rset.getString("last_name");
                String initials = rset.getString("initials");
                String affil = rset.getString("author_affil");

                String[] toks = first.split("[\\s]+");
                if (toks.length > 1 && toks[toks.length-1].length() == 1) {
                    first = toks[0];
                    for (int i = 1; i < toks.length-1; ++i)
                        first += " "+toks[i];
                }

                List<Employee> employees = emplFinder
                    .where(Expr.and(Expr.eq("lastname", last),
                                    Expr.eq("forename", first)))
                    .findList();

                if (employees.isEmpty()) {
                    try {
                        // try text searching
                        TextIndexer.SearchResult results = 
                            Global.getInstance().getTextIndexer().search
                            ("lastname:"+last+" AND forename:"+first, 10);
                        if (results.isEmpty()) {
                            // not ncats
                            Author author = new Author ();
                            author.lastname = last;
                            author.forename = first;
                            author.initials = initials;
                            author.affiliation = affil;
                            author.save();
                            Logger.debug("++ Added author: "+last+", "+first);
                            ++authors;
                        }
                    }
                    catch (IOException ex) {
                        Logger.trace("Text search failed", ex);
                    }
                }
            }
            rset.close();
            Logger.debug("++ "+authors+" non-NCATS authors added!");

            return authors;
        }
        finally {
            stm.close();
        }
    }

    public static int migratePublications (Connection con) throws SQLException {
        Statement stm = con.createStatement();
        try {
            // now migrate publications
            ResultSet rset = stm.executeQuery
                ("select * from publication "
                 //+"where rownum <= 10"
                );
            int publications = 0;
            while (rset.next()) {
                long pmid = rset.getLong("pmid");
                if (rset.wasNull()) {
                }
                else {
                    Publication pub = PublicationFactory.byPMID(pmid);
                    if (pub == null) {
                        pub = Eutils.fetchPublication(pmid);
                        if (pub != null) {
                            for (PubAuthor p : pub.authors) {
                                p.author = instrument (p.author);
                            }

                            pub.save();
                            Logger.debug("+ New publication added "+pub.id
                                         +": "+pub.title);
                            ++publications;
                        }
                        else {
                            Logger.warn("Can't locate PMID "+pmid);
                        }
                    }
                    else {
                        Logger.debug("Publication "
                                     +pmid+" is already downloaded!");
                    }
                }
            }
            rset.close();
            Logger.debug(publications+" new publications added!");
            
            return publications;
        }
        finally {
            stm.close();
        }
    }
    
    static Author instrument (Author a) {
        List<Employee> employees = emplFinder
            .where(Expr.and(Expr.eq("lastname", a.lastname),
                            Expr.eq("forename", a.forename)))
            .findList();

        Author author = a;
        if (employees.isEmpty()) {
            try {
                // try text searching
                TextIndexer.SearchResult results = 
                    Global.getInstance().getTextIndexer().search
                    ("lastname:"+a.lastname+" AND forename:"+a.forename, 10);
                if (!results.isEmpty()) {
                    for (Iterator it = results.getMatches().iterator();
                         it.hasNext(); ) {
                        Object obj = it.next();
                        if (obj instanceof Author) {
                            author =(Author)obj;
                        }
                    }
                }
            }
            catch (IOException ex) {
                Logger.trace("Text search failed", ex);
            }
        }
        else {
            author = employees.iterator().next();
        }

        return author;
    }

    public static Result index () {
        return ok (ix.ncats.views.html.migration.render
                   ("Project/Publication Migration"));
    }
}
