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
import org.apache.commons.codec.binary.Base64;

import ix.utils.Global;
import ix.utils.Eutils;

import ix.core.search.TextIndexer;
import ix.core.models.Event;
import ix.core.models.Publication;
import ix.core.models.Author;
import ix.core.models.Figure;
import ix.core.models.PubAuthor;
import ix.core.models.Keyword;
import ix.core.models.Resource;
import ix.core.models.Attribute;
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
    static final Model.Finder<Long, Resource> resFinder = 
        new Model.Finder(Long.class, Resource.class);

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

            int projects = migrateProjects (con);
            Logger.debug("Migrating projects..."+projects);

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
        PreparedStatement pstm = con.prepareStatement
            ("select * from project_tag where proj_id = ? order by proj_id");
        PreparedStatement pstm2 = con.prepareStatement
            ("select * from project_image where proj_id =? order by img_order");

        Resource doResource = resFinder
            .where().eq("name", "Disease Ontology").findUnique();
        if (doResource == null) {
            doResource = Resource.newPublic("Disease Ontology");
            doResource.url = "http://www.disease-ontology.org";
            doResource.save();
            Logger.debug("Added Resource "+doResource.id+": "+doResource.name);
        }

        int count = 0;
        try {
            ResultSet rset = stm.executeQuery
                ("select * from project_summary");
            while (rset.next()) {
                String title = rset.getString("title");
                List<Project> projs = projFinder
                    .where().eq("title", title).findList();
                if (projs.isEmpty()) {
                    Project proj = new Project ();
                    proj.title = title;
                    proj.objective = rset.getString("objective");
                    proj.scope = rset.getString("scope");
                    Event ev = new Event ();
                    ev.title = rset.getString("status");
                    proj.milestones.add(ev);

                    long pid = rset.getLong("proj_id");
                    pstm.setLong(1, pid);
                    ResultSet rs = pstm.executeQuery();
                    while (rs.next()) {
                        String source = rs.getString("source");
                        String tag = rs.getString("tag_key");
                        String value = rs.getString("value");

                        Keyword key = new Keyword (value);
                        Attribute attr = new Attribute ("DOID", tag);
                        attr.resource = doResource;
                        //attr.save();
                        key.attrs.add(attr);

                        attr = new Attribute 
                            ("href", 
                             "http://www.disease-ontology.org/api/metadata/"
                             +tag);
                        attr.resource = doResource;
                        //attr.save();
                        key.attrs.add(attr);

                        proj.annotations.add(key);
                    }
                    rs.close();

                    try {
                        List<Figure> figs = createFigures (pstm2, pid);
                        for (Figure f : figs)
                            proj.figures.add(f);
                    }
                    catch (Exception ex) {
                        Logger.trace
                            ("Can't retrieve images for project="+pid, ex);
                    }
                    proj.save();
                
                    Logger.debug("New project "+proj.id+": "+proj.title);
                    ++count;
                }
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
        PreparedStatement pstm = con.prepareStatement
            ("select * from pub_image where db_pub_id = ? order by img_order");
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

                            // get image (if any)
                            try {
                                long id = rset.getLong("db_pub_id");
                                List<Figure> figs = createFigures (pstm, id);
                                for (Figure f : figs) {
                                    pub.figures.add(f);
                                }
                            }
                            catch (Exception ex) {
                                Logger.trace
                                    ("Can't retrieve images for pmid="+pmid, ex);
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
            pstm.close();
        }
    }

    static List<Figure> createFigures (PreparedStatement pstm, long id)
        throws Exception {
        List<Figure> figs = new ArrayList<Figure>();
        pstm.setLong(1, id);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            Figure fig = parseFigure (rs);
            if (fig != null)
                figs.add(fig);
        }
        rs.close();
        return figs;
    }

    static Figure parseFigure (ResultSet rs) throws Exception {
        Figure fig = null;
        String data = rs.getString("img_base64");
        if (data != null) {
            fig = new Figure ();
            int pos = data.indexOf(":image");
            if (pos > 0) {
                int end = data.indexOf(";");
                fig.mimeType = data.substring(pos+1, end);
                Base64 b64 = new Base64 ();
                pos = data.indexOf("base64,");
                if (pos > 0) {
                    byte[] d = data.substring(pos+7).getBytes("utf8");
                    fig.data = b64.decode(d);
                    fig.size = fig.data.length;
                    
                    MessageDigest md = MessageDigest.getInstance("sha1");
                    byte[] digest = md.digest(d);
                    StringBuilder sb = new StringBuilder ();
                    for (int i = 0; i < digest.length; ++i)
                        sb.append(String.format("%1$02x", digest[i]&0xff));
                    fig.sha1 = sb.toString();
                }
                else {
                    Logger.warn("Bogus image encoding");
                }
            }
            else {
                Logger.warn("Invalid image data encoding for");
            }
        }
        return fig;
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
