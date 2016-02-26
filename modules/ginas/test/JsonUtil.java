import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

import java.io.IOException;
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

        public JsonBuilder add(String key, JsonBuilder child){
            addLeadingCommaIfNeeded();

            quote(builder, key);
            builder.append(':')
                    //child builder should already have leading brace
                    .append(child.builder)
                    //but trailing brace not added until toJson()
                    .append("}");
            numFields++;
            return this;
        }
        public JsonBuilder add(String key, boolean value){
            addLeadingCommaIfNeeded();
            quote(builder, key);
            builder.append(':');
            //don't quote boolean
            builder.append(Boolean.toString(value));

            numFields++;

            return this;
        }
        public JsonBuilder add(String key, Object value){
            addLeadingCommaIfNeeded();
            quote(builder, key);
            builder.append(':');
            quote(builder, value);

            numFields++;

            return this;
        }

        public JsonBuilder add(String key, long value){
            addLeadingCommaIfNeeded();
            quote(builder, key);
            builder.append(':');
            quote(builder, value);

            numFields++;

            return this;
        }

        public JsonBuilder add(String key, Object[] value){
            addLeadingCommaIfNeeded();

            quote(builder, key);
            if(value.length ==0){
                //special case handle empty
                builder.append(":[ ]");
            }else {
                builder.append(":[");

                for (Object o : value) {
                    quote(builder, o);
                    builder.append(',');
                }

                builder.setLength(builder.length() - 1); // remove trailing comma
                builder.append(']');
            }
            numFields++;

            return this;
        }

        public JsonBuilder add(String key, int[] value){
            addLeadingCommaIfNeeded();
            quote(builder, key);
            if(value.length ==0){
                //special case handle empty
                builder.append(":[ ]");
            }else {
                builder.append(":[");
                for (int i : value) {
                    builder.append(i).append(',');
                }
                builder.setLength(builder.length() - 1); // remove trailing comma
                builder.append(']');
            }
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
                String content = builder.append("}").toString();
               // System.out.println(content);

                return mapper.readTree(content);
            } catch (IOException e) {
                throw new IllegalStateException("error building Json", e);
            }
        }
    }
}
