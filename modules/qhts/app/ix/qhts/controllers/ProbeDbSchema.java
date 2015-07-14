package ix.qhts.controllers;

import java.util.*;
import java.sql.*;
import java.io.*;
import javax.sql.DataSource;

import play.Logger;

public class ProbeDbSchema {
    public static int PROBEDB_ASSAY = 0;
    public static int PROBEDB_PROTOCOL = 1;
    public static int PROBEDB_SAMPLETYPE = 2;

    public static final String[] FIELDS = {
        "ac50",
        "log_ac50",
        "hill_coef",
        "r2",
        "max_response",
        "inf_activity",
        "zero_activity",
        "curve_class",
        "mask_flags",
        "max_read1",
        "sample_conc_id",
        "supplier",
        "smiles_iso",
        "curve_class2",
        "sample_data_id",
        "supplier_id",
        "sample_name",
        "alias",
        "tox21_sid",
        "pubchem_sid",
        "data0",
        "data1",
        "data2",
        "data3",
        "data4",
        "data5",
        "data6",
        "data7",
        "data8",
        "data9",
        "data10",
        "data11",
        "data12",
        "data13",
        "data14",
        "data15",
        "data16",
        "data17",
        "data18",
        "data19",
        "data20",
        "data21",
        "data22",
        "data23",
        "data24"
    };

    public static class ConcBin {
        static final double LN10 = 2.30258509299404568401;
        
        private double minconc;
        private double maxconc;
        private int nbins;
        private double range;
        private double bsize;
        private int[] counts;
        private double[] accums;
        
        private Map<String, Number[]> mapvals
            = new HashMap<String, Number[]>();
        
        public ConcBin (double minconc, double maxconc) {
            this (25, minconc, maxconc);
        }
        
        public ConcBin (int nbins, double minconc, double maxconc) {
            this.minconc = Math.log10(minconc);
            this.maxconc = Math.log10(maxconc);
            this.nbins = nbins;
            range = this.maxconc - this.minconc;
            bsize = range / nbins;
            
            if (bsize < 0.05) {
                bsize = 0.05;
                this.nbins = (int)(range / bsize + 0.5);
                Logger.warn("** warning: conc bin size is too small (" 
                            + bsize + "); readjusting # bins to "
                            + this.nbins);
            }
            
            counts = new int[this.nbins+1];
            accums = new double[this.nbins+1];
        }

        public ConcBin (double bsize, double minconc, double maxconc) {
            this.minconc = Math.log10(minconc);
            this.maxconc = Math.log10(maxconc);
            this.bsize = bsize;
            range = this.maxconc - this.minconc;
            this.nbins = (int)(range / bsize + 0.5);
            counts = new int[nbins+1];
            accums = new double[nbins+1];
        }
        
        public int getBin (double v) {
            double lv = Math.log10(v);
            return (int)((lv - minconc)/bsize);
        }
        
        public void addConcSeries (String id, Number[] vals) {
            mapvals.put(id, vals);
        }
        
        public int[] getConcSeries (String id) {
            Number[] vals = mapvals.get(id);
            int[] bins = null;
            if (vals != null) {
                BitSet bs = new BitSet ();
                for (Number v : vals) {
                    if (v != null) {
                    bs.set(getBin (v.doubleValue()));
                    }
                }
                
                bins = new int[bs.cardinality()];
                for (int i = bs.nextSetBit(0), j = 0; i >= 0;
                     i = bs.nextSetBit(i+1)) {
                    bins[j++] = i;
                }
            }
            return bins;
        }
        
        public Number[] getConcValues (String id) {
            return mapvals.get(id);
        }
        
        public String[] getConcSeries () {
            return mapvals.keySet().toArray(new String[0]);
        }
        
        public void accum (double v) {
            double lv = Math.log10(v);
            int b = (int)((lv - minconc)/bsize);
            ++counts[b];
            accums[b] += lv;
        }
        
        public boolean isEmpty (int bin) {
            return counts[bin] == 0;
        }
        
