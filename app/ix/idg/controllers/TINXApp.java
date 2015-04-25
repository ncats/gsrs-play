package ix.idg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.idg.models.Disease;
import ix.idg.models.TINX;
import ix.idg.models.Target;
import ix.ncats.controllers.App;
import ix.utils.Util;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class TINXApp extends App {

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


    static Double getNovelty(Target t) {
        Double nov = -1.0;
        List<Value> props = t.getProperties();
        for (Value v : props) {
            if (v.label.equals(TcrdRegistry.TINX_NOVELTY)) {
                nov = (Double) v.getValue();
                break;
            }
        }
        return nov;
    }

    static String getId(Target t) {
        Keyword kw = t.getSynonym(Commons.UNIPROT_ACCESSION);
        return kw != null ? kw.term : null;
    }

    public static String getId(Disease d) {
        Keyword kw = d.getSynonym(DiseaseOntologyRegistry.DOID, "UniProt");
        return kw != null ? kw.term : null;
    }

    public static Result tinx() {
        try {
            String sha1 = Util.sha1(request());
            return getOrElse("tinx/" + sha1, new Callable<Result>() {
                public Result call() throws Exception {
                    return _tinx();
                }
            });
        } catch (Exception e) {
            return _internalServerError(e);
        }
    }

    // aggregate by target
    static Result _tinx() {
        List<TINX> all = TINXFactory.finder.all();
        ArrayNode root = mapper.createArrayNode();

        Map<String, Map<String, Double>> imap = new HashMap<>();
        Map<String, TINX> nmap = new HashMap<>();
        for (TINX t : all) {
            nmap.put(t.getUniprotId(), t);
            Map<String, Double> ivalue;
            if (imap.containsKey(t.getUniprotId())) {
                ivalue = imap.get(t.getUniprotId());
                ivalue.put(t.getDoid(), t.getImportance());
            } else {
                ivalue = new HashMap<>();
                ivalue.put(t.getDoid(), t.getImportance());
            }
            imap.put(t.getUniprotId(), ivalue);
        }

        // compute a mean importance and construct json
        for (String key : nmap.keySet()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("id", nmap.get(key).id);
            node.put("acc", nmap.get(key).getUniprotId());
            node.put("novelty", nmap.get(key).getNovelty());
            Map<String, Double> ivals = imap.get(key);
            Double mi = 0.0;
            for (Double v : ivals.values()) mi += v;
            mi = mi / (double) ivals.size();
            node.put("meanImportance", mi);
            // construct array of per disease importance
            ArrayNode inode = mapper.createArrayNode();
            for (String doid : ivals.keySet()) {
                ObjectNode node2 = mapper.createObjectNode();
                node2.put("doid", doid);
                node2.put("importance", ivals.get(doid));
                inode.add(node2);
            }
            node.put("importance", inode);
            root.add(node);
        }
        return ok(root);
    }

    public static Result tinxForTarget(final Long id) {
        try {
            String sha1 = Util.sha1(request());
            return getOrElse("tinx/q/" + sha1, new Callable<Result>() {
                public Result call() throws Exception {
                    return _tinxForTarget(id);
                }
            });
        } catch (Exception e) {
            return _internalServerError(e);
        }
    }

    public static Result _tinxForTarget(final Long id) {
        TINX t = TINXFactory.getTINX(id);
        if (t == null) return _notFound("TINX, pharos id:" + id + " not found");

        ObjectNode root = mapper.createObjectNode();
        ArrayNode imps = mapper.createArrayNode();

        root.put("id", t.id);
        root.put("acc", t.getUniprotId());
        root.put("novelty", t.getNovelty());


        List<XRef> xrefs = t.getLinks();
        Double meanImportance = 0.0;
        int n = 0;

        for (XRef xref : xrefs) {
            if (xref.kind.equals(Disease.class.getName())) {
                Disease d = (Disease) xref.deRef();
                for (Value v : xref.properties) {
                    if (v.label.equals(TcrdRegistry.TINX_IMPORTANCE)) {
                        meanImportance += (Double) v.getValue();
                        n++;

                        ObjectNode node = mapper.createObjectNode();
                        node.put("doid", getId(d));
                        node.put("imp", (Double) v.getValue());
                        imps.add(node);
                    }
                }
            }
        }
        if (n > 0)
            meanImportance /= n;
        else meanImportance = 1e-10;
        root.put("meanImportance", meanImportance);
        root.put("importances", imps);

        return (ok(root));
    }

}
