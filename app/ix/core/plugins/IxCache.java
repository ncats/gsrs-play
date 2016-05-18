package ix.core.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

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
    public static final String CACHE_NAME = "IxCache";
    
    static final int MAX_ELEMENTS = 10000;
    static final int TIME_TO_LIVE = 60*60; // 1hr
    static final int TIME_TO_IDLE = 60*60; // 1hr

    public static final String CACHE_MAX_ELEMENTS = "ix.cache.maxElements";
    public static final String CACHE_TIME_TO_LIVE = "ix.cache.timeToLive";
    public static final String CACHE_TIME_TO_IDLE = "ix.cache.timeToIdle";

    private final Application app;
    private Cache cache;
    private IxContext ctx;

    static private IxCache _instance;
    
    private KeyMaster keymaster = new ExplicitMapKeyMaster();
    private CacheEventListener cacheListener = new CacheEventListener(){
    	@Override
    	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
    		
    		
    		gateKeep(arg1);
    		
    	}
    	@Override
    	public void notifyElementRemoved(Ehcache arg0, Element arg1) throws CacheException {
    		gateKeep(arg1);
    	}
    	public void gateKeep(Element arg1){
    		String adaptKey = arg1.getObjectKey().toString();
    		String key=unAdaptKey(adaptKey);
    		keymaster.removeKey(key, adaptKey);
    		CacheStrategy cacheStrat=arg1.getObjectValue().getClass().getAnnotation(CacheStrategy.class);
    		if(cacheStrat!=null && !cacheStrat.evictable()){
    			if(!arg1.isExpired()){
    				_instance.cache.put(new Element(arg1.getObjectKey(), arg1.getObjectValue(),arg1.isEternal(),arg1.getTimeToIdle(),arg1.getTimeToLive()));
    			}
    		}
    	}
    	@Override
    	public void dispose() {}
    	@Override
    	public void notifyElementExpired(Ehcache arg0, Element arg1) {}
    	@Override
    	public void notifyElementPut(Ehcache arg0, Element arg1) throws CacheException {}
    	@Override
    	public void notifyElementUpdated(Ehcache arg0, Element arg1) throws CacheException {}
    	@Override
    	public void notifyRemoveAll(Ehcache arg0) {}
    	
    	public Object clone(){
    		return null;
    	}
    };
    
    
    public IxCache (Application app) {
        this.app = app;
    }
    
    
    

    @Override
    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");
        ctx = app.plugin(IxContext.class);
        
        int maxElements = app.configuration()
            .getInt(CACHE_MAX_ELEMENTS, MAX_ELEMENTS);
        CacheConfiguration config =
            new CacheConfiguration (CACHE_NAME, maxElements)
            .timeToLiveSeconds(app.configuration()
                               .getInt(CACHE_TIME_TO_LIVE, TIME_TO_LIVE))
            .timeToIdleSeconds(app.configuration()
                               .getInt(CACHE_TIME_TO_IDLE, TIME_TO_IDLE));
        cache = new Cache (config);
        CacheManager.getInstance().addCache(cache);     
        cache.setSampledStatisticsEnabled(true);
        cache.getCacheEventNotificationService().registerListener(cacheListener);
        _instance = this;
    }

    @Override
    public void onStop () {
        Logger.info("Stopping plugin "+getClass().getName());   
        try {
            cache.dispose();
            CacheManager.getInstance().removeCache(cache.getName());
        }
        catch (Exception ex) {
            Logger.trace("Disposing cache", ex);
        }
    }

    public static Element getElm (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        return _instance.cache.get(adaptKey(key));
    }
    
    public static Object get (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        Element elm = _instance.cache.get(adaptKey(key));
        return elm != null ? elm.getObjectValue() : null;
    }
    private static Object getRaw (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        Element elm = _instance.cache.get(key);
        return elm != null ? elm.getObjectValue() : null;
    }

    public static long getLastAccessTime (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        Element elm = _instance.cache.get(adaptKey(key));
        return elm != null ? elm.getLastAccessTime() : 0l;
    }

    public static long getExpirationTime (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        Element elm = _instance.cache.get(adaptKey(key));
        return elm != null ? elm.getExpirationTime() : 0l;
    }

    public static boolean isExpired (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        Element elm = _instance.cache.get(adaptKey(key));
        return elm != null ? elm.isExpired() : false;
    }

    /**
     * apply generator if the cache was created before epoch
     */
    public static <T> T getOrElse (long epoch,
                                   String key, Callable<T> generator)
        throws Exception {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        Element elm = _instance.cache.get(adaptKey(key));
        if (elm == null || elm.getCreationTime() < epoch) {
            T v = generator.call();
            elm = new Element (adaptKey(key), v);
            _instance.cache.put(elm);
        }
        try {
            return (T) elm.getObjectValue();
        }
        catch(Exception e){
            T v = generator.call();
            elm = new Element (adaptKey(key), v);
            _instance.cache.put(elm);
            return (T) elm.getObjectValue();
        }
    }
    
    public static <T> T getOrElse (String key, Callable<T> generator)
        throws Exception {
    	return getOrElse(key,generator,0);
    }

    
    
    // mimic play.Cache 
    public static <T> T getOrElse (String key, Callable<T> generator,
                                   int seconds) throws Exception {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        
        Object value = get (key);
        
        
        	
        if (value == null) {
            if (_instance.ctx.debug(2))
                Logger.debug("IxCache missed: "+adaptKey(key));
            T v = generator.call();
            String adaptKey=adaptKey(key);
            _instance.cache.put
                (new Element (adaptKey, v, seconds <= 0, seconds, seconds));
            _instance.keymaster.addKey(key, adaptKey);
            
            return v;
        }
        return (T)value;
    }
    
    
    
    
    
    public static <T> T getOrElseRaw (String key, Callable<T> generator,
            int seconds) throws Exception {
		if (_instance == null)
		throw new IllegalStateException ("Cache hasn't been initialized!");
		
		Object value = getRaw (key);
		if (value == null) {
		if (_instance.ctx.debug(2))
		Logger.debug("IxCache missed: "+key);
		T v = generator.call();
		_instance.cache.put
		(new Element (key, v, seconds <= 0, seconds, seconds));
		return v;
		}
		return (T)value;
	}

    public static List getKeys () {
        try {
            return new ArrayList (_instance.cache.getKeys());
        }
        catch (Exception ex) {
            Logger.trace("Can't get cache keys", ex);
        }
        return null;
    }
    
    public static List getKeys (int top, int skip) {
        List keys = getKeys ();
        if (keys != null) {
            keys = keys.subList(skip, Math.min(skip+top, keys.size()));
        }
        return keys;
    }
    
    public static void set (String key, Object value) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        _instance.cache.put(new Element (adaptKey(key), value));
    }

    public static void set (String key, Object value, int expiration) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        _instance.cache.put
            (new Element (adaptKey(key), value, expiration <= 0, expiration, expiration));
    }

    public static boolean remove (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        return _instance.cache.remove(adaptKey(key));
    }
    
    public static boolean removeAllChildKeys (String key){
    	if (_instance == null){
            throw new IllegalStateException ("Cache hasn't been initialized!");
    	}
    	return _instance.keymaster.removeAllChildKeys(key);
    }
    
   
    
    public static Statistics getStatistics () {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        return _instance.cache.getStatistics();
    }

    public static boolean contains (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        return _instance.cache.isKeyInCache(adaptKey(key));
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
		 if (_instance == null)
	            throw new IllegalStateException ("Cache hasn't been initialized!");
	      _instance.cache.put(new Element (key, value));
	}

	public static interface KeyMaster{
		public Set<String> getAllAdaptedKeys(String baseKey);
		public void addKey(String baseKey, String adaptKey);
		public void removeKey(String baseKey, String adaptKey);
		default boolean removeAllChildKeys(String key){
			boolean worked=true;
	    	Set<String> oldKeys=_instance.keymaster.getAllAdaptedKeys(key);
	    	if(oldKeys!=null){
		    	for(String okey:oldKeys){
		    		worked &=_instance.cache.remove(okey);
		    	}
	    	}
	    	return worked;
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
