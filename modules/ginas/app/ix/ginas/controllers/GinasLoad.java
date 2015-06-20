package ix.ginas.controllers;

import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.models.Ginas;
import ix.ginas.models.v1.*;
import ix.ncats.controllers.App;
import ix.core.chem.Chem;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.db.ebean.Model;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;
import tripod.chem.indexer.StructureIndexer;

import com.avaje.ebean.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;


public class GinasLoad extends App {
    static final TextIndexer TEXT_INDEXER = 
        Play.application().plugin(TextIndexerPlugin.class).getIndexer();
                    
    public static Result error (int code, String mesg) {
        return ok (ix.ginas.views.html.error.render(code, mesg));
    }

    public static Result _notFound (String mesg) {
        return notFound (ix.ginas.views.html.error.render(404, mesg));
    }
    
    public static Result _badRequest (String mesg) {
        return badRequest (ix.ginas.views.html.error.render(400, mesg));
    }
    
    public static Result _internalServerError (Throwable t) {
        t.printStackTrace();
        return internalServerError
            (ix.ginas.views.html.error.render
             (500, "Internal server error: "+t.getMessage()));
    }
    
    static FacetDecorator[] decorate (Facet... facets) {
        List<FacetDecorator> decors = new ArrayList<FacetDecorator>();
        // override decorator as needed here
        for (int i = 0; i < facets.length; ++i) {
            decors.add(new GinasFacetDecorator (facets[i]));
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
        GinasFacetDecorator (Facet facet) {
            super (facet, true, 6);
        }
        
        @Override
        public String name () {
            return super.name().trim();
        }

        @Override
        public String label (final int i) {
            final String label = super.label(i);
            final String name = super.name();

            return label;
        }
    }


    static class GinasV1ProblemHandler
        extends  DeserializationProblemHandler {
        GinasV1ProblemHandler () {
        }
        
        public boolean handleUnknownProperty
            (DeserializationContext ctx, JsonParser parser,
             JsonDeserializer deser, Object bean, String property) {

            try {
                boolean parsed = true;
                if ("hash".equals(property)) {
                    Structure struc = (Structure)bean;
                    //Logger.debug("value: "+parser.getText());
                    struc.properties.add(new Keyword
                                         (Structure.H_LyChI_L4,
                                          parser.getText()));
                }
                else if ("references".equals(property)) {
                    //Logger.debug(property+": "+bean.getClass());
                    if (bean instanceof Structure) {
                        Structure struc = (Structure)bean;
                        parseReferences (parser, struc.properties);
                    }
                    else {
                        parsed = false;
                    }
                }
                else if ("count".equals(property)) {
                    if (bean instanceof Structure) {
                        // need to handle this.
                        parser.skipChildren();
                    }
                }
                else {
                    parsed = false;
                }

                if (!parsed) {
                    Logger.warn("Unknown property \""
                                +property+"\" while parsing "
                                +bean+"; skipping it..");
                    Logger.debug("Token: "+parser.getCurrentToken());
                    parser.skipChildren();
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }

        int parseReferences (JsonParser parser, List<Value> refs)
            throws IOException {
            int nrefs = 0;
            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                while (JsonToken.END_ARRAY != parser.nextToken()) {
                    String ref = parser.getValueAsString();
                    refs.add(new Keyword (Ginas.REFERENCE, ref));
                    ++nrefs;
                }
            }
            return nrefs;
        }
    }

    public static Substance persistJSON
        (InputStream is, Class<? extends Substance> cls) throws Exception {
        ObjectMapper mapper = new ObjectMapper ();
        mapper.addHandler(new GinasV1ProblemHandler ());
        JsonNode tree = mapper.readTree(is);
        JsonNode subclass = tree.get("substanceClass");
        
        if (subclass != null && !subclass.isNull()) {
            Substance.SubstanceClass type =
                Substance.SubstanceClass.valueOf(subclass.asText());
            switch (type) {
            case chemical:
                if (cls == null) {
                    ChemicalSubstance sub =
                        mapper.treeToValue(tree, ChemicalSubstance.class);
                    return persist (sub);
                }
                else if (cls.isAssignableFrom(ChemicalSubstance.class)) {
                    ChemicalSubstance sub =
                        (ChemicalSubstance)mapper.treeToValue(tree, cls);
                    return persist (sub);
                }
                else {
                    Logger.warn(tree.get("uuid").asText()+" is not of type "
                                +cls.getName());
                }
                break;
                
            case protein:
                if (cls == null) {
                    ProteinSubstance sub =
                        mapper.treeToValue(tree, ProteinSubstance.class);
                    return persist (sub);
                }
                else if (cls.isAssignableFrom(ProteinSubstance.class)) {
                    ProteinSubstance sub =
                        (ProteinSubstance)mapper.treeToValue(tree, cls);
                    return persist (sub);
                }
                else {
                    Logger.warn(tree.get("uuid").asText()+" is not of type "
                                +cls.getName());
                }
                break;
                
            case mixture:
                if (cls == null) {
                    MixtureSubstance sub =
                        mapper.treeToValue(tree, MixtureSubstance.class);
                    sub.save();
                    return sub;
                }
                else if (cls.isAssignableFrom(MixtureSubstance.class)) {
                    MixtureSubstance sub =
                        (MixtureSubstance)mapper.treeToValue(tree, cls);
                    sub.save();
                    return sub;
                }
                else {
                    Logger.warn(tree.get("uuid").asText()+" is not of type "
                                +cls.getName());
                }
                break;

            case polymer:
                if (cls == null) {
                    PolymerSubstance sub =
                        mapper.treeToValue(tree, PolymerSubstance.class);
                    sub.save();
                    return sub;
                }
                else if (cls.isAssignableFrom(PolymerSubstance.class)) {
                    PolymerSubstance sub =
                        (PolymerSubstance)mapper.treeToValue(tree, cls);
                    sub.save();
                    return sub;
                }
                else {
                    Logger.warn(tree.get("uuid").asText()+" is not of type "
                                +cls.getName());
                }
                break;

            case structurallyDiverse:
                if (cls == null) {
                    StructurallyDiverseSubstance sub =
                        mapper.treeToValue
                        (tree, StructurallyDiverseSubstance.class);
                    sub.save();
                    return sub;
                }
                else if (cls.isAssignableFrom
                         (StructurallyDiverseSubstance.class)) {
                    StructurallyDiverseSubstance sub =
                        (StructurallyDiverseSubstance)mapper
                        .treeToValue(tree, cls);
                    sub.save();
                    return sub;
                }
                else {
                    Logger.warn(tree.get("uuid").asText()+" is not of type "
                                +cls.getName());
                }
                break;

            case specifiedSubstanceG1:
                if (cls == null) {
                    SpecifiedSubstanceGroup1 sub =
                        mapper.treeToValue
                        (tree, SpecifiedSubstanceGroup1.class);
                    sub.save();
                    return sub;
                }
                else if (cls.isAssignableFrom
                         (SpecifiedSubstanceGroup1.class)) {
                    SpecifiedSubstanceGroup1 sub =
                        (SpecifiedSubstanceGroup1)mapper
                        .treeToValue(tree, cls);
                    sub.save();
                    return sub;
                }
                else {
                    Logger.warn(tree.get("uuid").asText()+" is not of type "
                                +cls.getName());
                }
                break;
                
            default:
                Logger.warn("Skipping substance class "+type);
            }
        }
        else {
            Logger.error("Not a valid JSON substance!");
        }
        return null;
    }
    
    public static Result load () {
        if (Play.isProd()) {
            return redirect (ix.ginas.controllers.routes.GinasFactory.index());
        }
        return ok (ix.ginas.views.html.load.render());
    }
    
    public static Result loadJSON () {
        if (Play.isProd()) {
            return badRequest ("Invalid request!");
        }
        
        DynamicForm requestData = Form.form().bindFromRequest();
        String type = requestData.get("substance-type");
        Logger.debug("substance-type: "+type);
        
        Substance sub = null;
        try {
            InputStream is = null;
            String url = requestData.get("json-url");
            Logger.debug("json-url: "+url);
            if (url != null && url.length() > 0) {
                URL u = new URL (url);
                is = u.openStream();
            }
            else {
                // now try json-file
                Http.MultipartFormData body =
                    request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart part =
                    body.getFile("json-file");
                if (part != null) {
                    File file = part.getFile();
                    Logger.debug("json-file: "+file);
                    is = new FileInputStream (file);
                }
                else {
                    part = body.getFile("json-dump");
                    if (part != null) {
                        File file = part.getFile();
                        try {
                            // see if it's a zip file
                            ZipFile zip = new ZipFile (file);
                            for (Enumeration<? extends ZipEntry> en = zip.entries();
                                 en.hasMoreElements(); ) {
                                ZipEntry ze = en.nextElement();
                                Logger.debug("processing "+ze.getName());
                                is = zip.getInputStream(ze);
                                break;
                            }
                        }
                        catch (Exception ex) {
                            Logger.warn("Not a zip file \""+file+"\"!");
                            // try as plain txt file
                            is = new FileInputStream (file);
                        }
                        return processDump (is);
                    }
                    else {
                        return badRequest
                            ("Neither json-url nor json-file nor json-dump "
                             +"parameter is specified!");
                    }
                }
            }

            sub = persistJSON (is, null);
        }
        catch (Exception ex) {
            return _internalServerError (ex);
        }

        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(sub));
    }

    public static Result processDump (InputStream is) throws Exception {
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        int count = 0;
        for (String line; (line = br.readLine()) != null; ) {
            String[] toks = line.split("\t");
            Logger.debug("processing "+toks[0]+" "+toks[1]+"..."+count);
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream
                    (toks[2].getBytes("utf8"));
                Substance sub = persistJSON (bis, null);
                if (sub == null) {
                    Logger.warn("Can't persist record "+toks[1]);
                }
                else {
                    ++count;
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        br.close();
        return ok (count+" record(s) processed!");
    }

    static Substance persist (ChemicalSubstance chem) throws Exception {
        // now index the structure for searching
        try {
            Chem.setFormula(chem.structure);
            chem.structure.save();
            // it's bad to reference App from here!!!!
            strucIndexer.add(String.valueOf(chem.structure.id),
                             chem.structure.molfile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (Moiety m : chem.moieties)
            m.structure.save();
        chem.save();
        
        return chem;
    }

    static Substance persist (ProteinSubstance sub) throws Exception {
        Transaction tx = Ebean.beginTransaction();
        try {
            sub.save();
            tx.commit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            tx.end();
        }
        return sub;     
    }
}
