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



public class GroupDeserializer extends JsonDeserializer<Group> {
    
	public GroupDeserializer() {
    }

    public Group deserialize
            (JsonParser parser, DeserializationContext ctx)
            throws IOException, JsonProcessingException {

    	String name=parser.getValueAsString();
    	Group grp = AdminFactory.registerGroupIfAbsent(new Group(name));
    	
        return grp;
    }
}