        public double getValue (int bin) {
            /*
              if (isEmpty (bin)) {
              return getBindMid (bin);
              }
            */
            return Math.exp((accums[bin] / counts[bin]) * LN10);
        }
        public int getCount (int bin) { return counts[bin]; }
        
        public double getBinMin (int b) {
            return Math.exp((minconc+b*bsize)*LN10);
        }
        public double getBinMax (int b) {
            return Math.exp((minconc+(b+1)*bsize)*LN10);
        }
        public double getBinMid (int b) {
            return Math.exp((minconc+(b+.5)*bsize)*LN10);
        }
        
        public int size () { return nbins+1; }
        public double getMin () { return Math.exp(minconc*LN10); }
        public double getMax () { return Math.exp(maxconc*LN10); }
    } // ConcBin

    private DataSource htsDS; // hts
    private DataSource regDS; // registry

    public ProbeDbSchema (DataSource htsDS, DataSource regDS) {
        this.htsDS = htsDS;
        this.regDS = regDS;
    }

    public String[][] getAllAssays () throws Exception {
        Connection con = getConnection ();
        try {
            String sql = "select b.assay_name, "
                +"b.protocol_name, "
                +"a.sample_data_type, "
                +"count(1) "
                +"from sample_data a, assay_protocol b "
                +"where b.assay_protocol_id = a.assay_protocol_id "
                +"and b.assay_name is not null "
                +"and b.protocol_name is not null "
                +"and a.sample_data_type is not null "
                +"group by b.assay_name, b.protocol_name, a.sample_data_type "
                +"order by b.assay_name, b.protocol_name";

            List<String[]> rows = new ArrayList<String[]>();
            Statement stm = con.createStatement();
            ResultSet rset = stm.executeQuery(sql);
            while (rset.next()) {
                String[] row = new String[4];
                row[0] = rset.getString(1);
                row[1] = rset.getString(2);
                row[2] = rset.getString(3);
                row[3] = rset.getString(4);
                rows.add(row);
            }
            rset.close();
            stm.close();
            
            return rows.toArray(new String[0][]);
        }
        finally {
            con.close();
        }
    }

    public ConcBin[] getConc (String[][] assays) throws SQLException {
        return getConc (assays, 25, null);
    }
    
    public ConcBin[] getConc (String[][] assays, int nbins)
        throws SQLException {
        return getConc (assays, nbins, null);
    }

