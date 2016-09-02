package ix.core.search;

import java.util.List;
import java.util.Objects;

import ix.core.plugins.IxCache;
import ix.core.search.FutureList.NamedCallable;
import ix.core.search.text.EntityUtils;
import ix.core.search.text.EntityUtils.EntityInfo;
import ix.core.util.CachedSupplier;
import ix.utils.Tuple;
import play.Logger;

public class EntityFetcher<K> implements NamedCallable<K>{
	//final String field;
	final String kind;
	final Object id;
	List<String> expand;
	
	public EntityFetcher(String kind, Object id, List<String> expand) throws Exception{
		Objects.requireNonNull(kind);
		Objects.requireNonNull(id);
		this.expand=expand;
		this.kind=kind;
		this.id=id;
	}
	
	
	// This can probably be cached without user-specific 
	// concerns
	@SuppressWarnings("unchecked")
	@Override
	public K call() throws Exception {
		return (K) IxCache.getOrElseTemp(
				EntityUtils.getEntityInfoFor(kind).uniqueKeyWithId(id),
							() -> findObject(kind, id, expand)
						);
	}
	
	public String uniqueKey(){
		return uniqueKeyFor(new Tuple<String,String>(kind,id.toString()));
	}
	
	//The key formation for things being stored
	public static String uniqueKeyFor(Tuple<String,String> fieldAndId){
		return fieldAndId.k() + "._id:" + fieldAndId.v();
	}
	
	public String getName(){
		return id.toString();
	}
	
	public static Object findObject (String kind, Object id, List<String> expand) throws Exception {
        	//If you see this in the code base, erase it
            //it's only here for debugging
            //Specifically, we are testing if delayed adding
            //of objects causes a problem for accurate paging.
            //if(Math.random()>.9){
            //		Util.debugSpin(100);
            //}
            //System.out.println("added:" + matches.size());
		    
			EntityInfo ei = EntityUtils.getEntityInfoFor(kind);
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