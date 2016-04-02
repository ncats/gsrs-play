package ix.ginas.models.v1;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ix.ginas.models.StructureSerializer;

public class MoietySerializer extends JsonSerializer<Moiety> {
    StructureSerializer serializer = new StructureSerializer ();
    public MoietySerializer () {
    }

    public void serialize (Moiety moiety, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        serializer.serializeValue(moiety.structure, jgen, provider);
        provider.defaultSerializeField("count", moiety.getCount(), jgen);
        jgen.writeEndObject();
    }
}
