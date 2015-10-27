package ix.core.models;

import play.db.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name = "ix_core_role")
public class Role extends Model implements be.objectify.deadbolt.core.models.Role {
    public enum Kind implements be.objectify.deadbolt.core.models.Role {
        Guest,
        User, // authenticated user
        Owner,
        Admin;

        @Override
        public String getName() {
            return name();
        }
    }

    @Id
    public Long id;
    public Kind role;
    public String name; //same as role, but deadbolt2 works with 'name' field


    @ManyToOne(cascade = CascadeType.ALL)
    public Principal principal;

    public Role() {
    }

    public Role(Kind role) {
        this.role = role;
        this.name = role.toString();
    }

    @Override
    public String getName() {
        return name;
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


}
