package ix.core.models;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import play.db.ebean.Model;

/**
 * Base class of objects in our model which
 * use a long as an Id instead of a String or UUID.
 */
@MappedSuperclass
public abstract class LongBaseModel extends BaseModel{
	@Id
	public Long id;
	
	
	@Override
	public String fetchGlobalId() {
		if(id!=null)return this.getClass().getName() + ":" + id.toString();
		return null;
	}
	
}
