package ix.ncats.controllers.auth;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.logging.*;

import be.objectify.deadbolt.core.models.Permission;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.EntityProcessor;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.models.*;
import play.*;
import play.Logger;
import play.api.mvc.Request;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.avaje.ebean.*;

import ix.core.plugins.IxCache;
import ix.core.plugins.IxContext;
import ix.core.util.ConfigHelper;
import ix.ncats.models.Employee;
import ix.utils.Util;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import ix.ncats.controllers.NIHLdapConnector;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple controller to authenticate via ldap
 */

public class NCATSAuthentication extends Controller {
   
    public static class Secured extends Security.Authenticator {
        @Override
        public String getUsername(Http.Context ctx) {
            return ctx.session().get(Authentication.SESSION);
        }

        @Override
        public Result onUnauthorized(Http.Context ctx) {
            return redirect(routes.NCATSAuthentication.login(null));
        }
    }
    public static Result authenticate(String url) {
    	
        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");
        Logger.debug("username: " + username);
        try{
        	Authentication.directlogin(username,password);
        }catch(IllegalArgumentException e){
        	return redirect(routes.NCATSAuthentication.login(null));
        }catch(Exception e){
        	return internalServerError(e.getMessage());
        }
        if (url != null) {
            return redirect(url);
        }
        return redirect(routes.NCATSAuthentication.secured());
    }
    
   
    
    

    public static Result login(String url) {
        Session session = Authentication.getSession();
        if (session != null) {
            return url != null ? redirect(url)
                    : redirect(routes.NCATSAuthentication.secured());
        }
        return ok(ix.ncats.views.html.login.render(url, Authentication.APP.get()));
    }

    public static Result logout() {
        Session session = Authentication.getSession();
        if (session != null) {
            flash("message", session.profile.user.username
                    + ", you've logged out!");
            Authentication.flush(session);
        }
        return redirect(routes.NCATSAuthentication.login(null));
    }
    
    @Security.Authenticated(Secured.class)
    @JsonIgnore
    public static Result secured() {
    	Session session = Authentication.getSession();
        if (session.expired || !session.profile.active) {
            flash("message", "Session timeout; please login again!");
            return redirect(routes.NCATSAuthentication.login(null));
        }
        
        String context = ConfigHelper.getOrDefault("application.context", "app");
        return redirect(context);
    }
}
