package ix.core.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.AbstractGinasTest;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.utils.Tuple;


@RunWith(Parameterized.class)
public class JustAnotherOldTest extends AbstractGinasTest{

	@Entity
	public static class Thing{
		public List<SubThing> subthings = new ArrayList<>();
		public String baseProperty = "base";
		
	}
	@Entity
	public static class SubThing{
		
		@Id
		public UUID uuid = UUID.randomUUID();
		public String property;
		
		public UUID getId(){
			return uuid;
		}
		
	}
	
	public static interface LambdaTest<T>{
		public T getObject();
		public PojoPointer getPointer();
		public JsonNode getExpected();
		public String getName();
		
		public static <T> LambdaTest<T> of(T t, PojoPointer pp, JsonNode expected, String name){
			return new LambdaTest<T>(){

				@Override
				public T getObject() {
					return t;
				}

				@Override
				public PojoPointer getPointer() {
					return pp;
				}

				@Override
				public JsonNode getExpected() {
					return expected;
				}
				@Override
				public String getName() {
					return name;
				}
			};
		}
		public static <T> LambdaTest<T> ofObj(T t, PojoPointer pp, Object expected, String name){
			JsonNode expectedSerialized;
			if(expected==null){
				expectedSerialized=missingJson;
			}else{
				expectedSerialized=EntityWrapper.of(expected).toFullJsonNode();
			}
			return of(t, pp, expectedSerialized, name);
		}
		
		public static <T> LambdaTest<T> ofObj(T t, String pp, Object expected, String name){
			return ofObj(t, PojoPointer.fromUriPath(pp),expected, name);
		}
	}
	
	private static final JsonNode missingJson = CachedSupplier.ofCallable(()->(new ObjectMapper()).readTree("{}").at("/missing"))
															  .get();
	
	public static class MyTest{
		public String simpleValue = "Simple";
		List<String> stringlist = new ArrayList<>();
	}
	
	public static <T> List<T> of(T ... ts){
		return Arrays.stream(ts).collect(Collectors.toList());
	}
	
	public static <T> List<Map<String, T>> oftup(Tuple<String,T> ... ot){
		return Arrays.stream(ot)
					.map(t->map(t))
					.collect(Collectors.toList());
	}
	
	public static <T> Map<String,T> map(Tuple<String,T> t){
		Map<String, T> singleton = new HashMap<>();
		singleton.put(t.k(), t.v());
		return singleton;
	}
	public static <T> Map<String,T> map(String k, T value){
		return map(Tuple.of(k, value));
	}
	
	public static <K,V> Tuple<K,V> tup(K k, V v){
		return Tuple.of(k, v);
	}
	
