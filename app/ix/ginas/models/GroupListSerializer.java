package ix.ginas.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ix.core.models.Group;
import ix.core.models.Principal;

import java.io.IOException;


public class GroupListSerializer extends JsonSerializer<Group> {
    public GroupListSerializer () {}
    public void serialize (Group g, JsonGenerator jgen,
                           SerializerProvider provider)
            throws IOException, JsonProcessingException {
        provider.defaultSerializeValue(g.name, jgen);
    }
}
