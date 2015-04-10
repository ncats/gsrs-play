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
import ix.core.models.Keyword;
import ix.core.models.Value;

public class MoietySerializer extends JsonSerializer<Moiety> {
    StructureSerializer serializer = new StructureSerializer ();
    public MoietySerializer () {
    }

    public void serialize (Moiety moiety, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        serializer.serializeValue(moiety.structure, jgen, provider);
        provider.defaultSerializeField("count", moiety.count, jgen);
        jgen.writeEndObject();
    }
}
