package ix.ginas.models;

import ix.core.models.Group;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "ix_ginas_access")
public class GinasAccessContainer {
	@Id
	Long id;

	@ManyToMany(cascade = CascadeType.PERSIST)
	@JsonSerialize(using = GroupListSerializer.class)
	@JsonDeserialize(using = GroupListDeserializer.class)
	public Set<Group> access = new LinkedHashSet<Group>();
	
	public String entityType;

	public void add(Group p) {
		if (access == null) {
			access = new LinkedHashSet<Group>();
		}
		access.add(p);
	}
	
	public GinasAccessContainer(){
		
	}
	public GinasAccessContainer(Object o){
		this.entityType=o.getClass().getName();
	}
}