    public ConcBin[] getConc (String[][] assays, int nbins,
                              Map<String, int[]> merged/*out*/) 
        throws SQLException {

        Connection con = getConnection ();
        try {
            String sql0 = "select ";
            for (int i = 0; i < 25; ++i) {
                sql0 += "a.data"+i + ",";
            }
            sql0 += " sample_id "
                +"from sample_layer a, assay_protocol b "
                +"where b.assay_name = ? "
                +"and b.protocol_name = ? "
                +"and a.sample_data_type = ? "
                +"and a.assay_protocol_id = b.assay_protocol_id "
                +"and sample_conc_id is null";
            
            String sql1 = "select ";
            for (int i = 0; i < 25; ++i) {
                sql1 += "a.data"+i + ",";
            }
            sql1 += "sample_id from sample_layer a where sample_conc_id = ?";
            
            if (merged == null) {
                merged = new HashMap<String, int[]>();
            }
            
            {
                Set<String> uniq = new HashSet<String>();
                for (int i = 0; i < assays.length; ++i) {
                    // the third column is assumed to specify if any assays
                    // are to be merged
                    if (assays[i].length > 3) { 
                        uniq.add(assays[i][3]);
                    }
                    else {
                        uniq.add(null);
                    }
                }
                
                String classes[] = uniq.toArray(new String[0]);
                for (int i = 0; i < classes.length; ++i) {
                    BitSet bs = new BitSet ();
                    for (int j = 0; j < assays.length; ++j) {
                        if (assays[j].length > 3) {
                            if (classes[i] == assays[j][3] 
                                || classes[i].equals(assays[j][3])) {
                                bs.set(j);
                            }
                        }
                        else if (classes[i] == null) {
                            bs.set(j);
                        }
                    }
                    int[] set = new int[bs.cardinality()];
                    for (int j = bs.nextSetBit(0), k = 0; j >= 0;
                         j = bs.nextSetBit(j+1)) {
                        set[k++] = j;
                    }
                    merged.put(classes[i], set);
                }
            }
            ConcBin[] results = new ConcBin[assays.length];
            
            String varConcSql = "SELECT sample_conc_id "
                +"FROM sample_layer a,"
                +"  assay_protocol b "
                +"WHERE b.assay_name = ? "
                +" AND b.protocol_name = ? "
                +" AND a.sample_data_type = ? "
                +" AND a.assay_protocol_id = b.assay_protocol_id"
                +" AND sample_conc_id is not null";
            
            PreparedStatement varstm = con.prepareStatement(varConcSql);
            PreparedStatement pstm0 = con.prepareStatement(sql0);
            PreparedStatement pstm1 = con.prepareStatement(sql1);
            
            for (Map.Entry<String, int[]> e : merged.entrySet()) {
                //System.out.println("## processing class " + e.getKey());
                int[] eqv = e.getValue();
                
                double minconc = Double.MAX_VALUE;
                double maxconc = -1.;
                List<Double> concvals = new ArrayList<Double>();
                Map<String, Number[]> concmap = new HashMap<String, Number[]>();
                
                for (int j = 0; j < eqv.length; ++j) {
                    int i = eqv[j];
                    
                    Logger.debug
                        ("** getting concentration values for " 
                         + assays[i][0] + "/" + assays[i][1] + "/"
                         + assays[i][2]);
                    
                    // do variable concentration first
                    varstm.setString(1, assays[i][0]);
                    varstm.setString(2, assays[i][1]);
                    varstm.setString(3, assays[i][2]);
                    
                    ResultSet rset = varstm.executeQuery();
                    while (rset.next()) {
                        long concID = rset.getLong(1);
                        pstm1.setLong(1, concID);
                        
                        ResultSet rs0 = pstm1.executeQuery();
                        if (rs0.next()) {
                            Number[] conc = new Number[25];
                            for (int c = 0; c < 25; ++c) {
                                double v = rs0.getDouble(c+1);
                                if (!rs0.wasNull()) {
                                    if (v < minconc) {
                                        minconc = v;
                                    }
                                    if (v > maxconc) {
                                        maxconc = v;
                                    }
                                    concvals.add(v);
                                    conc[c] = v;
                                }
                            }
                            concmap.put(String.valueOf(concID), conc);
                            //System.out.println("conc_id=" + concID);
                        }
                        rs0.close();
                    }
                    rset.close();
                    
                    pstm0.setString(1, assays[i][0]);
                    pstm0.setString(2, assays[i][1]);
                    pstm0.setString(3, assays[i][2]);
                    rset = pstm0.executeQuery();
                    
                    while (rset.next()) {
                        Number[] conc = new Number[25];
                        for (int c = 0; c < 25; ++c) {
                            double v = rset.getDouble(c+1);
                            if (!rset.wasNull()) {
                                if (v < minconc) {
                                    minconc = v;
                                }
                                if (v > maxconc) {
                                    maxconc = v;
                                }
                                concvals.add(v);
                                conc[c] = v;
                            }
                        }
                        String sampleID = rset.getString(26);
                        concmap.put(sampleID, conc);
                    }
                    rset.close();
                }
                
                ConcBin cb = new ConcBin (nbins, minconc, maxconc);
                for (Double v : concvals) {
                    cb.accum(v);
                }
                
                for (Map.Entry<String, Number[]> ee : concmap.entrySet()) {
                    cb.addConcSeries(ee.getKey(), ee.getValue());
                }
                
                for (int j = 0; j < eqv.length; ++j) {
                    results[eqv[j]] = cb;
                }
            }
            
            varstm.close();
            pstm0.close();
            pstm1.close();
            
            return results;
        }
        finally {
            con.close();
        }
    }

