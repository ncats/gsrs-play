package ix.ginas.models.serialization;

import ix.core.controllers.AdminFactory;
import ix.core.models.Group;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;



public class GroupListDeserializer extends JsonDeserializer<Set<Group>> {
    public GroupListDeserializer() {
    }

    public Set<Group> deserialize
            (JsonParser parser, DeserializationContext ctx)
            throws IOException, JsonProcessingException {

    	Set<Group> groups = new LinkedHashSet<Group>();
        if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
            while (JsonToken.END_ARRAY != parser.nextToken()) {
                String name = parser.getValueAsString();
                Group grp = AdminFactory.groupfinder.where().eq("name", name).findUnique();
                if(grp == null){
                    grp = AdminFactory.registerGroupIfAbsent(new Group(name));
                }
                groups.add(grp);
            }
        } else {}
        return groups;
    }
}


