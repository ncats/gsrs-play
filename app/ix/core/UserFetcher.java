package ix.core;

import java.lang.reflect.Method;

import ix.core.controllers.PrincipalFactory;
import ix.core.models.Principal;
import ix.core.models.UserProfile;
import play.db.ebean.Model;

public class UserFetcher {
	static Model.Finder<Long, UserProfile> _profiles =
            new Model.Finder<Long, UserProfile>(Long.class, UserProfile.class);
	
	private static final String DEFAULT_USERNAME = "AUTO_IMPORTER";
	private static final String USER_FETCH_METHOD = "getUser";
	private static final String USER_FETCH_CLASS = "ix.ncats.controllers.auth.Authentication";
	private static Method userMethod = null;

    private static ThreadLocal<Principal> localUser = new ThreadLocal<Principal>();
	public static Principal getActingUser(){
		try {
			setupMethod();
			Principal p = ((Principal) userMethod.invoke(null));
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
	private static void setupMethod(){
		if(userMethod!=null)return;
		Class<?> act;
		try {
			act = Class.forName(USER_FETCH_CLASS);
			userMethod = act.getMethod(USER_FETCH_METHOD);
		}catch(Exception e){
			
		}
	}
	public static UserProfile getActingUserProfile(){
		Principal p= getActingUser();
		if(p!=null){
			return getActingUserProfile(p.username);
		}
		return null;
	}
	
	private static UserProfile getActingUserProfile(String username){
		UserProfile profile = _profiles.where().eq("user.username", username).findUnique();
    	return profile;
	}
	
}
