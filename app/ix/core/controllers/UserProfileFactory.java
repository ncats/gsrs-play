package ix.core.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import be.objectify.deadbolt.java.actions.Dynamic;
import ix.core.NamedResource;
import ix.core.models.Group;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.core.util.CachedSupplier;
import ix.core.util.StreamUtil;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.utils.Util;
import play.db.ebean.Model;

//
//@NamedResource(name="users",
//
//type=UserProfile.class,
//description="Resource for handling user profiles")
public class UserProfileFactory extends EntityFactory {
	static private CachedSupplier<Model.Finder<Long, UserProfile>> finder = 
			Util.finderFor(Long.class,UserProfile.class);

	
	public static UserProfile getUserProfileForPrincipal(Principal p){
		return getUserProfileForUsername(p.username);
	}
	public static UserProfile getUserProfileById(long id){
		return finder.get()
				.where()
				.idEq(id)
				.findUnique();
	}
	public static UserProfile getUserProfileForUsername(String username){
		
		UserProfile profile = finder.get()
									.where()
									.ieq("user.username", username)
									.findUnique();
		return profile;
	}
	
	public synchronized static UserProfile addActiveUser(Principal newUser, String password, List rolesChecked, List groupsChecked ) {
		

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
	        			e.printStackTrace();
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
                    try {
						g.save();
					}catch(Throwable t){
                        for(Principal p : g.members){
                            System.out.println(p.username + " id = " + p.id);
                        }
                        throw t;
					}
                }
            }
            prof.save();
        }
        return prof;
	}
	
    public static UserProfile addActiveUser(String username, String password, List rolesChecked, List groupsChecked ) {
        Principal newUser = new Principal();
        newUser.username =username;
        return addActiveUser(newUser,password,rolesChecked,groupsChecked);
    }
    
    public static Stream<UserProfile> userStream(){
    	Iterator<UserProfile> profiles= finder.get().findIterate();
    	return StreamUtil.forIterator(profiles);
    }
}
