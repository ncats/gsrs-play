package ix.core.models;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import be.objectify.deadbolt.core.models.Subject;
import com.fasterxml.jackson.annotation.JsonBackReference;
import ix.core.controllers.AdminFactory;
import ix.core.models.Acl.Permission;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_userprof")
public class UserProfile extends IxModel implements Subject {
    @Basic(fetch=FetchType.EAGER)
    @OneToOne(cascade=CascadeType.ALL)
    public Principal user;
    
    // is the profile currently active? authorization should take
    // this into account
    public boolean active;

    public String hashp;
    public String salt;
    public boolean systemAuth; //FDA, NIH employee
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_userprof_prop")
    public List<Value> properties = new ArrayList<Value>();

    public UserProfile () {}
    public UserProfile (Principal user) {
        this.user = user;
    }

    @Override
    public List<Role> getRoles()
    {
        return AdminFactory.rolesByPrincipal(user); //roles;

    }

    @Override
    public List<Acl> getPermissions()
    {
        return AdminFactory.permissionByPrincipal(user); //return permissions;
    }

    @Override
    public String getIdentifier()
    {
        return user.username;
    }
}
