package ix.ginas.models.serialization;

import java.util.Date;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;

public class DateDeserializer extends JsonDeserializer<Date> {
    public DateDeserializer () {
    }

    public Date deserialize (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
        return new Date (parser.getValueAsLong());
    }
}
