package ix.test.processor.basic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.junit.Before;
import org.junit.Test;

import ix.AbstractGinasClassServerTest;
import ix.core.EntityProcessor;
import ix.core.EntityProcessor.FailProcessingException;
import ix.core.factories.EntityProcessorFactory;
import play.Play;

public class ReflectionProcessorTest extends AbstractGinasClassServerTest{
	
	EntityProcessorFactory epf;
	
	@Before
	public void setup(){
		epf=EntityProcessorFactory.getInstance(Play.application());
	}
	
	@Entity
	public static class TestEntity{
		List<String> methodsCalled = new ArrayList<String>(); 
		@PreUpdate
		public void preUpdate(){
			methodsCalled.add("preUpdate");
		}
		@PostUpdate
		public void postUpdate(){
			methodsCalled.add("postUpdate");
		}
		@PrePersist
		public void prePersist(){
			methodsCalled.add("prePersist");
		}
		@PostPersist
		public void postPersist(){
			methodsCalled.add("postPersist");
		}
		@PreRemove
		public void preRemove(){
			methodsCalled.add("preRemove");
		}
		@PostRemove
		public void postRemove(){
			methodsCalled.add("postRemove");
		}
		@PostLoad
		public void postLoad(){
			methodsCalled.add("postLoad");
		}
	}
	
	@Entity
	public static class ExtendingTestEntity extends TestEntity{
		@PreUpdate
		public void preUpdate2(){
			methodsCalled.add("preUpdate2");
		}
	}
	
	@FunctionalInterface
	public static interface UncheckedBiConsumer<T,V>{
		public void accept(T t, V v) throws Exception;
	}
	@FunctionalInterface
	public static interface ExceptionThrower<T extends Exception>{
		public void accept(T t);
	}
	
	public void testCall(UncheckedBiConsumer<TestEntity,EntityProcessor> cons, String... expected) throws Exception{
		testCall(cons,1, expected);
	}
	
	public void testCall(UncheckedBiConsumer<TestEntity,EntityProcessor> cons, int repeat, String... expected) throws Exception{
		testCall(()->new TestEntity(),cons,repeat, expected);
	}
	
	public void testCall(Supplier<TestEntity> sup, UncheckedBiConsumer<TestEntity,EntityProcessor> cons, int repeat, String... expected) throws Exception{
		TestEntity testEntity= sup.get();
		EntityProcessor ep=epf.getSingleResourceFor(testEntity.getClass());
		ExceptionThrower thrower= (t)->{throw new IllegalStateException(t);};
		IntStream.range(0, repeat).forEach(i->{
			try {
				cons.accept(testEntity,ep);
			} catch (Exception e) {
				thrower.accept(e);
			}
		});
		List<String> expectedList = Arrays.asList(expected).stream().sorted().collect(Collectors.toList());
		List<String> foundList = testEntity.methodsCalled.stream().sorted().collect(Collectors.toList());
		
		assertEquals(expectedList, foundList);
	}
	
	public void testCallRepeated(UncheckedBiConsumer<TestEntity,EntityProcessor> cons, int r, String expected) throws Exception{
		List<String> got= new ArrayList<String>();
		IntStream.range(0, r).forEach(i->{
			got.add(expected);
		});
		testCall(cons,r, got.toArray(new String[0]));
	}
	
	
	@Test
	public void testPostUpdateHookIsCalled() throws Exception{
		testCall((t,ep)->ep.postUpdate(t),"postUpdate");
	}
	@Test
	public void testPostPersistHookIsCalled() throws Exception{
		testCall((t,ep)->ep.postPersist(t),"postPersist");
	}
	@Test
	public void testPostRemoveHookIsCalled() throws Exception{
		testCall((t,ep)->ep.postRemove(t),"postRemove");
	}
	@Test
	public void testPreUpdateHookIsCalled() throws Exception{
		testCall((t,ep)->ep.preUpdate(t),"preUpdate");
	}
	@Test
	public void testPrePersistHookIsCalled() throws Exception{
		testCall((t,ep)->ep.prePersist(t),"prePersist");
	}
	@Test
	public void testPreRemoveHookIsCalled() throws Exception{
		testCall((t,ep)->ep.preRemove(t),"preRemove");
	}
	@Test
	public void testPostLoadHookIsCalled() throws Exception{
		testCall((t,ep)->ep.postLoad(t),"postLoad");
	}
	
	@Test
	public void testEachHookTwiceGetsCalledTwice() throws Exception{
		testCallRepeated((t,ep)->ep.postLoad(t),2,"postLoad");
		testCallRepeated((t,ep)->ep.postUpdate(t),2,"postUpdate");
		testCallRepeated((t,ep)->ep.postPersist(t),2,"postPersist");
		testCallRepeated((t,ep)->ep.postRemove(t),2,"postRemove");
		testCallRepeated((t,ep)->ep.prePersist(t),2,"prePersist");
		testCallRepeated((t,ep)->ep.preUpdate(t),2,"preUpdate");
		testCallRepeated((t,ep)->ep.preRemove(t),2,"preRemove");
	}
	
	@Test 
	public void testExtendingClassUpdateShouldCallItsMethodsAndItsParents() throws Exception{
		testCall(()->new ExtendingTestEntity(),(t,ep)->ep.preUpdate(t),1,"preUpdate", "preUpdate2");
	}
	
	

	
}
