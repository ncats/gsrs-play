package ix.ginas.exporters;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPath;
import io.burt.jmespath.Expression;
import io.burt.jmespath.jackson.JacksonRuntime;

/**
 * Created by epuzanov on 11/8/21.
 */
public class JmespathColumnValueRecipe<T> implements ColumnValueRecipe<T> {

    private final String columnName;
    private final Expression<JsonNode> expression;
    private final String delimiter;

    public JmespathColumnValueRecipe(String columnName, Expression<JsonNode> expression, String delimiter) {
        Objects.requireNonNull(columnName);
        Objects.requireNonNull(expression);
        Objects.requireNonNull(delimiter);
        this.columnName = columnName;
        this.expression = expression;
        this.delimiter = delimiter;
    }

    static <T>  ColumnValueRecipe<T> create(Enum<?> enumValue, String expression, String delimiter) {
        return create(enumValue.name(), expression, delimiter);
    }

    static <T>  ColumnValueRecipe<T> create(String columnName, String expression, String delimiter) {
        JmesPath<JsonNode> jmespath = new JacksonRuntime();
        return new JmespathColumnValueRecipe<T>(columnName, jmespath.compile(expression), delimiter);
    }

    @Override
    public int writeValuesFor(Spreadsheet.SpreadsheetRow row, int currentOffset, T obj) {
        JsonNode results = expression.search((JsonNode) obj);
        if (results.isValueNode() && ! results.isNull()) {
            row.getCell(currentOffset).writeString(results.asText());
        } else if (results.isArray()) {
            StringBuilder sb = new StringBuilder();
            for(JsonNode result: results){
                if (result.isValueNode() && ! result.isNull()) {
                    String value = result.asText();
                    if (value != null && !value.isEmpty()) {
                        if(sb.length()!=0){
                            sb.append(delimiter);
                        }
                        sb.append(value);
                    }
                }
            }
            if (sb.length()!=0) {
                row.getCell(currentOffset).writeString(sb.toString());
            }
        }
        return 1;
    }

    @Override
    public int writeHeaderValues(Spreadsheet.SpreadsheetRow row, int currentOffset) {
        row.getCell(currentOffset).writeString(columnName);
        return 1;
    }

    @Override
    public boolean containsColumnName(String name) {
        return Objects.equals(columnName, name);
    }

    @Override
    public JmespathColumnValueRecipe<T> replaceColumnName(String oldName, String newName) {
        Objects.requireNonNull(oldName);
        Objects.requireNonNull(newName);

        if(containsColumnName(oldName)){
            return new JmespathColumnValueRecipe<>(newName, expression, delimiter);
        }
        return this;
    }
}
