package ix.idg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ix.idg.models.HarmonogramCDF;
import ix.ncats.controllers.App;
import ix.utils.Util;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Result;

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


    public static Result view(String q) {
        if (q == null) return _badRequest("Must specify a comma separated list of Uniprot ID's");
        return (ok(ix.idg.views.html.harmonogram.render(q.split(","))));
    }

    public static Result hgForTarget(final String q, final String format) {
        return _handleHgRequest(q, format);
    }

    public static Result hgForTargetPost() {
        DynamicForm requestData = Form.form().bindFromRequest();
        final String q = requestData.get("q");
        if (q == null) return _badRequest("Must specify one or more targets via the q query parameter");
        final String format = requestData.get("format");
        return _handleHgRequest(q, format);
    }

    static Result _handleHgRequest(final String q, final String format) {
        if (q == null) return _badRequest("Must specify one or more targets via the q query parameter");
        try {
            final String key = "hg/target/" + q + "/" + format + "/" + Util.sha1(request());
            return getOrElse(key, new Callable<Result>() {
                public Result call() throws Exception {
                    Logger.debug("Cache missed: " + key);
                    if (q.contains(",")) return _hgForTargets(q.split(","), format);
                    return _hgForTargets(new String[]{q}, format);
                }
            });
        } catch (Exception e) {
            return _internalServerError(e);
        }
    }

    static String _hgmapToTsv(Map<String, Map<String, Double>> map, String[] header) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sym");
        for (String aHeader : header) sb.append("\t").append(aHeader);
        sb.append("\n");

        String[] syms = map.keySet().toArray(new String[]{});
        Arrays.sort(syms);
        for (String asym : syms) {
            sb.append(asym).append("\t").append(_hgmapToTsvRow(map.get(asym), header));
            sb.append("\n");
        }
        return sb.toString();
    }

    static String _hgmapToTsvRow(Map<String, Double> map, String[] keys) {
        StringBuilder sb = new StringBuilder();
        String delimiter = "";
        for (String akey : keys) {
            sb.append(delimiter).append(map.get(akey));
            delimiter = "\t";
        }
        return sb.toString();
    }

    public static Result _hgForTargets(String[] accs, String format) {
        List<HarmonogramCDF> hg = HarmonogramFactory.finder
                .where().in("uniprotId", accs).findList();
        if (hg.isEmpty()) {
            return _notFound("No harmonogram data found for targets");
        }

        Map<String, Map<String, Double>> allValues = new HashMap<>();
        Set<String> colNames = new HashSet<>();
        for (HarmonogramCDF acdf : hg) {
            String sym = acdf.getSymbol();
            Map<String, Double> values;
            if (!allValues.containsKey(sym)) values = new HashMap<>();
            else values = allValues.get(sym);
            values.put(acdf.getDataSource(), acdf.getCdf());
            allValues.put(sym, values);
            colNames.add(acdf.getDataSource());
        }

        // Arrange column names in a default ordering - needs to be updated
        String[] header = colNames.toArray(new String[]{});
        Arrays.sort(header);

        String page = null;
        if (format != null && format.toLowerCase().equals("tsv")) {
            return (ok(_hgmapToTsv(allValues, header)));
        } else {

            ArrayNode rowNodes = mapper.createArrayNode();
            ArrayNode colNodes = mapper.createArrayNode();
            ArrayNode links = mapper.createArrayNode();

            int rank = 1;
            for (String sym : allValues.keySet()) {
                ObjectNode aRowNode = mapper.createObjectNode();
                aRowNode.put("clust", 1);
                aRowNode.put("rank", rank++);
                aRowNode.put("name", sym);
                rowNodes.add(aRowNode);
            }

            rank = 1;
            for (String aColName : header) {
                ObjectNode aColNode = mapper.createObjectNode();
                aColNode.put("name", aColName);
                aColNode.put("cluster", 1);
                aColNode.put("rank", rank++);
                colNodes.add(aColNode);
            }

            int row = 0;
            for (String sym : allValues.keySet()) {
                for (int col = 0; col < header.length; col++) {
                    Double value = allValues.get(sym).get(header[col]);
                    ObjectNode node = mapper.createObjectNode();
                    node.put("source", row);
                    node.put("target", col);
                    node.put("value", value);
                    links.add(node);
                }
            }

            ObjectNode root = mapper.createObjectNode();
            root.put("row_nodes", rowNodes);
            root.put("col_nodes", colNodes);
            root.put("links", links);
            return (ok(root));
        }
    }

}
