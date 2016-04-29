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
import ix.core.plugins.IxCache;

import tripod.chem.indexer.StructureIndexer;
import static tripod.chem.indexer.StructureIndexer.ResultEnumeration;

import ix.tox21.models.*;
import ix.qhts.models.Sample;
import ix.qhts.models.Assay;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.MolAtom;
import chemaxon.util.MolHandler;

public class Tox21App extends App {
    static final int ROWS_PER_PAGE = 10;
    
    static final PayloadPlugin Payload =
        Play.application().plugin(PayloadPlugin.class);

    static final StructureProcessorPlugin Processor =
        Play.application().plugin(StructureProcessorPlugin.class);

    static class Tox21SearchResultProcessor
        extends SearchResultProcessor<StructureIndexer.Result, QCSample> {
        int count;

        Tox21SearchResultProcessor () throws IOException {
        }

        @Override
        protected QCSample instrument (StructureIndexer.Result r)
            throws Exception {
            List<QCSample> samples = Tox21Factory.finder
                .where().eq("structure.id", r.getId()).findList();

            if (!samples.isEmpty()) {
                int[] amap = new int[r.getMol().getAtomCount()];
                int i = 0, nmaps = 0;
                for (MolAtom ma : r.getMol().getAtomArray()) {
                    amap[i] = ma.getAtomMap();
                    if (amap[i] > 0)
                        ++nmaps;
                    ++i;
                }
                
                if (nmaps > 0) {
                    IxCache.set("AtomMaps/"+getContext().getId()+"/"
                                +r.getId(), amap);
                }
                return samples.get(0);
            }
            
            return null;
        }
    }
    
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
                qc.structure = struc;
            }
            else {
                Logger.error(status+": "
                             +qc.getSynonym(Sample.S_NCGC).term
                             +": "+mesg);
            }
            
            try {
                qc.save();
                
                Logger.debug(status+": QCsample "
                             +qc.id+" sample "+qc.getName());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
        
    static final String[] QC_FACETS = new String[] {
        "QC Grade T0",
        "QC Grade T4",  
        "Molecular Weight",
        "Molecular Formula",    
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
                +grade.desc+"\">"
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
            else {
                int pos = label.indexOf(':');
                if (pos > 0) {
                    return label.substring(0,pos);
                }
            }
            return label;
        }
    }

    static FacetDecorator[] decorate (Facet... facets) {
        FacetDecorator[] decors = new FacetDecorator[facets.length];
        for (int i = 0; i < facets.length; ++i) {
            if (facets[i].getName().startsWith("QC")) {
                // use special decorator
                decors[i] = new Tox21GradeFacetDecorator (facets[i]);
            }
            else if (facets[i].getName().equals("Sample")) {
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
            for (Molecule m; (m = mi.read()) != null; ) {
                String id = m.getProperty("ncgc_id");
                if (id == null) {
                    id = m.getProperty("PUBCHEM_EXT_DATASOURCE_REGID");
                }

                if (id == null) {
                    Logger.warn("Structure contains no valid id");
                }
                else {
                    pstm.setString(1, id);
                    if (load (payload.name, m, pstm.executeQuery()) > 0) {
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
        return load (source, null, rset);
    }
    
    static int load (String source, Molecule struc, ResultSet rset)
        throws SQLException {
        int count = 0;
        while (rset.next()) {
            String name = rset.getString("sample_name");
            String ncgc = rset.getString("sample_id");
            String cas = rset.getString("cas");
            String sid = rset.getString("tox21_sid");
            String gradeT0 = rset.getString("tox21_t0");
            String gradeT4 = rset.getString("tox21_t4");
            String toxid = rset.getString("tox21_id");
            String inchi = rset.getString("inchi_hash");
            
            QCSample qc = new QCSample (name);
            qc.synonyms.add(new Keyword (Sample.S_TOX21, toxid));
            qc.synonyms.add(new Keyword (Sample.S_SID, sid));
            qc.synonyms.add(new Keyword (Sample.S_CASRN, cas));
            qc.synonyms.add(new Keyword (Sample.S_NCGC, ncgc));
            /*
            // this inchi is bogus.. it was generated not on the entire
            // structure.. only on the largest component
            if (inchi != null && (smiles != null || struc != null))
                qc.synonyms.add(new Keyword (Sample.S_InChIKey, inchi));
            */

            if (struc != null) {
                qc.properties.add
                    (new Text (Sample.P_MOLFILE, struc.toFormat("mol")));
                qc.properties.add
                    (new Text (Sample.P_SMILES_ISO,
                               struc.toFormat("smiles:-a")));
            }
            else {
                String mol = rset.getString("structure");
                if (mol != null) {
                    qc.properties.add(new Text (Sample.P_MOLFILE, mol));
                    try {
                        struc = new MolHandler (mol).getMolecule();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                
                String smiles = rset.getString("smiles_iso");
                if (smiles != null) {
                    qc.properties.add
                        (new Text (Sample.P_SMILES_ISO, smiles));
                    if (mol == null) {
                        try {
                            struc = new MolHandler (smiles).getMolecule();
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            try {
                if (gradeT0 != null) {
                    qc.gradeT0 = QCSample.Grade.valueOf
                        (QCSample.Grade.class, gradeT0);
                }
                else {
                    qc.gradeT0 = QCSample.Grade.ND;
                }

                if (gradeT4 != null) {
                    qc.gradeT4 = QCSample.Grade.valueOf
                        (QCSample.Grade.class, gradeT4);
                }
                else {
                    qc.gradeT4 = QCSample.Grade.ND;
                }

                StructureReceiver receiver =
                    new Tox21StructureReceiver (source, qc);
                if (struc != null)
                    Processor.submit(struc, receiver);
                else {
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
        //return redirect (routes.Tox21App.samples(null, ROWS_PER_PAGE, 1));
        return ok (ix.tox21.views.html.index.render
                   ("Tox21 Data Browser", Tox21Factory.getAssayCount(),
                    Tox21Factory.getSampleCount()));
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
                        pages, decorate (facets), samples, null));
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
                       (page, rows, total, pages, decorate (facets),
                        samples, null));
        }
    }

    static Result _assays (final String q, int rows, final int page)
        throws Exception {
        Logger.debug("Assays: q="+q+" rows="+rows+" page="+page);

        return ok (ix.tox21.views.html.assays.render
                   (page, rows, 0, new int[0],
                    decorate (new TextIndexer.Facet[0]),
                    new ArrayList<Assay>()));
    }
    
    public static Result assays (final String q,
                                 final int rows, final int page) {
        try {
            final String key = Util.sha1(request ());
            return getOrElse (key, new Callable<Result>() {
                    public Result call () throws Exception {
                        Logger.debug("Cache missed: "+key);
                        return _assays (q, rows, page);
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

    static Result _sample (final String id) {
        List<QCSample> samples = Tox21Factory.finder.where()
            .eq("synonyms.term", id).setMaxRows(1).findList();
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

    public static Result qc (final String id) {
        return redirect (routes.Tox21App.sample(id));
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

    protected static Result _render (Sample sample, final String ctx,
                                     final int size) throws Exception {
        Result r = null;
        if (sample != null) {
            MolHandler mh = new MolHandler (sample.structure.molfile);
            int[] amap = null;
            if (ctx != null) {
                amap = (int[])IxCache.get("AtomMaps/"
                                          +ctx+"/"+sample.structure.id);
            }
            r = ok (App.render(mh.getMolecule(), "svg", size, amap));
        }
        return r;
    }
    
    public static Result render (final String id,
                                 final String ctx, final int size) {
        Logger.debug("render "+id+" ctx="+ctx+" size="+size);
        try {
            final Sample sample =
                getOrElse ("samples::"+id, new Callable<Sample>() {
                    public Sample call () throws Exception {
                        List<QCSample> samples = Tox21Factory
                        .finder.where().eq("synonyms.term", id).findList();
                        return samples.isEmpty()
                        ? null : samples.iterator().next();
                    }
                });
            if (sample != null) {
                String key = "render::sample::"+sample.id+"::"+size;
                Result result = getOrElse
                    (key, new Callable<Result>() {
                            public Result call () throws Exception {
                                return _render (sample, ctx, size);
                            }
                        });
                if (result == null) {
                    // don't cache this..
                    IxCache.remove(key);
                }
                response().setContentType("image/svg+xml");
                return result;
            }
            
            return notFound (new byte[0]);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                (ix.tox21.views.html.error.render
                 (500, "Internal server error: "+ex));
        }
    }

    public static Result structureResult
        (final SearchResultContext context, int rows, int page)
        throws Exception {
        return App.fetchResult
            (context, rows, page, new DefaultResultRenderer<QCSample> () {
                    public Result render (SearchResultContext context,
                                          int page, int rows,
                                          int total, int[] pages,
                                          List<TextIndexer.Facet> facets,
                                          List<QCSample> samples) {
                        return ok (ix.tox21.views.html.samples.render
                                   (page, rows, total,
                                    pages, decorate
                                    (filter (facets, QC_FACETS)),
                                    samples, context.getId()));
                    }
                });
    }

    public static Result similarity (final String query,
                                     final double threshold,
                                     final int rows,
                                     final int page) {
        try {
            SearchResultContext context = similarity
                (query, threshold, rows, page,
                 new Tox21SearchResultProcessor ());
            if (context != null) {
                return structureResult (context, rows, page);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform similarity search", ex);
        }
        return internalServerError
            (ix.tox21.views.html.error.render
             (500, "Unable to perform similarity search: "+query));
    }
    
    public static Result substructure
        (final String query, final int rows, int page) {
        try {
            SearchResultContext context = substructure
                (query, rows, page, new Tox21SearchResultProcessor ());
            if (context != null) {
                return structureResult (context, rows, page);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("Can't perform substructure search", ex);
        }
        return internalServerError
            (ix.tox21.views.html.error.render
             (500, "Unable to perform substructure search: "+query));
    }

    static final String[] PDF_SUFFIXES = {
        "LCMS.pdf",
        "LCMS1.pdf",
        "LCMS3.pdf",
        "GCMS1.pdf"
    };
    static String PDF_ROOT =
        Play.application().configuration().getString("tox21.pdf");
    public static Result getSpectra (final String id, final int t) {
        try {
            File file = getSpectraFile (id, t);
            if (file != null) {
                return ok (file);
            }
            
            return notFound (ix.tox21.views.html.error.render
                             (400, "No spectra for sample "+id));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError (ex.getMessage());
        }
    }

    public static boolean hasSpectra (final String id, final int t) {
        return null != getSpectraFile (id, t);
    }

    public static File getSpectraFile (final String id, final int t) {
        try {
            final String key = "file::"+id+"::"+t;
            return getOrElse (key, new Callable<File> () {
                    public File call () throws Exception {
                        for (String s : PDF_SUFFIXES) {
                            File file = new File (PDF_ROOT, String.format
                                                  ("%1$s_0%2$d_%3$s",
                                                   id, t, s));
                            if (file.exists())
                                return file;
                        }
                        return null;
                    }
                });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getId (Sample sample) {
        return sample.getSynonym(Sample.S_TOX21).term;
    }
}
