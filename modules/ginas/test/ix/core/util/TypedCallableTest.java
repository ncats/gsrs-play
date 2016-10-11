package ix.core.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.junit.Test;

import ix.AbstractGinasTest;
import ix.utils.CallableUtil.TypedCallable;

public class TypedCallableTest extends AbstractGinasTest {


	private String value = "TEST";
	private Class<String> expected = String.class;


	public <T> void assertTypeCorrect(TypedCallable<T> callable, Class<?> type, T value) throws Exception{
		assertEquals(value, callable.call());
		assertEquals(type, callable.getType());
	}
	public <T> void assertTypeCorrect2(TypedCallable<?> callable, Class<?> type, T value) throws Exception{
		assertEquals(value, callable.call());
		assertEquals(type, callable.getType());
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testAnnonymousInnerClassTypedCallableHasCorrectType() throws Exception{

		assertTypeCorrect(new TypedCallable<String>(){
			@Override
			public String call() throws Exception {
				return value;
			}
		}, expected, value);

	}

	@Test
	public void testLambdaTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(()->value,expected,value);
	}
	@Test
	public void testPassedThroughRawLambdaTypedCallableHasObjectType() throws Exception{
		assertTypeCorrect(loseMyType(()->value),Object.class,value);
	}
	@Test
	public void testPassedThroughKeepLambdaTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(keepMyType(()->value),String.class,value);
	}
	@Test
	public void testPassedThroughQuestionLambdaTypedCallableHasObjectType() throws Exception{
		assertTypeCorrect2(idkMyType(()->value),Object.class,value);
	}

	@Test
	public void testStaticHelperInstantiationOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(TypedCallable.of(()->value, expected),expected,value);
	}
	
	
	@SuppressWarnings("serial")
	@Test
	public void testInstantiatedExtendedStaticAnnonymousClassTypeOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(new SpecialTypedCallable<String>(value){},expected,value);
	}
	
	
	
	@Test
	public void testInstantiatedExtendedStaticClassTypeOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(new StringSpecialTypeCallable(value),expected,value);
	}

	@Test
	public void testSingleProvidedTypeOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(TypedCallable.of(value),expected,value);
	}
	
	@Test
	public void testDaisyChainedTypeOfTypedCallableHasCorrectType() throws Exception{
		Callable<String> c = ()->value;
		assertTypeCorrect(()->c.call(),expected,value);
	}
	
	@Test
	public void untypedTypedCallableReturnsObjectType(){
		TypedCallable tc = (()->"Test");
		assertEquals(Object.class,tc.getType()); //will be Object.class
	}
	@Test
	public void testInstantiatedRawTypeOfTypedCallableHasObjectType() throws Exception{
		assertTypeCorrect(new SpecialTypedCallable(value),Object.class,value);
	}
	
	@Test
	public void testInstantiatedNonRawStaticClassTypeOfTypedCallableHasObjectType() throws Exception{
		assertTypeCorrect(new SpecialTypedCallable<String>(value),Object.class,value);
	}

	@Test
	public void testStaticInstantiatedImplicitTypeOfTypedCallableHasObjectType() throws Exception{
		assertTypeCorrect(SpecialTypedCallable.of(value),Object.class,value);
	}

	
	
	@Test
	public void typedTypedCallableReturnsExpectedType(){
		TestThingSub tt= new TestThingSub();
		TypedCallable<TestThingSub> tc = (()->tt);
		assertEquals(TestThingSub.class,tc.getType()); //will be TestThingSub.class
	}
	
	@Test
	public void lessGranularlyTypedTypedCallableReturnsExpectedType(){
		TestThingSub tt= new TestThingSub();
		TypedCallable<TestThing> tc = (()->tt);
		assertEquals(TestThing.class,tc.getType()); //will be TestThing.class
	}
	
	public TypedCallable loseMyType(TypedCallable tc){
		return tc;
	}
	public <T> TypedCallable<T> keepMyType(TypedCallable<T> tc){
		return tc;
	}
	public TypedCallable<?> idkMyType(TypedCallable<?> tc){
		return tc;
	}
	
	public static class TestThing{}
	public static class TestThingSub extends TestThing{}
	
	
	

	@SuppressWarnings("serial")
	public static class SpecialTypedCallable<T> implements TypedCallable<T>{
		T o;
		public SpecialTypedCallable(T o){
			this.o=o;
		}
		@Override
		public T call() throws Exception {
			return o;
		}
		public static <T> SpecialTypedCallable<T> of(T t){
			return new SpecialTypedCallable<T>(t);
		}
	}
	
	public static class StringSpecialTypeCallable extends SpecialTypedCallable<String>{
		private static final long serialVersionUID = 2L;

		public StringSpecialTypeCallable(String o) {
			super(o);
		}
	}
}
