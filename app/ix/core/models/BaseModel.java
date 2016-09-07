package ix.core.models;

import javax.persistence.MappedSuperclass;

import play.db.ebean.Model;

@MappedSuperclass
public abstract class BaseModel extends Model{

	//This may be less necessary now that `Key`
	//exists
	public abstract String fetchGlobalId();
	
	public BaseModel(){
		
	}
	
	//This may no longer be necessary
	public Class<?>[] fetchEquivalentClasses() {
		return new Class<?>[]{this.getClass()};
	}
}
