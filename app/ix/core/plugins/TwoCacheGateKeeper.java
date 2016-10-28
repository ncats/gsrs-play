package ix.core.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import ix.core.CacheStrategy;
import ix.core.util.CachedSupplier;

import ix.utils.CallableUtil.TypedCallable;
import ix.core.util.TimeUtil;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import play.Logger;


/**
 * Created by katzelda on 5/19/16.
 */
public class TwoCacheGateKeeper implements GateKeeper {

	private Map<String, TimeUtilCacheElement> temporaryCache = new ConcurrentHashMap<String,TimeUtilCacheElement>();

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



    @SuppressWarnings("unchecked")
	@Override
    public Stream<Element> elements(int top, int skip) {
    	Stream<String> s1=evictableCache.getKeys().stream().map(k->k.toString());
    	Stream<String> s2=nonEvictableCache.getKeys().stream().map(k->k.toString());
    	Stream<String> s3=this.temporaryCache.keySet().stream();
    	
        Stream<String> stream = Stream.concat(Stream.concat(s1,s2),s3);

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
           Class<?> type=delegate.getType();
           
           final EvictionType originalType=getEvictionPolicy(type);
           
           CachedSupplier<T> memdelegate=ListeningCachedSupplier.of(()->{
        	   try{
        		   T ret= delegate.call();
        		   return ret;
        	   }catch(Exception e){
        		   return null;
        	   }
           }, ret->{
        	   if(ret==null){
     			  removeRaw(adaptedKey);
     		   }else{
     			  if(originalType==EvictionType.UNKNOWN){
     				 refreshElementAtWith(adaptedKey,ret.getClass());
     			  }
     		   }
           });
           
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



    @SuppressWarnings("unchecked")
	private  <T> T getOrElseRaw(String key, CacheGeneratorWrapper<T> generator, Predicate<Element> regeneratePredicate) throws Exception{
    	Element e = getRawElement(key);
    	
        if(e ==null || regeneratePredicate.test(e)){
            if (debugLevel >= 2) {
                Logger.debug("IxCache missed: " + key);
            }
            return generator.call().getSync();
        }
        
        try {
            return (T) getObjectFromElement(e);
        }catch(Exception ex){
            //in case there is a cast problem
            //or some other problem with the cached value
            //re-generate
        	return generator.call().getSync();
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
    
    @SuppressWarnings("unchecked")
	private Object getObjectFromElement(Element e){
        if(e ==null){
            return null;
        }
        Object avalue=e.getObjectValue();
        Object retValue=avalue;
        if(avalue instanceof CachedSupplier){
        	retValue=((CachedSupplier<Object>)avalue).get();	
        }
        return retValue;
    }
    
    private void refreshElementAtWith(String adaptedKey, Class<?> type){
    	TimeUtilCacheElement elm = (TimeUtilCacheElement)this.getRawElement(adaptedKey);
    	refreshElementWith(elm, type);
    }
    
    private void refreshElementWith(TimeUtilCacheElement e, Class<?> type){
    	if(e==null){
    		return;
    	}
		TimeUtilCacheElement polElm = (TimeUtilCacheElement) e;
		if (polElm.getEvictionType() == EvictionType.UNKNOWN) {
			EvictionType et = getEvictionPolicy(type);
			if (et == EvictionType.UNKNOWN) {
				et = EvictionType.EVICTABLE;
			}
			polElm.setEvictionType(et);
			resortElement(polElm);
		}
    }
    
    private void resortElement(TimeUtilCacheElement polElm){
    	addElementToCache(polElm);
		this.temporaryCache.remove(polElm.getObjectKey().toString());
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
    	cgw.call().getSync();
    }
    
    @Override
    public void putRaw(String key, Object value){
        putRaw(key, value, 0);
    }
    
    @Override
    public void putRaw(String key, Object value, int expiration){
    	CacheGeneratorWrapper<Object> cgw = createRaw(TypedCallable.of(value),key,expiration);
    	cgw.call().getSync();
    }
    
    
    /**
     * Extension of EhCache Element that overrides
     * the Element expiration calculation to use our {@link TimeUtil}
     * library to get the current time instead of System time.
     * This lets us play around with what the cache thinks is "now"
     * in our tests.
     * 
     * In addition, this element allows a little more information
     * about what kind of eviction policy should be used
     * for the object being cached.
     *
     * @author katzelda
     */
    public static class TimeUtilCacheElement extends Element{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public EvictionType evictionType=EvictionType.EVICTABLE;
		public TimeUtilCacheElement(Object key, Object value, Boolean eternal, Integer timeToIdleSeconds,
				Integer timeToLiveSeconds) {
			super(key, value, eternal, timeToIdleSeconds, timeToLiveSeconds);
		}

		public TimeUtilCacheElement(Object key, Object value, long version, long creationTime, long lastAccessTime, long hitCount,
				boolean cacheDefaultLifespan, int timeToLive, int timeToIdle, long lastUpdateTime) {
			super(key, value, version, creationTime, lastAccessTime, hitCount, cacheDefaultLifespan, timeToLive, timeToIdle,
					lastUpdateTime);
		}

		public TimeUtilCacheElement(Object key, Object value, long version, long creationTime, long lastAccessTime,
				long lastUpdateTime, long hitCount) {
			super(key, value, version, creationTime, lastAccessTime, lastUpdateTime, hitCount);
		}

		public TimeUtilCacheElement(Object key, Object value, long version) {
			super(key, value, version);
		}

		public TimeUtilCacheElement(Object key, Object value) {
			super(key, value);
		}

		public TimeUtilCacheElement(Serializable key, Serializable value, long version) {
			super(key, value, version);
		}

		public TimeUtilCacheElement(Serializable key, Serializable value) {
			super(key, value);
		}
		public EvictionType getEvictionType(){
			return evictionType;
		}
		public void setEvictionType(EvictionType et){
			evictionType=et;
		}
		
        @Override
        public boolean isExpired() {
            if (!isLifespanSet() || isEternal()) {
                return false;
            }
            long now = TimeUtil.getCurrentTimeMillis();
            long expirationTime = getExpirationTime();
            return now > expirationTime;
        }
    	
    }
    
    private TimeUtilCacheElement addToCache(String adaptedKey, CachedSupplier<?> value, int expiration, Class<?> type) {
        if(value == null){
            return null;
        }
        long now = TimeUtil.getCurrentTimeMillis();
        
        TimeUtilCacheElement e=new TimeUtilCacheElement (adaptedKey, value, 1, now, now, now,0);
        e.setEternal(expiration <=0);

        e.setTimeToIdle(expiration);

        e.setTimeToLive(expiration);
        
        e.setEvictionType(getEvictionPolicy(type));
        addElementToCache(e);
        return e;
    }
    
    private void addElementToCache(TimeUtilCacheElement e){
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
    
    
    private static class ListeningCachedSupplier<T> extends CachedSupplier<T>{
		public ListeningCachedSupplier(Supplier<T> supplier,Consumer<T> whenFirstCalled) {
			super(()->{
				T ret=supplier.get();
				whenFirstCalled.accept(ret);
				return ret;
			});
		}
		public static <T> ListeningCachedSupplier<T> of(Supplier<T> supplier,Consumer<T> whenFirstCalled){
			return new ListeningCachedSupplier<T>(supplier,whenFirstCalled);
		}
    }
    
}
