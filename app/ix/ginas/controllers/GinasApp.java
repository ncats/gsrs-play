package ix.ginas.controllers;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.controllers.*;
import ix.core.models.*;
import ix.core.search.TextIndexer;
import static ix.core.search.TextIndexer.*;
import ix.utils.Util;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.models.*;
import ix.ginas.models.*;
import ix.ginas.models.v1.*;
import ix.ncats.controllers.App;

import tripod.chem.indexer.StructureIndexer;
import static tripod.chem.indexer.StructureIndexer.ResultEnumeration;

import play.Logger;
import play.cache.*;
import play.data.*;
import play.db.*;
import play.mvc.*;
import play.libs.ws.WS;
import play.db.ebean.Model;
import com.avaje.ebean.Expr;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.zip.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;


public class GinasApp extends App {
    static final TextIndexer TEXT_INDEXER = 
        play.Play.application().plugin(TextIndexerPlugin.class).getIndexer();
    static final StructureIndexer STRUC_INDEXER =
        play.Play.application().plugin(StructureIndexerPlugin.class).getIndexer();

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
                    if (bean instanceof Structure) {
                        Structure struc = (Structure)bean;
                        if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                            while (JsonToken.END_ARRAY != parser.nextToken()) {
                                String ref = parser.getValueAsString();
                                struc.properties.add
                                    (new Keyword (Ginas.REFERENCE, ref));
                            }
                        }
                        else {
                            return false;
                        }
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
    }

    public static <T extends Substance> T parseJSON
        (InputStream is, Class<T> cls) throws IOException {
        ObjectMapper mapper = new ObjectMapper ();
        mapper.addHandler(new GinasV1ProblemHandler ());
        JsonNode tree = mapper.readTree(is);
        JsonNode subclass = tree.get("substanceClass");
        if (subclass != null && !subclass.isNull()) {
            Substance.SubstanceClass type =
                Substance.SubstanceClass.valueOf(subclass.asText());
            switch (type) {
            case chemical:
                if (cls.isAssignableFrom(ChemicalSubstance.class)) {
                    return mapper.treeToValue(tree, cls);
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
        return ok (ix.ginas.views.html.load.render());
    }
    
    public static Result loadJSON () {
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

            if (type.equalsIgnoreCase("chemical")) {
                ChemicalSubstance chem =
                    parseJSON(is, ChemicalSubstance.class);
                sub = persist (chem);
            }
            else if (type.equalsIgnoreCase("protein")) {
            }
            else if (type.equalsIgnoreCase("nucleic acid")) {
            }
            else if (type.equalsIgnoreCase("polymer")) {
            }
            else if (type.equalsIgnoreCase("Structurally Diverse")) {
            }
            else if (type.equalsIgnoreCase("mixture")) {
            }
            else {
                return badRequest ("Unknown substance type: "+type);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return internalServerError
                ("Can't parse json: "+ex.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(sub));
    }

    public static Result processDump (InputStream is) throws Exception {
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        int count = 0;
        for (String line; (line = br.readLine()) != null; ) {
            String[] toks = line.split("\t");
            Logger.debug("processing "+toks[0]+" "+toks[1]+"...");
            ByteArrayInputStream bis = new ByteArrayInputStream
                (toks[2].getBytes("utf8"));
            ChemicalSubstance chem = parseJSON (bis, ChemicalSubstance.class);
            if (chem != null) {
                try {
                    persist (chem);
                    ++count;
                }
                catch (Exception ex) {
                    Logger.error("Can't persist record "+toks[1], ex);
                }
            }
        }
        br.close();
        return ok (count+" record(s) processed!");
    }

    // there is some thing in how jackson's object creation
    // doesn't get registered with ebean for it to realize that
    // the bean's state has changed.
    static Substance persist (ChemicalSubstance chem) throws Exception {
        chem.structure.save();
        // now index the structure for searching
        STRUC_INDEXER.add(String.valueOf(chem.structure.id),
                          chem.structure.molfile);
        for (Moiety m : chem.moieties)
            m.structure.save();
        chem.save();
        return chem;
    }

    public static Result search (String kind) {
        return ok ("Coming soon!");
    }

    public static Result authenticate () {
        return ok ("You're authenticated!");
    }
}
