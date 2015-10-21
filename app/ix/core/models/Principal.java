package ix.core.models;

import javax.persistence.*;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Permission;
import com.fasterxml.jackson.annotation.JsonBackReference;
import ix.core.controllers.AdminFactory;
import play.data.validation.Constraints.*;
import play.db.ebean.Model;

import java.util.List;

@Entity
@Table(name="ix_core_principal")
@Inheritance
@DiscriminatorValue("PRI")
public class Principal extends IxModel implements Subject{
    // provider of this principal
    public String provider; 
    
    @Required
    @Indexable(facet=true,name="Principal")
    @Column(unique=true)
    public String username;

    @Required
    @Email
    public String email;
    public boolean admin = false;

    @Column(length=1024)
    public String uri; // can be email or any unique uri

    @ManyToOne(cascade=CascadeType.ALL)
    public Figure selfie;

    @Override
    public List<? extends SecurityRole> getRoles()
    {
        return AdminFactory.rolesByPrincipal(this);
    }

    @Override
    @JsonBackReference
    public List<? extends Acl> getPermissions()
    {
        return AdminFactory.permissionByPrincipal(this);
    }

    public Principal () {}
    public Principal (boolean admin) {
        this.admin = admin;
    }
    public Principal (String email) {
        this.email = email;
    }
    public Principal (boolean admin, String email) {
        this.admin = admin;
        this.email = email;
    }
    public Principal (String username, String email) {
        this.username = username;
        this.email = email;
    }

    public boolean isAdmin () { return admin; }

    @Override
    public String getIdentifier()
    {
        return username;
    }

}
