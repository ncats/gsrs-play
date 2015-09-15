package ix.idg.controllers;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.idg.models.Disease;
import ix.idg.models.TINX;
import ix.idg.models.Target;
import ix.ncats.controllers.App;
import ix.utils.Util;
import play.Logger;
import play.db.ebean.Model;
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

    public static Result tinxForTarget (final String acc) {
        try {
            final String key = "tinx/"+acc+"/"+Util.sha1(request());
            return getOrElse(key, new Callable<Result>() {
                public Result call() throws Exception {
                    Logger.debug("Cache missed: "+key);
                    return _tinxForTarget (acc);
                }
            });
        } catch (Exception e) {
            return _internalServerError(e);
        }
    }

    public static Result _tinxForTarget(final String acc) {
        List<TINX> tinx = TINXFactory.finder
            .where().eq("uniprotId", acc).findList();
        if (tinx.isEmpty()) {
            return _notFound ("No TINX found for target \""+acc+"\"");
        }

        ArrayNode imps = mapper.createArrayNode();

        Double meanImportance = 0.0;
        Double novelty = null;

        for (TINX tx : tinx) {
            if (novelty == null) {
                // TODO Daniel will updated TCRD with per-disease novelty values
                novelty = tx.novelty;
            }
            meanImportance += tx.importance;
            List<Disease> diseases = DiseaseFactory.finder
                    .where(Expr.and(Expr.eq("synonyms.label", "DOID"),
                            Expr.eq("synonyms.term", tx.doid)))
                    .findList();
            ObjectNode node = mapper.createObjectNode();
            node.put("doid", tx.doid);
            node.put("imp", tx.importance);
            if (!diseases.isEmpty()) node.put("dname", diseases.get(0).getName());
            imps.add(node);
        }
        
        if (tinx.size() > 0)
            meanImportance /= tinx.size();
        else meanImportance = 1e-10;
        
        ObjectNode root = mapper.createObjectNode();
        root.put("acc", acc);
        root.put("novelty", novelty);
        root.put("meanImportance", meanImportance);
        root.put("importances", imps);

        return (ok(root));
    }

}
