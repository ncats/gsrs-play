package ix.core.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;

import ix.test.SubstanceJsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.AbstractGinasClassServerTest;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.pojopointer.PojoPointer;
import ix.utils.Tuple;


@RunWith(Parameterized.class)
public class FunctionalPointerTest extends AbstractGinasClassServerTest{

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
			return this.uuid;
		}
	}

	public static interface LambdaTest<T>{
		public T getObject();
		public PojoPointer getPointer();
		public JsonNode getExpected();
		public String getName();

		public static <T> LambdaTest<T> of(final T t, final PojoPointer pp, final JsonNode expected, final String name){
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
		public static <T> LambdaTest<T> ofObj(final T t, final PojoPointer pp, final Object expected, final String name){
			JsonNode expectedSerialized;
			if(expected==null){
				expectedSerialized=FunctionalPointerTest.missingJson;
			}else{
				expectedSerialized=EntityWrapper.of(expected).toFullJsonNode();
			}
			return of(t, pp, expectedSerialized, name);
		}

		public static <T> LambdaTest<T> ofObj(final T t, final String pp, final Object expected, final String name){
			return ofObj(t, PojoPointer.fromURIPath(pp),expected, name);
		}
	}

	private static final JsonNode missingJson = CachedSupplier
			.ofCallable(()->(new ObjectMapper())
							.readTree("{}")
							.at("/missing"))
			.get();



	@SafeVarargs
	public static <T> List<T> list(final T ... ts){
		return Arrays.stream(ts).collect(Collectors.toList());
	}
	
	@SafeVarargs
	public static <T> List<Map<String, T>> listtup(final Tuple<String,T> ... ot){
		return Arrays.stream(ot)
				.map(t->map(t))
				.collect(Collectors.toList());
	}

	public static <T> Map<String,T> map(final Tuple<String,T> t){
		final Map<String, T> singleton = new HashMap<>();
		singleton.put(t.k(), t.v());
		return singleton;
	}
	public static <T> Map<String,T> map(final String k, final T value){
		return map(Tuple.of(k, value));
	}

	public static <K,V> Tuple<K,V> tup(final K k, final V v){
		return Tuple.of(k, v);
	}
	
	@Parameters(name="{0}")
	static public Collection<Object[]> findstuff(){
		final List<LambdaTest<?>> mylist  =  new ArrayList<>();

		addSimpleTests(mylist);
		addSimpleObjectTests(mylist);
		addComplexObjectTests(mylist);
		addFlatMapTests(mylist);
		addFlatMapSubelementTests(mylist);
		addIDLocatorTests(mylist);
		addMissingFilterTests(mylist);

		return mylist.stream()
					 .map(lt->new Object[]{lt.getName(),lt})
					 .collect(Collectors.toList());
	}
	
	private static void addSimpleTests(final List<LambdaTest<?>> mylist){
		final List<String> mlist=list("value1", "value2","value3","value4","value2");
		final List<String> exmlist=list("value2","value2");
		mylist.add(LambdaTest.ofObj(mlist, "(:value2)", exmlist, "Simple string filter returns list of strings matching criteria"));
	}

	

	private static void addMissingFilterTests(final List<LambdaTest<?>> mylist) {
		{
			final List<Map<String,Map<String,Object>>> mlist=listtup(tup("key",map("subkey","subvalue1")),
					tup("key",map("subkey","subvalue2")),
					tup("key",map("subkey","subvalue1")));
			final List<Map<String,Map<String,Object>>> exmlist=new ArrayList<>();
			mylist.add(LambdaTest.ofObj(mlist, "(key/nonexistingsubkey:subvalue1)", exmlist, "Nested Simple Filter Returns Empty List Test"));
		}
	}

	private static void addIDLocatorTests(final List<LambdaTest<?>> mylist) {
		{
			final Thing t= new Thing();
			final SubThing sub1 = new SubThing();
			sub1.property="value1";
			final SubThing sub2 = new SubThing();
			sub2.property="value1";
			final SubThing sub3 = new SubThing();
			sub3.property="value2";
			t.subthings.addAll(list(sub1,sub2,sub3));

			final UUID uuid1= sub1.uuid;

			mylist.add(LambdaTest.ofObj(t, "subthings(" + uuid1 + ")" , sub1 , "Fetch by id should return subelement with that id"));
			mylist.add(LambdaTest.ofObj(t, "subthings(uuid:" + uuid1 + ")" , list(sub1) , "Fetch by id filter should return list of subelement with that id"));
			mylist.add(LambdaTest.ofObj(t, "subthings($0)/uuid" , uuid1 , "fetch id field should return the id"));
			mylist.add(LambdaTest.ofObj(t, "subthings($0)/id" , uuid1 , "fetch id field delegate should return the id"));
			mylist.add(LambdaTest.ofObj(t, "baseProperty" , t.baseProperty , "fetch base property of object should return that property"));
			mylist.add(LambdaTest.ofObj(t, "subthings($0)/property" , sub1.property , "fetch subelement property fields should return that property"));
		}
	}

	private static void addFlatMapSubelementTests(final List<LambdaTest<?>> mylist) {
		{
			final List<Map<String,List<String>>> mlist=list(map("akey",list("TEST1", "TEST2", "TEST3")),
											  map("akey",list("TEST4", "TEST5", "TEST6")),
											  map("akey",list("TEST7", "TEST8", "TEST9")));
			final List<String> expected=list("TEST1", "TEST2", "TEST3","TEST4", "TEST5", "TEST6","TEST7", "TEST8", "TEST9");
			mylist.add(LambdaTest.ofObj(mlist, "!flatmap(akey)",expected , "Flatmap of lists in subelements should return single list of subelements"));
		}
	}

	private static void addFlatMapTests(final List<LambdaTest<?>> mylist) {
		{
			final List<List<String>> mlist=list(list("TEST1", "TEST2", "TEST3"),
											  list("TEST4", "TEST5", "TEST6"),
											  list("TEST7", "TEST8", "TEST9"));
			final List<String> expected=list("TEST1", "TEST2", "TEST3","TEST4", "TEST5", "TEST6","TEST7", "TEST8", "TEST9");
			mylist.add(LambdaTest.ofObj(mlist, "!flatmap()",expected , "Flatmap of lists of lists should return single list of subelements"));
			mylist.add(LambdaTest.ofObj(map("somekey",mlist), "somekey!flatmap()",expected , "Flatmap of subelement which is lists of lists should return single list of subelements"));
			
		}
	}

	private static void addComplexObjectTests(final List<LambdaTest<?>> mylist) {
		{
			final List<Map<String,Map<String,Object>>> mlist=listtup(tup("key",map("subkey","subvalue1")),
					tup("key",map("subkey","subvalue2")),
					tup("key",map("subkey","subvalue1")));

			final List<Map<String,Map<String,Object>>> exmlist=listtup(tup("key",map("subkey","subvalue1")),
					tup("key",map("subkey","subvalue1")));
			mylist.add(LambdaTest.ofObj(mlist, "(key/subkey:subvalue1)", exmlist, "Nested simple filter returns list of objects matching sub element criteria"));
			mylist.add(LambdaTest.ofObj(mlist, "!map(key/subkey)", list("subvalue1","subvalue2","subvalue1"), "Nested simple explicit map function returns list of subvalues"));
			mylist.add(LambdaTest.ofObj(mlist, "!(key/subkey)", list("subvalue1","subvalue2","subvalue1"), "Nested simple implicit map function returns list of subvalues"));
			mylist.add(LambdaTest.ofObj(mlist, "(key/subkey:subvalue1)!(key/subkey)", list("subvalue1","subvalue1"), "Nested simple filtered list, with implicit map function returns list of filtered subvalues"));
			mylist.add(LambdaTest.ofObj(mlist, "!(key/fakekey)", list(), "Mapping to a non-existing sub element should yeild an empty list"));
			mylist.add(LambdaTest.ofObj(mlist, "!limit(0)", list(), "Limiting list results to 0 gives an empty list"));
			mylist.add(LambdaTest.ofObj(mlist, "!limit(1)", list(mlist.get(0)), "Limiting list results to 1 gives an first element of list"));
			mylist.add(LambdaTest.ofObj(mlist, "!skip(1)!limit(1)", list(mlist.get(1)), "Limiting list results to 1, and skipping 1 gives an first element of list"));
			mylist.add(LambdaTest.ofObj(mlist, "!skip(0)", mlist, "Skipping 0 records returns the same list"));
			mylist.add(LambdaTest.ofObj(mlist, "!count()", mlist.size(), "Counting list of 3 elements returns 3"));
			mylist.add(LambdaTest.ofObj(mlist, "!skip(1)!count()", mlist.size() -1 , "Counting list of 3 elements, skipping 1, returns 2"));
			mylist.add(LambdaTest.ofObj(mlist, "!distinct()",mlist.stream().distinct().collect(Collectors.toList()) , "Distinct objects from list should be distinct"));
			mylist.add(LambdaTest.ofObj(mlist, "!distinct()!count()",2 , "Distinct count of 3 objects, with 2 being equivalent, should be 2"));
			mylist.add(LambdaTest.ofObj(mlist, "!sort(key/subkey)",list(mlist.get(0),mlist.get(2), mlist.get(1)) , "Sorting by a subkey should return in ascending order"));
			mylist.add(LambdaTest.ofObj(mlist, "!revsort(key/subkey)",list(mlist.get(1),mlist.get(2),mlist.get(0)) , "Reverse sorting by a subkey should return in descending order"));
			
		}
	}

	private static void addSimpleObjectTests(final List<LambdaTest<?>> mylist) {
		{
			final List<Map<String,String>> mlist=listtup(tup("key","value1"),tup("key","value2"),tup("key","value1"));
			final List<Map<String,String>> exmlist=listtup(tup("key","value1"),tup("key","value1"));
			mylist.add(LambdaTest.ofObj(mlist, "(key:value1)", exmlist, "Simple object filter returns list of objects matching element criteria"));
		}
	}

	public LambdaTest<?> currentLambdaTest;

	public FunctionalPointerTest(final String name, final LambdaTest<?> lt){
		this.currentLambdaTest=lt;
	}

	public void evaluateEquals(final Object original, final PojoPointer pointer, final JsonNode expected){
		final Optional<EntityWrapper<?>> op=EntityWrapper.of(original)
				.at(pointer);
		JsonNode result;
		if(op.isPresent()){
			result = op.get().toFullJsonNode();
		}else{
			result = FunctionalPointerTest.missingJson;
		}
		SubstanceJsonUtil.assertEquals(expected, result, Comparator.comparing(Objects::toString));
//		assertEquals(expected,result);
	}

	@Test
	public void evaluateFunctionPointer() throws JsonProcessingException, IOException{
		evaluateEquals(this.currentLambdaTest.getObject(),
				this.currentLambdaTest.getPointer(),
				this.currentLambdaTest.getExpected());
	}

	@Test
	public void evaluateFunctionPointerAfterRewrite() throws JsonProcessingException, IOException{
		final String oldURI = this.currentLambdaTest.getPointer().toURIpath();

		assertEquals(oldURI,PojoPointer.fromURIPath(oldURI).toURIpath());

		evaluateEquals(this.currentLambdaTest.getObject(),
				PojoPointer.fromURIPath(oldURI),
				this.currentLambdaTest.getExpected());
	}


}
