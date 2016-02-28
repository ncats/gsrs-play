package ix.core.models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Subject;
import ix.core.controllers.AdminFactory;
import ix.utils.Util;

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
    
    @Lob
    @JsonIgnore
    private String _roles=null;  //this is a silly, but quick way to serialize roles
    
    
    //private key to be used in authentication
    //This is not a public/private 
    private String key;
    
    //Not sure if this should be shown here?
    public String getKey(){
    	
    	return key;
    }
    
    public void regenerateKey(){
    	key=Util.generateRandomString(20);
    	//System.out.println("Generated key:" + key + " for user:" + user.username);
    	//System.out.println("Current token:" + this.getComputedToken());
    }
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_userprof_prop")
    public List<Value> properties = new ArrayList<Value>();

    public UserProfile () {}
    public UserProfile (Principal user) {
        this.user = user;
        regenerateKey();
    }

    /*
    public List<Role.Kind> getRolesKinds(){
    	List<Role> roles=AdminFactory.rolesByPrincipal(user); 	//roles;
    	List<Role.Kind> rkind = new ArrayList<Role.Kind>();
    	if(roles==null || roles.isEmpty()){
    		
    	}else{
	    	for(Role r:roles){
	    		System.out.println("Fetchin roles:" + r.getName());
	    		rkind.add(r.role);
	    	}
    	}
        return rkind;
    }
    public void addRoleKind(Role.Kind role){
    	List<Role.Kind> roles=getRolesKinds();
    	roles.add(role);
    	setRoleKinds(new ArrayList<Role.Kind>(new LinkedHashSet<Role.Kind>(roles)));
    }
    
    public void setRoleKinds(List<Role.Kind> rolekinds){
    	List<Role> tempRoles=new ArrayList<Role>();
    	for(Role.Kind rk:rolekinds){
    		tempRoles.add(new Role(rk));
    	}
    	AdminFactory.updateRolesF(this.id, tempRoles);
    }
    */
    public List<Role.Kind> getRolesKinds(){
    	List<Role.Kind> rolekinds=new ArrayList<Role.Kind>();
    	if(this._roles!=null){
    		try{
	    		ObjectMapper om = new ObjectMapper();
	    		List l=om.readValue(_roles, List.class);
	    		for(Object o:l){
	    			try{
	    				rolekinds.add(Role.Kind.valueOf(o.toString()));
	    			}catch(Exception e){
	    				e.printStackTrace();
	    			}
	    		}
    		}catch(Exception e){
    			
    		}
    		
    	}
        return rolekinds;
    }
    
    public void addRoleKind(Role.Kind role){
    	List<Role.Kind> roles=getRolesKinds();
    	roles.add(role);
    	setRoleKinds(new ArrayList<Role.Kind>(new LinkedHashSet<Role.Kind>(roles)));
    }
    
    public void setRoleKinds(List<Role.Kind> rolekinds){
    	ObjectMapper om = new ObjectMapper();
    	_roles=om.valueToTree(rolekinds).toString();
    }
    
    @JsonIgnore
    public List<Role> getRoles(){
    	List<Role.Kind> rkinds=getRolesKinds();
    	List<Role> roles=new ArrayList<Role>();
    	for(Role.Kind rk:rkinds){
    		roles.add(new Role(rk));
    	}
    	return roles;
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
