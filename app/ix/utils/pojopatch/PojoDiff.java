package ix.utils.pojopatch;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

import ix.core.controllers.EntityFactory;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.utils.Util;


/**
 * Author: Tyler Peryea
 * 
 * 
 * The purpose of this set of utilities will be to bring the flexibility and
 * ease-of-use of JSON serialization, JSONPatch and JSONDiff to objects themselves.
 * 
 * Specifically, the intended use is eventually to apply a JSONPatch directly to a
 * source object, as if it had been serialized to JSON, applied, and then deserialized 
 * back. That naive direct approach is not always sufficient, as the specific objects
 * meant to be patched may have additional information that is needed for tracking (e.g.
 * ebean enrichment) which will be lost.
 * 
 * Right now, these utilities (still incomplete) do the following:
 * 
 * 1. Serialize to objects to JSON
 * 2. Get the JSONPatch from the first to second
 * 3. Find the ACTUAL object instance that each "path" in the JSONPatch is
 *    referencing (by reflection / recursion, attempting to honor Jackson's annotations)
 * 4. Set the fetched ACTUAL object instance from the *new* object to the actual location
 *    of the *existing* Object (again using Jackson's annotations for setters)
 * 5. Recursively call the appropriate setters all the way up the tree, to ensure that
 *    any specific setting methods expected to be called are called.
 * 6. Keep track of each object instance where a mutate operation was called
 * 
 *  
 * What is not yet done:
 * 	[?]	"move" and "copy" operations of JSONPatch are not applied
 *  [/] A JSONPatch itself can not be applied an existing object
 * 	[/] Array handling is likely broken
 *  [/] Certain Collection operations are also problematic, specifically involving
 *      the ambiguous '/-' JSONPointer notation, which may mean different things
 *      depending on context
 * 
 * 
 * 
 *  [ ]=No fix attempted
 *  [?]=Possibly fixed, untested
 *  [/]=Probably fixed, a little tested
 *  [X]=Fixed, tested
 * 
 */

public class PojoDiff {
	public static ObjectMapper _mapper = EntityFactory.EntityMapper.INTERNAL_ENTITY_MAPPER();
	
	public static Function<Object, Optional<String>> IDGetter = (o)->{
		EntityWrapper<?> ew = EntityWrapper.of(o);
		return ew.getOptionalKey().map(k->k.getIdString());
	};
	
	public static class JsonObjectPatch<T> implements PojoPatch<T>{
		private JsonPatch jp;
		private Class<T> cls;
		public JsonObjectPatch(JsonPatch jp, Class<T> cls){
			this.cls=cls;
			this.jp=jp;
		}
		public Stack<?> apply(T old, ChangeEventListener ... changeListener) throws Exception{
			
			JsonNode oldjson=EntityWrapper.of(old)
					                      .toFullJsonNode();
			JsonNode newjson=jp.apply(oldjson);
			T ntarget=EntityWrapper.of(old).getEntityInfo().fromJsonNode(newjson);
			return new EnhancedObjectPatch(old,ntarget).apply(old, changeListener);
		}
		@Override
		public List<Change> getChanges() {
			throw new UnsupportedOperationException("change list not yet supported");
		}
	}
	
	private static class JsonSimpleNodeChange{
		@JsonProperty
		public String op;
		@JsonProperty
		public String path;
		@JsonProperty
		public String from;
		@JsonProperty
		public boolean report=true;
		
		public JsonSimpleNodeChange(String op, String path, String from){
			this.op=op;
			this.path=path;
			this.from=from;
		}
		
		public JsonNode asJsonNode(){
			return _mapper.valueToTree(this);
		}
		
		@SuppressWarnings("unused")
		public static JsonSimpleNodeChange MOVE_OP(String from, String to){
			return new JsonSimpleNodeChange("move",to, from);
		}
		public static JsonSimpleNodeChange COPY_OP(String from, String to){
			return new JsonSimpleNodeChange("copy",to, from);
		}
		public static JsonSimpleNodeChange REMOVE_OP(String path) {
			return new JsonSimpleNodeChange("remove",path,null);
		}
		public JsonSimpleNodeChange noReport(){
			this.report=false;
			return this;
		}
	}
	
	public static class EnhancedObjectPatch<T> implements PojoPatch<T>{
		private T oldV;
		private T newV;
		private JsonNode jps;
		private JsonNode[] oldAndNew = new JsonNode[2];
		
		private JsonPatch plainOldJsonPatch=null;
		
		
		public EnhancedObjectPatch(T oldV, T newV){
			this.oldV=oldV;
			this.newV=newV;
		}
		
		public Stack<?> apply(T old, ChangeEventListener ... changeListener) throws Exception{
			if(old==oldV){
				return applyChanges(oldV, newV, getJsonPatch(),changeListener);
			}else{
				//This is weird, but it actually serializes the original old and new
				//gets the JsonPatch, serializes this target, then applies
				//the patch to this new target, then deserializes.
				//At this point, you have the correct object, but not via mutating the original.
				//To do this, you need to then apply a traditional EnhancedObjectPatch
				//using the original supplied object, and this new target.
				JsonPatch temp= getPlainOldJsonPatch();
				EntityWrapper<T> newOld = EntityWrapper.of(old);
				JsonNode json=newOld.toFullJsonNode();
				T newtarget=newOld.getEntityInfo().fromJsonNode(temp.apply(json));
				return new EnhancedObjectPatch<T>(old, newtarget).apply(old);
			}
		}
		
