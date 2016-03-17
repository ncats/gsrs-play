package ix.ncats.controllers.auth;

import java.util.*;
import java.util.concurrent.Callable;

import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.*;
import play.*;
import play.Logger;
import play.db.ebean.*;
import play.mvc.*;

import com.avaje.ebean.*;

import ix.core.plugins.IxCache;
import ix.utils.Util;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple controller to authenticate via ldap
 */

public class Authentication extends Controller {
    public static String APP;
    public static final String SESSION = "ix.session";
    public static int TIMEOUT; // session idle

    
    static Model.Finder<UUID, Session> _sessions =
            new Model.Finder<UUID, Session>(UUID.class, Session.class);
    static Model.Finder<Long, UserProfile> _profiles =
            new Model.Finder<Long, UserProfile>(Long.class, UserProfile.class);
    
    private static Cache tokenCache=null;
    private static Cache tokenCacheUserProfile=null;
    private static long lastCacheUpdate=-1;

    static ix.core.plugins.SchedulerPlugin scheduler;

    
    static{
    	init();
    	
    }

	public static void init(){
		APP = Play.application()
				.configuration().getString("ix.app", "MyApp");

		TIMEOUT = 1000 * Play.application()
				.configuration().getInt(SESSION, 7200); // session idle

        scheduler =
                Play.application().plugin(ix.core.plugins.SchedulerPlugin.class);
		setupCache();

	}
    
    public static void setupCache(){
    	String CACHE_NAME="TOKEN_CACHE";
    	String CACHE_NAME_UP="TOKEN_UP_CACHE";
    	//always hold onto the tokens for twice the time required
    	long tres=Util.getTimeResolutionMS()*2;
    	
    	int maxElements=99999;
    	int maxElementsUP=100;
        Application app=Play.application();
        
        CacheConfiguration config =
            new CacheConfiguration (CACHE_NAME, maxElements)
            .timeToLiveSeconds(tres/1000);
        tokenCache = new Cache (config);
        CacheManager.getInstance().removeCache(CACHE_NAME);
		CacheManager.getInstance().addCache(tokenCache);

        tokenCache.setSampledStatisticsEnabled(true);
        
        CacheConfiguration configUp =
                new CacheConfiguration (CACHE_NAME_UP, maxElementsUP)
                .timeToIdleSeconds(tres/1000/100);
        tokenCacheUserProfile= new Cache (configUp);

        CacheManager.getInstance().removeCache(CACHE_NAME_UP);

        CacheManager.getInstance().addCache(tokenCacheUserProfile);     
        tokenCacheUserProfile.setSampledStatisticsEnabled(true);
        lastCacheUpdate=-1;
        
    }
    
    
    public static void setupCacheIfNeccessary(){
    	if(tokenCache==null){
    		setupCache();
    	}else{
	    	try{
	    		tokenCache.getSize();
	    	}catch(Exception e){
	    		setupCache();
	    	}
    	}
    	
    }

    public static boolean loginUserFromHeader(Http.Request r){
		try {
			if(r==null){
				r=request();
			}
			if (Play.application().configuration()
					    .getBoolean("ix.authentication.trustheader")) {
				String usernameheader = Play.application().configuration()
						.getString("ix.authentication.usernameheader");
				String usernameEmailheader = Play.application().configuration()
						.getString("ix.authentication.useremailheader");
				String username = r.getHeader(usernameheader);
				String userEmail = r.getHeader(usernameEmailheader);
				
				if (username != null) {
					if (validateUserHeader(username,r)) {
						setSessionUser(username,userEmail);
						return true;
					}
				}
			}
		} catch (Exception e) {

			
		}
		return false;
    }
    
    public static boolean validateUserHeader(String username, Http.Request r){
    	if(r==null){
			r=request();
		}
    	if(Play.application().configuration().getBoolean("ix.authentication.trustheader")){
    		String userHeader = Play.application().configuration().getString("ix.authentication.usernameheader");
    		String headerUser=r.getHeader(userHeader);
    		if(headerUser!=null && headerUser.equals(username)){
    			return true;
    		}
    	}
    	return false;
    }
    
    public static boolean allowNonAuthenticated(){
    	return Play.application().configuration().getBoolean("ix.authentication.allownonauthenticated",true) 
    		   ||
    		   Play.application().configuration().getBoolean("ix.admin",false);
    }
    
