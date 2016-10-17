package ix.ncats.controllers.auth;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.auth.AuthenticationCredentials;
import ix.core.auth.Authenticator;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.factories.AuthenticatorFactory;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.Session;
import ix.core.models.UserProfile;
import ix.core.plugins.IxCache;
import ix.core.util.CachedSupplier;
import ix.core.util.TimeUtil;
import ix.utils.Util;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;

/**
 * A simple controller to authenticate
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
    
    private static CachedSupplier<CacheManager> manager=CachedSupplier.of(()->CacheManager.getInstance());

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
        
        
        CacheManager manager = Authentication.manager.get();
        
        
        CacheConfiguration config =
            new CacheConfiguration (CACHE_NAME, maxElements)
            .timeToLiveSeconds(tres/1000);
        tokenCache = new Cache (config);
        manager.removeCache(CACHE_NAME);
        manager.addCache(tokenCache);

        tokenCache.setSampledStatisticsEnabled(true);
        
        CacheConfiguration configUp =
                new CacheConfiguration (CACHE_NAME_UP, maxElementsUP)
                .timeToIdleSeconds(tres/1000/100);
        tokenCacheUserProfile= new Cache (configUp);

        manager.removeCache(CACHE_NAME_UP);
        manager.addCache(tokenCacheUserProfile);  
        
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
    
	public static UserProfile getUserProfileOrElse(String username, Supplier<UserProfile> orElse) {
		
		UserProfile profile = fetchProfile(username);
		
		if(profile!=null && profile.user!=null){
			return profile;
		}else{
			return orElse.get();
		}

	}
    
    public static boolean allowNonAuthenticated(){
    	return Play.application().configuration().getBoolean("ix.authentication.allownonauthenticated",true) 
    		   ||
    		   Play.application().configuration().getBoolean("ix.admin",false);
    }
    
    static UserProfile setSessionUser(String username){
    	return setSessionUser(username,null);
    }
    
    
    public static UserProfile setUserProfileSessionUsing(String username, String email){
    	return setSessionUser(username,email);
    }
    
    
    //Set and trust username / email as user
    //Only call after authenticated
    private static UserProfile setSessionUser(String username, String email){
        
        UserProfile profile= getUserProfileOrElse(username, ()->{
        	if(Play.application().configuration().getBoolean("ix.authentication.autoregister",false)){
        		Transaction tx = Ebean.beginTransaction();
        		try{
	        		Logger.info("Autoregistering user:" + username);
	        		Principal p = new Principal(username, email);
	        		p= PrincipalFactory.registerIfAbsent(p);
	        		UserProfile up = p.getUserProfile();
	        		if(up==null){
	        			up = new UserProfile(p);
	        		}
	        		if(Play.application().configuration().getBoolean("ix.authentication.autoregisteractive",false)){
	                	up.active = true;	
	        		}else{
	        			up.active = false;
	        		}
	        		up.systemAuth=false;
	        		tx.commit();
	        		tokenCache.put(new Element(up.getComputedToken(), up.getIdentifier()));
	        		return up;
        		}catch(Exception e){
        			Logger.error("Error creating profile", e);
        			return null;
        		}finally{
        			Ebean.endTransaction();
        		}
        	}else{
        		Logger.info("No user found, and autoregistering is not allowed");
        		return null;
        	}
        });
        
        if(profile==null){
        	throw new IllegalStateException("Unable to set session for user:" + username);
        }
        
        Transaction tx = Ebean.beginTransaction();
        try {
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
        IxCache.setRaw(session.id.toString(), session);

        Logger.debug("** new session " + session.id
                + " created for user "
                + profile.user.username + "!");
        session().clear();
        session(SESSION, session.id.toString());
    }
    
    public static UserProfile getUserProfileFromKey(String username, String key){
    	UserProfile profile = fetchProfile(username);
    	if(profile!=null){
    		if(profile.acceptKey(key)){
    			return profile;
    		}
    	}
    	return null;
    }
    
    
    public static UserProfile getUserProfileFromPassword(String username, String password){
    	UserProfile profile = fetchProfile(username);
    	if(profile!=null){
    		if(AdminFactory.validatePassword(profile, password)){
    			return profile;
    		}
    	}
    	return null;
    }
    

    public static UserProfile authenticate(AuthenticationCredentials cred){
    	Authenticator auth = AuthenticatorFactory
    						.getInstance(Play.application())
    						.getAuthenticator();
    	UserProfile up = auth.authenticate(cred);
    	
    	return up;
    }
    
    public static UserProfile directlogin(String username, String password) throws Exception{
    	//Principal cred;

		AuthenticationCredentials credentials = AuthenticationCredentials.create(username, password);

		UserProfile up = authenticate(credentials);
		
		
		
        if (up == null) {
        	flash("message", "Invalid username or password");
            throw new IllegalArgumentException("Invalid credentials!");
        }
        
        try{
        	Authentication.setSessionUser(up.user.username);
        }catch(Exception e){
        	throw e;
        }
        
        return up;
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
    	Element upelm=tokenCacheUserProfile.get(username.toUpperCase());
    	if(upelm!=null){
    		return (UserProfile)upelm.getObjectValue();
    	}
    	UserProfile profile = fetchProfile(username);
    	if(profile!=null){
    		tokenCacheUserProfile.put(new Element(username.toUpperCase(), profile));	
    	}
    	return profile;
    }

	private static UserProfile fetchProfile(String username) {
		return _profiles.where().ieq("user.username", username).findUnique();
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

        AuthenticationCredentials cred=AuthenticationCredentials.create(ctx());
        
        
        List<Authenticator> authenticators= AuthenticatorFactory
        	.getInstance(Play.application())
        	.getRegisteredResourcesFor(AuthenticatorFactory.RESOURCE_CLASS);
        
        
        
        Optional<UserProfile> opup=authenticators.stream()
        		.map(au->au.authenticate(cred))
        		.filter(Objects::nonNull)
        		.findFirst();
        
        if(opup.isPresent()){
        	setUserSessionDirectly(opup.get());
        }
        return opup.orElse(null);
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
                session = IxCache.getOrElseRaw
                        (id, ()->_sessions.byId(UUID.fromString(id)), 0);
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
            long current = TimeUtil.getCurrentTimeMillis();
            if ((current - session.accessed) > TIMEOUT) {
                Logger.debug("Session " + session.id + " expired!");
                flash("warning", "Your session has expired!");
                flush(session);
                return null;
            } else {
                session.accessed = current;
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
				UserProfile profile = _profiles.where().ieq("user.username", p1.username).findUnique();
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
