package ix.ginas.models.serialization;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.VIntArray;

public class IntArraySerializer extends JsonSerializer<VIntArray> {
    public IntArraySerializer () {}
    public void serialize (VIntArray array, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        if (array != null) {
            int[] ary = array.getArray();
            jgen.writeStartArray();
            for (int i = 0; i < ary.length; ++i)
                jgen.writeNumber(ary[i]);
            jgen.writeEndArray();
        }
        else {
            jgen.writeNull();
        }
    }
}
