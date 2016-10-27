package ix.core.search;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import ix.core.controllers.BackupFactory;
import ix.core.plugins.IxCache;
import ix.core.search.LazyList.NamedCallable;
import ix.core.util.ConfigHelper;
import ix.core.util.EntityUtils.Key;
import ix.utils.Util;


/**
 * Utility "wrapper" for producing an entity from some source.
 * 
 * Currently, it accepts a Key, and will generate a value on 
 * call, by various sources, depending on the CacheType.
 * 
 * @author peryeata
 *
 * @param <K>
 */
public class EntityFetcher<K> implements NamedCallable<Key,K>{
	public static final long debugDealy = ConfigHelper.getLong("ix.settings.debug.dbdelay",0);


	public enum CacheType{
		/**
		 * Don't use a cache always refetch from db.
		 */
		NO_CACHE{
			@Override
			<K> K get(EntityFetcher<K> fetcher) throws Exception {
				return (K) fetcher.findObject();
			}
		},
		/**
		 * Everyone sees everything (works)
		 */
		GLOBAL_CACHE{
			@Override
			<K> K get(EntityFetcher<K> fetcher) throws Exception{
				return (K) IxCache.getOrFetchTempRecord(fetcher.theKey);
			}
		},
		/**
		 * look at last indexing, is it older then last time this was put?
		 */
		GLOBAL_CACHE_WHEN_NOT_CHANGED{
			@Override
			<K> K get(EntityFetcher<K> fetcher) throws Exception {
				if(IxCache.mightBeDirtySince(fetcher.lastFetched)){
					IxCache.setTemp(fetcher.theKey.toString(), fetcher.findObject ());
				}
				return (K)IxCache.getTemp(fetcher.theKey.toString());
			}
		},
		/**
		 * look at last indexing, is it older then last time this was called?
		 */
		SUPER_LOCAL_CACHE_WHEN_NOT_CHANGED{
			//for now copy super local eager
			@Override
			<K> K get(EntityFetcher<K> fetcher) throws Exception {
				return LOCAL_EAGER.get(fetcher);
			}
		},
		/**
		 * OLD way (user-specific) (WARNING: BROKEN?)
		 */
		DEFAULT_CACHE{
			@Override
			<K> K get(EntityFetcher<K> fetcher) throws Exception {
				return (K) IxCache.getOrElse(fetcher.theKey.toString(),() -> fetcher.findObject());
			}
		},
		/**
		 * Store object here, return it directly.
		 */
		ACTIVE_LOAD{
			@Override
			<K> K get(EntityFetcher<K> fetcher) throws Exception {
				return fetcher.getOrReload().get();
			}
		},
		/**
		 * Store object here, right away, return it directly (this is almost what happened before).
		 */
		LOCAL_EAGER {
			@Override
			<K> K get(EntityFetcher<K> fetcher) throws Exception {
				if(IxCache.mightBeDirtySince(fetcher.lastFetched)){
					fetcher.reload();
				}
				return fetcher.stored.get();
			}
		},
		BACKUP_JSON_EAGER {
			@Override
			<K> K get(EntityFetcher<K> fetcher) throws Exception {				
				if(fetcher.theKey.getEntityInfo().hasBackup()){
					return IxCache.getOrElseTemp(fetcher.theKey.toString(), ()->(K) BackupFactory.getByKey(fetcher.theKey).getInstantiated());
				}else{
					System.out.println("Fetching otherwise");
				}
				return GLOBAL_CACHE.get(fetcher);
			}
		}
		;


		 abstract <K> K get(EntityFetcher<K> fetcher) throws Exception;

	}
	public final CacheType cacheType; 
	
	
	final Key theKey;
	
	private Optional<K> stored = Optional.empty(); //
	
	long lastFetched=0l;
	
	public EntityFetcher(Key theKey) throws Exception{
		this(theKey, CacheType.GLOBAL_CACHE); //This is probably the best option
	}
	
	public EntityFetcher(Key theKey, CacheType ct) throws Exception{
        Objects.requireNonNull(theKey);
        cacheType= ct;
        this.theKey=theKey;
        if(cacheType == CacheType.LOCAL_EAGER){
            reload();
        }
    }
	
	// This can probably be cached without user-specific 
	// concerns
	@Override
	public K call() throws Exception {

		return cacheType.get(this);
	}
	
	public Key getName(){
		return theKey;
	}
	
	public Optional<K> getOrReload(){
		if(stored.isPresent()){
			return stored;
		}else{
			return reload();
		}
	}
	
	//Refresh the "localest" of caches
	public Optional<K> reload() throws NoSuchElementException {
		try{
			stored=Optional.of(findObject());
		}catch(Exception e){
			stored=Optional.empty();
		}
		return stored;
	}
	
	public K findObject () throws NoSuchElementException {
		lastFetched=System.currentTimeMillis();
		//If you see this in the code base, erase it
        //it's only here for debugging
        //Specifically, we are testing if delayed adding
        //of objects causes a problem for accurate paging.
        Util.debugSpin(debugDealy);
        //System.out.println("added:" + matches.size());
		return (K) theKey.fetch().get().getValue();
    }

	public static EntityFetcher<?> of(Key k) throws Exception {
		return new EntityFetcher<>(k);
	}
	
	public static <T> EntityFetcher<T> of(Key k, Class<T> cls) throws Exception {
		return new EntityFetcher<>(k);
	}
	
	
	public static EntityFetcher<?> of(Key k, CacheType cacheType) throws Exception {
        return new EntityFetcher<>(k, cacheType);
    }
    
    public static <T> EntityFetcher<T> of(Key k, Class<T> cls,CacheType cacheType) throws Exception {
        return new EntityFetcher<>(k, cacheType);
    }
    
}