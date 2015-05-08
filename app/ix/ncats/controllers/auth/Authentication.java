package ix.ncats.controllers.auth;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;

import ix.core.models.Principal;
import ix.ncats.models.Employee;
import ix.ncats.controllers.NIHLdapConnector;

/**
 * A simple controller to authenticate via ldap
 */
public class Authentication extends Controller {
    public static Result authenticate (String url) {
        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");
        Logger.debug("username: "+username);

        /*
        Principal cred = NIHLdapConnector.getEmployee(username, password);
        if (cred != null) {
            session().clear();
            session ("username", cred.username);
            if (url != null) {
                return redirect (url);
            }
            return ok ("User \""+username+"\" is successfully authenticated!");
            }*/
        if (username.equalsIgnoreCase("caodac") && password.equalsIgnoreCase("foobar")) {
            session().clear();
            session ("username", username);
            return redirect (routes.Authentication.logout());
        }
        flash ("message", "Invalid credential!");
        return redirect (routes.Authentication.login(null));
    }
    
    public static Result login (String url) {
        return ok (ix.ncats.views.html.login.render(url, "MyApp"));
    }

    public static Result logout () {
        flash ("message", session ("username")+", you've logged out!");
        session().clear();
        return redirect (routes.Authentication.login(null));
    }

    @Security.Authenticated(Secured.class)
    public static Result index () {
        return ok (session ("username")+", you've reached a secure area!");
    }
}
