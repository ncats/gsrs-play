package ix.core.plugins;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.MemoryUnit;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Created by katzelda on 5/26/16.
 */
public final class GateKeeperFactory {

    private static final String IX_CACHE_EVICTABLE = "IxCache-Evictable";
    private static final String IX_CACHE_NOT_EVICTABLE = "IxCache-Not-Evictable";


    private Supplier<GateKeeper> supplier;

    private GateKeeperFactory(Supplier<GateKeeper> supplier){
        this.supplier = supplier;
    }

    public GateKeeper create(){
        return supplier.get();
    }


    public static class Builder{
        private int debugLevel = 2;

        private final int maxElements, timeToLive, timeToIdle;
        private Integer evictableMaxElements, evictableTimeToLive,evictableTimeToIdle;

        public Builder(int maxElements, int timeToLive, int timeToIdle){
            this.maxElements = maxElements;
            this.timeToIdle = timeToIdle;
            this.timeToLive = timeToLive;
        }

        public Builder debugLevel(int level){
            this.debugLevel = level;
            return this;
        }

        public Builder useNonEvictableCache(int maxElements, int timeToLive, int timeToIdle){
            this.evictableMaxElements = maxElements;
            this.evictableTimeToLive = timeToLive;
            this.evictableTimeToIdle = timeToIdle;
            return this;
        }

        public GateKeeperFactory build(){
            Supplier<GateKeeper> supplier;
            if(evictableMaxElements ==null){
                //single cache
                supplier = ()->{

                    Cache evictableCache = new Cache( new CacheConfiguration()
                            .name(IX_CACHE_NOT_EVICTABLE)
                            .maxBytesLocalHeap(maxElements, MemoryUnit.MEGABYTES)
                            .timeToLiveSeconds(timeToLive)
                            .timeToIdleSeconds(timeToIdle));
                    CacheManager.getInstance().removeCache(evictableCache.getName());
                    CacheManager.getInstance().addCache(evictableCache);


                    evictableCache.setSampledStatisticsEnabled(true);
                    return new SingleCacheGateKeeper(debugLevel, new ExplicitMapKeyMaster(), evictableCache);
                };
            }else{
                supplier = ()->{
                    Cache evictableCache = new Cache(new CacheConfiguration()
                            .name(IX_CACHE_EVICTABLE)
                            .maxEntriesLocalHeap(maxElements)
                            //.maxBytesLocalHeap(maxElements, MemoryUnit.MEGABYTES)
                            .timeToLiveSeconds(timeToLive)
                            .timeToIdleSeconds(timeToIdle));

                    Cache nonEvictableCache = new Cache ( new CacheConfiguration()
                            .name(IX_CACHE_NOT_EVICTABLE)
                            .maxEntriesLocalHeap(maxElements)
                            //.maxBytesLocalHeap(evictableMaxElements, MemoryUnit.MEGABYTES)
                            .timeToLiveSeconds(evictableTimeToLive)
                            .timeToIdleSeconds(evictableTimeToIdle));



                    CacheManager.getInstance().removeCache(evictableCache.getName());
                    CacheManager.getInstance().addCache(evictableCache);


                    CacheManager.getInstance().removeCache(nonEvictableCache.getName());
                    CacheManager.getInstance().addCache(nonEvictableCache);


                    evictableCache.setSampledStatisticsEnabled(true);

                    return new TwoCacheGateKeeper(debugLevel, new ExplicitMapKeyMaster(), evictableCache, nonEvictableCache);
                };
            }

            return new GateKeeperFactory(supplier);
        }
    }

    private static class ExplicitMapKeyMaster implements KeyMaster{
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

        @Override
        public void removeAll() {
            thekeys.clear();
        }
    }
}
