package ix.core.search;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import ix.core.plugins.IxCache;
import ix.core.search.LazyList.NamedCallable;
import ix.core.search.text.EntityUtils.Key;


/**
 * Utility "wrapper" for producing an entity from some source.
 * 
 * Currently, it accepts a Key, and will generate a value on 
 * call, by various sources, depending on the CacheType.
 * @author peryeata
 *
 * @param <K>
 */
public class EntityFetcher<K> implements NamedCallable<K>{
	public static enum CacheType{
		NO_CACHE,
		GLOBAL_CACHE,
		GLOBAL_CACHE_WHEN_NOT_CHANGED, 		//look at last indexing, is it older then last time this was put?
		SUPER_LOCAL_CACHE_WHEN_NOT_CHANGED, //look at last indexing, is it older then last time this was called?
		LOCAL_CACHE,						
		ACTIVE_LOAD
	}
	public static final CacheType cacheType = CacheType.GLOBAL_CACHE;
	
	Key theKey;
	
	private Optional<K> stored = Optional.empty();
	
	long lastFetched=0l;
	
	public EntityFetcher(Key theKey) throws Exception{
		Objects.requireNonNull(theKey);
		this.theKey=theKey;
	}
	
	// This can probably be cached without user-specific 
	// concerns
	@Override
	public K call() throws Exception {
		switch(cacheType){
			case GLOBAL_CACHE:
				return (K) IxCache.getOrElseTemp(theKey.toString(),() -> findObject());
			case GLOBAL_CACHE_WHEN_NOT_CHANGED:
				throw new UnsupportedOperationException("Global timeout cache not supported yet for this operation");
			case NO_CACHE:
				return (K) findObject();
			case SUPER_LOCAL_CACHE_WHEN_NOT_CHANGED:
				if(IxCache.hasChangedSince(lastFetched)){
					reload();
				}
				return stored.get();
			case ACTIVE_LOAD:
				return reload().get();
			default:
			case LOCAL_CACHE:
				return (K) IxCache.getOrElse(theKey.toString(),() -> findObject());	
				
		}
	}
	
	public String getName(){
		return theKey.getIdString();
	}
	
	//Refresh the localest of caches
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
        //if(Math.random()>.9){
        //		Util.debugSpin(100);
        //}
        //System.out.println("added:" + matches.size());
		return (K) theKey.fetch().get().getValue();
    }
}