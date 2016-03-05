package ix.utils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Tester {
	public static class TestStructure{
		public String rootProperty1;
		public boolean rootProperty2;
		public int rootProperty3;
		private String hiddenProperty;
		
		@JsonIgnore
		public String hiddenPropertySpecial;
		
		
		public String getHiddenProperty(){
			return this.hiddenProperty;
		}
		public void setHiddenProperty(String hidd){
			this.hiddenProperty=hidd;
		}
		
		public String getSpecialHiddenProperty(){
			return this.hiddenPropertySpecial;
		}
		
		public void setSpecialHiddenProperty(String hidd){
			this.hiddenPropertySpecial=hidd;
		}
		
		public TestStructure(String t1,boolean t2, int t3){
			rootProperty1=t1;
			rootProperty2=t2;
			rootProperty3=t3;
			this.setHiddenProperty(t1+t2);
			this.setSpecialHiddenProperty(t1+t2+t3);
		}
		public TestStructure(){
			
		}
		
	}
	public static void main(String[] args) throws NoSuchMethodException, SecurityException{
		//boolean b=Boolean.class..isAssignableFrom(TestGuy.class.getMethod("test2",null).getReturnType());
		//System.out.println(b);
		TestStructure g1= new TestStructure("Test", false, 5);
		TestStructure g2= new TestStructure("AnotherTest", true, 4);
		
	
		ObjectPointerFetcher.setObjectAt(g1, "/rootProperty1", "WHAT!?",null);
		System.out.println(g1.rootProperty1);
		
//		Map<String, ObjectPointerFetcher.Getter> mygetters=ObjectPointerFetcher.getGetters(TestGuy.class);
//		System.out.println(mygetters.toString());
//		
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
	public static class ObjectPointerFetcher {
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
				if(prop.equals("-")){
					throw new IllegalStateException("'-'  not yet implemented");
				}
				int c=Integer.parseInt(prop);
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
			}else{
				//System.out.println(tr.setters.keySet() + " in class '" + o.getClass() +  "' does not have " + prop);
				//System.out.println(tr.getters.keySet() + " are the getters");
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
			}else{
				//System.out.println(tr.setters.keySet() + " in class '" + o.getClass() +  "' does not have " + prop);
				//System.out.println(tr.getters.keySet() + " are the getters");
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
			//System.out.println(objPointer);
			if(objPointer==null||objPointer.equals(""))return;
			String subPath=objPointer.replaceAll("/[^/]*$", "");
			String lastPath=objPointer.replaceAll(".*/([^/]*)$", "$1");
			
			Object fetched=getObjectAt(src,subPath,changeChain);
			
			TypeRegistry.Setter s=getSetterDirect(fetched,lastPath);
			if(s!=null){
				s.set(fetched, newValue);
				System.out.println("able to set:" + fetched.getClass());
				setObjectAt(src,subPath,fetched,changeChain);
			}else{
				System.out.println("not able to set:" + fetched.getClass());
			}
		}
		public static void removeObjectAt(Object src, String objPointer, Collection<Object> changeChain){
			//System.out.println(objPointer);
			List<Object> visited= new ArrayList<Object>();
			String subPath=objPointer.replaceAll("/[^/]*$", "");
			String lastPath=objPointer.replaceAll(".*/([^/]*)$", "$1");
			
			//this is the container for that object
			Object fetched=getObjectAt(src,subPath,visited);
			
			
			//This gets the setter for the object 
			TypeRegistry.Setter s=getRemoverDirect(fetched,lastPath);
			if(s!=null){
				s.set(fetched, null);
				System.out.println("able to remove:" + fetched.getClass());
				//Ideally, "fetched" is set correctly now,
				//so save up the tree
				setObjectAt(src,subPath,fetched,visited);
				
			}else{
				System.out.println("not able to remove:" + fetched.getClass());
			}
			changeChain.addAll(visited);
		}
		
		
		
		
		
	}
}
