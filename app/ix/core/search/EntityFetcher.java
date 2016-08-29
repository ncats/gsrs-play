package ix.core.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ix.core.plugins.IxCache;
import ix.core.search.FutureList.NamedCallable;
import play.Logger;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

public class EntityFetcher implements NamedCallable{
	 static final Map<String, Model.Finder> finders =
	            new HashMap<String, Model.Finder>();
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
	
	
	@Override
	public Object call() throws Exception {
		return IxCache.getOrElse(
							uniqueKey(kind, id),
							() -> findObject(kind, id, expand)
						);
	}
	
	public static String uniqueKey(String kind, Object id){
		return kind+"._id"+ ":" + id.toString();
	}
	
	public String getName(){
		return id.toString();
	}
	
	static Object findObject (String kind, Object id, List<String> expand)
            throws Exception {
        	//If you see this in the code base, erase it
            //it's only here for debugging
            //Specifically, we are testing if delayed adding
            //of objects causes a problem for accurate paging.
            //if(Math.random()>.9){
            //Util.debugSpin(100);
            //}
            //System.out.println("added:" + matches.size());
            
            Object value = null;
            
            Model.Finder finder = finders.get(kind);
            if (finder == null) {
                finder = new Model.Finder
                    (id.getClass(), Class.forName(kind));
                finders.put(kind, finder);
            }
            
            //if options don't include an "expand" keyword,
            //then just fetch default by ID
            if (expand.isEmpty()) {
                value = finder.byId(id);
            }else {
            // otherwise,
            // make sure to include specified paths
            // pre-loaded, to avoid future lazy loading
            // issues
                com.avaje.ebean.Query ebean = finder.setId
                    (id);
                for (String path : expand)
                    ebean = ebean.fetch(path);
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