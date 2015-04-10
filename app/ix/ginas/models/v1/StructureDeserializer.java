package ix.ginas.models.v1;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.core.JsonToken;

import ix.core.models.Structure;

public class StructureDeserializer extends JsonDeserializer<Structure> {
    class StructureProblemHandler
        extends  DeserializationProblemHandler {
        StructureProblemHandler () {
        }
        
        public boolean handleUnknownProperty
            (DeserializationContext ctx, JsonParser parser,
             JsonDeserializer deser, Object bean, String property) {
            try {
                Structure struc = (Structure)bean;
                System.err.println("## handling "+property);
                JsonToken tok = parser.getCurrentToken();
                parser.skipChildren();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            return true;
        }
    }

    StructureProblemHandler handler = new StructureProblemHandler ();
    
    public StructureDeserializer () {
        
    }

    public Structure deserialize (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
        ctx.getConfig().withHandler(handler);
        JsonNode tree = parser.getCodec().readTree(parser);
        Structure struc = parser.getCodec().treeToValue(tree, Structure.class);
        return struc;
    }
}
