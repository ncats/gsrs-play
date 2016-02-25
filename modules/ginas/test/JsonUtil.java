import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by katzelda on 2/25/16.
 */
public class JsonUtil {


    public enum ChangeType{
        REMOVED,
        ADDED
    }

    public static class Change{

        private final JsonNode value;
        private final ChangeType type;

        public Change(JsonNode value, ChangeType type) {
            Objects.requireNonNull(value);
            Objects.requireNonNull(type);
            this.value = value;
            this.type = type;
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

            if (!value.equals(change.value)) return false;
            return type == change.type;

        }

        @Override
        public int hashCode() {
            int result = value.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Change{" +
                    "value=" + value +
                    ", type=" + type +
                    '}';
        }
    }

    public static Map<String, Change> getDestructiveChanges(JsonNode before, JsonNode after ){
        JsonNode jp = JsonDiff.asJson(before,after);
        Map<String, Change> changes = new HashMap<>();
        for(JsonNode jn:jp){

            final String op = jn.get("op").asText();

            if("remove".equals(op)){

                JsonNode jsbefore=before.at(jn.get("path").textValue());
                //TODO check if jsbefore is equivalent to null in some way:
                // [], {}, "", [""]
                if(jsbefore.toString().equals("[\"\"]")){

                }else{
                    changes.put(jn.get("path").asText(), new Change(jsbefore, ChangeType.REMOVED));

                }

                //System.out.println("Error:" + jn + " was:" + before.at(jn.get("path").textValue()));
            }else if("add".equals(op)){
                JsonNode jsAfter=after.at(jn.get("path").textValue());
                changes.put(jn.get("path").asText(), new Change(jsAfter, ChangeType.ADDED));
            }
        }

        return changes;
    }



    public static class JsonBuilder{
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder builder = new StringBuilder("{");

        int numFields=0;

        public JsonBuilder(){

        }
        public JsonBuilder add(String key, String value){
            addLeadingCommaIfNeeded();
            builder.append('\"').append(key).append('\"').append(':')
                    .append('\"').append(value).append('\"');

            numFields++;

            return this;
        }

        public JsonBuilder add(String key, long value){
            addLeadingCommaIfNeeded();
            builder.append('\"').append(key).append('\"').append(':')
                    .append('\"').append(value).append('\"');

            numFields++;

            return this;
        }

        public JsonBuilder add(String key, Object[] value){
            addLeadingCommaIfNeeded();
            quote(builder, key);
            builder.append(":[");
            for(Object o : value){
                quote(builder, o);
                builder.append(',');
            }
            builder.setLength(builder.length() -1); // remove trailing comma
            builder.append(']');
            builder.append('\"').append(key).append('\"').append(':')
                    .append('\"').append(Arrays.toString(value)).append('\"');

            numFields++;

            return this;
        }
        private void quote(StringBuilder builder, long l){
            builder.append(l);
        }
        private void quote(StringBuilder builder, Object obj){
            builder.append('\"').append(obj).append('\"');
        }

        private void addLeadingCommaIfNeeded() {
            if(numFields>0){
                builder.append(',');
            }
        }

        public JsonNode toJson() {
            try {
                return mapper.readTree(builder.append("}").toString());
            } catch (IOException e) {
                throw new IllegalStateException("error building Json", e);
            }
        }
    }
}
