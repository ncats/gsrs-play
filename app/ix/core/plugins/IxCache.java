package ix.core.plugins;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import play.Application;
import play.Logger;
import play.Plugin;

public class IxCache extends Plugin {

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

    System.out.println("####################");
        System.out.println("max elements = " + maxElements);

        GateKeeper gateKeeper = new GateKeeperFactory.Builder( maxElements, timeToLive, timeToIdle)
                .debugLevel(debugLevel)
                .useNonEvictableCache(notEvictableMaxElements,timeToLive,timeToIdle)
                .cacheAdapter( new FileDbCache(context.cache(), "inMemCache"))
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
	public static <T> T getOrElseTemp(String key, Callable<T> generator) throws Exception{
		Object o=getTemp(key);
		if(o==null){
			o=generator.call();
			if(o!=null){
				setTemp(key,o);
			}
		}
		return (T)o;
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
	
	
	/* Here are the interfaces
    public Object createEntry (Object key) throws Exception {
        if (!(key instanceof Serializable)) {
            throw new IllegalArgumentException
                ("Cache key "+key+" is not serliazable!");
        }

        Element elm = null;
        try {
            DatabaseEntry dkey = getKeyEntry (key);
            DatabaseEntry data = new DatabaseEntry ();
            OperationStatus status = db.get(null, dkey, data, null);
            if (status == OperationStatus.SUCCESS) {
                ObjectInputStream ois = new ObjectInputStream
                    (new ByteArrayInputStream
                     (data.getData(), data.getOffset(), data.getSize()));
                elm = new Element (key, ois.readObject());
                ois.close();
            }
            else if (status == OperationStatus.NOTFOUND) {
                // 
            }
            else {
                Logger.warn("Unknown status for key "+key+": "+status);
            }
        }
        catch (Exception ex) {
            Logger.error("Can't recreate entry for "+key, ex);
        }
        return elm;
    }
    
   
    @Override
    public void init () {
        try {
            File dir = new File (ctx.cache(), "ix");
            dir.mkdirs();
            EnvironmentConfig envconf = new EnvironmentConfig ();
            envconf.setAllowCreate(true);
            Environment env = new Environment (dir, envconf);
            DatabaseConfig dbconf = new DatabaseConfig ();
            dbconf.setAllowCreate(true);
            db = env.openDatabase(null, CACHE_NAME, dbconf);
        }
        catch (Exception ex) {
            Logger.error("Can't initialize lucene for "+ctx.cache(), ex);
        }
    }
    
    @Override
    public void dispose () {
        if (db != null) {       
            try {
                Logger.debug("#### closing cache writer "+cache.getName()
                             +"; "+db.count()+" entries #####");
                db.close();
            }
            catch (Exception ex) {
                Logger.error("Can't close lucene index!", ex);
            }
        }
    }
    
    @Override
    public void delete (CacheEntry entry) {
        Object key = entry.getKey();
        if (!(key instanceof Serializable))
            return;

        try {
            DatabaseEntry dkey = getKeyEntry (key);
            OperationStatus status = db.delete(null, dkey);
            if (status != OperationStatus.SUCCESS)
                Logger.warn("Delete cache key '"
                            +key+"' returns status "+status);
        }
        catch (Exception ex) {
            Logger.error("Deleting cache "+key+" from persistence!", ex);
        }
    }
    
    @Override
    public void write (Element elm) {
        Serializable key = elm.getKey();
        if (key != null) {
            //Logger.debug("Persisting cache key="+key+" value="+elm.getObjectValue());
            try {
                DatabaseEntry dkey = getKeyEntry (key);
                DatabaseEntry data = new DatabaseEntry
                    (Util.serialize(elm.getObjectValue()));
                OperationStatus status = db.put(null, dkey, data);
                if (status != OperationStatus.SUCCESS)
                    Logger.warn
                        ("** PUT for key "+key+" returns status "+status);
            }
            catch (Exception ex) {
                Logger.error("Can't write cache element: key="
                             +key+" value="+elm.getObjectValue(), ex);
            }
        }
        else {
            Logger.warn("Key "+elm.getObjectKey()+" isn't serializable!");
        }
    }

    @Override
    public void deleteAll (Collection<CacheEntry> entries) {
        for (CacheEntry e : entries)
            delete (e);
    }
    
    @Override
    public void writeAll (Collection<Element> entries) {
        for (Element elm : entries)
            write (elm);
    }

    @Override
    public void throwAway (Element elm,
                           SingleOperationType op, RuntimeException ex) {
        Logger.error("Throwing away cache element "+elm.getKey(), ex);
    }

    @Override
    public CacheWriter clone (Ehcache cache) {
        throw new UnsupportedOperationException
            ("This implementation doesn't support clone operation!");
    }
    
    */
//}

	
}
