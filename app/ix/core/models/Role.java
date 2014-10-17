package ix.core.models;

import java.util.List;
import java.util.ArrayList;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_role")
public class Role extends Model {
    public enum Kind {
        Guest,
            User, // authenticated user
            Owner,
            Admin
            }

    @Id
    public Long id;
    public Kind role;

    @ManyToOne(cascade=CascadeType.ALL)
    public Principal principal;

    public Role () {
    }
    public Role (Kind role) {
        this.role = role;
    }

    public static Role newGuest () {
        return new Role (Kind.Guest);
    }
    public static Role newUser () {
        return new Role (Kind.User);
    }
    public static Role newOwner () {
        return new Role (Kind.Owner);
    }
    public static Role newAdmin () {
        return new Role (Kind.Admin);
    }
}