    static UserProfile setSessionUser(String username){
    	return setSessionUser(username,null);
    }
    
    //Set and trust username / email as user
    //Only call after authenticated
    private static UserProfile setSessionUser(String username, String email){
        boolean systemAuth = false;
        boolean newregistered=false;
        
        UserProfile profile = _profiles.where().eq("user.username", username).findUnique();
        Principal cred;
        if(profile==null || profile.user == null){
        	if(Play.application().configuration().getBoolean("ix.authentication.autoregister",false)){
        		Logger.info("Autoregistering user:" + username);
        		Principal p = new Principal(username, email);
        		cred= PrincipalFactory.registerIfAbsent(p);
        		newregistered=true;
        	}else{
        		Logger.info("Autoregistering not allowed");
        		throw new IllegalStateException("User:" + username + " is not a current user in the system");
        	}
        }else{
        	systemAuth=true;
        	cred= profile.user;
        }
         
        
        Transaction tx = Ebean.beginTransaction();
        try {
        	List<UserProfile> users =
                _profiles.where().eq("user.username", username).findList();

            if (users == null || users.isEmpty()) {
                profile = new UserProfile(cred);
                if(newregistered){
                	if(Play.application().configuration().getBoolean("ix.authentication.autoregisteractive",false)){
                    	profile.active = true;	
            		}else{
            			profile.active = false;
            		}
                }else{
                	profile.active = true;
                }
                profile.systemAuth = systemAuth;
                profile.save();
                tx.commit();
        		tokenCache.put(new Element(profile.getComputedToken(), profile.getIdentifier()));
            } else {
                profile = users.iterator().next();
                profile.user.username = username;

                
            }
            if (!profile.active) {
            	flash("message", "User is no longer active!");
                throw new IllegalStateException("User:" + username + " is not an active user");
            }
            setUserSessionDirectly(profile);
            tx.commit();
        } catch (Exception ex) {
            Logger.trace
                    ("Can't update UserProfile for user " + username, ex);
            throw new IllegalStateException(ex);
        } finally {
            Ebean.endTransaction();
        }
        return profile;
    }
    
    private static void setUserSessionDirectly(UserProfile profile){
    	Session session = new Session(profile);
        session.save();
        IxCache.set(session.id.toString(), session);

        Logger.debug("** new session " + session.id
                + " created for user "
                + profile.user.username + "!");
        session().clear();
        session(SESSION, session.id.toString());
    }
    
    public static UserProfile getUserProfileFromKey(String username, String key){
    	UserProfile profile = _profiles.where().eq("user.username", username).findUnique();
    	if(profile!=null){
    		if(profile.acceptKey(key)){
    			return profile;
    		}
    	}
    	return null;
    }
    public static UserProfile getUserProfileFromPassword(String username, String password){
    	
    	UserProfile profile = _profiles.where().eq("user.username", username).findUnique();
    	if(profile!=null){
    		if(AdminFactory.validatePassword(profile, password)){
    			return profile;
    		}
    	}
    	return null;
    }
    
    public static UserProfile directlogin(String username, String password) throws Exception{
    	Principal cred;
        UserProfile profile = UserProfileFactory.finder.where().eq("user.username", username).findUnique();
        
        if (profile != null && AdminFactory.validatePassword(profile, password) && profile.active) {
                cred = profile.user;
        } else {
        		cred = AdminFactory.externalAuthenticate(username,password);
        }
        if (cred == null) {
            throw new IllegalArgumentException("Invalid credentials!");
        }
        try{
        	Authentication.setSessionUser(username);
        }catch(Exception e){
        	throw e;
        }
        return profile;
    }
    
    public static UserProfile getUserProfileFromToken(String username, String token){
    	UserProfile profile = getUserProfile(username);
    	if(profile!=null){
    		if(profile.acceptToken(token)){
    			return profile;
    		}
    	}
    	return null;
    }
    
    public static UserProfile getUserProfileFromTokenAlone(String token){
    	updateUserProfileTokenCacheIfNecessary();
    	Element uelm=tokenCache.get(token);
    	if(uelm!=null){
    		String username=(String)uelm.getObjectValue();
    		UserProfile profile = getUserProfile(username);
    		if(profile.acceptToken(token)){
    			return profile;
    		}
    	}else{
    		System.out.println("token:" + token + " does not exist in cache, size:" + tokenCache.getSize());
    	}
    	return null;
    }
    
