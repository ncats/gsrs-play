package ix.core.plugins;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.config.CacheConfiguration;
import play.Application;
import play.Logger;
import play.Plugin;

public class IxCache extends Plugin {
    private static final String IX_CACHE_EVICTABLE = "IxCache-Evictable";
    private static final String IX_CACHE_NOT_EVICTABLE = "IxCache-Not-Evictable";
    
    static final int MAX_ELEMENTS = 10000;
    static final int TIME_TO_LIVE = 60*60; // 1hr
    static final int TIME_TO_IDLE = 60*60; // 1hr

    public static final String CACHE_MAX_ELEMENTS = "ix.cache.maxMemoryInMB";
    public static final String CACHE_TIME_TO_LIVE = "ix.cache.timeToLive";
    public static final String CACHE_TIME_TO_IDLE = "ix.cache.timeToIdle";

    private final Application app;

    private GateKeeper gateKeeper;


    static private IxCache _instance;

    
    
    public IxCache (Application app) {
        this.app = app;
    }


    public IxCache(int debugLevel, int maxElements, int timeToLive, int timeToIdle) {

        app = null;


//        evictableCache = new Cache ( new CacheConfiguration (IX_CACHE_EVICTABLE, maxElements)
//                .timeToLiveSeconds(timeToLive)
//                .timeToIdleSeconds(timeToIdle));
//
//        nonEvictableCache = new Cache ( new CacheConfiguration (IX_CACHE_NOT_EVICTABLE, 1)
//                .timeToLiveSeconds(timeToLive)
//                .timeToIdleSeconds(timeToIdle));
//
//
//
//        CacheManager.getInstance().removeCache(evictableCache.getName());
//        CacheManager.getInstance().addCache(evictableCache);
//
//
//        CacheManager.getInstance().removeCache(nonEvictableCache.getName());
//        CacheManager.getInstance().addCache(nonEvictableCache);
//
//
//        evictableCache.setSampledStatisticsEnabled(true);
//
//        gateKeeper = new TwoCacheGateKeeper(debugLevel, new ExplicitMapKeyMaster(), evictableCache, nonEvictableCache);

        gateKeeper = new GateKeeperFactory.Builder(maxElements, timeToLive, timeToIdle)
                                            .debugLevel(debugLevel)
                                            .useNonEvictableCache(maxElements,timeToLive,timeToIdle)
                                            .build()
        .                                    create();
    }


    @Override
    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");
        int debugLevel = app.plugin(IxContext.class).getDebugLevel();
        
        int maxElements = app.configuration()
            .getInt(CACHE_MAX_ELEMENTS, MAX_ELEMENTS);

        int timeToLive = app.configuration()
                .getInt(CACHE_TIME_TO_LIVE, TIME_TO_LIVE);

        int timeToIdle = app.configuration()
                .getInt(CACHE_TIME_TO_IDLE, TIME_TO_IDLE);

    System.out.println("####################");
        System.out.println("max elements = " + maxElements);

        _instance = new IxCache(debugLevel,maxElements,timeToLive,timeToIdle);
    }

    @Override
    public void onStop () {
        Logger.info("Stopping plugin "+getClass().getName());
        if(this!=_instance){
            _instance.gateKeeper.close();
        }else{
            gateKeeper.close();
        }
    }



    public static Element getElm (String key) {
        checkInitialized();
        return _instance.gateKeeper.getRawElement(key);
    }

    private static void checkInitialized(){
        if (_instance == null) {
            throw new IllegalStateException("Cache hasn't been initialized!");
        }
    }

    public static Object get (String key) {
        checkInitialized();
        return _instance.gateKeeper.get(key);
    }
    
    
    private static Object getRaw (String key) {
        checkInitialized();
        return _instance.gateKeeper.getRaw(key);
    }



    /**
     * apply generator if the evictableCache was created before epoch
     */
    public static <T> T getOrElse (long epoch,
                                   String key, Callable<T> generator)
        throws Exception {

        checkInitialized();
        return _instance.gateKeeper.getSinceOrElse(key,epoch, generator);


    }
    
    public static <T> T getOrElse (String key, Callable<T> generator)
        throws Exception {
    	return getOrElse(key,generator,0);
    }

    
    
    // mimic play.Cache 
    public static <T> T getOrElse (String key, Callable<T> generator,
                                   int seconds) throws Exception {

        checkInitialized();
        return _instance.gateKeeper.getOrElse(key,  generator,seconds);

    }
    
    
    
    public static void clearCache(){
        _instance.gateKeeper.clear();
    }
    
    public static <T> T getOrElseRaw (String key, Callable<T> generator,
            int seconds) throws Exception {

        checkInitialized();
        return _instance.gateKeeper.getOrElseRaw(key,  generator,seconds);

	}


    public static JsonNode toJson(String key){
        Element e = _instance.gateKeeper.getRawElement(key);
        return new ObjectMapper().valueToTree(e);
    }

    public static Stream<Element> toJsonStream(int top, int skip){
        checkInitialized();
        return _instance.gateKeeper.elements(top,skip);


    }


    
    public static void set (String key, Object value) {

        checkInitialized();
        _instance.gateKeeper.put(key, value);

    }

    public static void set (String key, Object value, int expiration) {
        checkInitialized();
        _instance.gateKeeper.put(key, value, expiration);
    }

    public static boolean remove (String key) {
        checkInitialized();
        return _instance.gateKeeper.remove(key);

    }
    
    public static boolean removeAllChildKeys (String key){
        checkInitialized();
        return _instance.gateKeeper.removeAllChildKeys(key);

    }
    
   
    
    public static Statistics getStatistics () {
        //TODO how to handle multiple caches
       checkInitialized();
        return _instance.gateKeeper.getStatistics();
    }

    public static boolean contains (String key) {
        checkInitialized();
        return _instance.gateKeeper.contains(key);

    }
    


	public static void setRaw(String key, Object value) {
        checkInitialized();
        _instance.gateKeeper.putRaw(key, value);

	}



	/**
	 * Used for temporary cache storage which may be needed across
	 * users or within both attached and detached sessions 
	 * (background threads)
	 * 
	 * Gets a value set from setTemp
	 *  
	 * @param key
	 * @return
	 */
	public static Object getTemp(String key) {
		return getRaw("tmp123" + key);
	}
	
	/**
	 * Used for temporary cache storage which may be needed across
	 * users or within both attached and detached sessions 
	 * (background threads)
	 * 
	 * Sets the value in such a way that the same key could fetch 
	 * that value, regardless of who is logged in.
	 * 
	 * @param key
	 * @return
	 */
	public static void setTemp(String key, Object value) {
		setRaw("tmp123" +key, value);
	}
	
}
