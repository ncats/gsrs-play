package ix.ncats.controllers.auth;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
import ix.core.util.StreamUtil;
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
	public static final String SESSION = "ix.session";
    public static CachedSupplier<String> APP = CachedSupplier.of(()->{
    	return Play.application()
					.configuration()
					.getString("ix.app", "MyApp");
    });
    
    public static CachedSupplier<Integer> TIMEOUT = CachedSupplier.of(()->{
    	return Play.application()
    			.configuration()
    			.getInt(SESSION, 7200); // session idle
    });
    
    public static CachedSupplier<Boolean> autoRegister = CachedSupplier.of(()->{
    	return Play.application().configuration().getBoolean("ix.authentication.autoregister",false);
    });
    
    public static CachedSupplier<Boolean> autoRegisterActive = CachedSupplier.of(()->{
    	return Play.application().configuration().getBoolean("ix.authentication.autoregisteractive",false);
    });
    

    
    static Model.Finder<UUID, Session> _sessions =
            new Model.Finder<UUID, Session>(UUID.class, Session.class);
    static Model.Finder<Long, UserProfile> _profiles =
            new Model.Finder<Long, UserProfile>(Long.class, UserProfile.class);
    
    public static CachedSupplier<TokenCache> tokens = CachedSupplier.of(()->{
    	return new TokenCache();
    });
    
    
    
    public static class TokenCache{
    	   private Cache tokenCache=null;
    	   private Cache tokenCacheUserProfile=null;
    	   private long lastCacheUpdate=-1;
    	   
    	   public TokenCache(){
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

		private void updateIfNeeded() {
			if (Util.getCanonicalCacheTimeStamp() != lastCacheUpdate) {
				updateUserProfileTokenCache();
			}
		}

		public void updateUserCache(UserProfile up) {
			tokenCache.put(new Element(up.getComputedToken(), up.getIdentifier()));
			tokenCacheUserProfile.put(new Element(up.getIdentifier(), up));
		}

		public UserProfile getUserProfile(String token) {
			updateIfNeeded();
			Element e=tokenCache.get(token);
			if(e==null)return null;
			return (UserProfile)e.getObjectValue();
		}
		
		
		public UserProfile computeUserIfAbsent(String username, Function<String,UserProfile> fetcher){
			Element elm = tokenCacheUserProfile.get(username);
			if(elm==null|| elm.getObjectValue()==null){
				UserProfile upnew= fetcher.apply(username);
				if(upnew!=null){
					updateUserCache(upnew);
					return upnew;
				}
			}
			return (UserProfile)elm.getObjectValue();
		}
		private void updateUserProfileTokenCache(){
	    	try{
	    		StreamUtil.ofIterator(UserProfileFactory.users()).forEach(up->{
	    			updateUserCache(up);
	    		});
		    	lastCacheUpdate=Util.getCanonicalCacheTimeStamp();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		throw e;
	    	}
	    }	
    }
    
    
    private static CachedSupplier<CacheManager> manager=CachedSupplier.of(()->CacheManager.getInstance());

    
    
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
        		Transaction tx = Ebean.beginTransaction();
        		try{
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
	        		
	        		tokens.get().updateUserCache(up);
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
    	UserProfile profile=tokens.get().getUserProfile(token);
    	if(profile!=null){
    		return getUserProfileFromToken(profile.getIdentifier(),token);
    	}
    	return null;
    }
    
    private static UserProfile getUserProfile(String username){
    	return tokens.get().computeUserIfAbsent(username, (u)->{
    		return fetchProfile(u);
    	});
    }

	private static UserProfile fetchProfile(String username) {
		return UserProfileFactory.getUserProfileForUsername(username);
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
            if ((current - session.accessed) > TIMEOUT.get()) {
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
				UserProfile profile = p1.getUserProfile();
				if(profile==null){
					profile= new UserProfile(p1);
				}
				return profile;
			}
		}catch(Exception e){
			
		}
		
		return StreamUtil.ofIterator(UserProfileFactory.users())
			.filter(up->up.hasRole(Role.Admin))
			.findFirst()
			.orElse(null);
	}
}
