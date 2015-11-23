package ix.ginas.models;

import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.models.Group;
import ix.core.models.Keyword;
import ix.core.models.Principal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;



public class GroupListDeserializer extends JsonDeserializer<List<Group>> {
        public GroupListDeserializer () {
        }

        public List<Group> deserialize
                (JsonParser parser, DeserializationContext ctx)
                throws IOException, JsonProcessingException {

            List<Group> groups = new ArrayList<Group>();
            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                while (JsonToken.END_ARRAY != parser.nextToken()) {
                    String acc = parser.getValueAsString();
                   // groups.add(AdminFactory.registerGroupIfAbsent(new Group(acc)));
                    groups.add(new Group(acc));
                }
            }
            else {
            }

            return groups;
        }
    }



