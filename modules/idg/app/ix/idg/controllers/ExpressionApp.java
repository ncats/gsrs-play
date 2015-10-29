package ix.idg.controllers;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.models.Value;
import ix.core.models.XRef;
import ix.idg.models.Expression;
import ix.idg.models.Target;
import ix.ncats.controllers.App;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import play.Play;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Result;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Callable;

public class ExpressionApp extends App {

    static final Map<String, String> onm;

    static {
        onm = new HashMap<>();
        onm.put("brain", "nervous_system");
        onm.put("cortex", "nervous_system");
        onm.put("pituitary", "nervous_system");
        onm.put("cerebra", "nervous_system");
        onm.put("cerebell", "nervous_system");
        onm.put("bone", "bone");
        onm.put("blood", "blood");
        onm.put("artery", "blood");
        onm.put("circula", "blood");
        onm.put("skin", "skin");
        onm.put("sweat", "skin");
        onm.put("gall", "gall_bladder");
        onm.put("spleen", "spleen");
        onm.put("muscle", "muscle");
        onm.put("pancreas", "pancrease");
        onm.put("urine", "urine");
        onm.put("ureth", "urine");
        onm.put("saliva", "saliva");
        onm.put("lymph", "lymph_nodes");
        onm.put("thyroid", "thyroid_gland");
        onm.put("eye", "eye");
        onm.put("retina", "eye");
        onm.put("kidney", "kidney");
        onm.put("adrenal", "adrenal_gland");
        onm.put("bone_marrow", "bone_marrow");
        onm.put("marrow", "bone_marrow");
        onm.put("stomach", "stomach");
        onm.put("liver", "liver");
        onm.put("heart", "heart");
        onm.put("lung", "lung");
        onm.put("intestine", "intestine");
        onm.put("ileum", "intestine");
        onm.put("colon", "intestine");
        onm.put("digest", "intestine");

    }

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

    static String xml2str(Node node) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(node);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Map<String, Integer> getExprTissue(String q) {
        List<Target> targets = TargetFactory.finder.where().eq("uniprotId", q).findList();
        if (targets.size() == 0) return null;
        Target t = targets.get(0);
        for (Value v : t.getProperties()) {
            System.out.println(v.label);
        }
        return null;
    }

    public static List<Expression> getLinkedExpr(Target t, String sourceId) {
        List<Expression> ret = new ArrayList<>();
        for (XRef xref : t.getLinks()) {
            if (!xref.kind.equals(Expression.class.getName())) continue;
            Expression expr = (Expression) xref.deRef();

            // TODO HPM PROTEIN should get a key in Commons
            if (expr.getSourceid() != null && expr.getSourceid().equals(sourceId))
                ret.add(expr);
        }
        return ret;
    }

    public static Result homunculus(final String acc, final String source) throws Exception {
        if (acc == null)
            return _badRequest("Must specify a target accession");
        final String key = "expression/homunculus/" + acc + "/" + source;
        response().setContentType("image/svg+xml");
        return getOrElse(key, new Callable<Result>() {
            public Result call() throws Exception {
                List<Target> targets = TargetFactory.finder
                        .where(Expr.and(Expr.eq("synonyms.label", Commons.UNIPROT_ACCESSION),
                                Expr.eq("synonyms.term", acc))).findList();
                if (targets.size() == 0) return _notFound("No data found for " + acc);
                Target t = targets.get(0);

                // iterate over tissue names and map them to the SVG id's
                // may need to update mapping
                String ds = Commons.GTEx_EXPR;
                if (source != null) {
                    if ("idg".equalsIgnoreCase(source)) ds = Commons.IDG_EXPR;
                    else if ("hpm".equalsIgnoreCase(source)) ds = Commons.HPM_EXPR;
                    else if ("gtex".equalsIgnoreCase(source)) ds = Commons.GTEx_EXPR;
                    else if ("hpa".equalsIgnoreCase(source)) ds = Commons.HPA_RNA_EXPR;
                }

                HashMap<String,Integer> organs = new HashMap<>();
                for (XRef xref : t.getLinks()) {
                    if (!xref.kind.equals(Expression.class.getName())) continue;
                    Expression expr = (Expression) xref.deRef();

                    // TODO HPM PROTEIN should get a key in Commons
                    if (expr.getSourceid() == null || !expr.getSourceid().equals(ds)) continue;

                    // map to canonical organ terms
                    for (String key : onm.keySet()) {
                        if (expr.getTissue().toLowerCase().contains(key)) {

                            // derive the expr level or confidence
                            Integer theLevel = 0;
                            if (ds.equals(Commons.IDG_EXPR)) theLevel = expr.getConfidence().intValue();
                            else {
                                if (expr.getQualValue().toLowerCase().equals("low")) theLevel = 0;
                                else if (expr.getQualValue().toLowerCase().equals("medium")) theLevel = 1;
                                else if (expr.getQualValue().toLowerCase().equals("high")) theLevel = 2;
                            }

                            String tissue = onm.get(key);
                            if (organs.containsKey(tissue)) {
                                Integer level = organs.get(tissue);
                                if (theLevel > level) organs.put(tissue, theLevel);
                            } else organs.put(tissue, theLevel);
                        }
                    }
                }

                String[] confidenceColorsIDG = new String[]{
                        "#ffffff", "#EDF8E9", "#BAE4B3", "#74C476", "#31A354", "#006D2C"
                };
                String[] confidenceColorsOther = new String[]{
                        "#EDF8E9", "#74C476", "#006D2C"
                };


                String suffix = "";
                if (!ds.equals(Commons.IDG_EXPR)) suffix = "-qual";

                Document doc;
                if (Play.isProd()) {
                    doc = XML.fromInputStream
                            (Play.application().resourceAsStream
                                    ("public/tissues_body_human" + suffix + ".svg"), "UTF-8");
                } else {
                    File svg = Play.application().getFile
                            ("app/assets/tissues_body_human" + suffix + ".svg");
                    FileInputStream fis = new FileInputStream(svg);
                    doc = XML.fromInputStream(fis, "UTF-8");
                }

                for (String tissue : organs.keySet()) {
                    Node node = XPath.selectNode("//*[@id='" + tissue + "']", doc);
                    NamedNodeMap attributes = node == null ? null : node.getAttributes();
                    if (attributes == null) continue;
                    Node attrNode = attributes.getNamedItem("fill");
                    if (ds.equals(Commons.IDG_EXPR))
                        attrNode.setNodeValue(confidenceColorsIDG[organs.get(tissue)]);
                    else
                        attrNode.setNodeValue(confidenceColorsOther[organs.get(tissue)]);
                    attributes.setNamedItem(attrNode);
                }
                return ok(xml2str(doc));
            }
        });
    }

}
