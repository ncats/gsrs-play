package ix.utils.pojopatch;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.ginas.models.EmbeddedKeywordList;
import ix.utils.EntityUtils;
import ix.utils.Util;
import play.Logger;


/**
 * Author: Tyler Peryea
 * 
 * 
 * The purpose of this set of utilities will be to bring the flexibility and
 * ease-of-use of JSON serialization, JSONPatch and JSONDiff to objects themselves.
 * 
 * Specifically, the intended use is eventually to apply a JSONPatch directly to a
 * source object, as if it had been serialized to JSON, applied, and then de-serialized 
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
 *  [ ]=No fix attempted
 *  [?]=Possibly fixed, untested
 *  [/]=Probably fixed, a little tested
 *  [X]=Fixed, tested
 * 
 */

public class PojoDiff {
	public static ObjectMapper _mapper = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
	
	public static class JsonObjectPatch<T> implements PojoPatch<T>{
		private JsonPatch jp;
		public JsonObjectPatch(JsonPatch jp){this.jp=jp;}
		public Stack apply(T old, ChangeEventListener ... changeListener) throws Exception{
			return applyPatch(old,jp,changeListener);
		}
		@Override
		public List<Change> getChanges() {
			return new ArrayList<Change>();
		}
	}
	
	public static class LazyObjectPatch implements PojoPatch{
		private Object oldV;
		private Object newV;
		JsonNode jp;
		public LazyObjectPatch(Object oldV, Object newV){
			this.oldV=oldV;
			this.newV=newV;
		}
		public Stack apply(Object old, ChangeEventListener ... changeListener) throws Exception{
			if(jp==null){
				jp=getJsonDiff(oldV,newV);
			}
			if(old==oldV){
				return applyChanges(oldV,newV, jp,changeListener);
			}else{
				return new JsonObjectPatch(JsonPatch.fromJson(jp)).apply(old);
			}
		}
		@Override
		public List<Change> getChanges() {
			List<Change> changes= new
					ArrayList<Change>();
			JsonNode jsnp = getJsonDiff(oldV,newV);
			for(JsonNode jsn:jsnp){
				changes.add(new Change(jsn));
			}
			return changes;
		}
	}
	
	public static class EnhancedObjectPatch implements PojoPatch{
		private Object oldV;
		private Object newV;
		JsonNode jp;
		public EnhancedObjectPatch(Object oldV, Object newV){
			this.oldV=oldV;
			this.newV=newV;
		}
		
