package ix.ginas.models.serialization;

import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.ginas.models.GinasCommonSubData;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ReferenceSetDeserializer extends JsonDeserializer<Set<Keyword>> {
    public ReferenceSetDeserializer () {
    }

    public Set<Keyword> deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
    	Set<Keyword> refs = null;
        JsonToken token = parser.getCurrentToken();
        if (JsonToken.START_ARRAY == token) {
            refs = new LinkedHashSet<Keyword>();
            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token == JsonToken.VALUE_STRING) {
                    refs.add(new Keyword
                             (GinasCommonSubData.REFERENCE, parser.getValueAsString()));
                }
            }
        }
        return refs;
    }
}
