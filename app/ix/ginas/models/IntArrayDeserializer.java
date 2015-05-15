package ix.ginas.models;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import ix.core.models.VIntArray;

public class IntArrayDeserializer extends JsonDeserializer<VIntArray> {
    public IntArrayDeserializer () { }

    public VIntArray deserialize
        (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {

        VIntArray array = null;
        JsonToken token = parser.getCurrentToken();
        if (JsonToken.START_ARRAY == token) {
            List<Integer> list = new ArrayList<Integer>();
            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token == JsonToken.VALUE_NUMBER_INT) {
                    list.add(parser.getIntValue());
                }
            }
            int[] ary = new int[list.size()];
            for (int i = 0; i < ary.length; ++i)
                ary[i] = list.get(i);
            
            array = new VIntArray (null, ary);
        }
        return array;
    }
}
