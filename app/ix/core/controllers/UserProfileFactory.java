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
	
	//@Dynamic(value = "adminUser", handlerKey = "idg")
    public static UserProfile addActiveUser(String username, String password, List<Role> rolesCheckeda, List<Group> groupsChecked ) {
        Principal newUser = new Principal();
        newUser.username =username;
        
        if(groupsChecked==null){
        	groupsChecked=new ArrayList<Group>();
        }
        
        
        
        newUser.save();
        UserProfile prof = UserProfileFactory.finder.where().eq("user.username", newUser.username).findUnique();

        if (prof == null) {
            prof = new UserProfile(newUser);
            prof.active = true;
            prof.setPassword(password);
            prof.setRoles(rolesCheckeda);
            
            for (Group g : groupsChecked) {
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
}
