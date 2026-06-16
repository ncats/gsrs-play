package ix.ginas.exporters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasUtils;

/**
 * Created by epuzanov on 8/30/21.
 */
public class JsonPortableExporter implements Exporter<Substance> {
    private final BufferedWriter out;

    private static final String LEADING_HEADER= "\t\t";
    private final ObjectWriter writer =  EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().writer();
    private static final List<String> fieldsToRemove = Arrays.asList("id", "uuid", "created", "createdBy", "lastEdited", "lastEditedBy", "_self", "self", "_isClassification", "_approvalIDDisplay", "approvalID", "approved", "approvedBy", "changeReason", "status", "refuuid");
    private static JsonNode codeSystem = new TextNode(GinasUtils.getApprovalIdGenerator().getName());

    public JsonPortableExporter(OutputStream out) throws IOException{
        Objects.requireNonNull(out);
        this.out = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
    }
    @Override
    public void export(Substance obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree(writer.writeValueAsString(obj));
        out.write(LEADING_HEADER);
        out.write(this.makePortable(tree).toString());
        out.newLine();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private JsonNode makePortable(JsonNode tree) {
        ((ObjectNode) tree).set("_origin", tree.get("_self"));
        ((ObjectNode) tree).set("_codeSystem", codeSystem);
        if (tree.has("approvalID")) {
            boolean found = false;
            String approvalID = tree.get("approvalID").textValue();
            ArrayNode codes = (ArrayNode)tree.at("/codes");
            for (JsonNode code: codes) {
                if (approvalID.equals(code.get("code").textValue()) && "PRIMARY".equals(code.get("type").textValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                try {
                    codes.add(new ObjectMapper().readTree("{\"deprecated\":false,\"codeSystem\":\"" + codeSystem + "\",\"code\":\"" + approvalID + "\",\"type\":\"PRIMARY\",\"references\":[],\"access\":[]}"));
                } catch (Exception e) {
                }
            }
        }
        Map<JsonNode, IntNode> references = new HashMap<JsonNode, IntNode>();
        int i = 0;
        for (JsonNode r: (ArrayNode) tree.at("/references")) {
            references.put(r.get("uuid"), new IntNode(i++));
        }
        for (JsonNode refsNode: tree.findValues("references")) {
            if (refsNode.isArray()) {
                ArrayNode refs = (ArrayNode) refsNode;
                for (i = 0; i < refs.size(); i++) {
                    JsonNode ref = refs.get(i);
                    if (ref.isTextual()) {
                        refs.set(i, references.get(ref));
                    }
                }
            }
        }
        for (JsonNode n: tree.findParents("created")) {
            ((ObjectNode) n).remove(fieldsToRemove);
        }
        return tree;
    }
}