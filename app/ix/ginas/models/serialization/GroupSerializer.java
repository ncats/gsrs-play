package ix.ginas.models.serialization;

import ix.core.models.Group;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


public class GroupSerializer extends JsonSerializer<Collection<Group>> {
    public GroupSerializer () {}

    public void serialize (Collection<Group> groups, JsonGenerator jgen,
                           SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        if(groups!=null){
	        for(Group grp : groups) {
	            provider.defaultSerializeValue(grp.name, jgen);
	        }
        }

        jgen.writeEndArray();
    }
}
