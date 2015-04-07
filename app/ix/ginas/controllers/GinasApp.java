package ix.ginas.controllers;

import ix.core.controllers.search.SearchFactory;
import ix.core.controllers.*;
import ix.core.models.*;
import ix.core.search.TextIndexer;
import static ix.core.search.TextIndexer.*;
import ix.ginas.models.Ginas;
import ix.ginas.models.v1.*;
import ix.utils.Util;
import ix.core.plugins.TextIndexerPlugin;
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
    static final TextIndexer INDEXER = 
        play.Play.application().plugin(TextIndexerPlugin.class).getIndexer();

    static class ChemicalProblemHandler
        extends  DeserializationProblemHandler {
        ChemicalProblemHandler () {
        }
        
        public boolean handleUnknownProperty
            (DeserializationContext ctx, JsonParser parser,
             JsonDeserializer deser, Object bean, String property) {
            Logger.warn("Unknown property \""
                        +property+"\" while parsing "+bean+"; skipping it..");
            try {
                Logger.debug("Token: "+parser.getCurrentToken());
                parser.skipChildren();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }
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
                    return badRequest
                        ("Neither json-url nor json-file parameter "
                         +"is specified!");
                }
            }

            if (type.equalsIgnoreCase("chemical")) {
                sub = parseChemicalJSON (is);
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
            return internalServerError
                ("Can't parse json: "+ex.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(sub));
    }

    public static void parse (InputStream is) throws IOException {
        JsonFactory factory = new JsonFactory ();
        JsonParser parser = factory.createJsonParser(is);
        for (JsonToken tok; (tok = parser.nextToken()) != null; ) {

            switch (tok) {
            }
        }
    }

    public static Substance parseChemicalJSON (InputStream is)
        throws IOException {
        ObjectMapper mapper = new ObjectMapper ();
        mapper.addHandler(new ChemicalProblemHandler ());
        return mapper.readValue(is, ChemicalSubstance.class);
    }
}