    private static UserProfile getUserProfile(String username){
    	Element upelm=tokenCacheUserProfile.get(username);
    	if(upelm!=null){
    		return (UserProfile)upelm.getObjectValue();
    	}
    	UserProfile profile = _profiles.where().eq("user.username", username).findUnique();
    	if(profile!=null){
    		tokenCacheUserProfile.put(new Element(username, profile));	
    	}
    	return profile;
    }
    
    public static void updateUserProfileTokenCacheIfNecessary(){
    	setupCacheIfNeccessary();
    	if(Util.getCanonicalCacheTimeStamp()!=lastCacheUpdate){
    		updateUserProfileTokenCache();
    	}else{
    	}
    }
    public static void updateUserProfileToken(UserProfile up){
    	tokenCache.put(new Element(up.getComputedToken(), up.getIdentifier()));
    }
    public static void updateUserProfileTokenCache(){
    	try{
	    	for(UserProfile up:_profiles.all()){
	    		tokenCache.put(new Element(up.getComputedToken(), up.getIdentifier()));
	    	}
	    	lastCacheUpdate=Util.getCanonicalCacheTimeStamp();
    	}catch(Exception e){
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    
    

    

    public static UserProfile getUserProfile() {
        Session session = getSession();
        if(session!=null){
        	if(session.profile!=null){
        		return session.profile;
        	}
        }
        
        //do key auth
        String user=request().getHeader("auth-username");
        String key=request().getHeader("auth-key");
        String token=request().getHeader("auth-token");
        String password=request().getHeader("auth-password");
        
        UserProfile up=null;
        
        if(user!=null && key!=null){
        	up=getUserProfileFromKey(user,key);
        }else if(token!=null){
        	try{
        		up=getUserProfileFromTokenAlone(token);
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }else if(user!=null && password!=null){
        	up=getUserProfileFromPassword(user,password);
        }
        
        if(up!=null){
        	setUserSessionDirectly(up);
        }
        
        return up;
    }

    public static Principal getUser(){
    	try{
    		UserProfile up=getUserProfile();
    		if(up!=null)return up.user;
    	}catch(Exception e){
    		//System.out.println("No user accessible");
    	}
    	
    	return null;
    }
    
    public static Session getCachedSession(final String id) {
        Session session = null;
        if (id != null) {
            try {
                session = IxCache.getOrElse
                        (id, new Callable<Session>() {
                            public Session call() throws Exception {
                                return _sessions.byId(UUID.fromString(id));
                            }
                        }, 0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return session;
    }

    public static Session getSession() {
        String id = session(SESSION);
        Session session = getCachedSession(id);
        if (session != null) {
            long current = System.currentTimeMillis();
            if ((current - session.accessed) > TIMEOUT) {
                Logger.debug("Session " + session.id + " expired!");
                flush(session);
            } else {
                session.accessed = current;
                //commented out for now
                //Logger.debug("Session " + session.id + " expires at " + new java.util.Date(current + TIMEOUT));
            }
        } else {
            session().clear();
        }
        return session;
    }
    

    static void flush(Session session) {
        Transaction tx = Ebean.beginTransaction();
        try {
            session.expired = true;
            session.update();
            tx.commit();
            session().clear();
            IxCache.remove(session.id.toString());
        } catch (Exception ex) {
            Logger.trace
                    ("Can't update session " + session.id, ex);
        } finally {
            Ebean.endTransaction();
        }
    }
	public static UserProfile getAdministratorContact() {
		Object o=Play.application().configuration().getObject("ix.sysadmin");
		try{
			if(o!=null){
				ObjectMapper om = new ObjectMapper();
				Principal p1=om.treeToValue(om.valueToTree(o), Principal.class);
				UserProfile profile = _profiles.where().eq("user.username", p1.username).findUnique();
				if(profile==null){
					profile= new UserProfile(p1);
				}
				return profile;
			}
		}catch(Exception e){
			
		}
		
		for(UserProfile profile:  _profiles.findList()){
			for(Role r:profile.getRoles()){
				if(r == Role.Admin){
					return profile;
				}
			}
		}
		return null;
	}
}
