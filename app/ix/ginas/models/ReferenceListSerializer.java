package ix.ginas.models;

import ix.core.models.Keyword;
import ix.core.models.Value;
import ix.ginas.models.GinasCommonSubData;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ReferenceListSerializer extends JsonSerializer<Set<Keyword>> {
    public ReferenceListSerializer () {}

    public void serialize (Set<Keyword> list, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        Set<String> refs = new LinkedHashSet<String>();
        for (Keyword val : list) {
            if (GinasCommonSubData.REFERENCE.equals(val.label) || val.label==null) {
                Keyword kw = (Keyword)val;
                refs.add(kw.term);
            }
        }
        provider.defaultSerializeValue(refs, jgen);
    }    
}
	