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

public class NameSerializer extends JsonSerializer<Name> {
    ObjectMapper mapper = new ObjectMapper ();    
    public NameSerializer () {}
    public void serialize (Name name, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        provider.defaultSerializeField("uuid", name.uuid, jgen);
        provider.defaultSerializeField("created", name.created, jgen);       
        provider.defaultSerializeField("lastEdited", name.lastEdited, jgen);
        provider.defaultSerializeField("lastEditedBy", name.lastEditedBy, jgen);
        provider.defaultSerializeField("deprecated", name.deprecated, jgen);
        provider.defaultSerializeField("name", name.name, jgen);
        provider.defaultSerializeField("type", name.type, jgen);
        provider.defaultSerializeField("domains", toArray (name.domains), jgen);
        provider.defaultSerializeField("languages", toArray (name.languages), jgen);
        provider.defaultSerializeField("nameJurisdiction", toArray (name.nameJurisdiction), jgen);
        provider.defaultSerializeField("nameOrgs", name.nameOrgs, jgen);
        provider.defaultSerializeField("preferred", name.preferred, jgen);
        jgen.writeEndObject();
    }

    String[] toArray (List<Keyword> keywords) {
        String[] ary = new String[keywords.size()];
        for (int i = 0; i < ary.length; ++i) {
            ary[i] = keywords.get(i).term;
        }
        return ary;
    }
}
