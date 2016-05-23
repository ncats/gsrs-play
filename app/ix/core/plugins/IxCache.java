package ix.core.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.CacheStrategy;
import ix.core.UserFetcher;
import ix.utils.Util;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.CacheEventListener;
import play.Application;
import play.Logger;
import play.Plugin;

public class IxCache extends Plugin {
    private static final String IX_CACHE_EVICTABLE = "IxCache-Evictable";
    private static final String IX_CACHE_NOT_EVICTABLE = "IxCache-Not-Evictable";
    
    static final int MAX_ELEMENTS = 10000;
    static final int TIME_TO_LIVE = 60*60; // 1hr
    static final int TIME_TO_IDLE = 60*60; // 1hr

    public static final String CACHE_MAX_ELEMENTS = "ix.evictableCache.maxElements";
    public static final String CACHE_TIME_TO_LIVE = "ix.evictableCache.timeToLive";
    public static final String CACHE_TIME_TO_IDLE = "ix.evictableCache.timeToIdle";

    private final Application app;
    private Cache evictableCache;
    private Cache nonEvictableCache;

    private GateKeeper gateKeeper;

    private IxContext ctx;

    static private IxCache _instance;
    
    private KeyMaster keymaster = new ExplicitMapKeyMaster();

    
    
    public IxCache (Application app) {
        this.app = app;
    }


    @Override
    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");
        ctx = app.plugin(IxContext.class);
        
        int maxElements = app.configuration()
            .getInt(CACHE_MAX_ELEMENTS, MAX_ELEMENTS);

        int timeToLive = app.configuration()
                .getInt(CACHE_TIME_TO_LIVE, TIME_TO_LIVE);

        int timeToIdle = app.configuration()
                .getInt(CACHE_TIME_TO_IDLE, TIME_TO_IDLE);


        evictableCache = new Cache ( new CacheConfiguration (IX_CACHE_EVICTABLE, maxElements)
                                            .timeToLiveSeconds(timeToLive)
                                            .timeToIdleSeconds(timeToIdle));

        nonEvictableCache = new Cache ( new CacheConfiguration (IX_CACHE_NOT_EVICTABLE, maxElements)
                                                .timeToLiveSeconds(timeToLive)
                                                .timeToIdleSeconds(timeToIdle));

        CacheManager.getInstance().addCache(evictableCache);

        CacheManager.getInstance().addCache(nonEvictableCache);

        evictableCache.setSampledStatisticsEnabled(true);

