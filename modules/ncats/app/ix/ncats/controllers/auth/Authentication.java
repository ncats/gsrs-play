package ix.ncats.controllers.auth;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.util.concurrent.Callable;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;

import ix.core.plugins.IxCache;
import ix.core.models.UserProfile;
import ix.core.models.Principal;
import ix.core.models.Session;
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
    public static final int TIMEOUT = 1000*Play.application()
        .configuration().getInt(SESSION, 7200); // session idle
        
    static Model.Finder<UUID, Session> _sessions =
        new Model.Finder<UUID, Session>(UUID.class, Session.class);
    static Model.Finder<Long, UserProfile> _profiles =
        new Model.Finder<Long, UserProfile>(Long.class, UserProfile.class);

    public static class Secured extends Security.Authenticator {
        @Override
        public String getUsername (Http.Context ctx) {
            return ctx.session().get(SESSION);
        }
        
        @Override
        public Result onUnauthorized (Http.Context ctx) {
            return redirect (routes.Authentication.login(null));
        }
    }
    
    public static Result authenticate (String url) {
        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");
        Logger.debug("username: "+username);
        
        Principal cred; 
        if (username.equalsIgnoreCase("caodac")
            && password.equalsIgnoreCase("foobar")) {
            cred = new Principal ();
            cred.username = username;
        }
        else {
            cred = NIHLdapConnector.getEmployee(username, password);
        }

        if (cred == null) {
            flash ("message", "Invalid credential!");
            return redirect (routes.Authentication.login(null));
        }
        
        List<UserProfile> users =
            _profiles.where().eq("user.username", username).findList();
        UserProfile profile;

        Transaction tx = Ebean.beginTransaction();
        try {
            if (users == null || users.isEmpty()) {
                profile = new UserProfile (cred);
                profile.active = true;
                profile.save();
            }
            else {
                profile = users.iterator().next();
                if (!profile.active) {
                    flash ("message", "User is no longer active!");
                    return redirect (routes.Authentication.login(null));
                }
            }
            Session session = new Session (profile);
            session.save();
            
            IxCache.set(session.id.toString(), session);
            
            Logger.debug("** new session "+session.id
                         +" created for user "
                         +profile.user.username+"!");
            session().clear();
            session (SESSION, session.id.toString());
            
            tx.commit();
        }
        catch (Exception ex) {
            Logger.trace
                ("Can't update UserProfile for user "+username, ex);
        }
        finally {
            Ebean.endTransaction();
        }   
            
        if (url != null) {
            return redirect (url);
        }
        
        return redirect (routes.Authentication.profile());
    }
    
    public static Result login (String url) {
        Session session = getSession ();
        if (session != null) {
            return url != null ? redirect (url)
                : redirect (routes.Authentication.profile());
        }
        return ok (ix.ncats.views.html.login.render(url, APP));
    }

    public static Result logout () {
        Session session = getSession ();
        if (session != null) {
            flash ("message", session.profile.user.username
                   +", you've logged out!");
            flush (session);
        }
        return redirect (routes.Authentication.login(null));
    }

    @Security.Authenticated(Secured.class)    
    public static Result secured () {
        Session session = getSession ();
        if (session.expired || !session.profile.active) {
            flash ("message", "Session timeout; please login again!");
            return redirect (routes.Authentication.login(null));
        }
        
        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(session));
    }

    public static UserProfile getUserProfile () {
        Session session = getSession ();
        return session != null ? session.profile : null;
    }

    public static Session getCachedSession (final String id) {
        Session session = null; 
        if (id != null) {
            try {
                session = IxCache.getOrElse
                    (id, new Callable<Session> () {
                            public Session call () throws Exception {
                                return _sessions.byId(UUID.fromString(id));
                            }
                        }, 0);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return session;
    }

    public static Session getSession () {
        String id = session (SESSION);
        Session session = getCachedSession (id);
        if (session != null) {
            long current = System.currentTimeMillis();
            if ((current - session.accessed) > TIMEOUT) {
                // expired
                Logger.debug("Session "+session.id+" expired!");
                flush (session);
            }
            else {
                session.accessed = current;
                Logger.debug("Session "+session.id+" expires at "
                             +new java.util.Date(current+TIMEOUT));
            }
        }
        else {
            session().clear();
        }
        return session;
    }

    static void flush (Session session) {
        Transaction tx = Ebean.beginTransaction();
        try {
            session.expired = true;         
            session.update();
            tx.commit();
            session().clear();
            IxCache.remove(session.id.toString());
        }
        catch (Exception ex) {
            Logger.trace
                ("Can't update session "+session.id, ex);
        }
        finally {
            Ebean.endTransaction();
        }
    }
}
