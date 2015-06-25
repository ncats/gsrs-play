package ix.core.plugins;

import java.util.concurrent.Callable;

import play.Logger;
import play.Plugin;
import play.Application;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.Statistics;

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

    static private IxCache _instance;
    
    public IxCache (Application app) {
        this.app = app;
    }

    @Override
    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");
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

    public static Object get (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        Element elm = _instance.cache.get(key);
        return elm != null ? elm.getObjectValue() : null;
    }

    public static <T> T getOrElse (String key, Callable<T> generator)
        throws Exception {
        Object value = get (key);
        if (value == null) {
            //Logger.debug("IxCache missed: "+key);
            T v = generator.call();
            _instance.cache.put(new Element (key, v));
            return v;
        }
        return (T)value;
    }

    // mimic play.Cache 
    public static <T> T getOrElse (String key, Callable<T> generator,
                                   int seconds) throws Exception {
        Object value = get (key);
        if (value == null) {
            //Logger.debug("IxCache missed: "+key);           
            T v = generator.call();
            _instance.cache.put
                (new Element (key, v, seconds <= 0, seconds, seconds));
            return v;
        }
        return (T)value;
    }

    public static boolean remove (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        return _instance.cache.remove(key);
    }
    
    public static Statistics getStatistics () {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        return _instance.cache.getStatistics();
    }

    public static boolean contains (String key) {
        if (_instance == null)
            throw new IllegalStateException ("Cache hasn't been initialized!");
        return _instance.cache.isKeyInCache(key);
    }
}
