package ix.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.AbstractGinasTest;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.pojopointer.PojoPointer;


@RunWith(Parameterized.class)
public class PojoPointerTest extends AbstractGinasTest{
	
	public static class SerializableObject{
		public String stringField;
		
		
		public String stringFieldNotVisible;
		
		
		@JsonProperty("explicitFieldWhichIsAlsoMethod")
		public String explicitFieldWhichIsAlsoMethod;
		
		@JsonIgnore
		public String ignoredField;
		
		public List<String> stringListField= new ArrayList<String>();
		public List<SerializableObject> objectListField = new ArrayList<>();
		
		
		public String getStringFieldNotVisible(){
			return "METHOD:" + stringFieldNotVisible;
		}
		
		
		@JsonProperty("dynamic")
		public String getDynamicBySomeOtherName(){
			return "dynamicContent";
		}
		
		//NOT POSSIBLE NOW
		@JsonProperty("")
		public String getEmptyKeyMethod(){
			return "empty";
		}
		
		public String getExplicitFieldWhichIsAlsoMethod(){
			return "CORRECT!" + this.explicitFieldWhichIsAlsoMethod;
		}
		
		@JsonIgnore
		public String getIgnoredMethod(){
			return "METHOD:UNSEEN";
		}
		
		private String privateField = "private";
		private String privateMethod(){
			return "METHOD:private";
		}
		
	}
	
    @Parameters(name="{0}")
    static public Collection<Object[]> findstuff(){
    	List<Object[]> mylist  =  new ArrayList<Object[]>();
    	for(FIND_WITH fw:FIND_WITH.values()){
    		for(POINT_WITH pw: POINT_WITH.values()){
    			mylist.add(new Object[]{fw+":"+pw,fw,pw});
    		}
    	}
    	return mylist;
    }
    
    public static enum POINT_WITH{
    	JSON_POINTER,
    	URI_POINTER
    }
	
	public static enum FIND_WITH{
		OBJECT_FINDER,
		JSON_NODE_FINDER;
		public <T> JsonNode getObjectAt(T o, PojoPointer pp){
			
			EntityWrapper<T> ew = EntityWrapper.of(o);
			switch(this){
			case JSON_NODE_FINDER:
				JsonNode jsn =ew.toFullJsonNode().at(pp.toJsonPointer());
				return jsn;	
			case OBJECT_FINDER:
				
				Optional<EntityWrapper<?>> opEwVal= ew.at(pp);
				
				if(opEwVal.isPresent()){
					return opEwVal.get().toFullJsonNode();
				}else{
					return new ObjectMapper().createObjectNode().at("/");
				}
			default:
				throw new UnsupportedOperationException("Finder '" + this + "' is not supported");
			}
		}
	}
	
	FIND_WITH fw;
	POINT_WITH pw;
	
	public JsonNode getObjectAt(Object o, PojoPointer pp){
		if(pw == POINT_WITH.URI_POINTER){
			String uripath=pp.toURIpath();
			System.out.println(pp.toJsonPointer().toString() + "=>" + uripath + "=>" + PojoPointer.fromUriPath(pp.toURIpath()).toJsonPointer());
			pp=PojoPointer.fromUriPath(pp.toURIpath());
		}
		return fw.getObjectAt(o, pp);
	}
	
	public PojoPointerTest(String name, FIND_WITH fw, POINT_WITH pw){
		this.fw=fw;
		this.pw=pw;
	}
	
	@Test
	public void simpleTest(){
		String s = "Test";
		String p = "";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(s,getObjectAt(s,pp).asText());
	}
	
	
	public static class SimpleObject{
		public String str;
		public SimpleObject(String s){
			this.str=s;
		}
	}
	
	@Test
	public void objectTest(){
		String s = "Test";
		SimpleObject obj = new SimpleObject(s);
		String p = "/str";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(s,getObjectAt(obj,pp).asText());
	}
	
	@Test
	public void complexObjectTest(){
		String s = "Test";
		SerializableObject obj = new SerializableObject();
		obj.stringField=s;
		String p = "/stringField";
		
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(obj.stringField,getObjectAt(obj,pp).asText());
	}
	
	@Test
	public void complexObjectGetterMethodsPreferredForSerializing(){
		String s = "Test";
		SerializableObject obj = new SerializableObject();
		obj.stringFieldNotVisible=s;
		String p = "/stringFieldNotVisible";
		
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(obj.getStringFieldNotVisible(),getObjectAt(obj,pp).asText());
	}
	
	
	@Test
	public void complexObjectGetterMethodsWithExplicitAnnotationShouldBeSerializable(){
		SerializableObject obj = new SerializableObject();
		String p = "/dynamic";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(obj.getDynamicBySomeOtherName(),getObjectAt(obj,pp).asText());
	}
	
	@Test
	public void complexObjectGetterMethodWithEmptyKeyIsSerializableExpectFailed(){
		SerializableObject obj = new SerializableObject();
		String p = "/";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		
		//this is NOT desired
		//but is not supported by JsonProperty 
		assertNotEquals(obj.getEmptyKeyMethod(),getObjectAt(obj,pp).asText());
	}
	
