package ix.publications.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import play.libs.Akka;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import scala.concurrent.duration.Duration;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import play.cache.Cache;
import play.db.DB;
import com.avaje.ebean.*;
import org.apache.commons.codec.binary.Base64;

import ix.utils.Global;
import ix.utils.Eutils;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.EutilsPlugin;

import ix.core.search.TextIndexer;
import ix.core.models.Event;
import ix.core.models.Publication;
import ix.core.models.Author;
import ix.core.models.Figure;
import ix.core.models.PubAuthor;
import ix.core.models.Keyword;
import ix.core.models.Namespace;
import ix.core.models.Attribute;
import ix.core.models.Thumbnail;
import ix.core.models.Organization;
import ix.core.models.XRef;
import ix.ncats.models.Project;
import ix.ncats.models.Employee;
import ix.ncats.models.Program;

import ix.core.controllers.PublicationFactory;
import ix.core.controllers.OrganizationFactory;
import ix.ncats.controllers.reach.EmployeeFactory;

import ix.publications.views.html.*;

public class PublicationProjectProcessor extends Controller {
    static final Model.Finder<Long, Project> projFinder = 
        new Model.Finder(Long.class, Project.class);
    static final Model.Finder<Long, Publication> pubFinder =
        new Model.Finder(Long.class, Publication.class);
    static final Model.Finder<Long, Employee> emplFinder =
        new Model.Finder(Long.class, Employee.class);
    static final Model.Finder<Long, Namespace> resFinder = 
        new Model.Finder(Long.class, Namespace.class);
    static final Model.Finder<Long, Program> progFinder = 
        new Model.Finder(Long.class, Program.class);
    static final EutilsPlugin eutils =
        Play.application().plugin(EutilsPlugin.class);

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static ActorRef DaemonActorRef;

    static class UpdateOp {
        final long id;
        final long pmid;        
        final int oper;
        final String rowid;
        
        UpdateOp (String rowid, long id, long pmid, int oper) {
            this.rowid = rowid;
            this.id = id;
            this.pmid = pmid;
            this.oper = oper;
        }
    }
    
    static class PublicationProcessorDaemon extends UntypedActor {
        final Cancellable tick = context().system().scheduler()
            .schedule(Duration.create(60, TimeUnit.SECONDS),
                      Duration.create(60*60, TimeUnit.SECONDS),
                      self (), "tick", context().dispatcher(), null);

        @Override
        public void preStart () {
            Logger.debug("Daemon "+self().path().name()
                         +" started: "+new java.util.Date());
        }
        
        @Override
        public void postStop () {
            Logger.debug("Daemon "+self().path().name()+" stoped: "
                         +new java.util.Date());
            tick.cancel();
            DaemonActorRef = null;
        }
        
        @Override
        public void onReceive (Object mesg) throws Exception {
            if (mesg.equals("tick")) {
                try {
                    doUpdate ();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.error("Can't do update", ex);
                }
            }
            else if (mesg instanceof UpdateOp) {
                UpdateOp op = (UpdateOp)mesg;
                Logger.debug(new java.util.Date()
                             +": Update status of "+op.rowid+" "
                             +op.id+" "+op.pmid);
                // clear cache..
                Cache.remove(Publication.class.getName()+".facets");
            }
            else {
                unhandled (mesg);
            }
        }

