package ix.core.controllers;

import java.util.ArrayList;
import java.util.List;

import ix.core.models.Group;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import play.db.ebean.Model;

/* TODO: make this a resource eventually
@NamedResource(name="users",
 
type=UserProfile.class,
description="Resource for handling user profiles")
*/
public class UserProfileFactory extends EntityFactory {
	static final public Model.Finder<Long, UserProfile> finder =
            new Model.Finder(Long.class, UserProfile.class);
	
	
	public static UserProfile getUserProfileForPrincipal(Principal p){
		UserProfile profile = finder.where().eq("user.username", p.username).findUnique();
		return profile;
	}
	public static UserProfile addActiveUser(Principal newUser, String password, List rolesChecked, List groupsChecked ) {
		
        
        if(groupsChecked==null){
        	groupsChecked=new ArrayList<>();
        }
        if(rolesChecked==null){
        	rolesChecked=new ArrayList<>();
        }
        List<Role> applyRoles= new ArrayList<Role>();
	        for(Object r:rolesChecked){
	        	if(r instanceof Role){
	        		applyRoles.add((Role)r);
	        	}else{
	        		try{
	        			applyRoles.add(Role.valueOf(r.toString()));
	        		}catch(Exception e){
	        			System.err.println("Uknown role:'" + r.toString() + "'");
	        		}
	        	}
	        }
        List<Group> applyGroups= new ArrayList<Group>();
	        for(Object r:groupsChecked){
	        	if(r instanceof Group){
	        		applyGroups.add((Group)r);
	        	}else{
	        		try{
	        			Group g = new Group(r.toString());
	        			g=AdminFactory.registerGroupIfAbsent(g);
	        			applyGroups.add(g);
	        		}catch(Exception e){
	        			System.err.println("Error adding group");
	        		}
	        	}
	        }
        
        
        newUser=PrincipalFactory.registerIfAbsent(newUser);
        
        UserProfile prof = getUserProfileForPrincipal(newUser);

        if (prof == null) {
            prof = new UserProfile(newUser);
            prof.active = true;
            prof.setPassword(password);
            prof.setRoles(applyRoles);
            
            for (Group g : applyGroups) {
                if(g.id != null) {
                    g.members.add(newUser);
                    g.saveManyToManyAssociations("members");
                }else {
                    g.members.add(newUser);
                    g.save();
                }
            }
            prof.save();
        }
        return prof;
	}
	
	//@Dynamic(value = "adminUser", handlerKey = "idg")
    public static UserProfile addActiveUser(String username, String password, List rolesChecked, List groupsChecked ) {
        Principal newUser = new Principal();
        newUser.username =username;
        return addActiveUser(newUser,password,rolesChecked,groupsChecked);
    }
}
