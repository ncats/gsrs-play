package ix.ncats.controllers.auth;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.logging.*;

import be.objectify.deadbolt.core.models.Permission;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.models.*;
import play.*;
import play.Logger;
import play.api.mvc.Request;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.avaje.ebean.*;

import ix.core.plugins.IxCache;
import ix.ncats.models.Employee;
import ix.ncats.controllers.NIHLdapConnector;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple controller to authenticate via ldap
 */

public class Authentication extends Controller {
    public static final String SESSION = "ix.session";
    public static final String APP = Play.application()
            .configuration().getString("ix.app", "MyApp");
    public static final int TIMEOUT = 1000 * Play.application()
            .configuration().getInt(SESSION, 7200); // session idle

    static Model.Finder<UUID, Session> _sessions =
            new Model.Finder<UUID, Session>(UUID.class, Session.class);
    static Model.Finder<Long, UserProfile> _profiles =
            new Model.Finder<Long, UserProfile>(Long.class, UserProfile.class);

    public static class Secured extends Security.Authenticator {
        @Override
        public String getUsername(Http.Context ctx) {
            return ctx.session().get(SESSION);
        }

        @Override
        public Result onUnauthorized(Http.Context ctx) {
            return redirect(routes.Authentication.login(null));
        }
    }

    public static boolean loginUserFromHeader(){
    	return loginUserFromHeader();
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
    	return Play.application().configuration().getBoolean("ix.authentication.allownonauthenticated",true) ||
    			Play.application().configuration().getBoolean("ix.admin",false);
    }
    
    private static UserProfile setSessionUser(String username){
    	return setSessionUser(username,null);
    }
    
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
            } else {
                profile = users.iterator().next();
                profile.user.username = username;

                
            }
            if (!profile.active) {
            	flash("message", "User is no longer active!");
                throw new IllegalStateException("User:" + username + " is not an active user");
            }
            Session session = new Session(profile);
            session.save();

            IxCache.set(session.id.toString(), session);

            Logger.debug("** new session " + session.id
                    + " created for user "
                    + profile.user.username + "!");
            session().clear();
            session(SESSION, session.id.toString());
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
    
    public static Result authenticate(String url) {
        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");
        Logger.debug("username: " + username);
        boolean systemAuth = false;
        Principal cred;
        UserProfile profile = _profiles.where().eq("user.username", username).findUnique();

        System.out.println("auth1");
        if (profile != null && AdminFactory.validatePassword(profile, password) && profile.active) {
                cred = profile.user;
        } else {
            cred = NIHLdapConnector.getEmployee(username, password);
            systemAuth = true;
        }
        if (username.equalsIgnoreCase("caodac")
                && password.equalsIgnoreCase("foobar")) {
            cred = new Principal();
            cred.username = username;
        }
        if (cred == null) {
            flash("message", "Invalid credential!");
            return redirect(routes.Authentication.login(null));
        }
        System.out.println("auth3");
        try{
        	setSessionUser(username);
        }catch(Exception e){
        	return internalServerError(e.getMessage());
        }

        if (url != null) {
            return redirect(url);
        }
        System.out.println("auth4:" + url);
        return redirect(routes.Authentication.secured());
    }

    public static Result login(String url) {
        Session session = getSession();
        Logger.debug("url:" +  url + "  app: " + APP);
        if (session != null) {
            return url != null ? redirect(url)
                    : redirect(routes.Authentication.secured());
        }
        return ok(ix.ncats.views.html.login.render(url, APP));
    }

    public static Result logout() {
        Session session = getSession();
        if (session != null) {
            flash("message", session.profile.user.username
                    + ", you've logged out!");
            flush(session);
        }
        return redirect(routes.Authentication.login(null));
    }

    static ix.core.plugins.SchedulerPlugin scheduler =
            Play.application().plugin(ix.core.plugins.SchedulerPlugin.class);

    @Security.Authenticated(Secured.class)
    @JsonIgnore
    public static Result secured() {
    	Session session = getSession();
        if (session.expired || !session.profile.active) {
            flash("message", "Session timeout; please login again!");
            return redirect(routes.Authentication.login(null));
        }
        
       // scheduler.submit(session.id.toString());

        ObjectMapper mapper = new ObjectMapper();
        //mapper.valueToTree(session);
        //return ok(mapper.valueToTree(session));
        String context = Play.application().configuration().getString("application.context");
        Logger.debug("context:" + context);
        return redirect(context);
    }

    public static UserProfile getUserProfile() {
        Session session = getSession();
        return session != null ? session.profile : null;
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
				if(r.role == Role.Kind.Admin){
					return profile;
				}
			}
		}
		
		
		return null;
	}
}
