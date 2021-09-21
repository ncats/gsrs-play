package ix.ginas.indexers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.burt.jmespath.JmesPath;
import io.burt.jmespath.Expression;
import io.burt.jmespath.jackson.JacksonRuntime;

import ix.core.controllers.EntityFactory;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.Substance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Egor Puzanov on 9/10/2021.
 */
public class JmespathIndexValueMaker implements IndexValueMaker<Substance> {

    private List<Expression<JsonNode>> expressions = new ArrayList<Expression<JsonNode>>();
    private final ObjectWriter writer = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().writer();

    @Override
    public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode tree = mapper.readTree(writer.writeValueAsString(substance));
            for (Expression<JsonNode> expression: expressions) {
                for (JsonNode result: expression.search(tree)) {
                    Iterator<Map.Entry<String, JsonNode>> fields = result.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        consumer.accept(IndexableValue.simpleFacetStringValue(field.getKey(), field.getValue().textValue()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Expression<JsonNode>> getExpressions() {
        return this.expressions;
    }

    public void setExpressions(List<Object> expressions) {
        JmesPath<JsonNode> jmespath = new JacksonRuntime();
        this.expressions.clear();
        for (Object expression: expressions) {
            if (expression instanceof Expression) {
                this.expressions.add((Expression<JsonNode>) expression);
            } else {
                try {
                    this.expressions.add((Expression<JsonNode>) jmespath.compile((String) expression));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}