package ix.ginas.models.v1;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;

import ix.core.models.Structure;

public class MoietyDeserializer extends JsonDeserializer<Moiety> {
    public MoietyDeserializer () {
    }

    public Moiety deserialize (JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
        JsonNode tree = parser.getCodec().readTree(parser);
        Moiety moiety = new Moiety ();
        moiety.structure =
            parser.getCodec().treeToValue(tree, GinasChemicalStructure.class);
        JsonNode n = tree.get("count");
        if (n != null) {
            moiety.count = n.asInt();
        }
        moiety.enforce();
        return moiety;
    }
}
