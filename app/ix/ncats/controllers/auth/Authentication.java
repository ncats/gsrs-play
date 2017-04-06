package ix.ncats.controllers.auth;

import java.util.*;
import java.util.function.Supplier;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.auth.AuthenticationCredentials;
import ix.core.auth.Authenticator;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.factories.AuthenticatorFactory;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.Session;
import ix.core.models.UserProfile;
import ix.core.plugins.IxCache;
import ix.core.util.CachedSupplier;
import ix.core.util.ConfigHelper;
import ix.core.util.StreamUtil;
import ix.core.util.TimeUtil;
import ix.utils.Util;
import net.sf.ehcache.CacheManager;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.Controller;

/**
 * A simple controller to authenticate
 */

public class Authentication extends Controller {
	static final String SESSION = "ix.session";
   	static String CACHE_NAME="TOKEN_CACHE";
   	static String CACHE_NAME_UP="TOKEN_UP_CACHE";
	
    public static CachedSupplier<String> APP = 
    		ConfigHelper.supplierOf("ix.app", "MyApp");  //idle time
    
    public static CachedSupplier<Integer> TIMEOUT = 
    		ConfigHelper.supplierOf(SESSION, 1000 * 60 * 15);  //idle time (that's 15 minutes)
    
    public static CachedSupplier<Boolean> autoRegister = 
    		ConfigHelper.supplierOf("ix.authentication.autoregister", false);
    
    public static CachedSupplier<Boolean> autoRegisterActive = 
    		ConfigHelper.supplierOf("ix.authentication.autoregisteractive", false);
    		
    static CachedSupplier<Model.Finder<UUID, Session>> _sessions =
    		Util.finderFor(UUID.class, Session.class);
    
    public static CachedSupplier<UserTokenCache> tokens = CachedSupplier.of(()->{
    	return new UserTokenCache();
    });
    
    
    
    

    
    
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
        	if(autoRegister.get()){
        		try(Transaction tx = Ebean.beginTransaction()){
	        		Logger.info("Autoregistering user:" + username);
	        		Principal p = new Principal(username, email);
	        		p= PrincipalFactory.registerIfAbsent(p);
	        		UserProfile up = p.getUserProfile();
	        		if(up==null){
	        			up = new UserProfile(p);
	        		}
	        		if(autoRegisterActive.get()){
	                	up.active = true;	
	        		}else{
	        			up.active = false;
	        		}
	        		up.systemAuth=false;
	        		tx.commit();
	        		
	        		tokens.getSync().updateUserCache(up);
	        		return up;
        		}catch(Exception e){
        			Logger.error("Error creating profile", e);
        			return null;
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
    	UserProfile profile = fetchProfile(username);
    	if(profile!=null){
    		if(profile.acceptToken(token)){
    			return profile;
    		}
    	}
    	return null;
    }
    
    public static UserProfile getUserProfileFromTokenAlone(String token){
    	try{
	    	UserProfile profile=tokens.getSync().getUserProfileFromToken(token);
	    	if(profile!=null){
	    		return getUserProfileFromToken(profile.getIdentifier(),token);
	    	}
	    	return null;
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }


	private static UserProfile fetchProfile(String username) {
		return tokens.get().computeUserIfAbsent(username, f->{
			return UserProfileFactory.getUserProfileForUsername(username);
		});
	}
    
    
    
    public static UserProfile getUserProfile() {
        Session session = getSession();
        if(session!=null){
        	if(session.profile!=null){
        		return session.profile;
        	}
        }

        AuthenticationCredentials cred=AuthenticationCredentials.create(ctx());
        
        Set<Authenticator> authenticators= AuthenticatorFactory
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
                        (id, ()->_sessions.get().byId(UUID.fromString(id)), 0);
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
            long duration = TIMEOUT.get()*1000;
            long timeSinceAccessed  = (current - session.accessed);
            
            if (timeSinceAccessed > duration) {
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
		Object o= ConfigHelper.getOrDefault("ix.sysadmin",null);
		try{
			if(o!=null){
				ObjectMapper om = new ObjectMapper();
				Principal p1=om.treeToValue(om.valueToTree(o), Principal.class);
				UserProfile profile = p1.getUserProfile();
				if(profile==null){
					profile= new UserProfile(p1);
				}
				return profile;
			}
		}catch(Exception e){
			
		}
		
		return StreamUtil.forIterator(UserProfileFactory.users())
			.filter(up->up.hasRole(Role.Admin))
			.findFirst()
			.orElse(null);
	}
}
