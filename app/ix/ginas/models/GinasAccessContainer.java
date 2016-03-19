package ix.ginas.models;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.BaseModel;
import ix.core.models.Group;

@Entity
@Table(name = "ix_ginas_access")
public class GinasAccessContainer extends BaseModel{
	@Id
	public Long id;

	@ManyToMany(cascade = CascadeType.ALL)
	@JsonSerialize(using = GroupListSerializer.class)
	@JsonDeserialize(using = GroupListDeserializer.class)
	public Set<Group> access;
	
	public String entityType;
	
	public void add(Group p) {
		if (access == null) {
			access = new LinkedHashSet<Group>();
		}
		access.add(p);
	}
	
	public GinasAccessContainer(){
		
	}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public GinasAccessContainer(Object o){
		this.entityType=o.getClass().getName();
	}

	public Set<Group> getAccess() {
		if (access == null) {
			return new LinkedHashSet<Group>();
		}
		return access;
	}
	
//	@PostUpdate
//	@PostPersist
//	public void testPersist(){
//		System.out.println("It saved:" +id + " " +  access.size());
//	}
}
