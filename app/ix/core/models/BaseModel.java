package ix.core.models;

import javax.persistence.MappedSuperclass;

import play.db.ebean.Model;

@MappedSuperclass
public class BaseModel extends Model{

	public BaseModel(){
		
	}
	

	public Class<?>[] fetchEquivalentClasses() {
		return new Class<?>[]{this.getClass()};
	}
	
	
}
