package ix.ginas.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ix.core.models.Group;

import java.io.IOException;
import java.util.List;


public class GroupListSerializer extends JsonSerializer<List<Group>> {
    public GroupListSerializer () {}

    public void serialize (List<Group> groups, JsonGenerator jgen,
                           SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        for(Group grp : groups) {
            provider.defaultSerializeValue(grp.name, jgen);
        }

        jgen.writeEndArray();
    }
}