    public interface RowObserver {
        public void observeRow (Map<String, Object> row);
    }

    public int getAssayActivities (RowObserver observer, String... args) 
        throws SQLException {
        Connection con = getConnection ();
        try {
            StringBuilder sql = new StringBuilder ("SELECT b.sample_id");
            for (int i = 0; i < FIELDS.length; ++i) {
                sql.append("," + FIELDS[i] + "\n");
            }
            sql.append(",h1 as lychi_h1\n"
                       +",h2 as lychi_h2\n"
                       +",h3 as lychi_h3\n"
                       +",h4 as lychi_h4\n"
                       +",std_smiles \n"
                       +",structure as molfile\n");
            sql.append("FROM registry.ncgc_sample a,"
                       +"  sample_data b,"
                       +"  assay_protocol c, "
                       +"  registry.ncgc_sample_lychi d\n"
                       +"WHERE b.sample_id = a.sample_id(+)"
                       +" AND b.sample_id = d.sample_id(+)"
                       //+" /*AND pubchem_sid is not null*/"
                       +" AND ref_sample_data_id is null "
                       +" AND b.assay_protocol_id = c.assay_protocol_id");
            
            if (args.length > 2) {
                sql.append(" AND c.assay_name = ?"
                           +" AND c.protocol_name = ?"
                           +" AND b.sample_data_type = ?");
                if (args.length > 3 && args[3] != null
                    && args[3].length() > 0) {
                    sql.append(" AND " + args[3]);
                }
            }
            else if (args.length > 1) {
                sql.append(" AND c.assay_name = ?"
                           +" AND c.protocol_name = ?");
            }
            else if (args.length > 0) {
                sql.append(" AND c.assay_name = ?");
            }
            else {
            }
            
            sql.append("\nORDER BY " //pubchem_sid, "
                       +" b.sample_id, "
                       +" ABS(curve_class2), "
                       +" ABS(curve_class), "
                       +" ac50, "
                       +" -ABS(max_response)");
                       
            Logger.debug("Query: " + sql);
            PreparedStatement pstm = con.prepareStatement(sql.toString());
            
            for (int i = 0; i < Math.min(args.length, 3); ++i) {
                pstm.setString(i+1, args[i]);
            }
            
            Map<String, Object> row = new TreeMap<String, Object>();
            
            ResultSet rset = pstm.executeQuery();
            ResultSetMetaData meta = rset.getMetaData();
            int count = 0;
            for (; rset.next(); ++count) {
                int ncols = meta.getColumnCount();
                for (int c = 1; c <= ncols; ++c) {
                    String label = meta.getColumnLabel(c);
                    Object obj = rset.getObject(c);
                    if (obj != null) {
                        row.put(label, obj);
                    }
                }
                observer.observeRow(row);
                row.clear();
            }
            rset.close();
            pstm.close();
            
            return count;
        }
        finally {
            con.close();
        }
    }

    public Map<String, double[][]> getSpectraForSample (String sample) 
        throws SQLException {
        Connection con = getConnection ();
        try {
            String sql1 = "select protocol_name,count(1) from spectra_data "
                + "where sample_id = ? group by protocol_name";
            
            String sql2 = "select protocol_name,cmpd_conc,dye_conc "
                + "from spectra_data where sample_id = ? "
                + "order by protocol_name,cmpd_conc";
            PreparedStatement pstm1 = con.prepareStatement(sql1);
            PreparedStatement pstm2 = con.prepareStatement(sql2);
            pstm1.setString(1, sample);
            
            Map<String, double[][]> spectra = new TreeMap<String, double[][]>();
            ResultSet rset = pstm1.executeQuery();
            while (rset.next()) {
                String name = rset.getString(1);
                int count = rset.getInt(2);
                spectra.put(name, new double[2][count]);
            }
            rset.close();
            pstm1.close();
            
            if (!spectra.isEmpty()) {
                pstm2.setString(1, sample);
                rset = pstm2.executeQuery();
                String prev = "";
                double[][] spec = null;
                for (int cnt = 0; rset.next(); ++cnt) {
                    String name = rset.getString(1);
                    if (!name.equals(prev)) {
                        spec = spectra.get(name);
                        cnt = 0;
                    }
                    spec[0][cnt] = rset.getDouble(2); // compound conc
                    spec[1][cnt] = rset.getDouble(3); // dye conc
                    prev = name;
                }
                rset.close();
            }
            pstm2.close();
            
            return spectra;
        }
        finally {
            con.close();
        }
    }


