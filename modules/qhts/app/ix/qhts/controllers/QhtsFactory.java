package ix.qhts.controllers;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.sql.DataSource;

import play.*;
import play.db.ebean.*;
import play.db.DB;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.Expr;

import ix.core.chem.StructureProcessor;
import ix.core.models.Keyword;
import ix.core.models.Text;
import ix.core.models.Value;
import ix.core.models.Structure;
import ix.qhts.models.*;
import ix.core.controllers.KeywordFactory;
import ix.core.controllers.EntityFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QhtsFactory extends Controller {

    static class ActivityAggregator
        implements ProbeDbSchema.RowObserver {
        List<Activity> activities = new ArrayList<Activity>();
        int count;
        
        ActivityAggregator () {}

        // RowObserver
        public void observeRow (Map<String, Object> row) {
            //Logger.debug(count+": "+row);
            Sample sample = SampleFactory.registerIfAbsent(parseSample (row));
            Logger.debug("Sample "+sample.id+": "+sample.getName());
        }

        public List<Activity> getActivities () { return activities; }
    }

    static String addIfNotNull (List<Keyword> keywords,
                                String label, Object value) {
        String key = null;
        if (value != null) {
            keywords.add(new Keyword (label, key = value.toString()));
        }
        return key;
    }

    static Structure parseStructure (Map<String, Object> row) {
        Structure struc = null;
        
        Object value = row.get("MOLFILE");
        if (value != null) {
            struc = StructureProcessor.instrument(toString ((Clob)value));
        }
        else {
            value = row.get("SMILES_ISO");
            if (value != null) {
                struc = StructureProcessor.instrument(value.toString());
            }
        }
        return struc;
    }

    static Sample parseSample (Map<String, Object> row) {
        return instrument (new Sample (), row);
    }

    static Sample instrument (Sample sample, Map<String, Object> row) {
        sample.structure = parseStructure (row);
        // inherite all hashkeys from structure
        for (Value val : sample.structure.properties) {
            if (val.label.startsWith("LyChI_L")
                || val.label.equals(Structure.H_InChI_Key)) {
                sample.synonyms.add((Keyword)val);
            }
        }
        
        String id = addIfNotNull
            (sample.synonyms, Sample.S_NCGC_BATCH, row.get("SAMPLE_ID"));
        if (id != null) {
            int pos = id.indexOf('-');
            if (pos > 0) {
                addIfNotNull (sample.synonyms,
                              Sample.S_NCGC, id.substring(0, pos));
            }
        }
        
        if (null == addIfNotNull (sample.synonyms,
                                  Sample.S_SID, row.get("TOX21_SID"))) {
            addIfNotNull (sample.synonyms,
                          Sample.S_SID, row.get("PUBCHEM_SID"));
        }

        String name = addIfNotNull
            (sample.synonyms, Sample.S_SYN, row.get("SAMPLE_NAME"));
        if (name != null)
            sample.name = name;
        
        Object value = row.get("ALIAS");
        if (value != null) {
            String[] aliases = value.toString().split(";");
            for (int i = 0; i < aliases.length; ++i) {
                String syn = aliases[i].trim();
                if (syn.length() > 0) {
                    if (sample.name == null)
                        sample.name = syn;
                    sample.synonyms.add(new Keyword (Sample.S_SYN, syn));
                }
            }
        }

        value = row.get("SUPPLIER");
        if (value != null) {
            String s = (String)row.get("SUPPLIER_ID");
            Keyword supplier = KeywordFactory.registerIfAbsent
                (Sample.P_SUPPLIER, value.toString(), s);
            sample.properties.add(supplier);
        }

        if (sample.name == null)
            sample.name = id;
        
        return sample;
    }

    static String toString (Clob clob) {
        try {
            StringBuilder sb = new StringBuilder ();        
            Reader reader = clob.getCharacterStream();
            char[] buf = new char[1024];
            for (int nb; (nb = reader.read(buf)) != -1;) {
                sb.append(buf, 0, nb);
            }
            reader.close();
            
            return sb.toString();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    static void instrument (Activity act, Map<String, Object> row) {
        
    }
    
    public static String formatConc (Number n) {
        double v = n.doubleValue();
        String u;
        
        /*
        if (v <= 5e-7) {
            v *= 1e9;
            u = "nM";
        }
        else if (v <= 1e-3) {
            v *= 1e6;
            u = "uM";
        }
        else  {
            v *= 1e3;
            u = "mM";
        }
        */
        
        v *= 1e6;
        u = "uM";
          
        String fmt;
        if (v < 0.001) {
            fmt = "%1$.10f %2$s";
        }
        else if (v < .01) {
            fmt = "%1$.5f %2$s";
        }
        else if (v < 10.) {
            fmt = "%1$.3f %2$s";
        }
        else if (v < 100f) {
            fmt = "%1$.2f %2$s";
        }
        else /*if (v < 1000.)*/ {
            fmt = "%1$.1f %2$s";
        }
        return String.format(fmt, v, u);
    }
    
    public static Result test1 () {
        Model.Finder<Long, Curve> curveFinder =
            new Model.Finder(Long.class, Curve.class);

        double[] conc = new double[10];
        double[] resp = new double[conc.length];
        
        Random rand = new Random ();
        conc[0] = Double.MAX_VALUE;
        resp[0] = Double.MIN_VALUE;
        conc[1] = 0.;
        resp[1] = 1.0;
        for (int i = 2; i < conc.length; ++i) {
            conc[i] = rand.nextDouble();
            resp[i] = rand.nextDouble();
        }

        Curve crc = null;
        EntityFactory.EntityMapper mapper = new EntityFactory.EntityMapper();
        try {
            crc = new Curve ();
            crc.conc = new Data (Data.Unit.m, conc);
            crc.response = new Data (Data.Unit.percent, resp);
            crc.save();
            
            Logger.debug("Curve "+crc.id+" saved!");
            Logger.debug(mapper.toJson(crc, true));
            // now read them back in
            crc = curveFinder.byId(crc.id);
        }
        catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }

        return ok (mapper.valueToTree(crc));
    }

    public static Result test2 () {
        Connection con = null;
        try {
            DataSource hts = DB.getDataSource("hts");
            con = hts.getConnection();
            return ok ("successfuly connect to HTS database!");
        }
        catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static Result test3 () {
        try {
            DataSource hts = DB.getDataSource("hts");
            DataSource registry = DB.getDataSource("registry");
            
            ProbeDbSchema db = new ProbeDbSchema (hts, registry);
            String[][] channels = new String[][] {
                //{"tau2-tht", "tau2-p1-tht", "set2"/*, "foobar"*/},
                {"tau2-tht", "tau2-p1-tht", "set1"/*, "foobar"*/},
                {"15hLO2", "15hLO2-f1", "ratio"}
            };
            ProbeDbSchema.ConcBin[] conc = db.getConc(channels);
            
            ObjectMapper mapper = new ObjectMapper ();
            ArrayNode nodes = mapper.createArrayNode();
            for (int i = 0; i < channels.length; ++i) {
                Logger.debug("## retrieving data for "+channels[i][0]+"/"
                             +channels[i][1]+"/"+channels[i][2]);
                db.getAssayActivities(new ActivityAggregator (), channels[i]);
                
                ObjectNode node = mapper.createObjectNode();
                node.put("assay", channels[i][0]);
                node.put("protocol", channels[i][1]);
                node.put("sampleType", channels[i][2]);
                ArrayNode cn = mapper.createArrayNode();
                for (int j = 0; j < conc[i].size(); ++j) {
                    if (!conc[i].isEmpty(j)) {
                        ObjectNode n = mapper.createObjectNode();
                        n.put("conc", formatConc (conc[i].getValue(j)));
                        n.put("count", conc[i].getCount(i));
                        cn.add(n);
                    }
                }
                node.put("conc", cn);
                nodes.add(node);
            }
            
            return ok (nodes);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError (ex.getMessage());
        }
    }
}
