package ix.ginas.models.serialization;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.Principal;

public class PrincipalSerializer extends JsonSerializer<Principal> {
    public PrincipalSerializer () {}
    public void serialize (Principal p, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        provider.defaultSerializeValue(p.username, jgen);
    }
}
