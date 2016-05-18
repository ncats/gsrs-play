package ix.ntd.controllers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ncats.controllers.App;
import ix.ntd.models.Patient;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Http;
import play.mvc.Result;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class NTDLoad extends App {

    public static Result error(int code, String mesg) {
        return ok(ix.ntd.views.html.error.render(code, mesg));
    }

    public static Result _notFound(String mesg) {
        return notFound(ix.ntd.views.html.error.render(404, mesg));
    }

    public static Result _badRequest(String mesg) {
        return badRequest(ix.ntd.views.html.error.render(400, mesg));
    }

    public static Result _internalServerError(Throwable t) {
        t.printStackTrace();
        return internalServerError
                (ix.ntd.views.html.error.render
                        (500, "Internal server error: " + t.getMessage()));
    }

    static FacetDecorator[] decorate(Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new GinasFacetDecorator(facets[i]));
        }
        // now add hidden facet so as to not have them shown in the alert
        // box
        //        for (int i = 1; i <= 8; ++i) {
        //            GinasFacetDecorator f = new GinasFacetDecorator
        //                (new TextIndexer.Facet
        //                 (ChemblRegistry.ChEMBL_PROTEIN_CLASS+" ("+i+")"));
        //            f.hidden = true;
        //            decors.add(f);
        //        }

        GinasFacetDecorator f = new GinasFacetDecorator
                (new TextIndexer.Facet("ChemicalSubstance"));
        f.hidden = true;
        decors.add(f);

        return decors.toArray(new FacetDecorator[0]);
    }

    static class GinasFacetDecorator extends FacetDecorator {
        GinasFacetDecorator(Facet facet) {
            super(facet, true, 6);
        }

        @Override
        public String name() {
            return super.name().trim();
        }

        @Override
        public String label(final int i) {
            final String label = super.label(i);
            final String name = super.name();

            return label;
        }
    }


    static class GinasV1ProblemHandler
            extends DeserializationProblemHandler {
        GinasV1ProblemHandler() {
        }

        public boolean handleUnknownProperty
                (DeserializationContext ctx, JsonParser parser,
                 JsonDeserializer deser, Object bean, String property) {

            try {
                boolean parsed = true;
                if ("hash".equals(property)) {
                    Structure struc = (Structure) bean;
                    //Logger.debug("value: "+parser.getText());
                    struc.properties.add(new Keyword
                            (Structure.H_LyChI_L4,
                                    parser.getText()));
                } else if ("references".equals(property)) {
                    //Logger.debug(property+": "+bean.getClass());
                    if (bean instanceof Structure) {
                        Structure struc = (Structure) bean;
                        parseReferences(parser, struc.properties);
                    } else {
                        parsed = false;
                    }
                } else if ("count".equals(property)) {
                    if (bean instanceof Structure) {
                        // need to handle this.
                        parser.skipChildren();
                    }
                } else {
                    parsed = false;
                }

                if (!parsed) {
                    Logger.warn("Unknown property \""
                            + property + "\" while parsing "
                            + bean + "; skipping it..");
                    Logger.debug("Token: " + parser.getCurrentToken());
                    parser.skipChildren();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }

        int parseReferences(JsonParser parser, List<Value> refs)
                throws IOException {
            int nrefs = 0;
            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                while (JsonToken.END_ARRAY != parser.nextToken()) {
                    String ref = parser.getValueAsString();
                    //  refs.add(new Keyword (Ginas.REFERENCE, ref));
                    ++nrefs;
                }
            }
            return nrefs;
        }
    }

    public static Result parse() throws Exception{
        Logger.info(request().body().asJson().toString());
        JsonNode js = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        Patient p = mapper.treeToValue(js, Patient.class);
        p.save();
        try {
            Logger.info("printing");
            String str = mapper.writeValueAsString(p);
            Logger.info(str);
        } catch (JsonProcessingException e) {
            Logger.info("broken");
            e.printStackTrace();
        }


        return ok(ix.ntd.views.html.index.render());
    }

    public static Patient persistJSON
        (InputStream is, Patient p) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree(is);

return p;
    }
