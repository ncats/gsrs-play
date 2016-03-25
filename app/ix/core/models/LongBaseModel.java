package ix.core.models;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import play.db.ebean.Model;

@MappedSuperclass
public abstract class LongBaseModel extends BaseModel{
	@Id
	public Long id;
	
	
	@Override
	public String fetchIdAsString() {
		if(id!=null)return this.getClass().getName() + ":" + id.toString();
		return null;
	}
	
}
