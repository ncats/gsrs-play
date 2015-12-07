package ix.ginas.models.v1;

import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.ginas.models.GinasSubData;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ReferenceListSerializer extends JsonSerializer<Set<Value>> {
    public ReferenceListSerializer () {}

    public void serialize (Set<Value> list, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        Set<String> refs = new LinkedHashSet<String>();
        for (Value val : list) {
            if (GinasSubData.REFERENCE.equals(val.label)) {
                Keyword kw = (Keyword)val;
                refs.add(kw.term);
            }
        }
        provider.defaultSerializeValue(refs, jgen);
    }    
}
	