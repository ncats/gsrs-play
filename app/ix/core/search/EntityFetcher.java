package ix.core.search;

import java.util.NoSuchElementException;
import java.util.Objects;

import ix.core.plugins.IxCache;
import ix.core.search.LazyList.NamedCallable;
import ix.core.search.text.EntityUtils.Key;


public class EntityFetcher<K> implements NamedCallable<K>{
	public static enum CacheType{
		NO_CACHE,
		GLOBAL_CACHE,
		GLOBAL_CACHE_WHEN_NOT_CHANGED,
		LOCAL_CACHE
	}
	public static final CacheType cacheType =CacheType.LOCAL_CACHE;
	//final String field;
	Key theKey;
	//List<String> expand;
	
	public EntityFetcher(Key theKey) throws Exception{
		Objects.requireNonNull(theKey);
		this.theKey=theKey;
		//this.expand=expand;
	}
	
	
	// This can probably be cached without user-specific 
	// concerns
	@SuppressWarnings("unchecked")
	@Override
	public K call() throws Exception {
		switch(cacheType){
			case GLOBAL_CACHE:
				return (K) IxCache.getOrElseTemp(theKey.toString(),() -> findObject());
			case GLOBAL_CACHE_WHEN_NOT_CHANGED:
				throw new UnsupportedOperationException("Global timeout cache not supported yet for this operation");
			case NO_CACHE:
				return (K) findObject();
			case LOCAL_CACHE:
			default:
				return (K) IxCache.getOrElse(theKey.toString(),() -> findObject());
		}

	}
	
	public String getName(){
		return theKey.getIdString();
	}
	
	public K findObject () throws NoSuchElementException {
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