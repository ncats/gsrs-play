package ix.core.models;

import java.util.*;

import be.objectify.deadbolt.core.models.Permission;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_acl")
public class Acl extends Model implements Permission{

    public enum Permission {
        None, // 
            Read, // read-only
            Write, // write-only
            ReadWrite, // read+write
            Execute, // execute
            Admin,
            Owner
            }
        
    @Id
    public Long id;

    public Permission perm = Permission.Read;

    @ManyToMany(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    @JoinTable(name="ix_core_acl_principal")
    public List<Principal> principals = new ArrayList<Principal>();

    @ManyToMany(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    @JoinTable(name="ix_core_acl_group")
    public List<Group> groups = new ArrayList<Group>();

    public Acl () {}
    public Acl (Permission perm) {
        this.perm = perm;
    }

    public static Acl newNone () { 
        return new Acl (Permission.None);
    }
    public static Acl newRead () {
        return new Acl (Permission.Read);
    }
    public static Acl newWrite () {
        return new Acl (Permission.Write);
    }
    public static Acl newReadWrite () {
        return new Acl (Permission.ReadWrite);
    }
    public static Acl newExecute () {
        return new Acl (Permission.Execute);
    }
    public static Acl newAdmin () {
        return new Acl (Permission.Admin);
    }

    public String getValue()
    {
        return perm.toString();
    }

    public static List<String> options(){
        List<String> vals = new ArrayList<String>();
        for (Permission permission: Permission.values()) {
            vals.add(permission.name());
        }
        return vals;
    }
}