	@Test
	public void mapWithEmptyValueKeyShouldSerialize(){
		Map<String,String> myMap = new HashMap<>();
		myMap.put("", "EXPECTED");
		String p = "/";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		
		assertEquals(myMap.get(""),getObjectAt(myMap,pp).asText());
	}
	
	@Test
	public void mapWithNestedEmptyValueKeyShouldSerialize() throws JsonProcessingException, IOException{
		Map<String,String> myMap = new HashMap<>();
		myMap.put("", "EXPECTED");
		
		Map<String,Map<String,String>> myMap2 = new HashMap<>();
		myMap2.put("", myMap);
		
		String p = "//";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(myMap2.get("").get(""),getObjectAt(myMap2,pp).asText());
	}
	
	@Test
	public void complexObjectPublicFieldOfStringListShouldBeSerializable(){
		SerializableObject obj = new SerializableObject();
		obj.stringListField.add("TEST 1");
		obj.stringListField.add("TEST 2");
		String p = "/stringListField/1";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(obj.stringListField.get(1),getObjectAt(obj,pp).asText());
	}
	
	@Test
	public void complexObjectPublicFieldOfObjectListShouldBeSerializable(){
		SerializableObject obj1 = new SerializableObject();
		obj1.stringListField.add("TEST 1A");
		obj1.stringListField.add("TEST 2A");
		
		SerializableObject obj2 = new SerializableObject();
		obj2.stringListField.add("TEST 1B");
		obj2.stringListField.add("TEST 2B");
		
		SerializableObject obj = new SerializableObject();
		obj.objectListField.add(obj1);
		obj.objectListField.add(obj2);
		
		String p = "/objectListField/1/stringListField/0";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(obj.objectListField.get(1).stringListField.get(0),getObjectAt(obj,pp).asText());
	}
	
	@Test
	public void complexObjectSerializedFromRootPathShouldEqualSerialized(){
		SerializableObject obj1 = new SerializableObject();
		obj1.stringListField.add("TEST 1A");
		obj1.stringListField.add("TEST 2A");
		
		SerializableObject obj2 = new SerializableObject();
		obj2.stringListField.add("TEST 1B");
		obj2.stringListField.add("TEST 2B");
		
		SerializableObject obj = new SerializableObject();
		obj.objectListField.add(obj1);
		obj.objectListField.add(obj2);
		
		String p = "";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(EntityWrapper.of(obj).toFullJsonNode(),getObjectAt(obj,pp));
	}
	
	@Test
	public void ensureMethodIsMoreImportantThanAnnotatedField(){
		SerializableObject obj1 = new SerializableObject();
		obj1.explicitFieldWhichIsAlsoMethod="EXPECTED";
		String p = "/explicitFieldWhichIsAlsoMethod";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(obj1.getExplicitFieldWhichIsAlsoMethod(),getObjectAt(obj1,pp).asText());
	}
	

	@Test
	public void ensureIgnoreFieldIsIgnored(){
		SerializableObject obj1 = new SerializableObject();
		obj1.ignoredField="Shouldn't be seen";
		
		String p = "/ignoredField";
		
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertTrue("Ignore Fields should not be found", getObjectAt(obj1,pp).isMissingNode());
	}
	
	@Test
	public void ensureIgnoreMethodIsIgnored(){
		SerializableObject obj1 = new SerializableObject();
		String p = "/ignoredMethod";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertTrue("Ignore Methods should not be found", getObjectAt(obj1,pp).isMissingNode());
	}
	
	@Test
	public void ensureCompletelyBogusPathGetsMissingNode(){
		SerializableObject obj1 = new SerializableObject();
		String p = "/some/path/to/nonsense";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertTrue("Ignore Methods should not be found", getObjectAt(obj1,pp).isMissingNode());
	}
	
	@Test
	public void ensurePrivateFieldAccessGetsMissingNode(){
		SerializableObject obj1 = new SerializableObject();
		String p = "/privateField";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertTrue("Private fields should not be found", getObjectAt(obj1,pp).isMissingNode());
	}
	
	@Test
	public void ensurePrivateMethodAccessGetsMissingNode(){
		SerializableObject obj1 = new SerializableObject();
		String p = "/privateMethod";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertTrue("Private methods should not be found", getObjectAt(obj1,pp).isMissingNode());
	}
	
	
	@Test
	public void ensureBasicListWorks(){
		
		List<String> list=
		IntStream.range(0,10)
				 .mapToObj(i->"VALUE" + i)
				 .collect(Collectors.toList());
		
		
		String p = "/3";
		PojoPointer pp = PojoPointer.fromJsonPointer(p);
		assertEquals(p, pp.toJsonPointer().toString());
		assertEquals(list.get(3),getObjectAt(list,pp).asText());
	}
	
	
	
	
}
