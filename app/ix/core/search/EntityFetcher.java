package ix.core.search;

import java.util.List;
import java.util.Objects;

import ix.core.plugins.IxCache;
import ix.core.search.FutureList.NamedCallable;
import ix.core.search.text.EntityUtils;
import ix.core.search.text.EntityUtils.EntityInfo;
import play.Logger;

public class EntityFetcher<K> implements NamedCallable<K>{
	public static enum CacheType{
		NO_CACHE,
		GLOBAL_CACHE,
		GLOBAL_CACHE_WHEN_NOT_CHANGED,
		LOCAL_CACHE
	}
	public static final CacheType cacheType =CacheType.NO_CACHE;
	//final String field;
	final String kind;
	final Object id;
	List<String> expand;
	EntityInfo ei;
	
	public EntityFetcher(String kind, Object id, List<String> expand) throws Exception{
		Objects.requireNonNull(kind);
		Objects.requireNonNull(id);
		this.expand=expand;
		this.kind=kind;
		this.id=id;
		ei=EntityUtils.getEntityInfoFor(kind);
	}
	
	
	// This can probably be cached without user-specific 
	// concerns
	@SuppressWarnings("unchecked")
	@Override
	public K call() throws Exception {
		switch(cacheType){
			case GLOBAL_CACHE:
				return (K) IxCache.getOrElseTemp(getKey(),() -> findObject(kind, id, expand));
			case GLOBAL_CACHE_WHEN_NOT_CHANGED:
				throw new UnsupportedOperationException("Global timeout cache not supported yet for this operation");
			case NO_CACHE:
				return (K) findObject(kind, id, expand);
			case LOCAL_CACHE:
			default:
				return (K) IxCache.getOrElse(getKey(),() -> findObject(kind, id, expand));
		}

	}
	
	public String getKey(){
		return ei.uniqueKeyWithId(id);
	}
	
	public String getName(){
		return id.toString();
	}
	
	public Object findObject (String kind, Object id, List<String> expand) throws Exception {
        	//If you see this in the code base, erase it
            //it's only here for debugging
            //Specifically, we are testing if delayed adding
            //of objects causes a problem for accurate paging.
            //if(Math.random()>.9){
            //		Util.debugSpin(100);
            //}
            //System.out.println("added:" + matches.size());
		    
		
			Object value = null;
            
            
            
            //if options don't include an "expand" keyword,
            //then just fetch default by ID
            if (expand.isEmpty()) {
                value = ei.getFinder().byId(id);
            }else {
	            // otherwise,
	            // make sure to include specified paths
	            // pre-loaded, to avoid future lazy loading
	            // issues
                com.avaje.ebean.Query ebean = ei.getFinder().setId(id);
                for (String path : expand){
                    ebean = ebean.fetch(path);
                }
                value = ebean.findUnique();
            }
                    
            if (value == null) {
                Logger.warn
                    (kind+":"+id
                     +" not available in persistence store!");
            }
            return value;
        }
}