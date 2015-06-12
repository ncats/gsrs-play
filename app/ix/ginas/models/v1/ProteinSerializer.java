package ix.ginas.models.v1;

import java.lang.reflect.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.models.Keyword;
import ix.ginas.models.Ginas;
import ix.ginas.models.utils.JSONEntity;

public class ProteinSerializer extends JsonSerializer<Protein> {
    public ProteinSerializer () {}
    public void serializeValue (Protein prot, JsonGenerator jgen,
                                SerializerProvider provider)
        throws IOException, JsonProcessingException {
        if (prot == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }

        provider.defaultSerializeField("sequenceType", prot.sequenceType, jgen);
        provider.defaultSerializeField("subunits", prot.subunits, jgen);
        provider.defaultSerializeField("disulfideLinks", prot.disulfideLinks, jgen);
        provider.defaultSerializeField("glycosylation", prot.glycosylation, jgen);
        provider.defaultSerializeField("modifications", prot.modifications, jgen);
        provider.defaultSerializeField("created", prot.created, jgen);
        provider.defaultSerializeField("deprecated", prot.deprecated, jgen);

        List<String> refs = new ArrayList<String>();
        for (Value val : prot.references) {
        	if (Ginas.REFERENCE.equals(val.label)) {
                Keyword kw = (Keyword)val;
                refs.add(kw.term);
            }
        }
        provider.defaultSerializeField("references", refs, jgen);
    }
    
    public void serialize (Protein prot, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        serializeValue (prot, jgen, provider);
        jgen.writeEndObject();
    }
}