        gateKeeper = new GateKeeper( ctx, new ExplicitMapKeyMaster(), evictableCache, nonEvictableCache);
        _instance = this;
    }

    @Override
    public void onStop () {
        Logger.info("Stopping plugin "+getClass().getName());

        gateKeeper.close();
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

//    public static long getLastAccessTime (String key) {
//        if (_instance == null)
//            throw new IllegalStateException ("Cache hasn't been initialized!");
//        Element elm = _instance.evictableCache.get(adaptKey(key));
//        return elm != null ? elm.getLastAccessTime() : 0l;
//    }
//
//    public static long getExpirationTime (String key) {
//        if (_instance == null)
//            throw new IllegalStateException ("Cache hasn't been initialized!");
//        Element elm = _instance.evictableCache.get(adaptKey(key));
//        return elm != null ? elm.getExpirationTime() : 0l;
//    }
//
//    public static boolean isExpired (String key) {
//        if (_instance == null)
//            throw new IllegalStateException ("Cache hasn't been initialized!");
//        Element elm = _instance.evictableCache.get(adaptKey(key));
//        return elm != null ? elm.isExpired() : false;
//    }

    /**
     * apply generator if the evictableCache was created before epoch
     */
    public static <T> T getOrElse (long epoch,
                                   String key, Callable<T> generator)
        throws Exception {

        checkInitialized();
        return _instance.gateKeeper.getSinceOrElse(key,epoch, generator);

//        if (_instance == null)
//            throw new IllegalStateException ("Cache hasn't been initialized!");
//        Element elm = _instance.evictableCache.get(adaptKey(key));
//        if (elm == null || elm.getCreationTime() < epoch) {
//            T v = generator.call();
//            elm = new Element (adaptKey(key), v);
//            _instance.evictableCache.put(elm);
//        }
//        try {
//            return (T) elm.getObjectValue();
//        }
//        catch(Exception e){
//            T v = generator.call();
//            elm = new Element (adaptKey(key), v);
//            _instance.evictableCache.put(elm);
//            return (T) elm.getObjectValue();
//        }
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
//
//        if (_instance == null)
//            throw new IllegalStateException ("Cache hasn't been initialized!");
//
//        Object value = get (key);
//
//
//
//        if (value == null) {
//            if (_instance.ctx.debug(2))
//                Logger.debug("IxCache missed: "+adaptKey(key));
//            T v = generator.call();
//            String adaptKey=adaptKey(key);
//            _instance.evictableCache.put
//                (new Element (adaptKey, v, seconds <= 0, seconds, seconds));
//            _instance.keymaster.addKey(key, adaptKey);
//
//            return v;
//        }
//        return (T)value;
    }
    
    
    
    
    
    public static <T> T getOrElseRaw (String key, Callable<T> generator,
            int seconds) throws Exception {

        checkInitialized();
        return _instance.gateKeeper.getOrElseRaw(key,  generator,seconds);
//
//		if (_instance == null)
//		throw new IllegalStateException ("Cache hasn't been initialized!");
//
//		Object value = getRaw (key);
//		if (value == null) {
//		if (_instance.ctx.debug(2))
//		Logger.debug("IxCache missed: "+key);
//		T v = generator.call();
//		_instance.evictableCache.put
//		(new Element (key, v, seconds <= 0, seconds, seconds));
//		return v;
//		}
//		return (T)value;
	}


    public static JsonNode toJson(String key){
        Element e = _instance.gateKeeper.getRawElement(key);
        return new ObjectMapper().valueToTree(e);
    }

    public static Stream<Element> toJsonStream(int top, int skip){
        checkInitialized();
        final ObjectMapper mapper = new ObjectMapper();

        Stream<String> stream = Stream.concat(
                                 _instance.evictableCache.getKeys().stream(),
                                 _instance.nonEvictableCache.getKeys().stream());

        return stream.skip(skip)
                .limit(top)
                .map(k -> _instance.gateKeeper.getRawElement(k));

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
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        return _instance.evictableCache.getStatistics();
    }

    public static boolean contains (String key) {
        checkInitialized();
        return _instance.gateKeeper.contains(key);

    }
    
    public static String adaptKey(String okey){
    	final String user = UserFetcher.getActingUser(true).username;
    	String nkey = "!" + okey + "#" + Util.sha1(user);
    	return nkey;
    }
    public static String unAdaptKey(String adaptKey){
    	if(!adaptKey.startsWith("!")){
    		return adaptKey;
    	}else{
    		int lastindex=adaptKey.lastIndexOf('#');
    			
    		String p= adaptKey.substring(1, lastindex);
    		return p;
    	}
    }

	public static void setRaw(String key, Object value) {
        checkInitialized();
        _instance.gateKeeper.putRaw(key, value);

	}


    public interface KeyMaster{
		Set<String> getAllAdaptedKeys(String baseKey);
		void addKey(String baseKey, String adaptKey);
		void removeKey(String baseKey, String adaptKey);

		default boolean removeAllChildKeys(String key){
			boolean worked=true;
	    	Set<String> oldKeys=_instance.keymaster.getAllAdaptedKeys(key);
	    	if(oldKeys!=null){
	    		for(String okey:new ArrayList<String>(oldKeys)){
		    		worked &=_instance.evictableCache.remove(okey);
		    	}
	    	}
	    	return worked;
		}

        default void add(String baseKey){
            addKey(baseKey, adaptKey(baseKey));
        }


        default String adaptKey(String baseKey){
            final String user = UserFetcher.getActingUser(true).username;
            String nkey = "!" + baseKey + "#" + Util.sha1(user);
            return nkey;
        }

        default String unAdaptKey(String adaptedKey) {
            if (!adaptedKey.startsWith("!")) {
                return adaptedKey;
            }
            int lastindex = adaptedKey.lastIndexOf('#');

            return adaptedKey.substring(1, lastindex);
        }

	}
	
	public static class ExplicitMapKeyMaster implements KeyMaster{
		private ConcurrentHashMap<String,Set<String>> thekeys= new ConcurrentHashMap<String,Set<String>>();
	    private int size=0;
		public Set<String> getAllAdaptedKeys(String baseKey){
			return thekeys.get(baseKey);
		}

		@Override
		public void addKey(String baseKey, String adaptKey) {
			if(thekeys.computeIfAbsent(baseKey, k-> new HashSet<>()).add(adaptKey)){
				size++;
			}
		}

		@Override
		public void removeKey(String baseKey, String adaptKey) {
			Set<String> keylist=thekeys.get(baseKey);
			if(keylist!=null){
				if(keylist.remove(adaptKey)){
					size--;
				}
			}
		}
	}
	



	
}
