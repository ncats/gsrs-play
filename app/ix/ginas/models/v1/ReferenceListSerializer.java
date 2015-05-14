package ix.ginas.models.v1;

import java.lang.reflect.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.ginas.models.Ginas;

public class ReferenceListSerializer extends JsonSerializer<List<Value>> {
    public ReferenceListSerializer () {}

    public void serialize (List<Value> list, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        List<String> refs = new ArrayList<String>();
        for (Value val : list) {
            if (Ginas.REFERENCE.equals(val.label)) {
                Keyword kw = (Keyword)val;
                refs.add(kw.term);
            }
        }
        provider.defaultSerializeValue(refs, jgen);
    }    
}
