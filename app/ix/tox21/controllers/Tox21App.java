package ix.tox21.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.util.concurrent.Callable;

import play.*;
import play.cache.Cache;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import play.libs.ws.*;
import play.libs.F;
import com.avaje.ebean.*;

import ix.utils.Global;
import ix.utils.Util;
import ix.core.search.TextIndexer;
import static ix.core.search.TextIndexer.*;

import ix.core.models.*;
import ix.ncats.controllers.App;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.StructureProcessorPlugin;
import ix.core.plugins.StructureReceiver;

import tripod.chem.indexer.StructureIndexer;
import static tripod.chem.indexer.StructureIndexer.ResultEnumeration;

import ix.tox21.models.*;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

public class Tox21App extends App {
    static final int ROWS_PER_PAGE = 20;
    
    static final PayloadPlugin Payload =
        Play.application().plugin(PayloadPlugin.class);

    static final StructureProcessorPlugin Processor =
        Play.application().plugin(StructureProcessorPlugin.class);

    static class Tox21StructureReceiver implements StructureReceiver {
        final QCSample qc;
        final String source;
        
        Tox21StructureReceiver (String source, QCSample qc) {
            this.qc = qc;
            this.source = source;
        }

        public String getSource () { return source; }
        public void receive (Status status, String mesg, Structure struc) {
            if (status == Status.OK) {
                qc.sample.structure = struc;
            }
            else {
                Logger.error(status+": "
                             +qc.sample.getSynonym(Sample.S_NCGC).term
                             +": "+mesg);
            }
            
            try {
                qc.sample.save();
                qc.save();
                
                Logger.debug(status+": QCsample "
                             +qc.id+" sample "+qc.sample.id+" "
                             +qc.sample.getName());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
        
    static final String[] QC_FACETS = new String[] {
        "QC Grade",
        "Sample",
        "Defined Stereocenters"
    };

    static class Tox21GradeFacetDecorator extends FacetDecorator {
        public Tox21GradeFacetDecorator (Facet facet) {
            super (facet, true, 14);
        }

        @Override
        public String label (int i) {
            String label = super.label(i);
            QCSample.Grade grade = QCSample.Grade.valueOf(label);
            return "<span class=\"label label-"
                +grade.label+"\" data-toggle=\"tooltip\" "
                +"data-placement=\"right\" data-html=\"true\" title=\""
                +grade.desc.replaceAll("\n","<p>")+"\">"
                +label+"</span>";
        }
    }

    static class Tox21FacetDecorator extends FacetDecorator {
        public Tox21FacetDecorator (Facet facet) {
            super (facet, true, 6);
        }
        
        @Override
        public String label (int i) {
            String label = super.label(i);
            if (label.length() > 20) {
                label = "<span "
                    +"data-toggle=\"tooltip\" data-placement=\"right\" "
                    +"title=\""+label+"\">"+label.substring(0,20)
                    +"...</span>";
            }
            return label;
        }
    }

    static class StereocentersFacetDecorator extends FacetDecorator {
        public StereocentersFacetDecorator (Facet facet) {
            super (facet, true, 6);
        }
        @Override
        public String label (int i) {
            String label = super.label(i);
            if ("0:1".equals(label))
                return "None";
            if ("1:2".equals(label))
                return "1";
            if ("2:5".equals(label))
                return "[2,5)";
            if ("5:7".equals(label))
                return "[5,7)";
            if ("7:10".equals(label))
                return "[7,10)";
            return label;
        }
    }

    static FacetDecorator[] decorate (Facet... facets) {
        FacetDecorator[] decors = new FacetDecorator[facets.length];
        for (int i = 0; i < facets.length; ++i) {
            if (QC_FACETS[0].equals(facets[i].getName())) {
                // use special decorator
                decors[i] = new Tox21GradeFacetDecorator (facets[i]);
            }
            else if (QC_FACETS[1].equals(facets[i].getName())) {
                decors[i] = new Tox21FacetDecorator (facets[i]);
            }
            else if ("Defined Stereocenters".equals(facets[i].getName())) {
                decors[i] = new StereocentersFacetDecorator (facets[i]);
            }
            else {
                // use default decorator
                decors[i] = new FacetDecorator (facets[i]);
            }
        }
        return decors;
    }

    public static Result load () {
        return ok (ix.tox21.views.html.load.render("Load Tox21 QC database"));
    }

    public static Result loader () {
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

        Connection con = null;  
        try {
            con = DriverManager.getConnection
                (jdbcUrl, jdbcUsername, jdbcPassword);
            
            Payload payload = Payload.parseMultiPart
                ("tox21-file", request ());
            if (payload != null) {
                loadFile (con, payload);
            }
            else {
                payload = Payload.parseMultiPart("tox21-sdf", request ());
                if (payload != null) {
                    loadSDF (con, payload);
                }
                else { // just use the database
                    load (con);
                }
            }
            return redirect (routes.Tox21App.index());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.tox21.views.html.error.render
                 (500, "Internal server error: "+ex));
        }
        finally {
            if (con != null) {
                try { con.close(); }
                catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    static void loadSDF (Connection con, Payload payload) throws Exception {
        MolImporter mi = new MolImporter (PayloadFactory.getStream(payload));
        PreparedStatement pstm = con.prepareStatement
            ("select * from ncgc_sample where sample_id = ?");
        try {
            int count = 0;
            for (Molecule m = new Molecule (); mi.read(m); ) {
                String id = m.getProperty("ncgc_id");
                if (id == null) {
                    id = m.getProperty("PUBCHEM_EXT_DATASOURCE_REGID");
                }

                if (id == null) {
                    Logger.warn("Structure contains no valid id");
                }
                else {
                    pstm.setString(1, id);
                    if (load (payload.name, pstm.executeQuery()) > 0) {
                        ++count;
                    }
                }
            }
            Logger.debug(count+" samples processed!");      
        }
        finally {
            mi.close();
            pstm.close();
        }
    }

    static void loadFile (Connection con, Payload payload) throws Exception {
        PreparedStatement pstm = con.prepareStatement
            ("select * from ncgc_sample where sample_id = ?");
        BufferedReader br = new BufferedReader
            (new InputStreamReader (PayloadFactory.getStream(payload)));
        try {
            int count = 0;
            for (String line; (line = br.readLine()) != null; ) {
                pstm.setString(1, line.trim());
                if (load (payload.name, pstm.executeQuery()) > 0) {
                    ++count;
                }
            }
            Logger.debug(count+" samples processed!");      
        }
        finally {
            pstm.close();
            br.close();
        }
    }
    
    static void load (Connection con) throws Exception {
        Statement stm = con.createStatement();
        try {
            ResultSet rset = stm.executeQuery
                ("select * from ncgc_sample where tox21_t0 is not null");
            int count = load ("ncgc_sample", rset);
            Logger.debug(count+" samples processed!");
        }
        finally {
            stm.close();
        }
    }

    static int load (String source, ResultSet rset) throws SQLException {
        int count = 0;
        while (rset.next()) {
            String name = rset.getString("sample_name");
            String ncgc = rset.getString("sample_id");
            String cas = rset.getString("cas");
            String sid = rset.getString("tox21_sid");
            String smiles = rset.getString("smiles_iso");
            String struc = rset.getString("structure");
            String grade = rset.getString("tox21_t0");
            String toxid = rset.getString("tox21_id");
            String inchi = rset.getString("inchi_hash");
            
            Sample sample = new Sample (name);
            sample.synonyms.add(new Keyword (Sample.S_TOX21, toxid));
            sample.synonyms.add(new Keyword (Sample.S_SID, sid));
            sample.synonyms.add(new Keyword (Sample.S_CASRN, cas));
            sample.synonyms.add(new Keyword (Sample.S_NCGC, ncgc));
            if (inchi != null)
                sample.synonyms.add(new Keyword (Sample.S_InChIKey, inchi));
            
            if (smiles != null)
                sample.properties.add
                    (new Text (Sample.P_SMILES_ISO, smiles));
            
            if (struc != null)
                sample.properties.add(new Text (Sample.P_MOLFILE, struc));
            try {

                QCSample qc = new QCSample ();
                qc.sample = sample;
                if (grade != null) {
                    qc.grade = QCSample.Grade.valueOf
                        (QCSample.Grade.class, grade);
                }
                else {
                    qc.grade = QCSample.Grade.ND;
                }

                StructureReceiver receiver =
                    new Tox21StructureReceiver (source, qc);
                if (struc != null)
                    Processor.submit(struc, receiver);
                else if (smiles != null)
                    Processor.submit(smiles, receiver);
                else {
                    sample.save();
                    qc.save();
                }
                
                ++count;
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return count;
    }

    public static Result index () {
        return redirect (routes.Tox21App.samples(null, ROWS_PER_PAGE, 1));
    }

    public static Result samples (final String q,
                                  final int rows, final int page) {
        try {
            String sha1 = Util.sha1(request ());
            return getOrElse (sha1, new Callable<Result>() {
                    public Result call () throws Exception {
                        return _samples (q, rows, page);
                    }
                });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.tox21.views.html.error.render
                 (500, "Internal server error: "+ex));
        }
    }

    static Result _samples (final String q, int rows, final int page)
        throws Exception {
        Logger.debug("Samples: q="+q+" rows="+rows+" page="+page);
        
        final int total = Tox21Factory.finder.findRowCount();
        if (request().queryString().containsKey("facet") || q != null) {
            TextIndexer.SearchResult result =
                getSearchResult (QCSample.class, q, total);
            
            TextIndexer.Facet[] facets = filter (result.getFacets(), QC_FACETS);
            List<QCSample> samples = new ArrayList<QCSample>();
            int[] pages = new int[0];
            if (result.count() > 0) {
                rows = Math.min(result.count(), Math.max(1, rows));
                pages = paging (rows, page, result.count());
                
                for (int i = (page-1)*rows, j = 0; j < rows
                         && i < result.count(); ++j, ++i) {
                    samples.add((QCSample)result.getMatches().get(i));
                }
            }
            
            return ok (ix.tox21.views.html.samples.render
                       (page, rows, result.count(),
                        pages, decorate (facets), samples));
        }
        else {
            String cache = QCSample.class.getName()+".facets";
            TextIndexer.Facet[] facets = getOrElse
                (cache, new Callable<TextIndexer.Facet[]>() {
                        public TextIndexer.Facet[] call () {
                            return filter
                            (getFacets (QCSample.class, FACET_DIM), QC_FACETS);
                        }
                    });
            
            rows = Math.min(total, Math.max(1, rows));
            int[] pages = {};
            List<QCSample> samples = new ArrayList<QCSample>();
            if (rows > 0) {
                pages = paging (rows, page, total);
                samples = Tox21Factory.getQCSamples(rows, (page-1)*rows, null);
            }
            return ok (ix.tox21.views.html.samples.render
                       (page, rows, total, pages, decorate (facets), samples));
        }
    }

    static Result _sample (final String id) {
        List<QCSample> samples = Tox21Factory.finder.where()
            .eq("sample.synonyms.term", id).setMaxRows(1).findList();
        if (!samples.isEmpty()) {
            return ok(ix.tox21.views.html.sampledetails.render
                      (samples.iterator().next()));
        }
        return notFound (ix.tox21.views.html.error.render
                         (400, "Unknown sample: "+id));
    }
    
    public static Result sample (final String id) {
        try {
            String sha1 = Util.sha1(request ());
            return getOrElse (sha1, new Callable<Result> () {
                    public Result call () throws Exception {
                        return _sample (id);
                    }
                });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.tox21.views.html.error.render(400, "Invalid sample: "+id));
        }
    }

    public static Result search (String q, int rows, int page) {
        String type = request().getQueryString("type");
        if (q.equals("")) {
            q = null;
        }
        else if (type != null && (type.equalsIgnoreCase("substructure")
                                  || type.equalsIgnoreCase("similarity"))) {
            // structure search
            String cutoff = request().getQueryString("cutoff");
            Logger.debug("Search: q="+q+" type="+type+" cutoff="+cutoff);
            try {
                if (type.equalsIgnoreCase("substructure")) {
                    return substructure (q, rows, page);
                }
                else {
                    return similarity
                        (q, Double.parseDouble(cutoff), rows, page);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            
            return notFound (ix.tox21.views.html.error.render
                             (400, "Invalid search parameters: type=\""+type
                              +"\"; q=\""+q+"\" cutoff=\""+cutoff+"\"!"));
        }
        
        //return redirect (routes.Tox21App.samples(q, rows, page));
        return samples (q, rows, page);
    }

    protected static Result _render (Long id, final int size) throws Exception {
        Sample sample = Tox21Factory.getSample(id);
        if (sample != null) {
            String ncgc = sample.getSynonym(Sample.S_NCGC).term;
            return render (ncgc, size);
        }
        return null;
    }
    
    public static Result render (final Long id, final int size) {
        //Logger.debug("render "+id+"."+size);
        try {
            String key = "render::sample::"+id+"::"+size;
            Result result = getOrElse
                (key, new Callable<Result>() {
                        public Result call () throws Exception {
                            return _render (id, size);
                        }
                    });
            if (result == null) {
                // don't cache this..
                Cache.remove(key);
            }
            response().setContentType("image/svg+xml");
            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.tox21.views.html.error.render
                 (500, "Internal server error: "+ex));
        }
    }

    public static Result structureResult
        (TextIndexer indexer, int rows, int page) throws Exception {
        TextIndexer.SearchResult result = SearchFactory.search
            (indexer, QCSample.class, null, indexer.size(), 0, FACET_DIM,
             request().queryString());

        TextIndexer.Facet[] facets = filter (result.getFacets(), QC_FACETS);
        List<QCSample> samples = new ArrayList<QCSample>();
        int[] pages = new int[0];
        if (result.count() > 0) {
            rows = Math.min(result.count(), Math.max(1, rows));
            pages = paging (rows, page, result.count());
            
            for (int i = (page-1)*rows, j = 0; j < rows
                     && i < result.count(); ++j, ++i) {
                samples.add((QCSample)result.getMatches().get(i));
            }
        }
        
        return ok (ix.tox21.views.html.samples.render
                   (page, rows, result.count(),
                    pages, decorate (facets), samples));
    }

    static TextIndexer createIndexer (ResultEnumeration results)
        throws Exception {
        long start = System.currentTimeMillis();        
        TextIndexer indexer = textIndexer.createEmptyInstance();
        int count = 0;
        while (results.hasMoreElements()) {
            StructureIndexer.Result r = results.nextElement();
            /*
            Logger.debug(r.getId()+" "+r.getSource()+" "
                         +r.getMol().toFormat("smiles"));
            */
            List<QCSample> samples = Tox21Factory.finder
                .where().eq("sample.structure.id", r.getId()).findList();
            for (QCSample qc : samples) {
                indexer.add(qc);
            }
            ++count;
        }
        
        double ellapsed = (System.currentTimeMillis() - start)*1e-3;
        Logger.debug(String.format("Ellapsed %1$.3fs to retrieve "
                                   +"%2$d structures...",
                                   ellapsed, count));
        return indexer;
    }

    public static Result similarity (final String query,
                                     final double threshold,
                                     int rows, int page) {
        try {
            String key = "similarity::"+Util.sha1(query)
                +"::"+String.format("%1$d", (int)(1000*threshold+.5));
            TextIndexer indexer = getOrElse
                (strucIndexer.lastModified(),
                 key, new Callable<TextIndexer> () {
                         public TextIndexer call () throws Exception {
                            ResultEnumeration results =
                                 strucIndexer.similarity(query, threshold, 0);
                            return createIndexer (results);
                         }
                     });
            
            return structureResult (indexer, rows, page);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return internalServerError
            (ix.tox21.views.html.error.render
             (500, "Unable to perform similarity search: "+query));
    }
    
    public static Result substructure
        (final String query, int rows, int page) {
        Logger.debug("substructure: query="+query+" rows="+rows+" page="+page);
        try {
            String key = "substructure::"+Util.sha1(query);
            TextIndexer indexer = getOrElse
                (strucIndexer.lastModified(),
                 key, new Callable<TextIndexer> () {
                         public TextIndexer call () throws Exception {
                            ResultEnumeration results =
                                 strucIndexer.substructure(query, 0);
                            return createIndexer (results);
                         }
                     });
            
            return structureResult (indexer, rows, page);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return internalServerError
            (ix.tox21.views.html.error.render
             (500, "Unable to perform substructure search: "+query));
    }
}
