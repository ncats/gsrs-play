package ix.ginas.models;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import ix.core.models.Principal;

public class PrincipalDeserializer extends JsonDeserializer<Principal> {
    public PrincipalDeserializer () {
    }

    public Principal deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {

        String acc = parser.getValueAsString();
        return new Principal (acc, null);
    }
}
