package ix.idg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static Result _hgForTargets(final String[] accs, final String format) {
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

        String page = null;
        if (format.toLowerCase().equals("tsv")) {
            String[] header = colNames.toArray(new String[]{});
            Arrays.sort(header);
            page = _hgmapToTsv(allValues, header);
        }

        return (ok(page));
    }

}
