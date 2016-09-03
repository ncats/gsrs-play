package ix.core.models;

import javax.persistence.MappedSuperclass;

import play.db.ebean.Model;

@MappedSuperclass
public abstract class BaseModel extends Model{

	public abstract String fetchGlobalId();
	
	public BaseModel(){
		
	}
	
	//Man I wish this could be a forced static method
	//for everything
	
	public Class<?>[] fetchEquivalentClasses() {
		return new Class<?>[]{this.getClass()};
	}
}
