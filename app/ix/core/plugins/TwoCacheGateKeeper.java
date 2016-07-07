package ix.core.plugins;

import ix.core.CacheStrategy;
import ix.core.models.BaseModel;
import ix.utils.Util;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;
import play.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * Created by katzelda on 5/19/16.
 */
public class TwoCacheGateKeeper implements GateKeeper {


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
    private <T> Callable<T> createRaw(Callable<T> delegate, String key){

        return createRaw(delegate, key, 0);
    }

    private <T> Callable<T> createRaw(Callable<T> delegate, String key, int seconds){

        return new CacheGeneratorWrapper<T>(delegate, key, key,seconds);
    }

    private <T> Callable<T> createKeyWrapper(Callable<T> delegate, String key, String adaptedKey){

        return createKeyWrapper(delegate, key, adaptedKey, 0);
    }

    private <T> Callable<T> createKeyWrapper(Callable<T> delegate, String key, String adaptedKey, int seconds){

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
     * Wraps a generateor that creates the actual thing to cache
     * and puts it in the correct cache with the correct adapted key.
     * @param <T>
     */
    private class CacheGeneratorWrapper<T> implements Callable<T>{
       private final Callable<T> delegate;
       private final String key, adaptedKey;

       private final int seconds;




       private CacheGeneratorWrapper(Callable<T> delegate, String key, String adaptedKey, int seconds) {
           this.delegate = delegate;
           this.key = key;
           this.adaptedKey = adaptedKey;
           this.seconds = seconds;
       }

       @Override
       public T call() throws Exception {
    	   
           T t = delegate.call();
           keyMaster.addKey(key, adaptedKey);
           addToCache(adaptedKey, t, seconds);
           return t;
       }
   }



    @Override
    public <T> T getSinceOrElse(String key, long creationTime, Callable<T> generator) throws Exception{
        return getSinceOrElse(key, creationTime, generator, 0);
    }
    @Override
    public <T> T getSinceOrElse(String key, long creationTime, Callable<T> generator, int seconds) throws Exception{
      String adaptedKey = keyMaster.adaptKey(key);
        return getOrElseRaw(adaptedKey,
                createKeyWrapper(generator, key, adaptedKey, seconds),
                e->e.getCreationTime() < creationTime
                );
    }



    private  <T> T getOrElseRaw(String key, Callable<T> generator, Predicate<Element> regeneratePredicate) throws Exception{
    	
    	Element e = evictableCache.get(key);
    	
    	
        if(e ==null || e.getObjectValue() == null){
            e = nonEvictableCache.get(key);
        }
        

        if(e ==null || e.getObjectValue() == null || regeneratePredicate.test(e)){
            if (debugLevel >= 2) {
                Logger.debug("IxCache missed: " + key);
            }
            return generator.call();
        }
        try {
            return (T) getObjectFromElement(e);
        }catch(Exception ex){
            //in case there is a cast problem
            //or some other problem with the cached value
            //re-generate
            return generator.call();
        }

    }

    @Override
    public <T> T getOrElseRaw(String key, Callable<T> generator, int seconds) throws Exception{
       return getOrElseRaw(key,
               createRaw(generator,key, seconds),
               (e)->false);
    }


    @Override
    public Object get(String key){
        return getRaw(keyMaster.adaptKey(key));
    }


    public Element getRawElement(String key){
        Element e = evictableCache.get(key);
        if(e ==null || e.getObjectValue()==null){
            e = nonEvictableCache.get(key);
        }
        return e;
    }
    @Override
    public Object getRaw(String key){
        Element e = getRawElement(key);

        
        return getObjectFromElement(e);
    }
    
    public static Object getObjectFromElement(Element e){

        if(e ==null){
            return null;
        }
        Object val=e.getObjectValue();
        if(val instanceof NonSerializedWrapper){
        	//return null;
        	return ((NonSerializedWrapper)val).getObject();
        }
        return val;
    }

    @Override
    public <T> T getOrElse(String key, Callable<T> generator, int seconds) throws Exception{
        String adaptedKey = keyMaster.adaptKey(key);

        return getOrElseRaw(adaptedKey,
                createKeyWrapper(generator, key, adaptedKey, seconds),
                e-> false);
    }

    @Override
    public void put(String key, Object value, int expiration){
        String adaptedKey = keyMaster.adaptKey(key);
        addToCache(adaptedKey, value, expiration);
        keyMaster.addKey(key, adaptedKey);
    }
    @Override
    public void putRaw(String key, Object value){
        putRaw(key, value, 0);
    }
    @Override
    public void putRaw(String key, Object value, int expiration){

        addToCache(key, value, expiration);
        keyMaster.addKey(key, key);
    }

    List<Object> cachedValues = new ArrayList<Object>();
    
    
    private void addToCache(String adaptedKey, Object value, int expiration) {
        if(value ==null ){
            return;
        }
        Object setvalue=value;
        if(isEvictable(value)){
        	Element e=new Element(adaptedKey, setvalue, expiration <= 0, expiration, expiration);
        	evictableCache.putWithWriter(e);
        }else{
            nonEvictableCache.putWithWriter(new Element (adaptedKey, setvalue, expiration <= 0, expiration, expiration));
        }
    }

    @Override
    public List<Statistics> getStatistics() {
        //TODO how to handle 2 caches?
    	List<Statistics> stats= new ArrayList<Statistics>();
    	stats.add(evictableCache.getStatistics());
    	stats.add(nonEvictableCache.getStatistics());
        return stats;
    }

    private static boolean isEvictable(Object o){

        if(o ==null){
            //TODO should we throw exception?
            return true;
        }
        CacheStrategy cacheStrat=o.getClass().getAnnotation(CacheStrategy.class);

        if(cacheStrat ==null){
            return true;
        }
        return cacheStrat.evictable();

    }

    @Override
    public boolean contains(String key){
        String adaptedKey = keyMaster.adaptKey(key);
        Element e = evictableCache.get(adaptedKey);
        if(e ==null){
            e = nonEvictableCache.get(adaptedKey);
        }
        return e !=null;
    }

    @Override
    public void put(String key, Object value) {
        put(key, value, 0);
    }

    @Override
    public <T> T getOrElseRaw(String key, Callable<T> generator) throws Exception {
        return getOrElseRaw(key, generator, 0);
    }

    @Override
    public <T> T getOrElse(String key, Callable<T> generator) throws Exception {
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
    
    
    /**
     * This is a wrapper to stop ehcache from trying to see the size of the value
     * contained
     * @author tyler
     *
     * @param <K>
     */
    @IgnoreSizeOf
    public class NonSerializedWrapper<K> {
    	
    	private transient K realValue;
    	public NonSerializedWrapper(K value){
    		this.realValue=value;
    	}
    	public K getObject(){
    		return this.realValue;
    	}
    }
}
