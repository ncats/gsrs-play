package ix.ginas.models;

import ix.core.models.Group;
import play.db.ebean.Model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "ix_ginas_access")
public class GinasAccessContainer extends Model{
	@Id
	public Long id;

	@ManyToMany(cascade = CascadeType.PERSIST)
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
	
//	@PostUpdate
//	@PostPersist
//	public void testPersist(){
//		System.out.println("It saved:" +id + " " +  access.size());
//	}
}