    public String[] structureSearching 
        (String query, String[][]assays) throws SQLException {
        return structureSearching (query, assays, null);
    }

    public String[] structureSearching 
        (String query, String[][]assays, String[] sampls) 
        throws SQLException {
        Connection con = getRegistryConnection ();
        try {
            String sql = "select a.sample_id from ncgc_sample a where "
                + "jchem.jc_compare(smiles_iso, ?, 't:s tautomer:y') = 1";
            if (assays != null) {
                sql = "select a.sample_id from ncgc_sample a, hts.sample_data b, "
                    +"hts.assay_protocol c where a.sample_id = b.sample_id "
                    +"and b.assay_protocol_id = c.assay_protocol_id "
                    +"and jchem.jc_compare(smiles_iso, ?, 't:s tautomer:y') = 1 "
                    +"and c.protocol_name in ('"+assays[0][1]+"'";
                for (int i = 1; i < assays.length; ++i) {
                    sql += ",'" + assays[i][1] + "'";
                }
                sql += ")";
            }
            
            if (sampls != null && sampls.length > 0) {
                StringBuffer s = new StringBuffer ("('" + sampls[0] + "'");
                for (int i = 1; i < sampls.length; ++i) {
                    s.append(",'" + sampls[i] + "'");
                }
                sql += " and a.sample_id in " + s + ")";
            }
            //System.out.println(sql);
            
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setString(1, query);
            Set<String> samples = new TreeSet<String>();
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                samples.add(rs.getString(1));
            }
            rs.close();
            pstm.close();
            
            return samples.toArray(new String[0]);
        }
        finally {
            con.close();
        }
    }

    private Connection getConnection () throws SQLException {
        if (htsDS == null)
            throw new IllegalStateException ("HTS data source is not set!");
        return htsDS.getConnection();
    }

    private Connection getRegistryConnection () throws SQLException {
        if (regDS == null)
            throw new IllegalStateException
                ("Registry data source is not set!");
        return regDS.getConnection();
    }

    static String toCSV (String[] values) {
        StringBuffer buf = new StringBuffer ();
        buf.append(values[0]);
        for (int i = 1; i < values.length; ++i) {
            buf.append("," + values[i]);
        }
        return buf.toString();
    }

    static void testQueryConc (DataSource htsDS, String argv[])
        throws Exception {
        
        String[][] channels = new String[][] {
            {"tau2-tht", "tau2-p1-tht", "set2"/*, "foobar"*/},
            {"tau2-tht", "tau2-p1-tht", "set1"/*, "foobar"*/}
        };

        ProbeDbSchema db = new ProbeDbSchema (htsDS, null);
        ConcBin[] conc = db.getConc(channels);
        for (int i = 0; i < conc.length; ++i) {
            System.out.println("** conc " + i + " " + conc[i].size());
            for (int j = 0; j < conc[i].size(); ++j) {
                if (!conc[i].isEmpty(j)) {
                    System.out.println(j + ": " + conc[i].getValue(j) + " " 
                                       + conc[i].getCount(j));
                }
            }
            String[] bins = conc[i].getConcSeries();
            for (String b : bins) {
                System.out.print(b + ":");
                int[] bv = conc[i].getConcSeries(b);
                for (int j = 0; j < bv.length; ++j) {
                    System.out.print(" " + bv[j]);
                }
                System.out.println();
            }
            System.out.println();
        }
    }
}
