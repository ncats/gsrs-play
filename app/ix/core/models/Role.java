package ix.core.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

@Entity
@Table(name = "ix_core_role")
public class Role extends Model implements be.objectify.deadbolt.core.models.Role {
    public enum Kind {
        Query,
        DataEntry,
        SuperDataEntry,
        Updater,
        SuperUpdate,
        Admin;
        //Guest, Owner, Admin, User; //authenticated user
    }


    @Id
    public Long id;
    public Kind role;


    //This doesn't make sense ... something is wrong here.
    //Why ... ?
    //Ok, this is not exactly a Role object. This is a role-object-link entity.
    
    @JsonIgnore
    @ManyToOne()
    public Principal principal;


    public Role() {
    }

    public Role(Kind role) {
        this.role = role;
    }

    @Override
    public String getName() {
        return role.name();
    }

    public static Role newAdmin() {
        return new Role(Kind.Admin);
    }

    public static List<String> options(){
        List<String> vals = new ArrayList<String>();
        for (Kind role: Kind.values()) {
            vals.add(role.name());
        }
        return vals;
    }


}
