package util.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * Created by katzelda on 2/26/16.
 */
public class Change {

    public enum ChangeType {
        REMOVED,
        ADDED
    }

    private final String key;
    private final JsonNode value;
    private final ChangeType type;

    public Change(String key, JsonNode value, ChangeType type) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(type);
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public JsonNode getValue() {
        return value;
    }

    public ChangeType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change change = (Change) o;

        if (!key.equals(change.key)) return false;
        if (value != null ? !value.equals(change.value) : change.value != null) return false;
        return type == change.type;

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Change{ key=" + key +

                ", value=" + value +
                ", type=" + type +
                '}';
    }
}
