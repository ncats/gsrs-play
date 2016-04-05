package ix.ginas.models.serialization;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import ix.core.controllers.PrincipalFactory;
import ix.core.models.Principal;

public class PrincipalDeserializer extends JsonDeserializer<Principal> {
    public PrincipalDeserializer () {
    }

    public Principal deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
        JsonToken token = parser.getCurrentToken();
        if (JsonToken.START_OBJECT == token) {
            JsonNode tree = parser.getCodec().readTree(parser);
            /* this is really inconsistent with below in that we don't 
             * register this principal if it's not already in the 
             * persistence store..
             */
            return parser.getCodec().treeToValue(tree, Principal.class);
        }
        else { // JsonToken.VALUE_STRING:
            String acc = parser.getValueAsString();
            return PrincipalFactory.registerIfAbsent
                (new Principal (acc, null));
        }
    }
}
