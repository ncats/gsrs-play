package ix.core.plugins;

import ix.core.CacheStrategy;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import play.Logger;

import java.io.Closeable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Created by katzelda on 5/19/16.
 */
public class GateKeeper implements Closeable{


    private final IxCache.KeyMaster keyMaster;
    private final Cache evictableCache;
    private final Cache nonEvictableCache;

    private volatile boolean isClosed;

    private final IxContext ctx;

    public GateKeeper(IxContext ctx, IxCache.KeyMaster keyMaster, Cache evictableCache, Cache nonEvictableCache){
        Objects.requireNonNull(ctx);
        Objects.requireNonNull(keyMaster);
        Objects.requireNonNull(evictableCache);
        Objects.requireNonNull(nonEvictableCache);

        this.ctx = ctx;
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

    public boolean remove(String key) {
        String adaptedKey = keyMaster.adaptKey(key);
        if(evictableCache.remove(adaptedKey)){
            return true;
        }
        return nonEvictableCache.remove(adaptedKey);
    }

    public boolean removeAllChildKeys(String key) {
        Set<String> set = keyMaster.getAllAdaptedKeys(key);
        if(set ==null || set.isEmpty()){
            return false;
        }
        set.stream()
                .forEach(k-> remove(k));

        return true;
    }


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



    public <T> T getSinceOrElse(String key, long creationTime, Callable<T> generator) throws Exception{
        return getSinceOrElse(key, creationTime, generator, 0);
    }
    public <T> T getSinceOrElse(String key, long creationTime, Callable<T> generator, int seconds) throws Exception{
      String adaptedKey = keyMaster.adaptKey(key);
        return getOrElseRaw(adaptedKey,
                createKeyWrapper(generator, key, adaptedKey, seconds),
                e-> e.getCreationTime() < creationTime);
    }



    private  <T> T getOrElseRaw(String key, Callable<T> generator, Predicate<Element> regeneratePredicate) throws Exception{
        Element e = evictableCache.get(key);
        if(e ==null){
            e = nonEvictableCache.get(key);
        }

        if(e ==null || regeneratePredicate.test(e)){
            if (ctx.debug(2)) {
                Logger.debug("IxCache missed: " + key);
            }
            return generator.call();
        }
        try {
            return (T) e.getObjectValue();
        }catch(Exception ex){
            //in case there is a cast problem
            //or some other problem with the cached value
            //re-generate
            return generator.call();
        }

    }

    public <T> T getOrElseRaw(String key, Callable<T> generator, int seconds) throws Exception{
       return getOrElseRaw(key,
               createRaw(generator,key, seconds),
               (e)->false);
    }


    public Object get(String key){
        return getRaw(keyMaster.adaptKey(key));
    }


    Element getRawElement(String key){
        Element e = evictableCache.get(key);
        if(e ==null){
            e = nonEvictableCache.get(key);
        }
        return e;
    }
    public Object getRaw(String key){
        Element e = getRawElement(key);

        if(e ==null){
            return null;
        }
        return e.getObjectValue();
    }

    public <T> T getOrElse(String key, Callable<T> generator, int seconds) throws Exception{
        String adaptedKey = keyMaster.adaptKey(key);
        return getOrElseRaw(adaptedKey, generator, seconds);
    }

    public void put(String key, Object value, int expiration){
        String adaptedKey = keyMaster.adaptKey(key);
        addToCache(adaptedKey, value, expiration);
        keyMaster.addKey(key, adaptedKey);
    }
    public void putRaw(String key, Object value){
        putRaw(key, value, 0);
    }
    public void putRaw(String key, Object value, int expiration){

        addToCache(key, value, expiration);
        keyMaster.addKey(key, key);
    }

    private void addToCache(String adaptedKey, Object value, int expiration) {
        if(isEvictable(value)){
            evictableCache.put(new Element(adaptedKey, value, expiration <= 0, expiration, expiration));
        }else{
            nonEvictableCache.put(new Element (adaptedKey, value, expiration <= 0, expiration, expiration));
        }
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

    public boolean contains(String key){
        String adaptedKey = keyMaster.adaptKey(key);
        Element e = evictableCache.get(adaptedKey);
        if(e ==null){
            e = nonEvictableCache.get(adaptedKey);
        }
        return e !=null;
    }

    public void put(String key, Object value) {
        put(key, value, 0);
    }

    public <T> T getOrElseRaw(String key, Callable<T> generator) throws Exception {
        return getOrElseRaw(key, generator, 0);
    }

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

    private void disposeCache(Cache c){
        try {
            //shouldn't call dispose, the CacheManager will do that for us
            CacheManager.getInstance().removeCache(c.getName());
        }catch(Exception e){
            Logger.trace("Disposing cache " + c.getName(), e);
        }
    }
}