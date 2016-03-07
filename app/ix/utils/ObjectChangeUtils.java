package ix.utils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;


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
 *  [?] A JSONPatch itself can not be applied an existing object
 * 	[ ] Array handling is likely broken
 *  [ ] Certain Collection operations are also problematic, specifically involving
 *      the ambiguous '/-' JSONPointer notation, which may mean different things
 *      depending on context
 * 
 *  [?]=Possibly fixed, untested
 *  [ ]=No fix attempted
 *  [X]=Fixed, tested
 * 
 */

public class ObjectChangeUtils {
	
	public static Stack applyPatch(Object oldValue, JsonPatch jp) throws IllegalArgumentException, JsonPatchException, JsonProcessingException{
		EntityMapper mapper = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
		JsonNode newNode=jp.apply(mapper.valueToTree(oldValue));
		Object newValue = mapper.treeToValue(newNode,oldValue.getClass());
		return applyChanges(oldValue,newValue);
	}
	
	public static Stack applyChanges(Object oldValue, Object newValue){
		LinkedHashSet<Object> changedContainers = new LinkedHashSet<Object>();
		ObjectMapper mapper = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
		JsonNode jp = JsonDiff.asJson(
    			mapper.valueToTree(oldValue),
    			mapper.valueToTree(newValue)
    			);
        	
        	if(jp==null){
        		System.out.println("There are no changes?");
        	}
        	for(JsonNode change:jp){
        		/*
        		System.out.println(
        				change.get("op").asText() + "\t" + 
        				change.get("path").asText() + "\t" + 
        				change.get("value")
        				);
        		*/
        		String path=change.get("path").asText();
        		String from=path;
        		Object newv=null;
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
        			Manipulator.setObjectAt(oldValue, path, newv, changedContainers);
        		}
        	    if("remove".equals(op) ||
        	         "move".equals(op)
        	    		){
        			Manipulator.removeObjectAt(oldValue, from, changedContainers);
        		}
        	    if( "add".equals(op) ||
        		   "copy".equals(op) ||
        		   "move".equals(op)
        				){
        			Manipulator.addObjectAt(oldValue, path, newv, changedContainers);
        		}
            	
        	}
        	//System.out.println("============");
        	Stack<Object> changeStack = new Stack<Object>();
        	for(Object o:changedContainers){
        		changeStack.push(o);
        	}
        	return changeStack;
	}
	public static class TypeRegistry{
		private Class cls;
		Map<String,Getter> getters;
		Map<String,Setter> setters;
		
		public TypeRegistry(Class cls){
			this.cls=cls;
			getters=getGetters(cls);
			setters=getSetters(cls);
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
			public void set(Object instance, Object set);
			public boolean isIgnored();
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
			public void set(Object instance, Object set) {
				try{
					m.invoke(instance, set);
				}catch(Exception e){
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
			public void set(Object instance, Object value) {
				try{
					m.set(instance, value);
				}catch(Exception e){
					throw new IllegalStateException(e);
				}
			}

			@Override
			public boolean isIgnored() {return this.ignore;}
		}
	}
	
	public static class Manipulator {
		private static Map<String, TypeRegistry> registries= new HashMap<String,TypeRegistry>();
		
		public static void addClassToRegistry(Class cls){
			if(registries.get(cls.getName())==null){
				registries.put(cls.getName(), new TypeRegistry(cls));
			}
		}
		private static Object getObjectDirect(Object o, String prop){
			addClassToRegistry(o.getClass());
			TypeRegistry tr=registries.get(o.getClass().getName());
			
			if(o instanceof Collection){
				int c=-1;
				if(prop.equals("-")){
					System.err.println(" '-' can mean either the end of this list, or the virtual object just beyond the end of a different list, depending on context");
					c=((Collection)o).size()-1;
					//throw new IllegalStateException("'-'  not yet implemented");
				}else{
					c=Integer.parseInt(prop);
				}
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
			return tr.getters.get(prop).get(o);
		}
		private static TypeRegistry.Setter getSetterDirect(Object o, final String prop){
			addClassToRegistry(o.getClass());
			TypeRegistry tr=registries.get(o.getClass().getName());
			
			if(o instanceof Collection){
				Collection col = (Collection)o;
				if(prop.equals("-")){
					throw new IllegalStateException("'-'  not yet implemented");
				}
				final int c=Integer.parseInt(prop);
				if(o instanceof List){
					return new TypeRegistry.Setter(){

						@Override
						public void set(Object instance, Object set) {
							((List)instance).set(c, set);
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}else{
					System.err.println("Setters for non-list collections are experimental");
					final Object old=col.toArray()[c];
					return new TypeRegistry.Setter(){

						@Override
						public void set(Object instance, Object set) {
							((Collection)instance).remove(old);
							((Collection)instance).add(set);
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}
				
			}
			if(tr.setters.containsKey(prop)){
				return tr.setters.get(prop);
			}
			return null;
		}
		private static TypeRegistry.Setter getRemoverDirect(Object o, final String prop){
			addClassToRegistry(o.getClass());
			TypeRegistry tr=registries.get(o.getClass().getName());
			
			if(o instanceof Collection){
				Collection col = (Collection)o;
				if(prop.equals("-")){
					throw new IllegalStateException("'-'  not yet implemented");
				}
				final int c=Integer.parseInt(prop);
				if(o instanceof List){
					return new TypeRegistry.Setter(){

						@Override
						public void set(Object instance, Object set) {
							((List)instance).remove(c);
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}else{
					System.err.println("Setters for non-list collections are experimental");
					final Object old=col.toArray()[c];
					return new TypeRegistry.Setter(){

						@Override
						public void set(Object instance, Object set) {
							((Collection)instance).remove(old);
							//((Collection)instance).add(set);
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}
				
			}
			if(tr.setters.containsKey(prop)){
				final TypeRegistry.Setter setter=tr.setters.get(prop);
				tr.setters.get(prop);
				return new TypeRegistry.Setter(){

					@Override
					public void set(Object instance, Object set) {
						setter.set(instance, null);
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
				int cind=-1;
				if(prop.equals("-")){
					//throw new IllegalStateException("'-'  not yet implemented");
				}else{
					cind=Integer.parseInt(prop);
				}
				final int c=cind;
				if(o instanceof List){
					return new TypeRegistry.Setter(){

						@Override
						public void set(Object instance, Object set) {
							if(c<0){
								((List)instance).add(set);
							}else{
								((List)instance).add(c, set);
							}
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}else{
					System.err.println("Setters for non-list collections are experimental");
					//final Object old=col.toArray()[c];
					return new TypeRegistry.Setter(){

						@Override
						public void set(Object instance, Object set) {
							if(c<0){
								((Collection)instance).add(set);
							}else{
								List<Object> tempGuy = new ArrayList<Object>();
								((Collection)instance).removeAll(tempGuy);
								for(int i=0;i<c;i++){
									((Collection)instance).add(tempGuy.get(i));
								}
								((Collection)instance).add(set);
								for(int i=c;i<tempGuy.size();i++){
									((Collection)instance).add(tempGuy.get(i));
								}
								
							}
							
						}
						@Override
						public boolean isIgnored() {return false;}
						
					};
				}
				
			}
			if(tr.setters.containsKey(prop)){
				final TypeRegistry.Setter setter=tr.setters.get(prop);
				return setter;
			}
			return null;
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
		
		public static void setObjectAt(Object src, String objPointer, Object newValue, Collection<Object> changeChain){
			if(objPointer==null||objPointer.equals(""))return;
			String subPath=objPointer.replaceAll("/[^/]*$", "");
			String lastPath=objPointer.replaceAll(".*/([^/]*)$", "$1");
			
			Object fetched=getObjectAt(src,subPath,changeChain);
			
			TypeRegistry.Setter s=getSetterDirect(fetched,lastPath);
			if(s!=null){
				s.set(fetched, newValue);
				//System.out.println("able to set:" + fetched.getClass());
				setObjectAt(src,subPath,fetched,changeChain);
			}else{
				//System.out.println("not able to set:" + fetched.getClass());
			}
		}
		public static void removeObjectAt(Object src, String objPointer, Collection<Object> changeChain){
			List<Object> visited= new ArrayList<Object>();
			String subPath=objPointer.replaceAll("/[^/]*$", "");
			String lastPath=objPointer.replaceAll(".*/([^/]*)$", "$1");
			
			//this is the container for that object
			Object fetched=getObjectAt(src,subPath,visited);
			
			
			//This gets the setter for the object 
			TypeRegistry.Setter s=getRemoverDirect(fetched,lastPath);
			if(s!=null){
				s.set(fetched, null);
				//System.out.println("able to remove:" + fetched.getClass());
				//Ideally, "fetched" is set correctly now,
				//so save up the tree
				setObjectAt(src,subPath,fetched,visited);
				
			}else{
				System.out.println("not able to remove:" + fetched.getClass());
			}
			changeChain.addAll(visited);
		}
		public static void addObjectAt(Object src, String objPointer, Object set, Collection<Object> changeChain){
			//System.out.println(objPointer);
			List<Object> visited= new ArrayList<Object>();
			String subPath=objPointer.replaceAll("/[^/]*$", "");
			String lastPath=objPointer.replaceAll(".*/([^/]*)$", "$1");
			
			//this is the container for that object
			Object fetched=getObjectAt(src,subPath,visited);
			
			
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
