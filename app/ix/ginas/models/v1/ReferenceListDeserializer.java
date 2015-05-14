package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import ix.ginas.models.Ginas;
import ix.core.models.Value;
import ix.core.models.Keyword;

public class ReferenceListDeserializer extends JsonDeserializer<List<Value>> {
    public ReferenceListDeserializer () {
    }

    public List<Value> deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
        List<Value> refs = null;
        JsonToken token = parser.getCurrentToken();
        if (JsonToken.START_ARRAY == token) {
            refs = new ArrayList<Value>();
            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token == JsonToken.VALUE_STRING) {
                    refs.add(new Keyword
                             (Ginas.REFERENCE, parser.getValueAsString()));
                }
            }
        }
        return refs;
    }
}
