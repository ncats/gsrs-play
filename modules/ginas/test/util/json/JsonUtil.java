package util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by katzelda on 2/25/16.
 */
public class JsonUtil {

	public static String toString(JsonNode jsn){
		if(jsn.isTextual())return jsn.asText();
		return jsn.toString();
	}

	//public static JsonNode
	
	
	// TP: This method takes arbitrary objects, serialized them, and returns the difference
	// this may be useful if you'd like to see the difference between random seralizable objects
	public static Changes computeChanges(Object before, Object after, ChangeFilter...filters ){
		ObjectMapper om = new ObjectMapper();
		JsonNode beforeNode=om.valueToTree(before);
		JsonNode afterNode=om.valueToTree(after);
		return computeChanges(beforeNode,afterNode,filters);
	}
	
	
	
    public static Changes computeChanges(JsonNode before, JsonNode after, ChangeFilter...filters ){
        JsonNode jp = JsonDiff.asJson(before,after);
        //System.out.println("THE CHANGES:" + jp);
        Map<String, Change> changes = new HashMap<>();

        //label is for short circuiting
        //during filter step
        NODE_LOOP: for(JsonNode jn:jp){

            final String op = jn.get("op").asText();
            Change change=null;
            if("remove".equals(op)){

                JsonNode jsbefore=before.at(jn.get("path").textValue());
                if(jsbefore.toString().equals("[\"\"]") ||
                   jsbefore.toString().equals("{}") ||
                   jsbefore.toString().equals("") ||
                   jsbefore.toString().equals("[]")
                		){

                }else{
                    String key = jn.get("path").asText();
                    change = new Change(key, toString(jsbefore), null, Change.ChangeType.REMOVED);
                }

                //System.out.println("Error:" + jn + " was:" + before.at(jn.get("path").textValue()));
            }else if("add".equals(op)){
                String key = jn.get("path").asText();
                JsonNode jsAfter=after.at(key);
                change= new Change(key, null, toString(jsAfter), Change.ChangeType.ADDED);
            }else if("replace".equals(op)){
            	
                String key = jn.get("path").asText();
                String vold=toString(before.at(key));
                String vnew=toString(after.at(key));
                change= new Change(key, vold, vnew, Change.ChangeType.REPLACED);
            }


            if(change !=null){
                for(ChangeFilter filter : filters){
                    if(filter.filterOut(change)){
                        continue NODE_LOOP;
                    }
                }
                //if we get this far we didn't filter out change
                changes.put(change.getKey(), change);
            }
        }

        return new Changes(changes);
    }


    public static class JsonNodeBuilder{
    	final JsonNode oldJson;
    	public static class JsonChange{
    		public String op;
    		public String path;
    		public String value;
    		public String from;
    		public static JsonChange set(String path, String value){
    			JsonChange jsc=new JsonChange();
    			jsc.op="replace";
    			jsc.path=path;
    			jsc.value=value;
    			return jsc;
    		}
    		public static JsonChange remove(String path){
    			JsonChange jsc=new JsonChange();
    			jsc.op="remove";
    			jsc.path=path;
    			return jsc;
    		}
    		public static JsonChange add(String path, String value){
    			JsonChange jsc=new JsonChange();
    			jsc.op="add";
    			jsc.path=path;
    			jsc.value=value;
    			return jsc;
    		}
    		public static JsonChange move(String path, String from){
    			JsonChange jsc=new JsonChange();
    			jsc.op="move";
    			jsc.path=path;
    			jsc.from=from;
    			return jsc;
    		}
    	}
    	List<JsonChange> changes = new ArrayList<JsonChange>();
    	public JsonNodeBuilder(JsonNode jt){
    		this.oldJson=jt.deepCopy();
    	}
    	
    	public JsonNodeBuilder set(String path, String value){
    		try{
	    		if(oldJson.at(path).isNull()){
	    			changes.add(JsonChange.add(path, value));
	    			return this;
	    		}
    		}catch(Exception e){
    			
    		}
    		changes.add(JsonChange.set(path, value));
    		return this;
    	}
    	public JsonNodeBuilder remove(String path){
    		changes.add(JsonChange.remove(path));
    		return this;
    	}
    	public JsonNodeBuilder add(String path,String value){
    		changes.add(JsonChange.add(path,value));
    		return this;
    	}
    	public JsonNodeBuilder move(String path, String from){
    		changes.add(JsonChange.move(path,from));
    		return this;
    	}
    	public JsonNode build(){
    		try {
    			//System.out.println("There are these changes, which will be turned into a patch:" + changes.size());
    			JsonPatch jp=JsonPatch.fromJson((new ObjectMapper()).valueToTree(changes));
    			//System.out.println("THE PATCH:" + jp);
				return jp.apply(oldJson);
			} catch (JsonPatchException | IOException e) {
				e.printStackTrace();
			}
    		return null;
    	}
    	
    	
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
