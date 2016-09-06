package ix.core.search;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import ix.core.plugins.IxCache;
import ix.core.search.LazyList.NamedCallable;
import ix.core.util.EntityUtils.Key;
import ix.utils.Util;
import play.Play;


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
public class EntityFetcher<K> implements NamedCallable<K>{
	public static final long debugDealy = Play.application().configuration().getLong("ix.settings.debug.dbdelay");
	public static enum CacheType{
		NO_CACHE,
		GLOBAL_CACHE,						//Everyone sees everything (works)
		GLOBAL_CACHE_WHEN_NOT_CHANGED, 		//look at last indexing, is it older then last time this was put?
		SUPER_LOCAL_CACHE_WHEN_NOT_CHANGED, //look at last indexing, is it older then last time this was called?
		DEFAULT_CACHE,						//OLD way (user-specific) (WARNING: BROKEN?)
		ACTIVE_LOAD,						//Store object here, return it directly
		SUPER_LOCAL_EAGER					//Store object here, right away, return it directly (this is almost what
											//happened before)
	}
	public static final CacheType cacheType = CacheType.GLOBAL_CACHE; //this one is probably the best option
	
	
	
	
	Key theKey;
	
	private Optional<K> stored = Optional.empty(); //
	
	long lastFetched=0l;
	
	public EntityFetcher(Key theKey) throws Exception{
		Objects.requireNonNull(theKey);
		this.theKey=theKey;
		if(cacheType == CacheType.SUPER_LOCAL_EAGER){
			reload();
		}
	}
	
	// This can probably be cached without user-specific 
	// concerns
	@Override
	public K call() throws Exception {
		switch(cacheType){
			case GLOBAL_CACHE:
				return (K) IxCache.getOrFetchTempRecord(theKey);
			case GLOBAL_CACHE_WHEN_NOT_CHANGED:
				throw new UnsupportedOperationException("Global timeout cache not supported yet for this operation");
			case NO_CACHE:
				return (K) findObject();
			case SUPER_LOCAL_CACHE_WHEN_NOT_CHANGED:
			case SUPER_LOCAL_EAGER:
				if(IxCache.hasChangedSince(lastFetched)){
					reload();
				}
				return stored.get();
			case ACTIVE_LOAD:
				return getOrReload().get();
			default:
			case DEFAULT_CACHE:
				return (K) IxCache.getOrElse(theKey.toString(),() -> findObject());	
				
		}
	}
	
	public String getName(){
		return theKey.getIdString();
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
}