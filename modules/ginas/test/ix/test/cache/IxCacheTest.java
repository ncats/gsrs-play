package ix.test.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.models.Session;
import ix.core.plugins.IxCache;
import ix.ginas.models.v1.Substance;
import ix.utils.Util;

public class IxCacheTest  extends AbstractGinasServerTest {


	@Test  
	public void testFetchFromCacheWithSameKeyReturnsFirstCachedValue() throws Exception {
		final String result1="First";
		final String result2="Second";

		String first= IxCache.getOrElse("Test", ()->result1);
		String second= IxCache.getOrElse("Test", ()->result2);
		assertEquals(first,second);
		assertEquals(first,result1);
	}

	//DEADLOCK
	@Test  
	public void fetchSlowGeneratorWith2ThreadsShouldNotCallSecondGenerator() throws Exception {
		final String result1="First";
		final String result2="Second";
		ConcurrentHashMap<String,String> myMap = new ConcurrentHashMap<>();
		CountDownLatch firstThreadNotifier = new CountDownLatch(1);
		CountDownLatch secondThreadNotifier = new CountDownLatch(1);
		CountDownLatch bothThreadsFinishedNotifier = new CountDownLatch(2);

		new Thread(()->{
			try {
				myMap.put("first", IxCache.getOrElse("Test", ()->{
					firstThreadNotifier.countDown();
					secondThreadNotifier.await(1000, TimeUnit.MILLISECONDS);
					return result1;
				}));
			} catch (Exception e) {
				myMap.put("first","null");
				e.printStackTrace();
			}finally{
				bothThreadsFinishedNotifier.countDown();
			}

		}).start();


		new Thread(()->{
			try {
				firstThreadNotifier.await();

				myMap.put("second", IxCache.getOrElse("Test", ()->{
					secondThreadNotifier.countDown();
					return result2;
				}));
			} catch (Exception e) {
				myMap.put("second","null 2");
				e.printStackTrace();
			} finally{
				bothThreadsFinishedNotifier.countDown();
			}
		}).start();


		bothThreadsFinishedNotifier.await();

		HashSet<String> hset = new HashSet<>();
		hset.add(result1);
		assertEquals(hset,myMap.values().stream().collect(Collectors.toSet()));
	}

	@Test  
	public void fetchSlowGeneratorWith2ThreadsShouldNotRecalculate() throws Exception {
		final int staggeredThreads = 2;
		final String result1="First";
		
		AtomicInteger generatorCalls = new AtomicInteger(0);
		CountDownLatch cacheCalls = new CountDownLatch(staggeredThreads);
		Runnable r = ()->{
			try {
				IxCache.getOrElse("Test", ()->{
					Util.debugSpin(1000); //wait a second
					generatorCalls.incrementAndGet();
					return result1;
				});
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				cacheCalls.countDown();
			}
		};

		for(int i=0;i<staggeredThreads;i++){
			new Thread(r).start();
			Thread.sleep(10);
		}

		cacheCalls.await();
		
		assertEquals(1,generatorCalls.get());
	}

	/**
	 * This test ensures that the pass-through cache (write to disk)
	 * will preserve the entries, unless explicitly removed by a call
	 * to IxCache.remove
	 * 
	 * 
	 * 
	 * @throws Exception
	 */
	@Test  
	public void testClearingCacheDoesNotClearSerializedValues() throws Exception {
		String found1="THIS SHOULD BE SERLIALIZABLE";
		String found2="THIS SHOULD NOT BE FOUND";
		String found3="THIS SHOULD BE FOUND AFTER DELETE";

		String actualFound1=IxCache.getOrElse("Test", ()->found1);

		IxCache.clearCache(); // just clears in-memory cache
		String actualFound2=IxCache.getOrElse("Test", ()->found2);
		assertEquals(actualFound1,actualFound2);

		IxCache.remove("Test");
		String actualFound3=IxCache.getOrElse("Test", ()->found3);

		assertEquals(found3,actualFound3);
	}
	
	/**
	 * This test ensures that the pass-through cache (write to disk)
	 * will not preserve the entries that are not serializable
	 * @throws Exception
	 */
	@Test  
	public void testClearingCacheDoesClearNonSerializedValues() throws Exception {
		
		NonSerailizable ns = new NonSerailizable();
		
		NonSerailizable nsFirst=IxCache.getOrElse("Test", ()->ns);
		assertEquals(ns,nsFirst);
		
		NonSerailizable nsSecond=IxCache.getOrElse("Test", ()-> new NonSerailizable());
		assertEquals(ns,nsSecond);

		IxCache.clearCache(); // just clears in-memory cache
		NonSerailizable nsThird=IxCache.getOrElse("Test", ()->new NonSerailizable());
		assertNotEquals(ns,nsThird);

	}
	
	public static class NonSerailizable{}

	@Test  
	public void modelObjectsShouldNotBePassedThroughToDisk() throws Exception {
		Substance s = new Substance();

		Substance actualFound1=IxCache.getOrElse("Test", ()->s);
		assertTrue("Caches model should be the same as initial model" , s==actualFound1);

		Substance s2=(Substance)IxCache.get("Test");

		assertEquals(s.uuid,s2.uuid);

		IxCache.clearCache(); // just clears in-memory cache

		String actualFound2=IxCache.getOrElse("Test", ()->null);

		assertNull(actualFound2);
	}

	@Test  
	public void sessionSaveShouldNotInvalidateCache() throws Exception {
		String ret1=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET1");

		Session s = new Session();
		s.save();

		String ret2=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET2");

		assertEquals(ret1,ret2);
	}

	@Test 
	public void sessionDeleteShouldNotInvalidateCache() throws Exception {

		String ret1=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET_1");
		String ret2=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET_2");
		assertEquals(ret1,ret2);
		
		Session s = new Session();
		String ret3=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET_3");
		assertEquals(ret1,ret3);
		s.save();
		String ret4=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET_4");
		assertEquals(ret1,ret4);
		s.delete();
		String ret5=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET_5");
		assertEquals(ret1,ret5);
	}

	

}
