package ix.idg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ix.core.plugins.IxCache;
import ix.core.search.TextIndexer;
import ix.idg.models.HarmonogramCDF;
import ix.idg.models.Target;
import ix.ncats.controllers.App;
import ix.utils.Util;
import play.Logger;
import play.mvc.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.Callable;

public class HarmonogramApp extends App {

    static ObjectMapper mapper = new ObjectMapper();

    public static Result error(int code, String mesg) {
        return ok(ix.idg.views.html.error.render(code, mesg));
    }

    public static Result _notFound(String mesg) {
        return notFound(ix.idg.views.html.error.render(404, mesg));
    }

    public static Result _badRequest(String mesg) {
        return badRequest(ix.idg.views.html.error.render(400, mesg));
    }

    public static Result _internalServerError(Throwable t) {
        t.printStackTrace();
        return internalServerError
                (ix.idg.views.html.error.render
                        (500, "Internal server error: " + t.getMessage()));
    }


    public static Result view(String q, final String ctx) {
        if (q == null && ctx == null)
            return _badRequest("Must specify a comma separated list of Uniprot ID's or a target search context");
        String[] accs = new String[0];
        if (q != null) accs = q.split(",");
        // if cache key is specified, this takes precedence over query string
        if (ctx != null) {
            try {
                TextIndexer.SearchResult result =
                        getOrElse(ctx, new Callable<TextIndexer.SearchResult>() {
                            public TextIndexer.SearchResult call() throws Exception {
                                return null;
                            }
                        });
                if (result == null)
                    return _notFound("No cache entry for key: " + ctx);
                List matches = result.getMatches();
                List<String> sq = new ArrayList<>();
                for (Object o : matches) {
                    if (o instanceof Target) sq.add(IDGApp.getId((Target) o));
                }
                accs = sq.toArray(new String[]{});
                Logger.debug("Got " + accs.length
                        + " targets from cache using key: " + ctx);
            } catch (Exception e) {
                return _internalServerError(e);
            }
        }
        return (ok(ix.idg.views.html.harmonogram.render(accs, ctx)));
    }

    public static Result hgForTarget(String q,
                                     final String ctx,
                                     final String format,
                                     final String type) {
        if (ctx != null) {
            TextIndexer.SearchResult result =
                    (TextIndexer.SearchResult) IxCache.get(ctx);
            if (result != null) {
                StringBuilder sb = new StringBuilder();
                for (Object obj : result.getMatches()) {
                    if (obj instanceof Target) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(IDGApp.getId((Target) obj));
                    }
                }

                // override whatever specified in q
                q = sb.toString();
            }
        }

