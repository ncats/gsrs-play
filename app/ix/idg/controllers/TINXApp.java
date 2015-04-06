package ix.idg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.idg.models.Disease;
import ix.idg.models.Target;
import ix.ncats.controllers.App;
import ix.utils.Util;
import play.mvc.Result;

import java.util.List;
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
        Keyword kw = t.getSynonym(UniprotRegistry.ACCESSION);
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
        List<Target> all = TargetFactory.finder.all();
        ArrayNode root = mapper.createArrayNode();
        Double nov = -1.0;
        for (Target t : all) {
            nov = getNovelty(t);

            List<XRef> xrefs = t.getLinks();
            Double meanImportance = 0.0;
            int n = 0;
            if (nov != -1) {
                for (XRef xref : xrefs) {
                    if (xref.kind.equals(Disease.class.getName())) {
                        for (Value v : xref.properties) {
                            if (v.label.equals(TcrdRegistry.TINX_IMPORTANCE)) {
                                meanImportance += (Double) v.getValue();
                                n++;
                            }
                        }
                    }
                }
                if (n > 0)
                    meanImportance /= n;
                else meanImportance = 1e-10;
                ObjectNode node = mapper.createObjectNode();
                node.put("pharos_id", t.id);
                node.put("uniprot_id", getId(t));
                node.put("importance", meanImportance);
                node.put("novelty", nov);
                root.add(node);
            }
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
        Target t = TargetFactory.getTarget(id);
        if (t == null) return _notFound("Target, pharos id:" + id + " not found");

        ObjectNode root = mapper.createObjectNode();
        ArrayNode imps = mapper.createArrayNode();

        root.put("pharos_id", t.id);
        root.put("uniprot_id", getId(t));
        root.put("novelty", getNovelty(t));

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