        void doUpdate () throws Exception {
            DataSource ds = DB.getDataSource("mgmt");
            Connection con = ds.getConnection();
            try {
                Statement stm = con.createStatement();
                ResultSet rset = stm.executeQuery
                    ("select a.rowid,a.*,b.pmid "
                     +"from update_queue a, publication b where "
                     +"entity_type = 'publication' and "
                     +"process is null and "
                     +"a.entity_id = b.db_pub_id "
                     +"order by a.created,a.entity_id,a.operation");
                List<UpdateOp> ops = new ArrayList<UpdateOp>();
                while (rset.next()) {
                    int oper = rset.getInt("operation");
                    long id = rset.getLong("entity_id");
                    long pmid = rset.getLong("pmid");
                    String rowid = rset.getString("rowid");
                    ops.add(new UpdateOp (rowid, id, pmid, oper));
                }
                rset.close();
                stm.close();
                
                Logger.debug(new java.util.Date()
                             +": "+ops.size()+" update(s) in queue!");
                if (!ops.isEmpty()) {
                    Map<Long, UpdateOp> add = new HashMap<Long, UpdateOp>();
                    for (UpdateOp op: ops) {
                        if (op.oper == 0) {
                            Logger.debug("Adding publication "+op.pmid+"...");
                            Publication pub =
                                PublicationFactory.byPMID(op.pmid);
                            if (pub != null) {
                                Logger.warn("Publication "+op.pmid
                                            +" is already available!");
                            }
                            else {
                                add.put(op.id, op);
                            }
                        }
                        else if (op.oper == 1) {
                            Publication pub =
                                PublicationFactory.byPMID(op.pmid);
                            if (pub != null) {
                                // delete
                                Logger.debug
                                    ("Deleting publication "+op.pmid+"...");
                                pub.delete();
                            }
                            else {
                                Logger.warn("Can't find publication "+op.pmid
                                            +"; no update performed!");
                            }
                            add.put(op.id, op); // then add 
                        }
                        else if (op.oper == 2) {
                            Publication pub =
                                PublicationFactory.byPMID(op.pmid);
                            if (pub != null) {
                                // delete
                                Logger.debug
                                    ("Deleting publication "+op.pmid+"...");
                                pub.delete();
                                self().tell(op, self ());
                            }
                            else {
                                Logger.warn("Can't find publication "+op.pmid
                                            +"; no delete performed!");
                            }
                        }
                        else {
                            Logger.warn("Unknown operation: "+op.oper);
                        }
                    }

                    if (!add.isEmpty()) {
                        PublicationProcessor processor =
                            new PublicationProcessor (con);
                        for (UpdateOp op : add.values()) {
                            Publication pub = processor.process(op.id, op.pmid);
                            if (pub != null) {
                                Logger.debug
                                    ("Publication \""+pub.title+"\" added!");
                                self().tell(op, self ());
                            }
                            else {
                                Logger.warn("Can't add publication: "
                                            +op.pmid);
                            }
                        }
                        processor.shutdown();
                    }
                }
            }
            finally {
                con.close();
            }
        }
    }

    public static boolean isDaemonRunning () {
        return DaemonActorRef != null;
    }

    public static Result status () {
        return ok (daemon.render("NCATS Publication Daemon"));
    }

    public static Result daemon () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String secret = requestData.get("secret-code");
        if (secret == null || secret.length() == 0
            || !secret.equals(Play.application()
                              .configuration().getString("ix.secret"))) {
            return unauthorized
                ("You do not have permission to access resource!");
        }
        String action = requestData.get("action");
        Logger.debug("Action: \""+action+"\"");
        if ("start".equalsIgnoreCase(action)) {
            if (DaemonActorRef != null) {
                Logger.warn("Daemon is already running!");
            }
            else {
                DaemonActorRef = Akka.system().actorOf
                    (Props.create(PublicationProcessorDaemon.class));
            }
        }
        else if ("stop".equalsIgnoreCase(action)) {
            if (DaemonActorRef != null) {
                DaemonActorRef.tell(PoisonPill.getInstance(),
                                    DaemonActorRef.noSender());
            }
            else {
                Logger.warn("Daemon is not running!");
            }
        }
        else {
            Logger.debug("Unknown action: \""+action+"\"");
        }
        