		public Stack apply(Object old, ChangeEventListener ... changeListener) throws Exception{
			if(jp==null){
				jp=getEnhancedJsonDiff(oldV,newV,null);
			}
			if(old==oldV){
				return applyChanges(oldV,newV, jp,changeListener);
			}else{
				return new JsonObjectPatch(JsonPatch.fromJson(jp)).apply(old);
			}
		}
		@Override
		public List<Change> getChanges() {
			List<Change> changes= new
					ArrayList<Change>();
			JsonNode[] oldAndNew = new JsonNode[2];
			JsonNode jsnp = getEnhancedJsonDiff(oldV,newV,oldAndNew);
			
			for(JsonNode jsn:jsnp){
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
	
	private static void sortDiff(JsonNode jp){
		List<JsonNode> diffs = new ArrayList<JsonNode>();
		for(JsonNode diff:jp){
			diffs.add(diff);
		}
		Collections.sort(diffs, new Comparator<JsonNode>(){
			@Override
			public int compare(JsonNode o1, JsonNode o2) {
				String path=o1.at("/path").asText();
				String op=o1.at("/op").asText();
				
				
				return 0;
			}
		});
	}
	
	
	public static <T> PojoPatch getDiff(T oldValue, T newValue){
		//return new EnhancedObjectPatch(oldValue,newValue);
		return new EnhancedObjectPatch(oldValue,newValue);
	}
	
	public static <T> PojoPatch getEnhancedDiff(T oldValue, T newValue){
		return new EnhancedObjectPatch(oldValue,newValue);
	}
	
	public static PojoPatch fromJsonPatch(JsonPatch jp) throws IOException{
		return new JsonObjectPatch(jp);
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
	/**
	 *
	 *	Okay, it don't work. Need to reevaluate.
	 *
	 *	
	 * 
	 * 
	 * 
	 **/
	
	private static ObjectNode mappify(ObjectNode m2){
		ObjectMapper om=new ObjectMapper();
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
				m2.put(key, mnew);
			}else if(o.isObject()){
				mappify((ObjectNode)o);
			}
		}
		return m2;
	}
	
	
	private static JsonNode mappifyJson(JsonNode js1){
		try {
			JsonNode mapped=mappify((ObjectNode)js1.deepCopy());
			return mapped;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return js1;
	}
	
	public static void canonicalizeDiff(List<JsonNode> diffs){
		Collections.sort(diffs,new Comparator<JsonNode>(){

			@Override
			public int compare(JsonNode o1, JsonNode o2) {
				String path1=o1.at("/path").asText();
				String op1=o1.at("/op").asText();
				
				
				String path2=o2.at("/path").asText();
				String op2=o2.at("/op").asText();
				
				int diff=op1.compareTo(op2);
				if(diff!=0){
					return diff;
				}
				//System.out.println(path1);
				
				path1=path1.replaceAll("/_([0-9][0-9]*)", "/#$1!");
				path2=path2.replaceAll("/_([0-9][0-9]*)", "/#$1!");
				//System.out.println("now:" + path1);
				String newpath1="";
				String newpath2="";
				String[] paths1=path1.split("#");
				String[] paths2=path2.split("#");
				for(int i=1;i<paths1.length;i++){
					//if(paths1[i].startsWith("#")){
						int k=Integer.parseInt(paths1[i].split("!")[0]);
						String np=String.format("%05d", k);
						newpath1+=np;
					//}
				}
				for(int i=1;i<paths2.length;i++){
					//if(paths2[i].startsWith("#")){
						int k=Integer.parseInt(paths2[i].split("!")[0]);
						String np=String.format("%05d", k);
						newpath2+=np;
					//}
				}
				
				int d=newpath1.compareTo(newpath2);
				if(op1.equals("remove")){
					d=-d;
				}
				
				return d;
			}
		});

	}
	private static ArrayNode canonicalizeDiff(JsonNode diffs){
		List<JsonNode> mynodes=new ArrayList<JsonNode>();
		for(JsonNode jsn:diffs){
			mynodes.add(jsn);
		}
		canonicalizeDiff(mynodes);
		ArrayNode arr=(new ObjectMapper()).createArrayNode();
		arr.addAll(mynodes);
		return arr;
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
		int i=0;
		
		for(JsonNode jsn:normalDiff){
			String path=jsn.at("/path").asText();
			path=path.replaceAll("[$][^_]*[_]", "").replaceAll("/_[0]*([0-9][0-9]*)","/$1");
			if(path.endsWith("/-")){
				int s=js1.at(path.replaceAll("/-$", "")).size();
				path=path.replaceAll("/-$", "/"+s+"");
			}
			String op=jsn.at("/op").asText();
			
			//System.out.println("old:" +jsn);
			positions.put(op + path, i);
			i++;
			
		}
		int j=0;
		for(JsonNode jsn:diff){
			String path=jsn.at("/path").asText();
			path=path.replaceAll("[$][^_]*[_]", "").replaceAll("/_[0]*([0-9][0-9]*)","/$1");
			String op=jsn.at("/op").asText();
			//System.out.println(jsn);
			Integer pos=positions.get(op + path);

			if(pos==null){
				reorderedDiffs.add(jsn);
			}else{
				cdiffs[pos]=jsn;
			}
			
			j++;
		}
		ArrayNode an=(new ObjectMapper()).createArrayNode();
		for(JsonNode jsn:cdiffs){
			if(jsn!=null){
				reorderedDiffs.add(jsn);
			}
		}
		canonicalizeDiff(reorderedDiffs);
		an.addAll(reorderedDiffs);
		return an;
		//return normalDiff;
	}
	
	private static <T> Stack applyPatch(T oldValue, JsonPatch jp, ChangeEventListener ... changeListener) throws IllegalArgumentException, JsonPatchException, JsonProcessingException{
		ObjectMapper mapper = _mapper;
		JsonNode oldNode=mapper.valueToTree(oldValue);
		JsonNode newNode=jp.apply(oldNode);
		//cat to T should be safe...
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
	
	private static <T> Stack applyChanges(T oldValue, T newValue, JsonNode jsonpatch,ChangeEventListener ... changeListener){
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
        	for(JsonNode change:jsonpatch){
        		String path=change.get("path").asText();
        		String from=path;
        		Object newv=null;
        		Object oldv=null;
        		
        		String op=change.get("op").asText();
        		if("replace".equals(op) ||
        			   "add".equals(op)	
        				){
        			newv=Manipulator.getObjectAt(newValue, path, null);
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
        	    
        	    Change c = new Change(path, op, oldv, newv, null);
        	    
        	    for(ChangeEventListener ch: changeListener){
    				ch.handleChange(c);
    			}
            	
        	}
        	
        	//System.out.println("============");
        	
        	Stack<Object> changeStack = new Stack<Object>();
        	
        	for(Object o:changedContainers){
        		changeStack.push(o);
        	}
        	
        	return changeStack;
	}
	private static class TypeRegistry{
		private Class cls;
		Map<String,Getter> getters;
		Map<String,Setter> setters;
		
		public TypeRegistry(Class cls){
			this.cls=cls;
			getters=getGetters(cls);
			setters=getSetters(cls);
		}
		
		public Setter getSetter(String prop){
			final Setter oset=setters.get(prop);
			return oset;
//			
//			return new Setter(){
//
//				@Override
//				public Object set(Object instance, Object set) {
//					if(set != null && set instanceof EmbeddedKeywordList){
//						System.out.println("###############Changing an embedded kwl");
//						set = new EmbeddedKeywordList((EmbeddedKeywordList)set);
//					}
//					return oset.set(instance, set);
//				}
//
//				@Override
//				public boolean isIgnored() {return false;}
//				
//			};
		}
		
		
		public static Map<String,Getter> getGetters(Class cls){
			// You may wonder why this is concurrent. That's because it's
			// written to use putIfAbsent, which exists in java 8,
			// but not Maps in java 7. It is only default in ConcurrentHashMap
			
			ConcurrentHashMap<String,Getter> getterMap = new ConcurrentHashMap<String,Getter>();
			
			for(Method m: cls.getMethods()){
				if(isExplicitGetterMethod(m)){
					getterMap.putIfAbsent(
							getMethodProperty(m),
							new MethodGetter(m)
							);
				}
			}
			
			for(Method m: cls.getMethods()){
				if(isImplicitGetterMethod(m)){
					getterMap.putIfAbsent(
							getMethodProperty(m),
							new MethodGetter(m)
							);
				}
			}
			
			for(Field m: cls.getFields()){
				if(isExplicitGetterField(m)){
					getterMap.putIfAbsent(
							getFieldProperty(m),
							new FieldGetter(m)
							);
				}
			}
			
			for(Field m: cls.getFields()){
				if(isImplicitGetterField(m)){
					getterMap.putIfAbsent(
							getFieldProperty(m),
							new FieldGetter(m)
							);
				}
			}
			
			List<String> toRemove=new ArrayList<String>();
			
			for(Entry<String,Getter> ent:getterMap.entrySet()){
				if(ent.getValue().isIgnored()){
					toRemove.add(ent.getKey());
				}
			}
			for(String s:toRemove){
				getterMap.remove(s);
			}
			
			return getterMap;
		}
		
		public static Map<String,Setter> getSetters(Class cls){
			ConcurrentHashMap<String,Setter> setterMap = new ConcurrentHashMap<String,Setter>();
			
			for(Method m: cls.getMethods()){
				if(isExplicitSetterMethod(m)){
					setterMap.putIfAbsent(
							getMethodProperty(m),
							new MethodSetter(m)
							);
				}
			}
			
			for(Method m: cls.getMethods()){
				if(isImplicitSetterMethod(m)){
					setterMap.putIfAbsent(
							getMethodProperty(m),
							new MethodSetter(m)
							);
				}
			}
			
			for(Field m: cls.getFields()){
				if(isExplicitSetterField(m)){
					setterMap.putIfAbsent(
							getFieldProperty(m),
							new FieldSetter(m)
							);
				}
			}
			for(Field m: cls.getFields()){
				if(isImplicitSetterField(m)){
					setterMap.putIfAbsent(
							getFieldProperty(m),
							new FieldSetter(m)
							);
				}
			}
			
			List<String> toRemove=new ArrayList<String>();
			
			for(Entry<String,Setter> ent:setterMap.entrySet()){
				if(ent.getValue().isIgnored()){
					toRemove.add(ent.getKey());
				}
			}
			for(String s:toRemove){
				setterMap.remove(s);
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
			if(Modifier.isStatic(mods))return false;
			return true;
		}
		
		public static boolean isImplicitSetterMethod(Method m){
			int mods=m.getModifiers();
			if(Modifier.isStatic(mods))return false;
			
			if(!m.getName().startsWith("set"))return false;
			if(m.getParameterTypes().length!=1)return false;
			if(m.getDeclaringClass().equals(java.lang.Object.class))return false;
			return true;
		}
		public static boolean isImplicitSetterField(Field m){
			int mods=m.getModifiers();
			if(Modifier.isStatic(mods))return false;
			return true;
		}
		
		
		public static interface Getter{
			public Object get(Object instance);
			public boolean isIgnored();
		}
		public static interface Setter{
			public Object set(Object instance, Object set);
			public boolean isIgnored();
		}
		public static class MapSetter implements Setter{
			private String key;
			public MapSetter(String key){
				this.key=key;
			}
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
	}
	
	private static class Manipulator {
		private static Map<String, TypeRegistry> registries= new HashMap<String,TypeRegistry>();
		
		public static void addClassToRegistry(Class cls){
			if(registries.get(cls.getName())==null){
				registries.put(cls.getName(), new TypeRegistry(cls));
			}
		}
		private static int getObjectWithID(Collection c, String id){
			int i=0;
			for(Object o:c){
				try {
					Object oid=EntityUtils.getId(o);
					if(id.equals(oid.toString())){
						return i;
					}
				} catch (Exception e) {
					
				}
				i++;
			}
			return -1;
		}
		
		private static int getCollectionPostion(Collection col, String prop){
			return getCollectionPostion(col, prop,false);
		}
		private static int getCollectionPostion(Collection col, String prop, boolean allowpseudo){
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
		private static Object getObjectDirect(Object o, String prop){
			addClassToRegistry(o.getClass());
			TypeRegistry tr=registries.get(o.getClass().getName());
			
			if(o instanceof Collection){
				final int c=getCollectionPostion((Collection)o,prop,true);
				
				if(((Collection)o).size()<=c){
					throw new IllegalStateException("Element '" + c + "' does not exist in collection of size " + ((Collection)o).size());
				}
				int i=0;
				for(Object f:(Collection)o){
					if(i==c){
						return f;
					}
					i++;
				}
			}
			if(o instanceof Map){
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
		private static TypeRegistry.Setter getSetterDirect(Object o, final String prop){
			addClassToRegistry(o.getClass());
			TypeRegistry tr=registries.get(o.getClass().getName());
			
			if(o instanceof Collection){
				
				Collection col = (Collection)o;
				//System.out.println("size:" + col.size());
				if(prop.equals("-")){
					throw new IllegalStateException("'-'  not yet implemented");
				}
				
				final int c=getCollectionPostion(col,prop);
				if(o instanceof List){
					return new TypeRegistry.Setter(){
						@Override
						public Object set(Object instance, Object set) {
							List asList=((List)instance);
							Object old=asList.get(c);
							asList.set(c, set);
							return old;
						}
						@Override
						public boolean isIgnored() {return false;}
					};
				}else{
					//System.err.println("Setters for non-list collections are experimental");
					final Object old=col.toArray()[c];
					return new TypeRegistry.Setter(){

						@Override
						public Object set(Object instance, Object set) {
							Collection c1=(Collection)instance;
							List l = new ArrayList(c1);
							c1.clear();
							for(Object o:l){
								if((o==old) || o.equals(old)){
									c1.add(set);
								}else{
									if(c1.contains(o)){
										c1.add(new Object(){});
									}
									c1.add(o);
								}
							}
							return old;
						}
						@Override
						public boolean isIgnored() {return false;}
						
						
						
					};
				}
				
			}
			if(o instanceof Map){
				return new TypeRegistry.MapSetter(prop);
			}
			return tr.getSetter(prop);
		}
		private static TypeRegistry.Setter getRemoverDirect(Object o, final String prop){
			addClassToRegistry(o.getClass());
			TypeRegistry tr=registries.get(o.getClass().getName());
			
			if(o instanceof Collection){
				Collection col = (Collection)o;
				if(prop.equals("-")){
					throw new IllegalStateException("'-'  not yet implemented");
				}
				final int c=getCollectionPostion(col,prop);
				if(o instanceof List){
					return new TypeRegistry.Setter(){

						@Override
						public Object set(Object instance, Object set) {
							return ((List)instance).remove(c);
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}else{
					//System.err.println("Setters for non-list collections are experimental");
					final Object old=col.toArray()[c];
					return new TypeRegistry.Setter(){

						@Override
						public Object set(Object instance, Object set) {
							((Collection)instance).remove(old);
							return old;
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}
				
			}
			final TypeRegistry.Setter setter = getSetterDirect(o,prop);
			if(setter!=null){
				return new TypeRegistry.Setter(){
					@Override
					public Object set(Object instance, Object set) {
						return setter.set(instance, null);
					}
					@Override
					public boolean isIgnored() {return false;}
				};
			}
			return null;
		}
		private static TypeRegistry.Setter getAdderDirect(Object o, final String prop){
			addClassToRegistry(o.getClass());
			TypeRegistry tr=registries.get(o.getClass().getName());
			
			if(o instanceof Collection){
				Collection col = (Collection)o;
				
				int cind=getCollectionPostion(col,prop,true);
				
				if(cind>=col.size()){
					cind=-1;
				}
				
				final int c=cind;
				if(o instanceof List){
					return new TypeRegistry.Setter(){
						@Override
						public Object set(Object instance, Object set) {
							List asList = (List)instance;
							if(c<0){
								asList.add(set);
							}else{
								asList.add(c, set);
							}
							return null;
						}
						@Override
						public boolean isIgnored() {return false;}
					};
				}else{
					//System.err.println("Setters for non-list collections are experimental");
					//final Object old=col.toArray()[c];
					return new TypeRegistry.Setter(){

						@Override
						public Object set(Object instance, Object set) {
							if(c<0){
								((Collection)instance).add(set);
							}else{
								List<Object> tempGuy = new ArrayList<Object>(((Collection)instance));
								((Collection)instance).removeAll(tempGuy);
								for(int i=0;i<=c;i++){
									((Collection)instance).add(tempGuy.get(i));
								}
								((Collection)instance).add(set);
								for(int i=c+1;i<tempGuy.size();i++){
									((Collection)instance).add(tempGuy.get(i));
								}
								
							}
							return null;
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}
				
			}
			
			return getSetterDirect(o,prop);
		}
		public static Object getObjectAt(Object src, String objPointer, Collection chainChange){
			//System.out.println(objPointer);
			if(chainChange!=null)
				chainChange.add(src);
			String[] paths= objPointer.split("/");
			if(paths.length<=1)return src;
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
