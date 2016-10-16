package ix.core.plugins;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.Key;
import ix.utils.CallableUtil.TypedCallable;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import play.Application;
import play.Logger;
import play.Plugin;

public class IxCache extends Plugin {
	private AtomicLong lastNotifiedChange=new AtomicLong(0l); // The last timestamp IxCache was told there was a change
	
    static final int DEFAULT_MAX_ELEMENTS = 10000;
    static final int DEFAULT_TIME_TO_LIVE = 60*60; // 1hr
    static final int DEFAULT_TIME_TO_IDLE = 60*60; // 1hr

    public static final String CACHE_MAX_ELEMENTS = "ix.cache.maxElements";
    public static final String CACHE_MAX_NOT_EVICTABLE_ELEMENTS = "ix.cache.maxElementsNotEvictable";
    public static final String CACHE_TIME_TO_LIVE = "ix.cache.timeToLive";
    public static final String CACHE_TIME_TO_IDLE = "ix.cache.timeToIdle";

    private final Application app;

    private GateKeeper gateKeeper;

    static private IxCache _instance;

    public IxCache (Application app) {
        this.app = app;
    }

    public IxCache( GateKeeper gateKeeper) {
        app = null;
        this.gateKeeper =gateKeeper;
    }


    @Override
    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");
        IxContext context = app.plugin(IxContext.class);
        int debugLevel = context.getDebugLevel();
        
        int maxElements = app.configuration()
        		.getInt(CACHE_MAX_ELEMENTS, DEFAULT_MAX_ELEMENTS);

        int notEvictableMaxElements = app.configuration()
                .getInt(CACHE_MAX_NOT_EVICTABLE_ELEMENTS, DEFAULT_MAX_ELEMENTS);

        int timeToLive = app.configuration()
                .getInt(CACHE_TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE);

        int timeToIdle = app.configuration()
                .getInt(CACHE_TIME_TO_IDLE, DEFAULT_TIME_TO_IDLE);

        GateKeeper gateKeeper = new GateKeeperFactory.Builder( maxElements, timeToLive, timeToIdle)
                .debugLevel(debugLevel)
                .useNonEvictableCache(notEvictableMaxElements,timeToLive,timeToIdle)
                .cacheAdapter(new FileDbCache(context.cache(), "inMemCache"))
                .keyMaster(new KeyMaster(){

					@Override
					public Set<String> getAllAdaptedKeys(String baseKey) {
						return Stream.of(baseKey).collect(Collectors.toSet());
					}

					@Override
					public void addKey(String baseKey, String adaptKey) {
						
					}

					@Override
					public void removeKey(String baseKey, String adaptKey) {
						
					}

					@Override
					public String adaptKey(String baseKey) {
						return baseKey;
					}

					@Override
					public String unAdaptKey(String adaptedKey) {
						return adaptedKey;
					}

					@Override
					public void removeAll() {
						
					}
                	
                })
                .build()
                .create();
        _instance = new IxCache(gateKeeper);
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
                                   String key, TypedCallable<T> generator)
        throws Exception {

        checkInitialized();
        return _instance.gateKeeper.getSinceOrElse(key,epoch, generator);


    }
    
    public static <T> T getOrElse (String key, TypedCallable<T> generator)
        throws Exception {
    	return getOrElse(key,generator,0);
    }
    
    // mimic play.Cache 
    public static <T> T getOrElse (String key, TypedCallable<T> generator,
                                   int seconds) throws Exception {
        checkInitialized();
        return _instance.gateKeeper.getOrElse(key,  generator,seconds);

    }
    
    public static void clearCache(){
        _instance.gateKeeper.clear();
    }
    
    public static <T> T getOrElseRaw (String key, TypedCallable<T> generator,
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
    
   
    
    public static List<Statistics> getStatistics () {
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


	@SuppressWarnings("unchecked")
	public static <T> T getOrElseTemp(String key, TypedCallable<T> generator) throws Exception{
		Object o=getTemp(key);
		if(o==null){
			o=generator.call();
			if(o!=null){
				setTemp(key,o);
			}
		}
		return (T)o;
	}
	
	public static Object getOrFetchTempRecord(Key k) throws Exception {
		return getOrElseTemp(k.toString(), ()->{
            Optional<EntityUtils.EntityWrapper<?>> ret = k.fetch();
            if(ret.isPresent()){
                return ret.get().getValue();
            }
            return null;
        });
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
		return getRaw(key);
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
		setRaw(key, value);
	}


	/**
	 * Whenever there's a change to some underlying data store (Lucene, Database, etc)
	 * that this cache will be caching, you can mark it here. Depending on certain
	 * policy considerations, you may be able to reject things older than this time of change
	 * 
	 */
	public static void markChange() {
		//System.err.println(Util.getExecutionPath());
		_instance.notifyChange(System.currentTimeMillis());
	}
	
	
	/**
	 * Stores the latest of the two time stamps as the time that things 
	 * should be fresh AFTER (possibly stale before this time).
	 * @param time
	 */
	public void notifyChange(long time){
		lastNotifiedChange.updateAndGet(u-> Math.max(u,time));
	}
	
	public boolean hasBeenNotifiedSince(long thistime){
		if(lastNotifiedChange.get()>thistime)return true;
		return false;
	}
	
	public static boolean mightBeDirtySince(long t){
		
		return _instance.hasBeenNotifiedSince(t);
	}
	
	public static IxCache getInstance(){
		return _instance;
	}
	
	
	
}
