package ix.core.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ix.core.CacheStrategy;
import ix.core.util.CachedSupplier;
import ix.utils.CallableUtil.TypedCallable;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import play.Logger;


/**
 * Created by katzelda on 5/19/16.
 */
public class TwoCacheGateKeeper implements GateKeeper {

	private Map<String, MyElement> temporaryCache = new ConcurrentHashMap<String,MyElement>();

    private final KeyMaster keyMaster;
    private final Ehcache evictableCache;
    private final Ehcache nonEvictableCache;

    private volatile boolean isClosed;


    private final int debugLevel;

    public TwoCacheGateKeeper(int debugLevel, KeyMaster keyMaster, Ehcache evictableCache, Ehcache nonEvictableCache){
        Objects.requireNonNull(keyMaster);
        Objects.requireNonNull(evictableCache);
        Objects.requireNonNull(nonEvictableCache);

        this.debugLevel = debugLevel;
        this.keyMaster = keyMaster;
        this.evictableCache = evictableCache;
        this.nonEvictableCache = nonEvictableCache;

    }

    private <T> CacheGeneratorWrapper<T> createRaw(TypedCallable<T> delegate, String key, int seconds){
        return new CacheGeneratorWrapper<T>(delegate, key, key,seconds);
    }

    private <T> CacheGeneratorWrapper<T> createKeyWrapper(TypedCallable<T> delegate, String key, String adaptedKey, int seconds){
        return new CacheGeneratorWrapper<T>(delegate, key, adaptedKey,seconds);
    }
    
    private <T> CacheGeneratorWrapper<T> createKeyWrapper(TypedCallable<T> delegate, String key, int seconds){
    	String adaptedKey = keyMaster.adaptKey(key);
        return new CacheGeneratorWrapper<T>(delegate, key, adaptedKey,seconds);
    }

   
    @Override
    public void clear() {
        keyMaster.removeAll();
        evictableCache.removeAll();
        nonEvictableCache.removeAll();
    }

    @Override
    public boolean remove(String key) {
        String adaptedKey = keyMaster.adaptKey(key);
        return removeRaw(adaptedKey);
    }

    private boolean removeRaw(String adaptedKey) {
    	temporaryCache.remove(adaptedKey);
        if(evictableCache.removeWithWriter(adaptedKey)){
            return true;
        }
        return nonEvictableCache.removeWithWriter(adaptedKey);
    }

    @Override
    public boolean removeAllChildKeys(String key) {
        Set<String> set = keyMaster.getAllAdaptedKeys(key);
        if(set ==null || set.isEmpty()){
            return false;
        }
        set.stream()
                .forEach(k-> removeRaw(k));

        return true;
    }



    @Override
    public Stream<Element> elements(int top, int skip) {
        Stream<String> stream = Stream.concat(
                evictableCache.getKeys().stream(),
                nonEvictableCache.getKeys().stream());

        return stream.skip(skip)
                .limit(top)
                .map(k -> getRawElement(k));

    }
    /**
     * Wraps a generator that creates the actual thing to cache
     * and puts it in the correct cache with the correct adapted key.
     * @param <T>
     */
    private class CacheGeneratorWrapper<T>{
       private final TypedCallable<T> delegate;
       private final String key, adaptedKey;
       private final int seconds;
       
       public CacheGeneratorWrapper(TypedCallable<T> delegate, String key, String adaptedKey, int seconds) {
           this.delegate = delegate;
           this.key = key;
           this.adaptedKey = adaptedKey;
           this.seconds = seconds;
       }

       public CachedSupplier<T> call() {
           //T t = delegate.call();
           keyMaster.addKey(key, adaptedKey);
           
           CachedSupplier<T> memdelegate=CachedSupplier.of(()->{
        	   try{
        		   T ret= delegate.call();
        		   if(ret==null){
        			  removeRaw(adaptedKey);
        		   }
        		   return ret;
        	   }catch(Exception e){
        		   removeRaw(adaptedKey);
        		   return null;
        	   }
           });
           
           Class<?> type=delegate.getType();
           addToCache(adaptedKey, memdelegate, seconds, type);
           
           
           return memdelegate;
       }
   }
    
   



