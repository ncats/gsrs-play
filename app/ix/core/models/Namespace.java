package ix.core.models;

import java.util.List;
import java.util.ArrayList;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_namespace")
public class Namespace extends Model {
    public enum Modifier {
        Public, // anyone can access this resource
            Internal, // only authenticated users have access
            Private // only specific users have access
            }

    @Id
    public Long id;

    @Column(unique=true)
    @Indexable(facet=true,name="Namespace")
    public String name;

    @Column(length=1024)
    public String url;

    public Modifier modifier = Modifier.Private;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_namespace_role")
    public List<Role> roles = new ArrayList<Role>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_namespace_acl")
    public List<Acl> acls = new ArrayList<Acl>();

    public Namespace () {}
    public Namespace (Modifier modifier) {
        this.modifier = modifier;
    }
    public Namespace (String name, Modifier modifier) {
        this.name = name;
        this.modifier = modifier;
    }

    public boolean isPublic () { 
        return modifier == Modifier.Public; 
    }

    public static Namespace newPublic () {
        return new Namespace (Modifier.Public);
    }
    public static Namespace newPublic (String name) {
        return new Namespace (name, Modifier.Public);
    }
    public static Namespace newInternal () {
        return new Namespace (Modifier.Internal);
    }
    public static Namespace newInternal (String name) {
        return new Namespace (name, Modifier.Internal);
    }
    public static Namespace newPrivate () {
        return new Namespace (Modifier.Private);
    }
    public static Namespace newPrivate (String name) {
        return new Namespace (name, Modifier.Private);
    }
}