        return _handleHgRequest(q, format, type);
    }

    static Result _handleHgRequest(final String q, final String format, final String type) {
        if (q == null) return _badRequest("Must specify one or more targets via the q query parameter");
        try {
            final String key = "hg/" + q + "/" + format + "/" + Util.sha1(request());
            return getOrElse(key, new Callable<Result>() {
                public Result call() throws Exception {
                    if (type != null && type.toLowerCase().contains("radar"))
                        return _hgForRadar(q, type);
                    if (q.contains(",")) return _hgForTargets(q.split(","), format);
                    return _hgForTargets(new String[]{q}, format);
                }
            });
        } catch (Exception e) {
            return _internalServerError(e);
        }
    }

    static String _hgmapToTsv(Map<String, Map<String, HarmonogramCDF>> map, String[] header) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sym");
        for (String aHeader : header) sb.append("\t").append(aHeader);
        sb.append("\n");

        for (String asym : map.keySet()) {
            sb.append(asym).append("\t").append(_hgmapToTsvRow(map.get(asym), header));
            sb.append("\n");
        }
        return sb.toString();
    }

    static String _hgmapToTsvRow(Map<String, HarmonogramCDF> map, String[] keys) {
        StringBuilder sb = new StringBuilder();
        String delimiter = "";
        for (String akey : keys) {
            sb.append(delimiter).append(map.get(akey).getCdf());
            delimiter = "\t";
        }
        return sb.toString();
    }

    static int getKeyIndex(Set<String> keys, String key) {
        int i = 0;
        for (String k : keys) {
            if (k.equals(key)) return i;
            i++;
        }
        return -1;
    }

    static ArrayNode arrayToArrayNode(Integer[] a) {
        ArrayNode node = mapper.createArrayNode();
        for (Integer elem : a) node.add(elem);
        return node;
    }

    public static Result dataSources(String field, final String value) throws Exception {
        if (field == null || field.trim().equals("") || value == null || value.trim().equals(""))
            return _badRequest("Must specify a field name and value");

        final String fieldName = field.split("-")[1];
        final String key = "hg/ds/" + fieldName + "/" + value;
        Set<String> ds = getOrElse(key, new Callable<Set<String>>() {
            public Set<String> call() throws Exception {
                List<HarmonogramCDF> cdfs = HarmonogramFactory.finder.select("dataSource").where().eq(fieldName, value).findList();
                Set<String> ds = new HashSet<>();
                for (HarmonogramCDF cdf : cdfs)
                    ds.add(cdf.getDataSource());
                return ds;
            }
        });
        ObjectNode root = mapper.createObjectNode();
        root.put("fieldName", fieldName);
        root.put("value", value);
        ArrayNode arr = mapper.createArrayNode();
        for (String s : ds) arr.add(s);
        root.put("ds", arr);
        return ok(root);
    }

    // only valid for single target
    public static Result _hgForRadar(String q, String type) throws Exception {
        if (q == null || q.contains(",") || !type.contains("-"))
            return _badRequest("Must specify a single Uniprot ID and/or type must be of the form radar-XXX");

        final String fieldName = type.split("-")[1];

        // Lets get unique list of field values
        final String key = "hg/radar/distinct-" + fieldName;
        List<String> fieldValues = getOrElse(key, new Callable<List<String>>() {
            public List<String> call() throws Exception {
                List<String> fieldValues = new ArrayList<>();
                Connection conn = play.db.DB.getConnection();
                PreparedStatement pst = conn.prepareStatement("select distinct " + fieldName + " from ix_idg_harmonogram");
                ResultSet rset = pst.executeQuery();
                while (rset.next()) fieldValues.add(rset.getString(1));
                rset.close();
                pst.close();
                conn.close();
                return fieldValues;
            }
        });

        List<HarmonogramCDF> hgs = HarmonogramFactory.finder
                .where().in("uniprotId", q).findList();
        if (hgs.isEmpty()) {
            return _notFound("No harmonogram data found for " + q);
        }

        HashMap<String, Double> attrMap = new HashMap<>();
        for (HarmonogramCDF hg : hgs) {
            String attrGroup = hg.getAttrGroup();
            if (fieldName.equals("attr_type"))
                attrGroup = hg.getAttrType();
            else if (fieldName.equals("data_type"))
                attrGroup = hg.getDataType();
            else if (fieldName.equals("attr_group"))
                attrGroup = hg.getAttrGroup();
            Double value;
            if (attrMap.containsKey(attrGroup)) value = attrMap.get(attrGroup) + hg.getCdf();
            else value = hg.getCdf();
            attrMap.put(attrGroup, value);
        }

        ArrayNode anode = mapper.createArrayNode();
        for (String axis : fieldValues) {
            ObjectNode onode = mapper.createObjectNode();
            onode.put("axis", axis);
            if (attrMap.containsKey(axis))
                onode.put("value", attrMap.get(axis));
            else
                onode.put("value", 0.0);
            anode.add(onode);
        }
        ObjectNode container = mapper.createObjectNode();
        container.put("className", q);
        container.put("axes", anode);
        ArrayNode root = mapper.createArrayNode();
        root.add(container);
        return ok(root);
    }

    public static Result _hgForTargets(String[] accs, String format) throws Exception {
        List<HarmonogramCDF> hg = HarmonogramFactory.finder
                .where().in("uniprotId", Arrays.asList(accs)).findList();
        if (hg.isEmpty()) {
            return _notFound("No harmonogram data found for targets");
        }

        Map<String, Map<String, HarmonogramCDF>> allValues = new TreeMap<>();
        Set<String> colNames = new HashSet<>();
        for (HarmonogramCDF acdf : hg) {
            String sym = acdf.getSymbol();
            Map<String, HarmonogramCDF> values;
            if (!allValues.containsKey(sym)) values = new HashMap<>();
            else values = allValues.get(sym);
            values.put(acdf.getDataSource(), acdf);
            allValues.put(sym, values);
            colNames.add(acdf.getDataSource());
        }
        Logger.debug("Retrieved Harmonogram data for " + allValues.size() + " targets");

        // Arrange column names in a default ordering - needs to be updated
        String[] header = colNames.toArray(new String[]{});
        Arrays.sort(header);


        if (format != null && format.toLowerCase().equals("tsv")) {
            return (ok(_hgmapToTsv(allValues, header)));
        } else {

            // Construct data matrix for clustering
            int r = 0;
            Double[][] matrix = new Double[allValues.size()][header.length];
            for (String sym : allValues.keySet()) {
                Double[] row = new Double[header.length];
                Map<String, HarmonogramCDF> cdfs = allValues.get(sym);
                for (int i = 0; i < header.length; i++) {
                    HarmonogramCDF cdf = cdfs.get(header[i]);
                    row[i] = cdf == null ? null : cdf.getCdf();
                }
                matrix[r++] = row;
            }
            HClust hc = new HClust();
            hc.setData(matrix, header, allValues.keySet().toArray(new String[]{}));
            hc.run();

            // construct the membership matrix, each column is cluster membership
            // for a given height. Each row is a target. So [i,j] indicates cluster
            // id for target i at the j'th height. Thus the group parameter in the
            // hgram json is simply the row of the matrix for that target
            double[] rowHeights = hc.getRowClusteringHeights();
            Integer[][] clusmem = new Integer[matrix.length][rowHeights.length];
            for (int i = 0; i < rowHeights.length; i++) {
                TreeMap<String, Integer> memberships = hc.getClusterMemberships(hc.rcluster, rowHeights[i]);
                int j = 0;
                for (String key : memberships.keySet()) clusmem[j++][i] = memberships.get(key);
            }


//            for (int i = 0; i < clusmem.length; i++) {
//                for (int j = 0; j < clusmem[0].length; j++) {
//                    System.out.print(clusmem[i][j]+" ");
//                }
//                System.out.println();
//            }

            ArrayNode rowNodes = mapper.createArrayNode();
            ArrayNode colNodes = mapper.createArrayNode();
            ArrayNode links = mapper.createArrayNode();

            int rank = 1;
            for (String sym : allValues.keySet()) {
                ObjectNode aRowNode = mapper.createObjectNode();
                Map<String, HarmonogramCDF> cdfs = allValues.get(sym);
                // get any CDF object for this symbol - they all have the same target info
                HarmonogramCDF acdf = cdfs.values().iterator().next();

                // extract the group vector
                int idx = getKeyIndex(allValues.keySet(), sym);
                aRowNode.put("group", arrayToArrayNode(clusmem[idx]));
                aRowNode.put("clust", clusmem[idx][0]);
                aRowNode.put("rank", rank++);
                aRowNode.put("name", sym);
                aRowNode.put("cl", acdf.getIDGFamily());

                rowNodes.add(aRowNode);
            }

            // create a map of column name to data type
            Map<String, String> colNameTypeMap = new HashMap<>();
            for (String col : header) {
                for (String key : allValues.keySet()) {
                    Map<String, HarmonogramCDF> cdfs = allValues.get(key);
                    HarmonogramCDF cdf = cdfs.get(col);
                    if (cdf != null) {
                        colNameTypeMap.put(col, cdf.getDataType());
                        break;
                    }
                }
            }
            rank = 1;
            for (String aColName : header) {
                ObjectNode aColNode = mapper.createObjectNode();
                aColNode.put("name", aColName);
                aColNode.put("cluster", 1);
                aColNode.put("rank", rank++);
                aColNode.put("cl", colNameTypeMap.get(aColName));
                colNodes.add(aColNode);
            }

            int row = 0;
            for (String sym : allValues.keySet()) {
                for (int col = 0; col < header.length; col++) {
                    HarmonogramCDF cdf = allValues.get(sym).get(header[col]);
                    ObjectNode node = mapper.createObjectNode();
                    node.put("source", row);
                    node.put("target", col);
                    node.put("value", cdf == null ? null : cdf.getCdf());
                    links.add(node);
                }
                row++;
            }

            ObjectNode root = mapper.createObjectNode();
            root.put("row_nodes", rowNodes);
            root.put("col_nodes", colNodes);
            root.put("links", links);
            return (ok(root));
        }
    }

}
