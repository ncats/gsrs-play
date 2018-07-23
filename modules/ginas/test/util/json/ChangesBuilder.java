package util.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 3/1/16.
 */
public class ChangesBuilder {

    private static Pattern IS_NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private Map<String, Change> map = new HashMap<>();

    private final JsonNode before, after;

    public ChangesBuilder(){
    	before=null;
    	after=null;
    }

    public ChangesBuilder(JsonNode before, JsonNode after) {
        this.before = before;
        this.after = after;
    }
    public ChangesBuilder added(String key){
        return change(key, Change.ChangeType.ADDED);
    }

    public ChangesBuilder removed(String key){
        return change(key, Change.ChangeType.REMOVED);
    }
    public ChangesBuilder replace(String key){
        return change(key, Change.ChangeType.REPLACED);
    }


    public ChangesBuilder change(Change change){
        map.put(change.getKey(), change);
        return this;
    }

    public ChangesBuilder deleteFromMap(String key){
        map.remove(key);
        return this;
    }
    
    public ChangesBuilder added(String key, String value){
    	return change(Change.add(key, value));
    }
    public ChangesBuilder removed(String key, String oldValue, String newValue){
    	return change(Change.remove(key, oldValue));
    }
    public ChangesBuilder replaced(String key, String oldValue, String newValue){
    	return change(Change.replace(key, oldValue, newValue));
    }


    private ChangesBuilder change(String key, Change.ChangeType type){
       // String[] path = key.split("/");
        JsonNode currentBefore= null;
        JsonNode currentAfter=null;
        String key2=key;
        switch(type){
            case ADDED: 
            	currentAfter=after; 
//            	key2=JsonUtil.normalizePath("add", key, before);
            	break;
            case REMOVED: currentBefore = before; break;
            case REPLACED:
                currentAfter = after;
                currentBefore = before;
                break;
            default:
                //shouldn't happen unless we add a new type
                //and forget to add it to switch
                throw new IllegalStateException("unknown type "+type);

        }
        
        map.put(key,new Change(key,getNodeFromPath(currentBefore, key),
                				   getNodeFromPath(currentAfter, key2),
                type));

        return this;

    }

    private String getNodeFromPath(JsonNode root, String path) {
        JsonNode current= root;
        if(root ==null) {
            return null;
        }
        JsonNode node= root.at(path);
        if(node ==null) {
            return null;
        }

        return JsonUtil.toString(node);
    }

    public Changes build(){
        //copy map so future changes don't affect already built objs
        return new Changes(new HashMap<>(map));
    }
}
