package util.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * Created by katzelda on 2/26/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Change {

    public enum ChangeType {
        REMOVED("remove"),
        ADDED("add"),
        REPLACED("replace")
        ;

        private String op;

        ChangeType(String op){
            this.op = op;
        }


    }

    private final String key;
    private final String newValue, oldValue;

    private final ChangeType type;

    
    public static Change add(String key, String newValue){
    	return new Change(key,null,newValue, ChangeType.ADDED);
    }
    public static Change remove(String key, String oldValue){
    	return new Change(key,oldValue,null, ChangeType.REMOVED);
    }
    public static Change replace(String key, String oldValue, String newValue){
    	return new Change(key,oldValue,newValue, ChangeType.REPLACED);
    }
    public Change(String key, String oldValue, String newValue, ChangeType type) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(type);

        if(oldValue ==null && newValue ==null){
            throw new NullPointerException("old value and new value both can not be null");
        }
        this.key = key;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.type = type;
    }

    public String getKey() {
        return key;
    }
    @JsonProperty("value")
    public String getNewValue() {
        return newValue;
    }

    public String getPath(){
        String value = getKey();
        if(value.charAt(0) == '/'){
            return value.substring(1);
        }
        return value;
    }

    public String getOldValue() {
        return oldValue;
    }

    @JsonIgnore
    public ChangeType getType() {
        return type;
    }

    public String getOp(){
        return type.op;
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Change change = (Change) object;

        if (!key.equals(change.key)) return false;
        if (newValue != null ? !newValue.equals(change.newValue) : change.newValue != null) return false;
        if (oldValue != null ? !oldValue.equals(change.oldValue) : change.oldValue != null) return false;
        if (type != change.type) return false;

        return true;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + key.hashCode();
        result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
        result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "Change{" +
                "key='" + key + '\'' +
                ", newValue=" + newValue +
                ", oldValue=" + oldValue +
                ", type=" + type +
                '}';
    }
}
