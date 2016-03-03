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
        String[] path = key.split("/");
        JsonNode currentBefore= null;
        JsonNode currentAfter=null;
        switch(type){
            case ADDED: currentAfter=after; break;
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

        map.put(key,new Change(key,getNodeFromPath(currentBefore, path),
                getNodeFromPath(currentAfter, path),
                type));

        return this;

    }

    private String getNodeFromPath(JsonNode root, String[] path) {
        JsonNode current= root;
        if(current ==null) {
            return null;
        }
        //paths start with leading '/' so skip that?
        for (int i = 1; i < path.length; i++) {
            String fieldName = path[i];
            Matcher m = IS_NUMERIC_PATTERN.matcher(fieldName);
            if (m.matches()) {
                //array ref
                current = current.get(Integer.parseInt(fieldName));
            } else {
                current = current.get(path[i]);
            }
        }

        return current.asText();
    }

    public Changes build(){
        //copy map so future changes don't affect already built objs
        return new Changes(new HashMap<>(map));
    }
}