    @Override
    public <T> T getSinceOrElse(String key, long creationTime, TypedCallable<T> generator) throws Exception{
        return getSinceOrElse(key, creationTime, generator, 0);
    }
    @Override
    public <T> T getSinceOrElse(String key, long creationTime, TypedCallable<T> generator, int seconds) throws Exception{
    	String adaptedKey = keyMaster.adaptKey(key);
        return getOrElseRaw(adaptedKey,
                createKeyWrapper(generator, key, adaptedKey, seconds),
                e->e.getCreationTime() < creationTime
                );
    }



    private  <T> T getOrElseRaw(String key, CacheGeneratorWrapper<T> generator, Predicate<Element> regeneratePredicate) throws Exception{
    	Element e = getRawElement(key);
    	
        if(e ==null || regeneratePredicate.test(e)){
            if (debugLevel >= 2) {
                Logger.debug("IxCache missed: " + key);
            }
            generator.call().get(); //force caching of supplier
            e=getRawElement(key);
        }
        
        try {
            return (T) getObjectFromElement(e);
        }catch(Exception ex){
            //in case there is a cast problem
            //or some other problem with the cached value
            //re-generate
        	generator.call().get();
        	return (T) getObjectFromElement(getRawElement(key));
        }

    }

    @Override
    public <T> T getOrElseRaw(String key, TypedCallable<T> generator, int seconds) throws Exception{
       return getOrElseRaw(key,
               createRaw(generator,key, seconds),
               (e)->false);
    }


    @Override
    public Object get(String key){
        return getRaw(keyMaster.adaptKey(key));
    }


    public Element getRawElement(String key){
    	Element e = this.temporaryCache.get(key);
    	if(e!=null){	
    		return e;
    	}
    	
        e = evictableCache.get(key);
        if(e ==null || e.getObjectValue()==null || getObjectFromElement(e)==null){
        	e = nonEvictableCache.get(key);
        	if(e==null || e.getObjectValue()==null || getObjectFromElement(e)==null){
        		return null;
        	}else{
        		return e;
        	}
        }
        return e;
    }
    @Override
    public Object getRaw(String key){
        Element e = getRawElement(key);
        return getObjectFromElement(e);
    }
    
    public Object getObjectFromElement(Element e){
        if(e ==null){
            return null;
        }
        Object avalue=e.getObjectValue();
        Object retValue=avalue;
        if(avalue instanceof CachedSupplier){
        	retValue=((CachedSupplier)avalue).get();	
        }
        
        //Takes care of those non-determinately Evictable things
        if(e instanceof MyElement){
        	MyElement polElm=(MyElement)e;
        	if(polElm.getEvictionType() == EvictionType.UNKNOWN){
        		EvictionType et=getEvictionPolicy(retValue.getClass());
        		if(et==EvictionType.UNKNOWN){
        			et=EvictionType.EVICTABLE;
        		}
        		polElm.setEvictionType(et);
        		addElementToCache(polElm);
        		this.temporaryCache.remove(polElm.getObjectKey().toString());
        	}
        }
        return retValue;
    }

    @Override
    public <T> T getOrElse(String key, TypedCallable<T> generator, int seconds) throws Exception{
        String adaptedKey = keyMaster.adaptKey(key);

        return getOrElseRaw(adaptedKey,
                createKeyWrapper(generator, key, adaptedKey, seconds),
                e-> false);
    }

    @Override
    public void put(String key, Object value, int expiration){
    	CacheGeneratorWrapper cgw = createKeyWrapper(TypedCallable.of(value),key,expiration);
    	cgw.call();
    }
    
    @Override
    public void putRaw(String key, Object value){
        putRaw(key, value, 0);
    }
    
    @Override
    public void putRaw(String key, Object value, int expiration){
    	CacheGeneratorWrapper cgw = createRaw(TypedCallable.of(value),key,expiration);
    	cgw.call();
    }
    
    public static class MyElement extends Element{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public EvictionType evictionType=EvictionType.EVICTABLE;
		public MyElement(Object key, Object value, Boolean eternal, Integer timeToIdleSeconds,
				Integer timeToLiveSeconds) {
			super(key, value, eternal, timeToIdleSeconds, timeToLiveSeconds);
			// TODO Auto-generated constructor stub
		}

