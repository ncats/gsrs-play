package ix.core.models;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.db.ebean.Model;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * A group of users usually
 * a way to provide access controls
 * on which groups can view/edit ginas objects.
 */
@Entity
@Table(name="ix_core_group")
public class Group extends LongBaseModel {
    @Id
    public Long id;

    @Column(unique=true)
    public String name;

    @ManyToMany(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    @JoinTable(name="ix_core_group_principal")
    @JsonIgnore
    public Set<Principal> members = new HashSet<Principal>();

   @JsonCreator
    public Group (@JsonProperty("name") String name) {
        this.name = name;
    }
    
    
    public int hashCode(){
    	return this.name.hashCode();
    }
    public boolean equals(Object o){
    	if(o!=null && o instanceof Group){
    		return this.name.equals(((Group)o).name);
    	}
    	return false;
    }
}
