package ix.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.controllers.ValueFactory;
import ix.core.models.Keyword;
import ix.core.models.Mesh;
import ix.core.models.Text;
import ix.core.models.VBin;
import ix.core.models.VInt;
import ix.core.models.VNum;
import ix.core.models.VRange;
import ix.core.models.VStr;
import ix.core.models.Value;

public class AbstractValueDeserializer extends JsonDeserializer<Value> {
	ObjectMapper om = new ObjectMapper();
	public static List<Class<? extends Value>> classes = new ArrayList<Class<? extends Value>>();

	static {
		classes.add(Keyword.class);
		classes.add(Text.class);
		classes.add(VBin.class);
		classes.add(VInt.class);
		classes.add(VNum.class);
		classes.add(VRange.class);
		classes.add(VStr.class);
		classes.add(Mesh.class);
		classes.add(Value.class);
	}

	public Value deserialize(JsonParser parser, DeserializationContext ctx)
			throws IOException, JsonProcessingException {
		ObjectNode objectNode = parser.readValueAsTree();
		Long l = objectNode.at("/id").longValue();
		Value v = null;

		for (Class<? extends Value> c : classes) {
			try {
				v = om.treeToValue(objectNode, c);
				break;
			} catch (Exception e) {

			}
		}
		if (v == null && l != null) {
			v = ValueFactory.finder.byId(l);
		}

		return v;
	}

}
