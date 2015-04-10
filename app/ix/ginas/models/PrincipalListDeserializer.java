package ix.ginas.models;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import ix.core.models.Principal;

public class PrincipalListDeserializer
    extends JsonDeserializer<List<Principal>> {
    public PrincipalListDeserializer () {
    }

    public List<Principal> deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {

        List<Principal> principals = new ArrayList<Principal>();
        if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
            while (JsonToken.END_ARRAY != parser.nextToken()) {
                String acc = parser.getValueAsString();
                principals.add(new Principal (acc, null));
            }
        }
        else {
        }
        
        return principals;
    }
}
