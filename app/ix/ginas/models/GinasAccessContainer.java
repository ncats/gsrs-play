package ix.ginas.models;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.Group;
import ix.ginas.models.serialization.GroupDeserializer;
import ix.ginas.models.serialization.GroupSerializer;

//@Entity
//@Table(name = "ix_ginas_access")
public class GinasAccessContainer{
	
	@Id
	public Long id;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JsonSerialize(contentUsing = GroupSerializer.class)
	@JsonDeserialize(contentUsing = GroupDeserializer.class)
	private Set<Group> access;
	
	
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
	public void setAccess(Collection<Group> acc){
		this.access=new LinkedHashSet<Group>(acc);
	}
	
	
	public boolean equals(Object o){
		return false;
	}
}
