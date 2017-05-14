package ix.core.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.controllers.AdminFactory;
import ix.core.controllers.UserProfileFactory;
import play.data.validation.Constraints.Email;

@Entity
@Table(name="ix_core_principal")
@Inheritance
@DiscriminatorValue("PRI")
public class Principal extends IxModel {
    // provider of this principal
    public String provider; 
    
   // @Required
    @Indexable(facet=true,name="Principal")
    @Column(unique=true)
    public String username;

    @Email
    public String email;
    
    @Column(name = "is_admin")
    public boolean admin = false;

    @Column(length=1024)
    public String uri; // can be email or any unique uri

    @ManyToOne(cascade = CascadeType.PERSIST)
    public Figure selfie;

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
    
    @JsonIgnore
    public String toString(){
    	return username;
    }
    
    @JsonIgnore
    public UserProfile getUserProfile(){
    	return UserProfileFactory.getUserProfileForPrincipal(this);
    }

    public boolean isAdmin () {
    	return admin; 
    }
    
    @Override
    public void delete(){
    	super.delete();
    }
    
}
