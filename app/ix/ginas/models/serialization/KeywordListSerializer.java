package ix.ginas.models.serialization;

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

public class KeywordListSerializer extends JsonSerializer<List<Keyword>> {
    public KeywordListSerializer () {}
    public void serialize (List<Keyword> keywords, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        //System.out.println("Keywords:" + keywords);
        for (Keyword kw : keywords) {
            provider.defaultSerializeValue(kw.term, jgen);
        }
        jgen.writeEndArray();
    }
}
