package ix.core.models;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.security.MessageDigest;
import java.util.ArrayList;

import be.objectify.deadbolt.core.models.Subject;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.controllers.AdminFactory;
import ix.core.models.Acl.Permission;
import ix.utils.Util;
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

    private String hashp;
    private String salt;
    public boolean systemAuth; //FDA, NIH employee
    
    //private key to be used in authentication
    //This is not a public/private 
    private String key;
    
    //Not sure if this should be shown here?
    public String getKey(){
    	return key;
    }
    
    public void regenerateKey(){
    	key=Util.generateRandomString(20);
    	System.out.println("Generated key:" + key + " for user:" + user.username);
    	System.out.println("Current token:" + this.getComputedToken());
    }
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_userprof_prop")
    public List<Value> properties = new ArrayList<Value>();

    public UserProfile () {}
    public UserProfile (Principal user) {
        this.user = user;
        regenerateKey();
    }

    @Override
    public List<Role> getRoles(){
        return AdminFactory.rolesByPrincipal(user); 	//roles;

    }

    @Override
    public List<Acl> getPermissions(){
        return AdminFactory.permissionByPrincipal(user); //return permissions;
    }

    public List<Group> getGroups() {
        return AdminFactory.groupsByPrincipal(user);
    }

    @Override
    public String getIdentifier(){
        return user.username;
    }
    
    public String getComputedToken(){
    	String date=""+Util.getCanonicalCacheTimeStamp();
    	return Util.sha1(date+this.user.username + this.key);
    }
    
    public Long getTokenTimeToExpireMS(){
    	long date=(Util.getCanonicalCacheTimeStamp()+1)*Util.getTimeResolutionMS();
    	return (date-System.currentTimeMillis());
    }
    
    
    private String getPreviousComputedToken(){
    	String date=""+(Util.getCanonicalCacheTimeStamp()-1);
    	return Util.sha1(date+this.user.username + this.key);
    }
    
    public boolean acceptKey(String key){
    	if(key.equals(this.key))return true;
    	return false;
    }
	public boolean acceptToken(String token) {
		if(this.getComputedToken().equals(token))return true;
		if(this.getPreviousComputedToken().equals(token))return true;
		return false;
	}
	public boolean acceptPassword(String password){
		if(this.hashp==null||this.salt==null)return false;
		return this.hashp.equals(Util.encrypt(password, this.salt));
	}
    
	public void setPassword(String password){
		this.salt = AdminFactory.generateSalt();
		this.hashp = Util.encrypt(password, this.salt);
	}
}
