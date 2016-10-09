package ix.test.processor.basic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import ix.core.EntityProcessor;
import ix.core.factories.EntityProcessorFactory;
import ix.test.AbstractGinasClassServerTest;
import ix.test.server.GinasTestServer;
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
	
	@FunctionalInterface
	public static interface UncheckedBiConsumer<T,V>{
		public void accept(T t, V v) throws Exception;
	}
	
	public void testCall(UncheckedBiConsumer<TestEntity,EntityProcessor> cons, String... expected) throws Exception{
		TestEntity testEntity= new TestEntity();
		EntityProcessor ep=epf.getSingleResourceFor(TestEntity.class);
		cons.accept(testEntity,ep);
		assertEquals(Arrays.asList(expected), testEntity.methodsCalled);
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
	
}
