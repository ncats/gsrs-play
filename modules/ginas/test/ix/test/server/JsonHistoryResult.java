package ix.test.server;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by katzelda on 3/17/16.
 */
public class JsonHistoryResult {
    private final JsonNode historyNode;

    private final JsonNode oldValue, newValue;

    public JsonHistoryResult(JsonNode historyNode, JsonNode oldValue, JsonNode newValue) {
        this.historyNode = historyNode;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public JsonNode getHistoryNode() {
        return historyNode;
    }

    public JsonNode getOldValue() {
        return oldValue;
    }

    public JsonNode getNewValue() {
        return newValue;
    }
}