		public MyElement(Object key, Object value, long version, long creationTime, long lastAccessTime, long hitCount,
				boolean cacheDefaultLifespan, int timeToLive, int timeToIdle, long lastUpdateTime) {
			super(key, value, version, creationTime, lastAccessTime, hitCount, cacheDefaultLifespan, timeToLive, timeToIdle,
					lastUpdateTime);
			// TODO Auto-generated constructor stub
		}

		public MyElement(Object key, Object value, long version, long creationTime, long lastAccessTime,
				long lastUpdateTime, long hitCount) {
			super(key, value, version, creationTime, lastAccessTime, lastUpdateTime, hitCount);
			// TODO Auto-generated constructor stub
		}

		public MyElement(Object key, Object value, long version) {
			super(key, value, version);
			// TODO Auto-generated constructor stub
		}

		public MyElement(Object key, Object value) {
			super(key, value);
			// TODO Auto-generated constructor stub
		}

		public MyElement(Serializable key, Serializable value, long version) {
			super(key, value, version);
			// TODO Auto-generated constructor stub
		}

		public MyElement(Serializable key, Serializable value) {
			super(key, value);
			// TODO Auto-generated constructor stub
		}
		public EvictionType getEvictionType(){
			return evictionType;
		}
		public void setEvictionType(EvictionType et){
			evictionType=et;
		}
    	
    }
    
    private void addToCache(String adaptedKey, CachedSupplier<?> value, int expiration, Class<?> type) {
        if(value == null){
            return;
        }
        MyElement e=new MyElement (adaptedKey, value, expiration <= 0, expiration, expiration);
        e.setEvictionType(getEvictionPolicy(type));
        addElementToCache(e);
    }
    
    private void addElementToCache(MyElement e){
        switch(e.getEvictionType()){
			case EVICTABLE:
				evictableCache.putWithWriter(e);
				break;
			case UNEVICTABLE:
				nonEvictableCache.putWithWriter(e);
				break;
			case UNKNOWN:
				temporaryCache.put(e.getObjectKey().toString(), e);
				break;
        }
    }

    @Override
    public List<Statistics> getStatistics() {
    	List<Statistics> stats= new ArrayList<Statistics>();
    	stats.add(evictableCache.getStatistics());
    	stats.add(nonEvictableCache.getStatistics());
        return stats;
    }
    
    //private Map<String, CacheStrategy> cacheStrategies = new ConcurrentHashMap<>();
    
    private static enum EvictionType{
    	EVICTABLE,
    	UNEVICTABLE,
    	UNKNOWN
    }

    private EvictionType getEvictionPolicy(Class<?> cls){
        if(cls ==null){
            return EvictionType.EVICTABLE;
        }
        if(cls.equals(Object.class)){
        	return EvictionType.UNKNOWN;
        }
        CacheStrategy cacheStrat=cls.getAnnotation(CacheStrategy.class);

        if(cacheStrat == null){
            return EvictionType.EVICTABLE;
        }
        return cacheStrat.evictable()?EvictionType.EVICTABLE:EvictionType.UNEVICTABLE;

    }

    @Override
    public boolean contains(String key){
        String adaptedKey = keyMaster.adaptKey(key);
        Element e=this.getRawElement(adaptedKey);
        return e !=null;
    }

    @Override
    public void put(String key, Object value) {
        put(key, value, 0);
    }

    @Override
    public <T> T getOrElseRaw(String key, TypedCallable<T> generator) throws Exception {
        return getOrElseRaw(key, generator, 0);
    }

    @Override
    public <T> T getOrElse(String key, TypedCallable<T> generator) throws Exception {
        return getOrElse(key, generator, 0);
    }

    @Override
    public void close() {
        if(isClosed){
            return;
        }
        isClosed=true;
        disposeCache(evictableCache);
        disposeCache(nonEvictableCache);
    }

    private void disposeCache(Ehcache c){
        try {
            //shouldn't call dispose, the CacheManager will do that for us
            CacheManager.getInstance().removeCache(c.getName());
        }catch(Exception e){
            Logger.trace("Disposing cache " + c.getName(), e);
        }
    }
    
    
}
