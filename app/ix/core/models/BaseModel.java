package ix.core.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.plugins.IxCache;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import play.db.ebean.Model;

@MappedSuperclass
public abstract class BaseModel extends Model{

	/**
	 * An implementation
	 * should ensure that the ID returned by this is
	 * globally unique. For UUIDs, returning just the
	 * UUID is sufficient. For sequence based
	 * identifiers, adding some type information
	 * (such as a prefix) is usually necessary.
	 * 
	 * <p>
	 * Note: this is less useful now that
	 * {@link EntityWrapper#getKey()} exists,
	 * which returns a globally unique key that
	 * can be used for fetching the models as well.
	 * TODO: Refactor these to use the same mechanism by
	 * default.
	 * </p>
	 * @return
	 */
	public abstract String fetchGlobalId();
	
	public BaseModel(){
		
	}
	
	//This may no longer be necessary
	public Class<?>[] fetchEquivalentClasses() {
		return new Class<?>[]{this.getClass()};
	}
	
	
	
	@Transient
	@JsonIgnore
	private transient ThreadLocal<Map<String,Object>> matchContext=new ThreadLocal<Map<String,Object>>();
	
	public void addMatchContextProperty(String key, Object value){
	    if(matchContext.get()==null)matchContext.set(new HashMap<String,Object>());
	    matchContext.get().put(key, value);
	}
	
	/**
	 * Sets the transient matchingContext map for returning query-specific
	 * information (such as chemical similarity, relevance, etc). This
	 * clears any previous matchingContext map associated with this
	 * record. Note that this information is only intended to be visible
	 * from the current executing thread, so care must be taken if there
	 * is intention to spawn an additional processing thread.
	 * 
	 * <p>
	 * <b>Usage note:</b> If the supplied map is null, this blanks out
	 * the current context only, and is equivalent to a call to
	 * {@link #clearAllMatchContextProperties()}.
	 * </p>
	 * @param extraInfo
	 */
	public void setMatchContextProperty(Map<String,Object> extraInfo){
	    clearAllMatchContextProperties();
	    if(extraInfo!=null){
            for(Entry<String,Object> es: extraInfo.entrySet()){
                addMatchContextProperty(es.getKey(),es.getValue());
            }
	    }
    }
	
	
	@JsonProperty("_matchContext")
	public Map<String,Object> getMatchContextProperties(){
        return matchContext.get();
    }
	
	@SuppressWarnings("unchecked")
    public <T> T getMatchContextPropertyOr(String key, T def){
	    Map<String,Object> map = getMatchContextProperties();
	    if(map!=null){
	        return (T)map.getOrDefault(key,def);
	    }
	    return def;
	}
	
	public void clearMatchContextProperty(String key){
        if(matchContext!=null){
            matchContext.get().remove(key);
        }
    }
	
	public void clearAllMatchContextProperties(){
	    matchContext.set(null);
	}
	
	
	
	public void setMatchContextFromID(String contextId){
	    Key k = EntityWrapper.of(this).getKey();
	    setMatchContextProperty(IxCache.getMatchingContextByContextID(contextId, k));
	}
	
}