		private JsonNode getJsonPatch(){
			if(jps==null){
				jps=getEnhancedJsonDiff(oldV,newV,oldAndNew);
			}
			return jps;
		}
		
		private JsonPatch getPlainOldJsonPatch(){
			if(plainOldJsonPatch==null){
				plainOldJsonPatch=	JsonDiff.asJsonPatch(EntityWrapper.of(oldV).toFullJsonNode(), EntityWrapper.of(newV).toFullJsonNode());
			}
			return plainOldJsonPatch;
		}
		
		@Override
		public List<Change> getChanges() {
			List<Change> changes= new ArrayList<Change>();
			
			for(JsonNode jsn:getJsonPatch()){
				Change c=new Change(jsn);
				try{
					String path=jsn.at("/path").asText();
					String old=oldAndNew[0].at(path).toString();
					c.oldValue=old;
				}catch(Exception e){
					e.printStackTrace();
				}
				changes.add(c);
			}
			return changes;
		}
	}
	
	
	
	/**
	 * Return a {@link ix.utils.pojopatch.PojoPatch} which captures the 
	 * differences between the provided objects. This can be used
	 * to mutate an object from the old form to a new form.
	 *  
	 * @param oldValue Original value for the diff
	 * @param newValue New value for the diff
	 * @return {@link ix.utils.pojopatch.PojoPatch} of diff
	 */
	public static <T> PojoPatch<T> getDiff(T oldValue, T newValue){
		//return new EnhancedObjectPatch(oldValue,newValue);
		return new EnhancedObjectPatch<T>(oldValue,newValue);
	}
	
	/**
	 * Mutates the first object to be the same as the second, returning
	 * a unique {@link java.util.Stack} of the changed objects, in the order
	 * that they were changed. 
	 * 
	 * This is the same as calling:
	 * 
	 * <p>
	 * <code>
	 * 		PojoDiff.getDiff(oldValue,newValue).apply(oldValue);
	 * </code>
	 * </p>
	 * @param oldValue The starting value, to be mutated
	 * @param newValue The ending value, which the first value should look like
	 * @return A unique {@link java.util.Stack} of the changed objects, in the order
	 * that they were changed.
	 * @throws Exception
	 */
	public static <T> Stack<?> mutateTo(T oldValue, T newValue) throws Exception{
		//return new EnhancedObjectPatch(oldValue,newValue);
		return getDiff(oldValue,newValue).apply(oldValue);
	}
	
	
	public static <T> PojoPatch<T> getEnhancedDiff(T oldValue, T newValue){
		return new EnhancedObjectPatch<T>(oldValue,newValue);
	}
	
	public static <T> PojoPatch<T> fromJsonPatch(JsonPatch jp, Class<T> type) throws IOException{
		return new JsonObjectPatch<T>(jp, type);
	}
	
