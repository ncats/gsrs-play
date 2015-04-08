package ix.ginas.models.v1;

import java.lang.reflect.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.models.Structure;

public class MoietySerializer extends JsonSerializer<Moiety> {
    ObjectMapper mapper = new ObjectMapper ();
    public MoietySerializer () {
    }

    public void serialize (Moiety moiety, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        ObjectNode node;
        if (moiety.structure != null) {
            node = (ObjectNode)mapper.valueToTree(moiety.structure);
        }
        else {
            node = mapper.createObjectNode();
        }
        node.put("count", moiety.count);
        provider.defaultSerializeValue(node, jgen);     
    }
}