//        mapper.addHandler(new GinasV1ProblemHandler ());
//        JsonNode tree = mapper.readTree(is);
//        JsonNode subclass = tree.get("substanceClass");
//
//        if (subclass != null && !subclass.isNull()) {
//            Substance.SubstanceClass type =
//                Substance.SubstanceClass.valueOf(subclass.asText());
//            switch (type) {
//            case chemical:
//                if (cls == null) {
//                    ChemicalSubstance sub =
//                        mapper.treeToValue(tree, ChemicalSubstance.class);
//                    sub.save();
//                    return sub;
//                }
//                else if (cls.isAssignableFrom(ChemicalSubstance.class)) {
//                    ChemicalSubstance sub =
//                        (ChemicalSubstance)mapper.treeToValue(tree, cls);
//                    sub.save();
//                    return sub;
//                }
//                else {
//                    Logger.warn(tree.get("uuid").asText()+" is not of type "
//                                +cls.getName());
//                }
//                break;
//
//            case protein:
//                if (cls == null) {
//                    ProteinSubstance sub =
//                        mapper.treeToValue(tree, ProteinSubstance.class);
//                    return persist (sub);
//                }
//                else if (cls.isAssignableFrom(ProteinSubstance.class)) {
//                    ProteinSubstance sub =
//                        (ProteinSubstance)mapper.treeToValue(tree, cls);
//                    return persist (sub);
//                }
//                else {
//                    Logger.warn(tree.get("uuid").asText()+" is not of type "
//                                +cls.getName());
//                }
//                break;
//
//            case mixture:
//                if (cls == null) {
//                    MixtureSubstance sub =
//                        mapper.treeToValue(tree, MixtureSubstance.class);
//                    sub.save();
//                    return sub;
//                }
//                else if (cls.isAssignableFrom(MixtureSubstance.class)) {
//                    MixtureSubstance sub =
//                        (MixtureSubstance)mapper.treeToValue(tree, cls);
//                    sub.save();
//                    return sub;
//                }
//                else {
//                    Logger.warn(tree.get("uuid").asText()+" is not of type "
//                                +cls.getName());
//                }
//                break;
//
//            case polymer:
//                if (cls == null) {
//                    PolymerSubstance sub =
//                        mapper.treeToValue(tree, PolymerSubstance.class);
//                    sub.save();
//                    return sub;
//                }
//                else if (cls.isAssignableFrom(PolymerSubstance.class)) {
//                    PolymerSubstance sub =
//                        (PolymerSubstance)mapper.treeToValue(tree, cls);
//                    sub.save();
//                    return sub;
//                }
//                else {
//                    Logger.warn(tree.get("uuid").asText()+" is not of type "
//                                +cls.getName());
//                }
//                break;
//
//            case structurallyDiverse:
//                if (cls == null) {
//                    StructurallyDiverseSubstance sub =
//                        mapper.treeToValue
//                        (tree, StructurallyDiverseSubstance.class);
//                    sub.save();
//                    return sub;
//                }
//                else if (cls.isAssignableFrom
//                         (StructurallyDiverseSubstance.class)) {
//                    StructurallyDiverseSubstance sub =
//                        (StructurallyDiverseSubstance)mapper
//                        .treeToValue(tree, cls);
//                    sub.save();
//                    return sub;
//                }
//                else {
//                    Logger.warn(tree.get("uuid").asText()+" is not of type "
//                                +cls.getName());
//                }
//                break;
//
//                case specifiedSubstanceG1:
//                    if (cls == null) {
//                       SpecifiedSubstanceGroup1 sub =
//                                mapper.treeToValue
//                                        (tree, SpecifiedSubstanceGroup1.class);
//                        sub.save();
//                        return sub;
//                    }
//                    else if (cls.isAssignableFrom
//                            (SpecifiedSubstanceGroup1.class)) {
//                        SpecifiedSubstanceGroup1 sub =
//                                (SpecifiedSubstanceGroup1)mapper
//                                        .treeToValue(tree, cls);
//                        sub.save();
//                        return sub;
//                    }
//                    else {
//                        Logger.warn(tree.get("uuid").asText()+" is not of type "
//                                +cls.getName());
//                    }
//                    break;
//
//            default:
//                Logger.warn("Skipping substance class "+type);
//            }
//        }
//        else {
//            Logger.error("Not a valid JSON substance!");
//        }
//        return null;
//    }

    public static Result load() {
        return ok(ix.ntd.views.html.load.render());
    }

    public static Result loadJSON() {
        DynamicForm requestData = Form.form().bindFromRequest();
        String type = requestData.get("substance-type");
        Logger.debug("substance-type: " + type);

        Patient sub = null;
        try {
            InputStream is = null;
            String url = requestData.get("json-url");
            Logger.debug("json-url: " + url);
            if (url != null && url.length() > 0) {
                URL u = new URL(url);
                is = u.openStream();
            } else {
                // now try json-file
                Http.MultipartFormData body =
                        request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart part =
                        body.getFile("json-file");
                if (part != null) {
                    File file = part.getFile();
                    Logger.debug("json-file: " + file);
                    is = new FileInputStream(file);
                } else {
                    part = body.getFile("json-dump");
                    if (part != null) {
                        File file = part.getFile();
                        try {
                            // see if it's a zip file
                            ZipFile zip = new ZipFile(file);
                            for (Enumeration<? extends ZipEntry> en = zip.entries();
                                 en.hasMoreElements(); ) {
                                ZipEntry ze = en.nextElement();
                                Logger.debug("processing " + ze.getName());
                                is = zip.getInputStream(ze);
                                break;
                            }
                        } catch (Exception ex) {
                            Logger.warn("Not a zip file \"" + file + "\"!");
                            // try as plain txt file
                            is = new FileInputStream(file);
                        }
                        return processDump(is);
                    } else {
                        return badRequest
                                ("Neither json-url nor json-file nor json-dump "
                                        + "parameter is specified!");
                    }
                }
            }

//            sub = persistJSON (is, null);
        } catch (Exception ex) {
            return _internalServerError(ex);
        }

        ObjectMapper mapper = new ObjectMapper();
        return ok(mapper.valueToTree(sub));
    }

    public static Result processDump(InputStream is) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        int count = 0;
        for (String line; (line = br.readLine()) != null; ) {
            String[] toks = line.split("\t");
            Logger.debug("processing " + toks[0] + " " + toks[1] + "..." + count);
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream
                        (toks[2].getBytes("utf8"));
                //   Substance sub = persistJSON (bis, null);
//                if (sub == null) {
//                    Logger.warn("Can't persist record "+toks[1]);
//                }
//                else {
//                    ++count;
//                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        br.close();
        return ok(count + " record(s) processed!");
    }
//
//    static Substance persist (ProteinSubstance sub) throws Exception {
//        Transaction tx = Ebean.beginTransaction();
//        try {
//            sub.save();
//            tx.commit();
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        finally {
//            tx.end();
//        }
//        return sub;
//    }

    protected static <T extends Model>
    Result create(Class<T> type,
                  Model.Finder<Long, T> finder) {
        if (!request().method().equalsIgnoreCase("POST")) {
            return badRequest("Only POST is accepted!");
        }

        String content = request().getHeader("Content-Type");
        if (content == null || (!content.toLowerCase().startsWith("application/json")
                && !content.equalsIgnoreCase("text/json"))) {
            return badRequest("Mime type gggggggg\"" + content + "\" not supported!");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = request().body().asJson();

            T inst = mapper.treeToValue(node, type);
            inst.save();

            return created(mapper.valueToTree(inst));
        } catch (Exception ex) {
            return internalServerError(ex.getMessage());
        }
    }


}
