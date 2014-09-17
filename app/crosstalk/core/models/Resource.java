package crosstalk.core.models;

import java.util.List;
import java.util.ArrayList;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_core_resource")
public class Resource extends Model {
    public enum Modifier {
        Public, // anyone can access this resource
            Protected, // only authenticated users have access
            Private // only specific users have access
            }

    @Id
    public Long id;

    @Column(unique=true)
    public String name;
    public Modifier modifier = Modifier.Private;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_core_resource_role")
    public List<Role> roles = new ArrayList<Role>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_core_resource_acl")
    public List<Acl> acls = new ArrayList<Acl>();

    public Resource () {}
    public Resource (Modifier modifier) {
        this.modifier = modifier;
    }
    public Resource (String name, Modifier modifier) {
        this.name = name;
        this.modifier = modifier;
    }

    public boolean isPublic () { 
        return modifier == Modifier.Public; 
    }

    public static Resource newPublic () {
        return new Resource (Modifier.Public);
    }
    public static Resource newPublic (String name) {
        return new Resource (name, Modifier.Public);
    }
    public static Resource newProtected () {
        return new Resource (Modifier.Protected);
    }
    public static Resource newProtected (String name) {
        return new Resource (name, Modifier.Protected);
    }
    public static Resource newPrivate () {
        return new Resource (Modifier.Private);
    }
    public static Resource newPrivate (String name) {
        return new Resource (name, Modifier.Private);
    }
}
