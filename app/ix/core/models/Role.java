package ix.core.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "ix_core_role")
public class Role extends Model implements be.objectify.deadbolt.core.models.Role {
    public enum Kind {
        Guest,
        User, // authenticated user
        Owner,
        Admin;
    }

    @Id
    public Long id;
    public Kind role;


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

    public static Role newGuest() {
        return new Role(Kind.Guest);
    }

    public static Role newUser() {
        return new Role(Kind.User);
    }

    public static Role newOwner() {
        return new Role(Kind.Owner);
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