        return ok (action);
    }

    static class PublicationProcessor {
        PreparedStatement pstm, pstm2, pstm3, pstm4;

        public PublicationProcessor (Connection con) throws SQLException {
            pstm = con.prepareStatement
                ("select * from pub_image "
                 +"where db_pub_id = ? order by img_order");
            pstm2 = con.prepareStatement
                ("select * from pub_tag where db_pub_id = ?");
            pstm3 = con.prepareStatement
                ("select * from pub_program where db_pub_id = ?");
            pstm4 = con.prepareStatement
                ("select * from publication where db_pub_id = ?");
        }

        public void shutdown () {
            try {
                pstm.close();
                pstm2.close();
                pstm3.close();
                pstm4.close();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public Publication process (long id, long pmid) {
            Publication pub = PublicationFactory.byPMID(pmid);
            if (pub != null) {
                Logger.warn("Publication "+pmid+" is already registered!");
                return pub;
            }
            
            pub = eutils.getPublication(pmid);
            if (pub == null) {
                Logger.warn("Can't locate PMID "+pmid);
                return pub;
            }
            
            for (PubAuthor p : pub.authors) {
                p.author = instrument (p.author);
            }
                
            // get image (if any)
            List<Keyword> tags = new ArrayList<Keyword>();
            try {
                List<Figure> figs = createFigures (pstm, id);
                for (Figure f : figs) {
                    pub.figures.add(f);
                }
            }
            catch (Exception ex) {
                Logger.trace("Can't retrieve images for pmid="+pmid, ex);
            }
            
            try {
                for (Keyword k : fetchPubCategories (pstm2, id)) {
                    if ("web-tag".equalsIgnoreCase(k.label)) {
                        tags.add(k);
                    }
                    pub.keywords.add(k);
                }
                
                for (Keyword k : fetchPrograms (pstm3, id))
                    pub.keywords.add(k);
            }
            catch (Exception ex) {
                Logger.trace
                    ("Can't retrieve categories for pmid="+pmid, ex);
            }
            
            try {
                pub.save();
                Logger.debug("+ New publication added "+pub.id
                             +": "+pub.title);
                
                // now create an xref with the tags
                if (!tags.isEmpty()) {
                    XRef ref = new XRef (pub);
                    pstm4.setLong(1, id);
                    ResultSet rs = pstm4.executeQuery();
                    if (rs.next()) {
                        String alias = rs.getString("web_alias");
                        if (alias != null) {
                            Logger.debug("++ web alias: "+alias);
                        }
                        else { // just use the title
                            alias = pub.title;
                        }
                        Keyword k = new Keyword ("rss-content", alias);
                        ref.properties.add(k);
                    }
                    rs.close();
                    ref.properties.add
                        (new Keyword("UUID", UUID.randomUUID().toString()));
                    ref.properties.addAll(tags);
                    ref.save();
                    Logger.debug("+ XRef "+ref.id+" created "
                                 +"for publication "+pub.id
                                 +" with "+tags.size()
                                 +" tags!");
                }
            }
            catch (Exception ex) {
                Logger.trace("Can't save publication: " +pub.title, ex);
            }
            return pub;
        }
    }

    static TextIndexer getIndexer () {
        TextIndexerPlugin plugin = 
            Play.application().plugin(TextIndexerPlugin.class);
        return plugin.getIndexer();
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
        
        String jdbcUrl = requestData.get("jdbc-url");
        String jdbcUsername = requestData.get("jdbc-username");
        String jdbcPassword = requestData.get("jdbc-password");

        String ldapUsername = requestData.get("ldap-username");
        String ldapPassword = requestData.get("ldap-password");
        Logger.debug("LDAP: "+ldapUsername);

        Connection con = null;
        try {
            if (jdbcUrl == null || jdbcUrl.equals("")) {
                // now try to fetch from configuration
                DataSource ds = DB.getDataSource("mgmt");
                if (ds == null) {
                    return badRequest ("No JDBC URL specified!");
                }
                Logger.debug("JDBC data source: "+ds);
                con = ds.getConnection();
            }
            else {
                Logger.debug("JDBC: "+jdbcUrl);
                con = DriverManager.getConnection
                    (jdbcUrl, jdbcUsername, jdbcPassword);
            }

            int projects = processProjects (con);
            Logger.debug("Processing projects..."+projects);

            int count = EmployeeFactory.createIfEmpty
                (ldapUsername, ldapPassword);
            Logger.debug(count+ " employees retrieved!");

            int pubs = processPublications (con);

            return redirect (ix.publications
                             .controllers.routes
                             .ReachApp.publications(null, 10, 1));
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

    public static int processProjects (Connection con) throws SQLException {
        Statement stm = con.createStatement();
        PreparedStatement pstm = con.prepareStatement
            ("select * from project_tag where proj_id = ? order by proj_id");
        PreparedStatement pstm2 = con.prepareStatement
            ("select * from project_image where proj_id =? order by img_order");
        PreparedStatement pstm3 = con.prepareStatement
            ("select * from project_collab where project_id = ?");
        PreparedStatement pstm4 = con.prepareStatement
            ("select * from project_category where proj_id = ?");

        Namespace doResource = resFinder
            .where().eq("name", "Disease Ontology").findUnique();
        if (doResource == null) {
            doResource = Namespace.newPublic("Disease Ontology");
            doResource.location = "http://www.disease-ontology.org";
            doResource.save();
            Logger.debug("Added Resource "+doResource.id+": "+doResource.name);
        }

        int count = 0;
        try {
            ResultSet rset = stm.executeQuery
                ("select * from project_summary order by proj_id");
            while (rset.next()) {
                String title = rset.getString("title");
                List<Project> projs = projFinder
                    .where().eq("title", title).findList();
                if (projs.isEmpty()) {
                    Project proj = new Project ();
                    proj.title = title;
                    proj.objective = rset.getString("objective");
                    proj.scope = rset.getString("scope");
                    proj.isPublic = "Y".equalsIgnoreCase
                        (rset.getString("is_public"));

                    String status = rset.getString("status");
                    if (status != null) {
                        Event ev = new Event ();
                        ev.title = "Status";
                        ev.description = status;
                        proj.milestones.add(ev);
                    }

                    String progs = rset.getString("programs");
                    if (progs != null) {
                        for (String p : progs.split(",")) {
                            Program prog = progFinder.where()
                                .eq("name", p.trim()).findUnique();
                            if (prog == null) {
                                prog = new Program (p.trim());
                                prog.save();
                                Logger.debug
                                    ("New program \""+p+"\" ("
                                     +prog.id+") added!");
                            }
                            proj.programs.add(prog);
                        }
                    }

                    long pid = rset.getLong("proj_id");
                    try {
                        List<Figure> figs = createFigures (pstm2, pid);
                        for (Figure f : figs)
                            proj.figures.add(f);
                    }
                    catch (Exception ex) {
                        Logger.trace
                            ("Can't retrieve images for project="+pid, ex);
                    }
                    try {
                        List<Keyword> categories =
                            fetchProjCategories (pstm4, pid);
                        proj.keywords.addAll(categories);
                    }
                    catch (Exception ex) {
                        Logger.trace
                            ("Can't retrieve categories for project="+pid,ex);
                    }
                    
                    List<Keyword> web = new ArrayList<Keyword>();
                    try {
                        List<Keyword> tags = fetchTags (pstm, pid);
                        for (Keyword t : tags) {
                            if ("web-tag".equalsIgnoreCase(t.label))
                                web.add(t);
                            proj.keywords.add(t);
                        }
                    }
                    catch (Exception ex) {
                        Logger.trace
                            ("Can't retrieve tags for project: "+proj.title,
                             ex);
                    }

                    /*
                    try {
                        List<Author> collab = fetchCollaborators (pstm3, pid);
                        proj.collaborators.addAll(collab);
                    }
                    catch (Exception ex) {
                        Logger.trace("Can't retrieve collaborators for "
                                     +"project: "+proj.title, ex);
                    }
                    */

                    try {
                        proj.save();
                        Logger.debug("New project "+proj.id+": "+proj.title);
                        if (!web.isEmpty()) {
                            XRef ref = new XRef (proj);
                            String alias = rset.getString("web_alias");
                            if (alias == null) {
                                alias = proj.title;
                            }
                            ref.properties.add
                                (new Keyword ("rss-content", alias));
                            ref.properties.add
                                (new Keyword
                                 ("UUID", UUID.randomUUID().toString()));
                            ref.properties.addAll(web);
                            ref.save();
                            Logger.debug("+ XRef "+ref.id+" created "
                                         +"for project "+proj.id
                                         +" with "+web.size()
                                         +" tags!");
                        }
                        ++count;
                    }
                    catch (Exception ex) {
                        Logger.trace("Can't save project: "+proj.title, ex);
                    }
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

    public static int processPubAuthors (Connection con) throws SQLException {
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
                        TextIndexer.SearchResult results = getIndexer().search
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

    public static int processPublications (Connection con) 
        throws SQLException {
        PublicationProcessor processor = new PublicationProcessor (con);
        Statement stm = con.createStatement();

        try {
            // now migrate publications
            ResultSet rset = stm.executeQuery
                ("select * from publication "
                 +"where rownum <= 100"
                 +"order by pmid, db_pub_id"
                );
            int publications = 0;
            while (rset.next()) {
                long pmid = rset.getLong("pmid");
                long id = rset.getLong("db_pub_id");
                if (rset.wasNull()) {
                    Logger.warn(id+": pmid is null!");
                }
                else {
                    Publication pub = processor.process(id, pmid);
                    if (pub != null)
                        ++publications;
                }
            }
            rset.close();
            Logger.debug(publications+" publications processed!");
            
            return publications;
        }
        finally {
            stm.close();
            processor.shutdown();
        }
    }

    static List<Keyword> fetchProjCategories (PreparedStatement pstm, long id)
        throws Exception {
        List<Keyword> cats = new ArrayList<Keyword>();
        pstm.setLong(1, id);
        ResultSet rset = pstm.executeQuery();
        while (rset.next()) {
            String cat = rset.getString("category");
            cats.add(new Keyword ("Category", cat));
        }
        rset.close();
        return cats;
    }
    
    static List<Keyword> fetchPubCategories (PreparedStatement pstm, long id)
        throws Exception {
        pstm.setLong(1, id);
        ResultSet rset = pstm.executeQuery();
        List<Keyword> keywords = new ArrayList<Keyword>();
        while (rset.next()) {
            String tag = rset.getString("tag_key");
            if ("category".equalsIgnoreCase(tag)
                || "web-tag".equalsIgnoreCase(tag)) {
                Keyword kw = new Keyword ();
                kw.label = tag;
                kw.term = rset.getString("value");
                keywords.add(kw);
            }
        }
        rset.close();
        return keywords;
    }

    static List<Keyword> fetchTags (PreparedStatement pstm, long id)
        throws Exception {
        List<Keyword> keywords = new ArrayList<Keyword>();      

        pstm.setLong(1, id);
        ResultSet rs = pstm.executeQuery();
        try {
            while (rs.next()) {
                String source = rs.getString("source");
                String tag = rs.getString("tag_key");
                String value = rs.getString("value");

                if (tag.startsWith("DOID")) {
                    Keyword key = new Keyword ("Disease", value);
                    key.href = "http://disease-ontology.org/term/"+tag;
                }
                else {
                    Keyword key = new Keyword (tag, value);
                    keywords.add(key);
                }
            }
            return keywords;
        }
        finally {
            rs.close();
        }
    }

    public List<Author> fetchCollaborators (PreparedStatement pstm, long id)
        throws Exception {
        List<Author> collabs = new ArrayList<Author>();
        pstm.setLong(1, id);
        ResultSet rset = pstm.executeQuery();
        try {
            while (rset.next()) {
                Organization org = new Organization ();
                org.name = rset.getString("base_affil");
                org.city = rset.getString("city");
                org.state = rset.getString("state");
                org.zipcode = rset.getString("zip");
                org.country = rset.getString("country");
                org = OrganizationFactory.registerIfAbsent(org);
            }
            return collabs;
        }
        finally {
            rset.close();
        }
    }
    
    static List<Keyword> fetchPrograms (PreparedStatement pstm, long id)
        throws Exception {
        pstm.setLong(1, id);
        ResultSet rset = pstm.executeQuery();
        List<Keyword> keywords = new ArrayList<Keyword>();
        while (rset.next()) {
            String program = rset.getString("program");
            Keyword kw = new Keyword ();
            kw.label = "Program";
            kw.term = program;
            keywords.add(kw);
        }
        rset.close();
        return keywords;
    }

    static List<Figure> createFigures (PreparedStatement pstm, long id)
        throws Exception {
        List<Figure> figs = new ArrayList<Figure>();
        pstm.setLong(1, id);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) {
            Figure fig = parseFigure (rs);
            if (fig != null) {
                figs.add(fig);
                if (rs.next()) { // thumbnail
                    Thumbnail thumb = new Thumbnail ();
                    parseFigure (thumb, rs);
                    thumb.parent = fig;
                    figs.add(thumb);
                }
            }
        }
        rs.close();
        return figs;
    }

    static Figure parseFigure (ResultSet rs) throws Exception {
        return parseFigure (null, rs);
    }

    static Figure parseFigure (Figure fig, ResultSet rs) throws Exception {
        String data = rs.getString("img_base64");
        if (data != null) {
            if (fig == null) 
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
        if (a.forename == null)
            return a;

        String[] toks = a.forename.split("[\\s]+");
        String firstname = toks[0];
        String initials = null;
        boolean quote = false;
        if (toks.length > 1) {
            if (toks[1].length() == 1)
                initials = toks[1];
            else {
                firstname = a.forename;
                quote = true;
            }
        }
        else
            firstname = a.forename;
        String lastname = a.lastname;

        List<Employee> employees = emplFinder
            .where(Expr.and(Expr.eq("lastname", a.lastname),
                            Expr.eq("forename", firstname)))
            .findList();

        Author author = a;
        if (employees.isEmpty()) {
            if (quote) {
                // "N Nora"
                if ("yang".equalsIgnoreCase(lastname)
                    && firstname.indexOf("Nora") >= 0)
                    firstname = "nora";
                else
                    firstname = "\""+firstname+"\"";
            }
            else if (firstname.length() == 1)
                firstname += "*";
            else if ("nguyen".equalsIgnoreCase(lastname)
                     && "D-T".equalsIgnoreCase(firstname))
                firstname = "trung";
            else if ("steve".equalsIgnoreCase(firstname) 
                     || "steven".equalsIgnoreCase(firstname))
                firstname = "(steve steven)";
            else if ("matt".equalsIgnoreCase(firstname)
                     || "matthew".equalsIgnoreCase(firstname)
                     || "mathew".equalsIgnoreCase(firstname))
                firstname = "(matt matthew mathew)";
            else if ("dave".equalsIgnoreCase(firstname) 
                     || "david".equalsIgnoreCase(firstname))
                firstname = "(dave david)";
            else if ("sam".equalsIgnoreCase(firstname)
                     || "samuel".equalsIgnoreCase(firstname))
                firstname = "(sam samuel)";
            else if ("chris".equalsIgnoreCase(firstname)
                     || "christopher".equalsIgnoreCase(firstname))
                firstname = "(chris christopher)";
            else if ("bill".equalsIgnoreCase(firstname)
                     || "william".equalsIgnoreCase(firstname))
                firstname = "(bill william)";
            else if ("gene".equalsIgnoreCase(firstname)
                     || "eugen".equalsIgnoreCase(firstname)
                     || "eugene".equalsIgnoreCase(firstname))
                firstname = "(gene eugen eugene)";
            else if ("henrike".equalsIgnoreCase(firstname))
                lastname = "(veith nelson)";
            else if ("Wichterman".equalsIgnoreCase(lastname)
                     || "Kouznetsova".equalsIgnoreCase(lastname))
                lastname = "(Kouznetsova Wichterman)";

            try {
                // try text searching
                TextIndexer.SearchResult results = getIndexer().search
                    ("lastname:"+lastname+" AND forename:"+firstname, 10);
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

    public static Result loader () {
        return ok (processor.render("Project/Publication Bulk Processor"));
    }

    static Figure createFigure (String url) {
        Figure fig = null;
        try {
            URL u = new URL (url);
            URLConnection con = u.openConnection();
            fig = new Figure ();
            fig.mimeType = con.getContentType();
            fig.size = con.getContentLength();

            InputStream is = con.getInputStream();
            byte[] buf = new byte[1024];
            MessageDigest md = MessageDigest.getInstance("sha1");
            ByteArrayOutputStream bos = new ByteArrayOutputStream (fig.size);
            for (int nb; (nb = is.read(buf, 0, buf.length)) > 0; ) {
                bos.write(buf, 0, nb);
                md.update(buf, 0, nb);
            }
            fig.data = bos.toByteArray();
            StringBuilder sb = new StringBuilder ();
            byte[] digest = md.digest();
            for (int i = 0; i < digest.length; ++i)
                sb.append(String.format("%1$02x", digest[i]&0xff));
            fig.sha1 = sb.toString();
            Logger.debug("data="+fig.data.length+" sha1="+fig.sha1);
        }
        catch (Throwable ex) {
            Logger.trace("Can't fetch figure from "+url, ex);
        }
        return fig;
    }
}