	private static String getID(JsonNode o){
		try{
			if(o.isObject()){
				JsonNode id=o.get("uuid");
				if(id!=null && !id.isNull()){
					return id.asText();
				}
				id=o.get("id");
				if(id!=null && !id.isNull()){
					return id.asText();
				}
				return null;
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return null;
	}
	
	
	private static ObjectNode mappify(ObjectNode m2){
		Iterator<String> fields=m2.fieldNames();
		List<String> fieldNames = new ArrayList<String>();
		while(fields.hasNext()){
			fieldNames.add(fields.next());
		}
		
		for(String key:fieldNames){
			JsonNode o=m2.get(key);
			
			//System.out.println("Value is:" + o);
			if(o.isArray()){
				ArrayNode arr=((ArrayNode)o);
				ObjectNode mnew=mappify(arr);
				m2.put(key, mnew);
			}else if(o.isObject()){
				mappify((ObjectNode)o);
			}
		}
		return m2;
	}
	
	private static ObjectNode mappify(ArrayNode o){
		ObjectMapper om=new ObjectMapper();
		ArrayNode arr=((ArrayNode)o);
		
		ObjectNode mnew=om.createObjectNode();
		//Map mnew = new HashMap();
		for(int i=0;i<arr.size();i++){
			JsonNode o2=arr.get(i);
			String id=getID(o2);
			String ind=String.format("%05d", i);
			if(id!=null){
				mnew.set("$" + id + "_" + ind, o2);
			}else{
				mnew.set("_" + ind, o2);
			}
			if(o2.isObject()){
				mappify((ObjectNode)o2);
			}
		}
		return mnew;
	}
	
	
	private static JsonNode mappifyJson(JsonNode js1){
		try {
			if(js1.isArray()){
				return mappify((ArrayNode)js1.deepCopy());
			}else{
				return mappify((ObjectNode)js1.deepCopy());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return js1;
	}
	
	public static void canonicalizeDiff(List<JsonNode> diffs){
		Function<String,String> mapper = path->{
			String npath=path.replaceAll("_([0-9][0-9]*)", "#$1!");
			String[] paths=npath.split("#");
			String newpath="";
			for(int i=1;i<paths.length;i++){
					int k=Integer.parseInt(paths[i].split("!")[0]);
					String np=String.format("%05d", k);
					newpath+=np;
			}
			return newpath;
		};
		
		Collections.sort(diffs,(JsonNode o1, JsonNode o2) -> {
				String path1=o1.at("/path").asText();
				String op1=o1.at("/op").asText();
				
				
				String path2=o2.at("/path").asText();
				String op2=o2.at("/op").asText();
				
				int diff=op1.compareTo(op2);
				if(diff!=0){
					return diff;
				}
				//System.out.println(path1);
				String adaptedPath1 =mapper.apply(path1);
				String adaptedPath2 =mapper.apply(path2);
				
				int d=adaptedPath1.compareTo(adaptedPath2);
				if(op1.equals("remove")){
					d=-d;
				}
				
				return d;
			});

	}
	
	
	public static JsonNode getEnhancedJsonDiff(Object oldValue, Object newValue, JsonNode[] oldAndNewValue){
		ObjectMapper mapper = _mapper;
		JsonNode js1;
		JsonNode js2;
		if(oldValue instanceof JsonNode){
			js1=(JsonNode) oldValue;
		}else{
			js1=mapper.valueToTree(oldValue);
		}
		if(newValue instanceof JsonNode){
			js2=(JsonNode) newValue;
		}else{
			js2=mapper.valueToTree(newValue);
		}
		if(oldAndNewValue==null || oldAndNewValue.length<2){
			oldAndNewValue=new JsonNode[2];
		}
		oldAndNewValue[0]=mappifyJson(js1);
		oldAndNewValue[1]=mappifyJson(js2);
		
		JsonNode diff= JsonDiff.asJson(
				oldAndNewValue[0],
				oldAndNewValue[1]
    			);
		List<JsonNode> reorderedDiffs= new ArrayList<JsonNode>();
		
		JsonNode normalDiff= JsonDiff.asJson(
				js1,
				js2
    			);
		
		
		JsonNode[] cdiffs= new JsonNode[normalDiff.size()];
		HashMap<String,Integer> positions = new HashMap<String,Integer>();
		
		Stream.of(normalDiff)
		      .map(Util.toIndexedTuple())
		      .forEach(jsnt->{
		    	  	String path=jsnt.v().at("/path").asText();
					path=path.replaceAll("[$][^_]*[_]", "").replaceAll("/_[0]*([0-9][0-9]*)","/$1");
					if(path.endsWith("/-")){
						int s=js1.at(path.replaceAll("/-$", "")).size();
						path=path.replaceAll("/-$", "/"+s+"");
					}
					String op=jsnt.v().at("/op").asText();
					positions.put(op + path, jsnt.k());
		      });
		
		for(JsonNode jsn:diff){
			String path=jsn.at("/path").asText();
			path=path.replaceAll("[$][^_]*[_]", "").replaceAll("/_[0]*([0-9][0-9]*)","/$1");
			String op=jsn.at("/op").asText();
			Integer pos=positions.get(op + path);

			if(pos==null){
				reorderedDiffs.add(jsn);
			}else{
				cdiffs[pos]=jsn;
			}
		}
		
		for(JsonNode jsn:cdiffs){
			if(jsn!=null){
				reorderedDiffs.add(jsn);
			}
		}
		
		canonicalizeDiff(reorderedDiffs);
		ArrayNode an=(new ObjectMapper()).createArrayNode();
		an.addAll(reorderedDiffs);
		return an;
		//return normalDiff;
	}
	
	private static <T> Stack<?> applyPatch(T oldValue, JsonPatch jp, ChangeEventListener ... changeListener) throws IllegalArgumentException, JsonPatchException, JsonProcessingException{
		ObjectMapper mapper = _mapper;
		JsonNode oldNode=mapper.valueToTree(oldValue);
		JsonNode newNode=jp.apply(oldNode);
		//cast to T should be safe...
		@SuppressWarnings("unchecked")
		T newValue =  (T) mapper.treeToValue(newNode,oldValue.getClass());
		return applyChanges(oldValue,newValue,null);
	}
	
	public static JsonNode getJsonDiff(Object oldValue, Object newValue){
			ObjectMapper mapper = _mapper;
			return JsonDiff.asJson(
	    			mapper.valueToTree(oldValue),
	    			mapper.valueToTree(newValue)
	    			);
	}
	/**
	 * Returns the unique path, with the embedded ID, and ignoring order
	 * of the supplied JSON pointer-esque path.
	 * 
	 * If the path does not have an embedded ID, returns null
	 * 
	 */
	private static String toUniqueIDPath(String path){
		
		String pathu=path.replaceAll("([$][^_]*[_])[0-9]*", "$1");
		if(!pathu.equals(path)){
			return pathu;
		}
		return null;
	}
	/**
	 * Returns the simplified (non ID-embedded) path
	 * 
	 */
	private static String toStandardPath(String path){
		return path.replaceAll("([$][^_]*)([_][0-9]*)", "$2");
	}
	
	private static <T> Stack<?> applyChanges(T oldValue, T newValue, JsonNode jsonpatch,ChangeEventListener ... changeListener){
			LinkedHashSet<Object> changedContainers = new LinkedHashSet<Object>();
			if(jsonpatch==null){
				ObjectMapper mapper = _mapper;
				jsonpatch = JsonDiff.asJson(
		    			mapper.valueToTree(oldValue),
		    			mapper.valueToTree(newValue)
		    			);
			}
        	
        	if(jsonpatch==null){
        		System.out.println("There are no changes?");
        	}
        	List<JsonNode> patchChanges = new ArrayList<JsonNode>();
        	
        	Map<String, JsonNode> adding = new HashMap<String,JsonNode>();
        	Map<String, JsonNode> removing = new HashMap<String,JsonNode>();
        	for(JsonNode change:jsonpatch){
        		String op=change.get("op").asText();
        		String path=change.get("path").asText();
        		String pathu=toUniqueIDPath(path);
        		if(pathu!=null){
	        		if(op.equals("add")){
	        			adding.put(pathu,change);
	        		}else if(op.equals("remove")){
	        			removing.put(pathu, change);
	        		}
        		}
        		patchChanges.add(change);
        	}
        	
        	// Those IDed objects which are being both removed and
        	// re-added are really being moved.
        	
        	// It wouldn't make a difference how it's done, except that
        	// we use the IDs as enhanced pointers, so adding 2 objects
        	// with the same ID will cause problems. Similarly, removing
        	// an object by ID that matches more than one record will
        	// cause unexpected results.
        	
        	// Typically, "move" operations are captured by JSONDiff
        	// However, if there is both a move AND an internal change, 
        	// JSONDiff isn't always good at spotting what happened.
        	// It may decide to capture it as a full add followed by
        	// a full remove
        	
        	// So, we can find the actual changes between the two versions
        	// and apply them directly to the object in question.
        	// Then replace the "add" with a "copy", and remove the ID
        	// element of the remove operation, and it should be as
        	// expected
        	
        	Map<String, Object> explicitNewValues = new HashMap<String,Object>();
        	for(String path1: adding.keySet()){
        		if(removing.containsKey(path1)){
        			JsonNode toAdd = adding.get(path1);
        			JsonNode toRemove = removing.get(path1);
        			Object oldv=Manipulator.getObjectAt(oldValue, toAdd.get("path").asText(), null);
        			Object newv=Manipulator.getObjectAt(newValue, toAdd.get("path").asText(), null);
        			int idofAdd=patchChanges.indexOf(toAdd);
        			int idofRemove=patchChanges.indexOf(toRemove);
        			String opath=toRemove.get("path").asText();
        			String npath=toAdd.get("path").asText();
        			
        			
        			PojoPatch<Object> patch =PojoDiff.getDiff(oldv, newv);
        			Stack<?> changeStack;
					try {
						//System.out.println("Applying subpatch");
						changeStack = patch.apply(oldv,changeListener);
	    	            changedContainers.addAll(changeStack);
	    	            if(idofAdd<idofRemove){
		    	            patchChanges.set(idofAdd, JsonSimpleNodeChange
			    	            	.COPY_OP(opath, toStandardPath(npath))
			    	            	.noReport()
			    	            	.asJsonNode());
		    	            patchChanges.set(
		    	            			idofRemove,
		    	            			JsonSimpleNodeChange
			    	            		.REMOVE_OP(toStandardPath(opath))
			    	            		.noReport()
			    	            		.asJsonNode()
			    	            		);
	    	            }else{
	    	            	explicitNewValues.put(npath, oldv);
	    	            }
					} catch (Exception e) {
						e.printStackTrace();
						throw new IllegalArgumentException(e);
					}
					
					
    	            
        		}
        	}
        	
        	
        	for(JsonNode change:patchChanges){
        		//System.out.println(new Change(change));
        		String path=change.get("path").asText();
        		String from=path;
        		Object newv=null;
        		Object oldv=null;
        		JsonNode report = change.get("report");
        		boolean reportChange = true;
        		if(report!=null && !report.isMissingNode() && !report.asBoolean()){
        			reportChange=false;
        		}
        		String op=change.get("op").asText();
        		if("replace".equals(op) ||
        			   "add".equals(op)	
        				){
        			newv=explicitNewValues.get(path);
        			if(newv==null){
        				newv=Manipulator.getObjectAt(newValue, path, null);
        			}
        		}
        		if("copy".equals(op) ||
        		   "move".equals(op) 
         				){
        			from=change.get("from").asText();
         			newv=Manipulator.getObjectAt(oldValue, from, null);
         		}
        		
        	    if("replace".equals(op)){
        	    	oldv=Manipulator.setObjectAt(oldValue, path, newv, changedContainers);
        		}
        	    
        	    if("remove".equals(op) ||
        	         "move".equals(op)
        	    		){
        			oldv=Manipulator.removeObjectAt(oldValue, from, changedContainers);
        		}
        	    if( "add".equals(op) ||
        		   "copy".equals(op) ||
        		   "move".equals(op)
        				){
        			Manipulator.addObjectAt(oldValue, path, newv, changedContainers);
        		}
        	    
        	    Change c = new Change(path, op, oldv, newv, null, from);
        	    if(reportChange){
	        	    for(ChangeEventListener ch: changeListener){
	    				ch.handleChange(c);
	    			}
        	    }
            	
        	}
        	
        	//System.out.println("============");
        	
        	Stack<Object> changeStack = new Stack<Object>();
        	
        	for(Object o:changedContainers){
        		changeStack.push(o);
        	}
        	
        	return changeStack;
	}
	
	private static class TypeRegistry<T>{
		private Class<T> cls;
		Map<String,Getter> getters;
		Map<String,Setter> setters;
		
		public TypeRegistry(Class<T> cls){
			this.cls=cls;
			getters=getGetters(cls);
			setters=getSetters(cls);
		}
		
		@SuppressWarnings("unused")
		public Class<T> getType(){
			return cls;
		}
		
		public Setter getSetter(String prop){
			final Setter oset=setters.get(prop);
			return oset;
		}
		
		
		public static <T> Map<String,Getter> getGetters(Class<T> cls){
			
			ConcurrentHashMap<String,Getter> getterMap = new ConcurrentHashMap<String,Getter>();
			
			for(Method m: cls.getMethods()){
				if(isExplicitGetterMethod(m)){
					getterMap.computeIfAbsent(
							getMethodProperty(m),
							k -> new MethodGetter(m)
							);
				}
			}
			
			for(Method m: cls.getMethods()){
				if(isImplicitGetterMethod(m)){
					getterMap.computeIfAbsent(
							getMethodProperty(m),
							k -> new MethodGetter(m)
							);
				}
			}
			
			for(Field m: cls.getFields()){
				if(isExplicitGetterField(m)){
					getterMap.computeIfAbsent(
							getFieldProperty(m),
							k -> new FieldGetter(m)
							);
				}
			}
			
			for(Field m: cls.getFields()){
				if(isImplicitGetterField(m)){
					getterMap.computeIfAbsent(
							getFieldProperty(m),
							k -> new FieldGetter(m)
							);
				}
				if(isUnwrappedField(m)){
					Map<String,Getter> subGetters=getGetters(m.getType());
					for(Entry<String,Getter> ent:subGetters.entrySet()){
						getterMap.computeIfAbsent(ent.getKey(),  k-> new UnwrappedDelegateFieldGetter(m,ent.getValue()));
					}
				}
			}
			

			Iterator<Getter> iter = getterMap.values().iterator();
			while(iter.hasNext()){
				if(iter.next().isIgnored()){
					iter.remove();
				}
			}

			
			return getterMap;
		}
		
		public static <T> Map<String,Setter> getSetters(Class<T> cls){
			ConcurrentHashMap<String,Setter> setterMap = new ConcurrentHashMap<String,Setter>();
			
			for(Method m: cls.getMethods()){
				if(isExplicitSetterMethod(m)){
					setterMap.computeIfAbsent(
							getMethodProperty(m),
							k -> new MethodSetter(m)
							);
				}
			}
			
			for(Method m: cls.getMethods()){
				if(isImplicitSetterMethod(m)){
					setterMap.computeIfAbsent(
							getMethodProperty(m),
							k -> new MethodSetter(m)
							);
				}
			}
			
			for(Field m: cls.getFields()){
				if(isExplicitSetterField(m)){
					setterMap.computeIfAbsent(
							getFieldProperty(m),
							k-> new FieldSetter(m)
							);
				}
			}
			for(Field m: cls.getFields()){
				if(isImplicitSetterField(m)){
					setterMap.computeIfAbsent(
							getFieldProperty(m),
							k -> new FieldSetter(m)
							);
				}
				if(isUnwrappedField(m)){
					Map<String,Setter> subSetters=getSetters(m.getType());
					for(Entry<String,Setter> ent:subSetters.entrySet()){
						setterMap.computeIfAbsent(ent.getKey(), k-> new UnwrappedDelegateFieldSetter(m,ent.getValue()));
					}
				}
			}

			Iterator<Setter> iter = setterMap.values().iterator();
			while(iter.hasNext()){
				if(iter.next().isIgnored()){
					iter.remove();
				}
			}

			return setterMap;
		}
		public static boolean isExplicitGetterMethod(Method m){
			JsonProperty jp= m.getAnnotation(JsonProperty.class);
			if(jp!=null){
				if(m.getName().startsWith("get"))return true;
				if(m.getName().startsWith("is")){
					if(isBooleanAltMethod(m))
					return true;
				}
			}
			return false;
		}
		public static boolean isExplicitSetterMethod(Method m){
			JsonProperty jp= m.getAnnotation(JsonProperty.class);
			if(jp!=null){
				if(m.getName().startsWith("set"))return true;
			}
			return false;
		}
		public static boolean isExplicitGetterField(Field m){
			JsonProperty jp= m.getAnnotation(JsonProperty.class);
			if(jp!=null){
				return true;
			}
			return false;
		}
		public static boolean isExplicitSetterField(Field m){
			JsonProperty jp= m.getAnnotation(JsonProperty.class);
			if(jp!=null){
				return true;
			}
			return false;
		}
		public static String getMethodProperty(Method m){
			JsonProperty jp= m.getAnnotation(JsonProperty.class);
			if(jp!=null){
				return jp.value();
			}
			String name=m.getName().substring("get".length());
			return Character.toLowerCase(name.charAt(0))+name.substring(1);
		}
		public static String getFieldProperty(Field m){
			JsonProperty jp= m.getAnnotation(JsonProperty.class);
			if(jp!=null){
				return jp.value();
			}
			return m.getName();
		}
		public static boolean isBooleanAltMethod(Method m){
			if(!m.getName().startsWith("is"))return false;
			if(m.getReturnType().isAssignableFrom(Boolean.class))return true;
			if(m.getReturnType().getName().equals("boolean"))return true;
			return false;
		}
		public static boolean isImplicitGetterMethod(Method m){
			int mods=m.getModifiers();
			if(Modifier.isStatic(mods))return false;
			
			if(!isBooleanAltMethod(m) && !m.getName().startsWith("get"))return false;
			if(m.getParameterTypes().length>0)return false;
			if(m.getDeclaringClass().equals(java.lang.Object.class))return false;
			return true;
		}
		public static boolean isImplicitGetterField(Field m){
			int mods=m.getModifiers();
			return  !Modifier.isStatic(mods);
		}
		
		public static boolean isUnwrappedField(Field m){
			JsonUnwrapped junwrapped= m.getAnnotation(JsonUnwrapped.class);
			return junwrapped!=null;
		}
		
		public static boolean isImplicitSetterMethod(Method m){
			int mods=m.getModifiers();
			if(Modifier.isStatic(mods))return false;
			
			if(!m.getName().startsWith("set"))return false;
			if(m.getParameterTypes().length!=1)return false;
			 return !(m.getDeclaringClass().equals(java.lang.Object.class));

		}
		public static boolean isImplicitSetterField(Field m){
			int mods=m.getModifiers();
			return !Modifier.isStatic(mods);
		}
		
		
		public static interface Getter{
			public Object get(Object instance);
			default boolean isIgnored(){
				return false;
			}
		}
		public static interface Setter{
			public Object set(Object instance, Object set);
			default boolean isIgnored(){
				return false;
			}
		}
		public static class MapSetter implements Setter{
			private String key;
			public MapSetter(String key){
				this.key=key;
			}
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Object set(Object instance, Object set) {
				if(instance instanceof Map){
					Map m= ((Map)instance);
					if(set==null){
						return m.remove(key);
					}else{
						Object old=m.get(key);
						m.put(key, set);
						return old;
					}
				}else{
					throw new IllegalStateException(instance.getClass() + " is not a Map");
				}
			}
			@Override
			public boolean isIgnored() {return false;}
			
		}
		
		public static class MethodSetter implements Setter{
			private Method m;
			boolean ignore=false;
			public MethodSetter(Method m){
				this.m=m;
				JsonIgnore jsn=m.getAnnotation(JsonIgnore.class);
				if(jsn!=null)ignore=true;
			}
			
			@Override
			public Object set(Object instance, Object set) {
				try{
					m.invoke(instance, set);
					return null;
				}catch(Exception e){
					e.getCause().printStackTrace();
					System.err.println(instance.getClass() + " set to:" + set + " using " + m);
					System.err.println(set.getClass());
					throw new IllegalStateException(e);
				}
			}

			@Override
			public boolean isIgnored() {
				return this.ignore;
			}
			
		}
		public static class MethodGetter implements Getter{
			private Method m;
			boolean ignore=false;
			public MethodGetter(Method m){
				this.m=m;
				JsonIgnore jsn=m.getAnnotation(JsonIgnore.class);
				if(jsn!=null)ignore=true;
			}
			
			@Override
			public Object get(Object instance) {
				try{
					return m.invoke(instance);
				}catch(Exception e){
					throw new IllegalStateException(e);
				}
			}

			@Override
			public boolean isIgnored() {
				return ignore;
			}
			
		}
		
		public static class FieldGetter implements Getter{
			private Field m;
			private boolean ignore=false;
			public FieldGetter(Field m){
				this.m=m;
				JsonIgnore jsn=m.getAnnotation(JsonIgnore.class);
				if(jsn!=null)ignore=true;
			}
			
			@Override
			public Object get(Object instance) {
				try{
					return m.get(instance);
				}catch(Exception e){
					throw new IllegalStateException(e);
				}
			}

			@Override
			public boolean isIgnored() {return this.ignore;}
		}
		public static class UnwrappedDelegateFieldGetter implements Getter{
			private Field m;
			private Getter g;
			private boolean ignore=false;
			public UnwrappedDelegateFieldGetter(Field m, Getter g){
				this.m=m;
				this.g=g;
				JsonIgnore jsn=m.getAnnotation(JsonIgnore.class);
				if(jsn!=null)ignore=true;
			}
			
			@Override
			public Object get(Object instance) {
				try{
					Object delegateInstance=m.get(instance);
					return g.get(delegateInstance); 
				}catch(Exception e){
					throw new IllegalStateException(e);
				}
			}

			@Override
			public boolean isIgnored() {return this.ignore;}
		}
		
		public static class FieldSetter implements Setter{
			private Field m;
			private boolean ignore=false;
			public FieldSetter(Field m){
				this.m=m;
				JsonIgnore jsn=m.getAnnotation(JsonIgnore.class);
				if(jsn!=null)ignore=true;
			}
			
			@Override
			public Object set(Object instance, Object value) {
				try{
					Object old=m.get(instance);
					m.set(instance, value);
					return old;
				}catch(Exception e){
					throw new IllegalStateException(e);
				}
			}

			@Override
			public boolean isIgnored() {return this.ignore;}
		}
		public static class UnwrappedDelegateFieldSetter implements Setter{
			private Field m;
			private Setter g;
			private boolean ignore=false;
			public UnwrappedDelegateFieldSetter(Field m, Setter g){
				this.m=m;
				this.g=g;
				
				JsonIgnore jsn=m.getAnnotation(JsonIgnore.class);
				if(jsn!=null)ignore=true;
			}
			
			@Override
			public Object set(Object instance, Object value) {
				try{
					Object delegateInstance=m.get(instance);
					//System.out.println("Setting:" + m + " to " + value.getClass() + value + " on " +delegateInstance);
					Object ret=g.set(delegateInstance,value);;
					if(g instanceof FieldSetter){
						((FieldSetter)g).m.get(delegateInstance);
					}
					return  ret;
				}catch(Exception e){
					throw new IllegalStateException(e);
				}
			}

			@Override
			public boolean isIgnored() {return this.ignore;}
		}
	}
	
	private static class Manipulator {
		private static Map<String, TypeRegistry<?>> registries= new ConcurrentHashMap<>();
		
		@SuppressWarnings("unchecked")
		public static <T> TypeRegistry<T> getClassRegistry(Class<T> cls){
			return (TypeRegistry<T>)registries.computeIfAbsent(cls.getName(), k->new TypeRegistry<T>(cls));
		}
		
		private static <T> int findFirstPositionMatching(Collection<T> c, Predicate<T> keep){
			Optional<Integer> position = c.stream()
										 .map(Util.toIndexedTuple())
										 .filter(t->keep.test(t.v()))
										 .findFirst()
										 .map(t->t.k());
			if(position.isPresent()){
				return position.get();
			}
			return -1;
		}
		
		private static <T> int getObjectWithID(Collection<T> c, String id){
			return findFirstPositionMatching(c, (t)-> {
				Optional<String> ido=IDGetter.apply(t);
				if(!ido.isPresent()){
					return false;
				}
				return ido.get().equals(id);
			});
		}
		
		private static <T> int getCollectionPostion(Collection<T> col, String prop){
			return getCollectionPostion(col, prop,false);
		}
		private static <T> int getCollectionPostion(Collection<T> col, String prop, boolean allowpseudo){
			int c=-1;
			if(allowpseudo && prop.equals("-")){
				//System.err.println(" '-' can mean either the end of this list, or the virtual object just beyond the end of a different list, depending on context");
				
				c=col.size()-1;
				
				//throw new IllegalStateException("'-'  not yet implemented");
			}else if(prop.startsWith("_")){
				try{
					c=Integer.parseInt(prop.substring(1));
				}catch(Exception e){
					
				}
			}else if(prop.startsWith("$")){
				c=getObjectWithID(col,prop.substring(1).split("_")[0]);
			}else{
				try{
					c=Integer.parseInt(prop);
				}catch(Exception e){
					
				}
			}

			return c;
		}
		
		
		private static <T> Object getObjectDirect(T o, String prop){
			@SuppressWarnings("unchecked")
			TypeRegistry<T> tr=getClassRegistry((Class<T>)o.getClass());
			
			if(o instanceof Collection){
				@SuppressWarnings("unchecked")
				Collection<Object> col = (Collection<Object>)o;
				final int c=getCollectionPostion(col,prop,true);
				if(c == -1){
					throw new IllegalStateException("Element '" + prop + "' does not exist in collection : " + o);
				}
				if(col.size()<c){
					throw new IllegalStateException("Element '" + c + "' does not exist in collection of size " + col.size());
				}
				if(col instanceof List){
					//random access
					return ((List<Object>)col).get(c);
				}
				Optional<Object> op =col.stream().skip(c).findFirst();
				if(op.isPresent()){
					return op.get();
				}
			}
			if(o instanceof Map){
				@SuppressWarnings("rawtypes")
				Map m=(Map)o;
				return m.get(prop);
			}
			
			TypeRegistry.Getter g= tr.getters.get(prop);
			if(g!=null){
				return g.get(o);
			}else{
				throw new IllegalArgumentException("No getter for '" + prop + "' in " + o.getClass().getName() );
			}
		}
		private static <T> TypeRegistry.Setter getSetterDirect(T t, final String prop){
			
			@SuppressWarnings("unchecked")
			TypeRegistry<T> tr=getClassRegistry((Class<T>)t.getClass());
			
			if(t instanceof Collection){
				Collection<?> col = (Collection<?>)t;
				if(prop.equals("-")){
					throw new IllegalStateException("'-'  not yet implemented");
				}
				
				final int c=getCollectionPostion(col,prop);
				
				if(col instanceof List){
					return (instance, set)->{
							@SuppressWarnings("unchecked")
							List<Object> asList=((List<Object>)instance);
							Object old=asList.get(c);
							asList.set(c, set);
							return old;
					};
				}else{
					//System.err.println("Setters for non-list collections are experimental");
					final Object old=col.stream().skip(c).findFirst().get();
					return (instance, set)->{
							@SuppressWarnings("unchecked")
							Collection<Object> c1=(Collection<Object>)instance;
							List<Object> l = new ArrayList<Object>(c1); //defensive copy
							c1.clear();
							for(Object obj:l){
								if((obj==old) || obj.equals(old)){
									c1.add(set);
								}else{
									if(c1.contains(obj)){
										c1.add(new Object(){}); //placeholder
									}
									c1.add(obj);
								}
							}
							return old;
						};
				}
				
			}
			if(t instanceof Map){
				return new TypeRegistry.MapSetter(prop);
			}
			return tr.getSetter(prop);
		}
		@SuppressWarnings("unchecked")
		private static <T> TypeRegistry.Setter getRemoverDirect(T o, final String prop){
			if(o instanceof Collection){
				Collection<Object> col = (Collection<Object>)o;
				if(prop.equals("-")){
					throw new IllegalStateException("'-'  not yet implemented");
				}
				final int c=getCollectionPostion(col,prop);
				if(col instanceof List){
					return (instance, set)->((List<Object>)instance).remove(c);
				}else{
					//System.err.println("Setters for non-list collections are experimental");
					final Object old=col.stream().skip(c).findFirst().get();
					return (instance, set)->{
							((Collection<Object>)instance).remove(old);
							return old;
						};
				}
			}
			final TypeRegistry.Setter setter = getSetterDirect(o,prop);
			if(setter!=null){
				return (instance, set)->setter.set(instance, null);
			}
			return null;
		}
		private static <T> TypeRegistry.Setter getAdderDirect(T o, final String prop){
			
			if(o instanceof Collection){
				@SuppressWarnings("unchecked")
				Collection<Object> col = (Collection<Object>)o;
				
				int cind=getCollectionPostion(col,prop,true);
				
				if(cind>=col.size()){
					cind=-1;
				}
				
				final int c=cind;
				if(o instanceof List){
					return (instance, set)->{
							@SuppressWarnings("unchecked")
							List<Object> asList = (List<Object>)instance;
							if(c<0){
								asList.add(set);
							}else{
								asList.add(c, set);
							}
							return null;
						};
				}else{
					//System.err.println("Setters for non-list collections are experimental");
					//final Object old=col.toArray()[c];
					return (instance, set)->{
							@SuppressWarnings("unchecked")
							Collection<Object> coll = (Collection<Object>)instance;
							if(c<0){
								coll.add(set);
							}else{
								List<Object> temporary = new ArrayList<Object>(coll);
								coll.clear();
								for(int i=0;i<=c;i++){
									coll.add(temporary.get(i));
								}
								coll.add(set);
								for(int i=c+1;i<temporary.size();i++){
									coll.add(temporary.get(i));
								}
							}
							return null;
						};
				}
				
			}
			
			return getSetterDirect(o,prop);
		}
		public static Object getObjectAt(Object src, String objPointer, Collection<Object> chainChange){
			//System.out.println(objPointer);
			if(chainChange!=null) {
				chainChange.add(src);
			}
			String[] paths= objPointer.split("/");
			if(paths.length<=1){
				return src;
			}
			String curPath = paths[1];
			
			String subPath=objPointer.substring(curPath.length()+1);
			
			Object fetched=getObjectDirect(src,curPath);
			
			
			if(paths.length<=2){
				if(chainChange!=null)chainChange.add(src);
				return fetched;
			}
			return getObjectAt(fetched,subPath, chainChange);
		}
		
		public static Object setObjectAt(Object src, String objPointer, Object newValue, Collection<Object> changeChain){
			if(objPointer==null||objPointer.equals(""))return null;
			String subPath=objPointer.replaceAll("/[^/]*$", "");
			String lastPath=objPointer.replaceAll(".*/([^/]*)$", "$1");
			Object old=null;
			
			Object fetched=getObjectAt(src,subPath,changeChain);
			changeChain.add(fetched);
			TypeRegistry.Setter s=getSetterDirect(fetched,lastPath);
			if(s!=null){
				old=s.set(fetched, newValue);
				//System.out.println("able to set:" + fetched.getClass());
				setObjectAt(src,subPath,fetched,changeChain);
			}else{
				//System.out.println("not able to set:" + fetched.getClass());
			}
			return old;
		}
		public static Object removeObjectAt(Object src, String objPointer, Collection<Object> changeChain){
			List<Object> visited= new ArrayList<Object>();
			String subPath=objPointer.replaceAll("/[^/]*$", "");
			String lastPath=objPointer.replaceAll(".*/([^/]*)$", "$1");
			
			Object oldValue=null;
			//this is the container for that object
			Object fetched=getObjectAt(src,subPath,visited);
			visited.add(fetched);
			
			//This gets the removal setter for the object 
			TypeRegistry.Setter s=getRemoverDirect(fetched,lastPath);
			if(s!=null){
				oldValue= s.set(fetched, null);
				//System.out.println("able to remove:" + fetched.getClass());
				//Ideally, "fetched" is set correctly now,
				//so save up the tree
				setObjectAt(src,subPath,fetched,visited);
				
			}else{
				System.out.println("not able to remove:" + fetched.getClass());
			}
			changeChain.addAll(visited);
			return oldValue;
		}
		public static void addObjectAt(Object src, String objPointer, Object set, Collection<Object> changeChain){
			//System.out.println(objPointer);
			List<Object> visited= new ArrayList<Object>();
			String subPath=objPointer.replaceAll("/[^/]*$", "");
			String lastPath=objPointer.replaceAll(".*/([^/]*)$", "$1");
			
			//this is the container for that object
			Object fetched=getObjectAt(src,subPath,visited);
			visited.add(fetched);
			
			//This gets the setter for the object 
			TypeRegistry.Setter s=getAdderDirect(fetched,lastPath);
			if(s!=null){
				s.set(fetched, set);
				//System.out.println("able to add:" + fetched.getClass());
				//Ideally, "fetched" is set correctly now,
				//so save up the tree
				setObjectAt(src,subPath,fetched,visited);
				
			}else{
				System.out.println("not able to add:" + fetched.getClass());
			}
			changeChain.addAll(visited);
		}
	}
}
