package ix.core;

import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import ix.ncats.controllers.auth.Authentication;

public class UserFetcher {
	
	private static final String DEFAULT_USERNAME = "AUTO_IMPORTER";
    private static ThreadLocal<Principal> localUser = new ThreadLocal<Principal>();
    
	public static Principal getActingUser(){
		try {
			Principal p = Authentication.getUser();
		    if(p!=null)
		    	return p;
		} catch (Exception e) {
			
		}
		Principal p=localUser.get();
		if(p!=null)return p;
		return PrincipalFactory.registerIfAbsent(new Principal(DEFAULT_USERNAME,null));
	}
	public static void setLocalThreadUser(Principal p){
		localUser.set(p);
	}
	public static UserProfile getActingUserProfile(){
		Principal p= getActingUser();
		if(p!=null){
			return getActingUserProfile(p.username);
		}
		return null;
	}
	
	private static UserProfile getActingUserProfile(String username){
		UserProfile profile = UserProfileFactory.finder.where().eq("user.username", username).findUnique();
    	return profile;
	}
	
}
