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
import ix.core.models.Keyword;

public class KeywordListDeserializer extends JsonDeserializer<List<Keyword>> {
    final String label;
    public KeywordListDeserializer (String label) {
        this.label = label;
    }

    public List<Keyword> deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
        List<Keyword> keywords = null;
        JsonToken token = parser.getCurrentToken();
        if (JsonToken.START_ARRAY == token) {
            keywords = new ArrayList<Keyword>();
            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token == JsonToken.VALUE_STRING) {
                    keywords.add(new Keyword
                                 (label, parser.getValueAsString()));
                }
            }
        }
        return keywords;
    }
}
