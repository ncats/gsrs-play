package ix.core.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.junit.Ignore;
import org.junit.Test;

import ix.AbstractGinasTest;
import ix.utils.CallableUtil.TypedCallable;

public class TypedCallableTest extends AbstractGinasTest {


	private String object = "TEST";
	private Class<?> expected = String.class;


	public <T> void assertTypeCorrect(TypedCallable<T> callable, Class<?> type, T value) throws Exception{
		assertEquals(type, callable.getType());
		assertEquals(value, callable.call());
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testAnnonymousInnerClassTypedCallableHasCorrectType() throws Exception{

		assertTypeCorrect(new TypedCallable<String>(){
			@Override
			public String call() throws Exception {
				return object;
			}
		}, expected, object);

	}

	@Test
	public void testLambdaTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(()->object,expected,object);
	}

	@Test
	public void testStaticHelperInstantiationOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(TypedCallable.of(()->object, expected),expected,object);
	}
	
	
	@SuppressWarnings("serial")
	@Test
	public void testInstantiatedExtendedStaticAnnonymousClassTypeOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(new SpecialTypedCallable<String>(object){},expected,object);
	}
	
	@Test
	public void testInstantiatedExtendedStaticClassTypeOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(new StringSpecialTypeCallable(object),expected,object);
	}

	@Test
	public void testInstantiatedImplicitTypeOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(new SpecialTypedCallable(object),expected,object);
	}

	@Test
	public void testSingleProvidedTypeOfTypedCallableHasCorrectType() throws Exception{
		assertTypeCorrect(TypedCallable.of(object),expected,object);
	}
	
	@Test
	public void testDaisyChainedTypeOfTypedCallableHasCorrectType() throws Exception{
		Callable<String> c = ()->object;
		assertTypeCorrect(()->c.call(),expected,object);
	}
	

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
	}
	
	public static class StringSpecialTypeCallable extends SpecialTypedCallable<String>{
		private static final long serialVersionUID = 2L;

		public StringSpecialTypeCallable(String o) {
			super(o);
		}
	}
}