	@Parameters(name="{0}")
    static public Collection<Object[]> findstuff(){
    	List<LambdaTest<?>> mylist  =  new ArrayList<>();
    	
    	{
        	List<String> mlist=of("value1", "value2","value3","value4","value2");
        	List<String> exmlist=of("value2","value2");
        	mylist.add(LambdaTest.ofObj(mlist, "(:value2)", exmlist, "Simple string filter returns list of strings matching criteria"));
        }
    	{
	    	List<Map<String,String>> mlist=oftup(tup("key","value1"),tup("key","value2"),tup("key","value1"));
	    	List<Map<String,String>> exmlist=oftup(tup("key","value1"),tup("key","value1"));
	    	mylist.add(LambdaTest.ofObj(mlist, "(key:value1)", exmlist, "Simple object filter returns list of objects matching element criteria"));
    	}
    	
    	{
	    	List<Map<String,Map<String,Object>>> mlist=oftup(tup("key",map("subkey","subvalue1")),
	    													 tup("key",map("subkey","subvalue2")),
	    													 tup("key",map("subkey","subvalue1")));
	    	
	    	List<Map<String,Map<String,Object>>> exmlist=oftup(tup("key",map("subkey","subvalue1")),
															 tup("key",map("subkey","subvalue1")));
	    	mylist.add(LambdaTest.ofObj(mlist, "(key/subkey:subvalue1)", exmlist, "Nested simple filter returns list of objects matching sub element criteria"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!map(key/subkey)", of("subvalue1","subvalue2","subvalue1"), "Nested simple explicit map function returns list of subvalues"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!(key/subkey)", of("subvalue1","subvalue2","subvalue1"), "Nested simple implicit map function returns list of subvalues"));
	    	mylist.add(LambdaTest.ofObj(mlist, "(key/subkey:subvalue1)!(key/subkey)", of("subvalue1","subvalue1"), "Nested simple filtered list, with implicit map function returns list of filtered subvalues"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!(key/fakekey)", of(), "Mapping to a non-existing sub element should yeild an empty list"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!limit(0)", of(), "Limiting list results to 0 gives an empty list"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!limit(1)", of(mlist.get(0)), "Limiting list results to 1 gives an first element of list"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!skip(1)!limit(1)", of(mlist.get(1)), "Limiting list results to 1, and skipping 1 gives an first element of list"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!skip(0)", mlist, "Skipping 0 records returns the same list"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!count()", mlist.size(), "Counting list of 3 elements returns 3"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!skip(1)!count()", mlist.size() -1 , "Counting list of 3 elements, skipping 1, returns 2"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!distinct()",mlist.stream().distinct().collect(Collectors.toList()) , "Distinct objects from list should be distinct"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!distinct()!count()",2 , "Distinct count of 3 objects, with 2 being equivalent, should be 2"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!sort(key/subkey)",of(mlist.get(0),mlist.get(2), mlist.get(1)) , "Sorting by a subkey should return in ascending order"));
	    	mylist.add(LambdaTest.ofObj(mlist, "!revsort(key/subkey)",of(mlist.get(1),mlist.get(2),mlist.get(0)) , "Reverse sorting by a subkey should return in descending order"));
	    	
    	}
    	
    	{
    		Thing t= new Thing();
    		SubThing sub1 = new SubThing();
    		sub1.property="value1";
    		SubThing sub2 = new SubThing();
    		sub2.property="value1";
    		SubThing sub3 = new SubThing();
    		sub3.property="value2";
    		t.subthings.addAll(of(sub1,sub2,sub3));
    		
    		UUID uuid1= sub1.uuid;
    		
    		mylist.add(LambdaTest.ofObj(t, "subthings(" + uuid1 + ")" , sub1 , "Fetch by id should return subelement with that id"));
    		mylist.add(LambdaTest.ofObj(t, "subthings(uuid:" + uuid1 + ")" , of(sub1) , "Fetch by id filter should return list of subelement with that id"));
    		mylist.add(LambdaTest.ofObj(t, "subthings($0)/uuid" , uuid1 , "fetch id field should return the id"));
    		mylist.add(LambdaTest.ofObj(t, "subthings($0)/id" , uuid1 , "fetch id field delegate should return the id"));
    		mylist.add(LambdaTest.ofObj(t, "baseProperty" , t.baseProperty , "fetch base property of object should return that property"));
    		mylist.add(LambdaTest.ofObj(t, "subthings($0)/property" , sub1.property , "fetch subelement property fields should return that property"));
    	}
    	
    	{
        	List<Map<String,Map<String,Object>>> mlist=oftup(tup("key",map("subkey","subvalue1")),
        													 tup("key",map("subkey","subvalue2")),
        													 tup("key",map("subkey","subvalue1")));
        	List<Map<String,Map<String,Object>>> exmlist=new ArrayList<>();
        	mylist.add(LambdaTest.ofObj(mlist, "(key/nonexistingsubkey:subvalue1)", exmlist, "Nested Simple Filter Returns Empty List Test"));
        }
    	
    	
    	
    	return mylist.stream().map(lt->new Object[]{lt.getName(),lt}).collect(Collectors.toList());
    }
	
    public LambdaTest<?> currentLambdaTest;
    
    public JustAnotherOldTest(String name, LambdaTest<?> lt){
    	currentLambdaTest=lt;
    }
    
    public void evaluateEquals(Object original, PojoPointer pointer, JsonNode expected){
    	Optional<EntityWrapper<?>> op=EntityWrapper.of(original)
				   .at(pointer);
		JsonNode result;
		if(op.isPresent()){
		result = op.get().toFullJsonNode();
		}else{
		result = missingJson;
		}
		assertEquals(expected,result);
    }
    
    @Test
    public void evaluateFunctionPointer() throws JsonProcessingException, IOException{
    	evaluateEquals(currentLambdaTest.getObject(),
    				   currentLambdaTest.getPointer(), 
    				   currentLambdaTest.getExpected());
    }
    
    @Test
    public void evaluateFunctionPointerAfterRewrite() throws JsonProcessingException, IOException{
    	String oldURI = currentLambdaTest.getPointer().toURIpath();
    	
    	assertEquals(oldURI,PojoPointer.fromUriPath(oldURI).toURIpath());
    	
    	evaluateEquals(currentLambdaTest.getObject(),
				   PojoPointer.fromUriPath(oldURI), 
				   currentLambdaTest.getExpected());
    }
    
	
}
