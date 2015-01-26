package ix.ncats.controllers.reach;

import java.io.*;
import java.security.*;
import java.util.*;
import java.sql.*;
import java.net.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;
import org.apache.commons.codec.binary.Base64;

import ix.utils.Global;
import ix.utils.Eutils;
import ix.core.plugins.TextIndexerPlugin;

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
import ix.ncats.models.Project;
import ix.ncats.models.Employee;
import ix.ncats.models.Program;

import ix.core.controllers.PublicationFactory;

public class Migration extends Controller {
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

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static TextIndexer getIndexer () {
        TextIndexerPlugin plugin = 
            Play.application().plugin(TextIndexerPlugin.class);
        return plugin.getIndexer();
    }

    public static Result migrate () {
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
                (ldapUsername, ldapPassword, new EmployeeFactory.Callback() {
                        public boolean ok (Employee e) {
                            updateProfile (e);
                            return true;
                        }
                    });
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
                    pstm.setLong(1, pid);
                    ResultSet rs = pstm.executeQuery();
                    while (rs.next()) {
                        String source = rs.getString("source");
                        String tag = rs.getString("tag_key");
                        String value = rs.getString("value");

                        Keyword key = new Keyword (value);
                        /*
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
                        */
                        if (!"disease".equalsIgnoreCase(value))
                            proj.keywords.add(key);
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
                else {
                    updateProfile (employees.iterator().next());
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

    public static int migratePublications (Connection con) 
        throws SQLException {
        Statement stm = con.createStatement();
        PreparedStatement pstm = con.prepareStatement
            ("select * from pub_image where db_pub_id = ? order by img_order");
        PreparedStatement pstm2 = con.prepareStatement
            ("select * from pub_tag where db_pub_id = ?");
        PreparedStatement pstm3 = con.prepareStatement
            ("select * from pub_program where db_pub_id = ?");

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

                                for (Keyword k : fetchCategories (pstm2, id))
                                    pub.keywords.add(k);
                                
                                for (Keyword k : fetchPrograms (pstm3, id))
                                    pub.keywords.add(k);
                            }
                            catch (Exception ex) {
                                Logger.trace
                                    ("Can't retrieve images for pmid="+pmid, 
                                     ex);
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

    static List<Keyword> fetchCategories (PreparedStatement pstm, long id)
        throws Exception {
        pstm.setLong(1, id);
        ResultSet rset = pstm.executeQuery();
        List<Keyword> keywords = new ArrayList<Keyword>();
        while (rset.next()) {
            String tag = rset.getString("tag_key");
            if ("category".equalsIgnoreCase(tag)) {
                Keyword kw = new Keyword ();
                kw.label = tag;
                kw.term = rset.getString("value");
                keywords.add(kw);
            }
        }
        rset.close();
        return keywords;
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
                firstname = "\""+firstname+"\"";
            }
            else if (firstname.length() == 1)
                firstname += "*";
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

    public static Result index () {
        /*
        EbeanServer server = Ebean.getServer
            (Play.application().plugin(EbeanPlugin.class).defaultServer());
        SqlQuery query = 
            server.createSqlQuery("select distinct term from ix_core_value");
        List<SqlRow> rows = query.findList();
        for (SqlRow r : rows) {
            Logger.info(r.getString("term"));
        }
        */
        
        return ok (ix.ncats.views.html.migration.render
                   ("Project/Publication Migration"));
    }

    public static void updateProfile (Employee empl) {
        if (empl.lastname.equalsIgnoreCase("Carrillo-Carrasco")) {
            empl.suffix = "M.D.";
            empl.url = "http://www.ncats.nih.gov/about/org/profiles/carrillo-carrasco.html";
            empl.biography = 
"Nuria Carrillo-Carrasco leads the clinical team for the Therapeutics for Rare and Neglected Diseases (TRND) program. The group conducts natural history studies and early-phase clinical trials needed to advance promising therapies for rare diseases, develops biomarkers, and identifies appropriate endpoints for clinical trials. Before she joined TRND, Carrillo-Carrasco studied clinical and translational aspects of inborn errors of metabolism and gene therapy.\n"+
"Carrillo-Carrasco's research focuses on therapeutic development for rare genetic diseases, including GNE myopathy, creatine transporter defect and other inborn errors of metabolism. She is a faculty member for the Medical Biochemical Genetics fellowship program at NIH. Carrillo-Carrasco earned her M.D. from the National Autonomous University of Mexico and completed her pediatrics residency at Georgetown University Hospital. She is board certified in pediatrics, medical genetics and biochemical genetics.";
            empl.title = 
"Leader, Clinical Group, Therapeutics for Rare and Neglected Diseases\n"+
"Division of Pre-Clinical Innovation\n"+
"National Center for Advancing Translational Sciences\n"+
"National Institutes of Health";
            empl.research = 
"Carrillo-Carrasco is interested in addressing the challenges of developing therapeutics for rare diseases by improving drug development tools and the design of natural history studies and clinical trials for these diseases. Currently, she is the principal investigator of two studies of GNE myopathy: a natural history study and a clinical trial of ManNAc as a potential therapy for the disease. GNE myopathy is an extremely rare disorder that occurs in just one of every 1 million people and causes devastating progressive muscle weakness. No treatment exists for the disorder, which is caused by mutations in the GNE gene that lead to a defect in the sialic acid biosynthetic pathway. As part of the natural history study, Carrillo-Carrasco has characterized more than 40 patients on clinical, functional and molecular grounds and is evaluating appropriate outcome measures to be used in clinical trials, developing better diagnostic tools and discovering biomarkers for the disease.";
        }
        else if (empl.lastname.equalsIgnoreCase("Gee")) {
            empl.url = "http://www.ncats.nih.gov/about/org/profiles/gee.html";
            empl.suffix = "M.S.";
            empl.title =
"Research Scientist, Biology\n"+
"Division of Pre-Clinical Innovation\n"+
"National Center for Advancing Translational Sciences\n"+
"National Institutes of Health";
            empl.biography =
"Amanda Wagner Gee joined NCATS in 2014. She works on assay development for cell-based high-content and high-throughput screening. Prior to joining NCATS, Gee worked in the laboratory of Lee Rubin, Ph.D., at the Harvard Stem Cell Institute, using directed differentiation, primary tissue isolation and image-based high content screening to study neural and muscular disease. She received her M.S. in cell biology at Duke University, where she studied BMP4 signaling and sensory neuron patterning in peripheral nervous system development in the laboratory of Fan Wang, Ph.D.";
            empl.research = 
"Gee has a particular interest in adult stem cells and degenerative diseases. She has studied the neural and muscular degeneration in spinal muscular atrophy, a childhood-onset disease. She also has researched muscle degeneration and weakness in sarcopenia, a phenomenon associated with aging. For that project, she designed and executed an image-based screen on primary adult muscle stem cells, the lead compound from which is currently under evaluation in animals as a potential treatment. Her experience also includes research on amyotrophic lateral sclerosis and Huntington's disease.\n"+
"Gee has worked with adult and embryonic stem cells, and she appreciates their potential as well as their occasional quirks. At NCATS, she is collaborating on projects using induced pluripotent stem cell-derived patient cells, primary cells, biosensors for vesicle and receptor trafficking, and image-based screening techniques. Gee has been impressed by the diversity of expertise under one roof at NCATS, and she is excited by the opportunity to learn about and work on so many different topics.";
        }
        else if (empl.lastname.equalsIgnoreCase("gerhold")) {
            empl.selfie = createFigure
                ("http://www.ncats.nih.gov/about/org/profiles/images/GerholdD.jpg");
            empl.suffix = "Ph.D.";
            empl.url = "http://www.ncats.nih.gov/about/org/profiles/gerhold.html";
            empl.title = 
"Leader, Genomic Toxicology\n"+
"Division of Pre-Clinical Innovation\n"+
"National Center for Advancing Translational Sciences\n"+
"National Institutes of Health";
            empl.biography =
"David Gerhold is a staff genomic toxicologist at NCATS. He is developing in vitro methods to identify toxic compounds by introducing differentiating stem cell models and the gene expression technologies RNAseq and RASL-Seq. These new technologies support efforts to reach several goals:\n"+
"Identify potentially toxic chemicals in the environment through the Toxicology in the 21st Century consortium;\n"+
"Identify biomarkers of genetic susceptibility to tobacco; and\n"+
"Facilitate drug development through the Therapeutics for Rare and Neglected Diseases program.\n"+
"Previously, Gerhold pioneered gene expression microarray technology at Merck Research Laboratories, applying this expertise to identify kidney injury biomarkers. He subsequently co-led the Kidney Biomarker Working Group within the Predictive Safety Testing Consortium, collaborating across the pharmaceutical industry to qualify seven biomarkers with the Food and Drug Administration and publishing the findings in 2010. Gerhold also worked as a liaison with clinical nephrologists initiating translational studies to improve nephrology standard of care.";
            empl.research = 
"Gerhold's unifying vision is to develop a high-throughput, robust gene expression platform and core facility. NCATS researchers will use the RASL-Seq platform to gather thorough dose- and time-response data, as well as extensive reference data sets, to unlock the meaning behind these data. Such a platform can help show the toxic mechanisms of drugs and environmental contaminants alike.\n"+
"Gerhold uses RASL-Seq to determine the toxic mechanisms by which environmental contaminants affect neurons, liver hepatocytes and other cell types. These studies demand adoption and production of improved cellular models for toxic responses and for diseases, such as models derived from immortalized cells and induced-pluripotent stem cells (iPSC). Gerhold also uses iPSC technology to generate \"disease-in-a-dish\" models. For example, both RASL-Seq and RNAseq help researchers understand the effects of tobacco components on vascular endothelial cells. By producing these cells using iPSC from smokers with and without vascular disease, Gerhold can determine whether some patients are genetically susceptible or resistant to the disease.";
        }
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
