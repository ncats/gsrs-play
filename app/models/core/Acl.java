package models.core;

import java.util.List;
import java.util.ArrayList;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ct_acl")
public class Acl extends Model {

    public enum Permission {
        None, // 
            Read, // read-only
            Write, // write-only
            ReadWrite, // read+write
            Execute, // execute
            Admin
            }
        
    @Id
    public Long id;
    public Permission perm = Permission.Read;

    @ManyToMany(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    @JoinTable(name="ct_acl_principal")
    public List<Principal> principals = new ArrayList<Principal>();

    @ManyToMany(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    @JoinTable(name="ct_acl_group")
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
}
