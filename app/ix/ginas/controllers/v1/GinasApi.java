package ix.ginas.controllers.v1;

import java.util.*;
import java.io.*;

import play.libs.Json;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

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

import com.avaje.ebean.*;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.ebean.event.BeanPersistListener;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.*;
import ix.ginas.models.v1.*;

public class GinasApi extends EntityFactory {
    static public final Model.Finder<UUID, Substance> finder =
        new Model.Finder(UUID.class, Substance.class);

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

    public static <T extends Substance> T parseJSON
        (InputStream is, Class<T> cls) throws IOException {
        ObjectMapper mapper = new ObjectMapper ();
        mapper.addHandler(new ChemicalProblemHandler ());
        return mapper.readValue(is, cls);
    }

    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }
    
    public static Result get (String id, String expand) {
        return get (UUID.fromString(id), expand, finder);
    }
}
