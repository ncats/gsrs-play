package ix.test.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;

import ix.core.models.Role;
import ix.core.models.Session;
import ix.core.plugins.IxCache;
import ix.ginas.models.v1.Substance;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.utils.Util;

public class IxCacheTest {
	@Rule
	public GinasTestServer ts = new GinasTestServer(9001);
	@Test 
	public void testFetchFromCacheWithSameKeyReturnsFirstCachedValue() throws Exception {
		try (RestSession session = ts.newRestSession(ts.createUser(Role.Admin))) {
			final String result1="First";
			final String result2="Second";
			
			String first= IxCache.getOrElse("Test", ()->result1);
			String second= IxCache.getOrElse("Test", ()->result2);
			assertEquals(first,second);
			assertEquals(first,result1);
			
		}
	}
	
	@Test 
	public void fetchSlowGeneratorWith2ThreadsShouldNotCallSecondGenerator() throws Exception {
		try (RestSession session = ts.newRestSession(ts.createUser(Role.Admin))) {
			final String result1="First";
			final String result2="Second";
			ConcurrentHashMap<String,String> myMap = new ConcurrentHashMap<>();
			
			new Thread(()->{
				try {
					myMap.put("first", IxCache.getOrElse("Test", ()->{
						Util.debugSpin(1000); //wait a second
						return result1;
					}));
				} catch (Exception e) {
					myMap.put("first","");
					e.printStackTrace();
				}
			}).start();
			Thread.sleep(10);
			new Thread(()->{
				try {
					myMap.put("second", IxCache.getOrElse("Test", ()->{
						Util.debugSpin(1000); //wait a second
						return result2;
					}));
				} catch (Exception e) {
					myMap.put("second","");
					e.printStackTrace();
				} 
			}).start();
			
			while(myMap.size()<2){
				Thread.sleep(10);
			}
			HashSet<String> hset = new HashSet<>();
			hset.add(result1);
			assertEquals(hset,myMap.values().stream().collect(Collectors.toSet()));
			
		}
	}
	
	@Test 
	public void fetchSlowGeneratorWith2ThreadsShouldNotRecalculate() throws Exception {
		try (RestSession session = ts.newRestSession(ts.createUser(Role.Admin))) {
			final String result1="First";
			AtomicInteger generatorCalls = new AtomicInteger(0);
			AtomicInteger cacheCalls = new AtomicInteger(0);
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
					cacheCalls.incrementAndGet();
				}
			};
			final int staggeredThreads = 2;
			for(int i=0;i<staggeredThreads;i++){
				new Thread(r).start();
				Thread.sleep(10);
			}
			
			while(cacheCalls.get()<staggeredThreads){
				Thread.sleep(10);
			}
			assertEquals(staggeredThreads,cacheCalls.get());
			assertEquals(1,generatorCalls.get());
		}
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
		try (RestSession session = ts.newRestSession(ts.createUser(Role.Admin))) {
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
	}
	
	@Test 
	public void modelObjectsShouldNotBePassedThroughToDisk() throws Exception {
		try (RestSession session = ts.newRestSession(ts.createUser(Role.Admin))) {
			Substance s = new Substance();
			
			Substance actualFound1=IxCache.getOrElse("Test", ()->s);
			
			Substance s2=(Substance)IxCache.get("Test");
			
			assertEquals(s.uuid,s2.uuid);
			
			IxCache.clearCache(); // just clears in-memory cache
			String actualFound2=IxCache.getOrElse("Test", ()->null);
			assertNull(actualFound2);
			
		}
	}
	
	@Test 
	public void sessionSaveShouldNotInvalidateCache() throws Exception {
		try (RestSession session = ts.newRestSession(ts.createUser(Role.Admin))) {
			
			String ret1=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET1");
			
			Session s = new Session();
			s.save();
			
			String ret2=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET2");
			
			assertEquals(ret1,ret2);
		}
	}
	
	@Test 
	public void sessionDeleteShouldNotInvalidateCache() throws Exception {
		try (RestSession session = ts.newRestSession(ts.createUser(Role.Admin))) {
			
			String ret1=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET1");
			
			Session s = new Session();
			s.save();
			s.delete();
			
			String ret2=ix.ncats.controllers.App.getOrElse("TestKey", ()->"RET2");
			
			assertEquals(ret1,ret2);
		}
	}
	
}
