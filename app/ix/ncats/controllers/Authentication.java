package ix.ncats.controllers;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import com.avaje.ebean.*;

import ix.ncats.models.Employee;

/**
 * A simple controller to authenticate via ldap
 */
public class Authentication extends Controller {
    public static Result authenticate () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");
        Logger.debug("username: "+username);

        Employee empl = NIHLdapConnector.getEmployee(username, password);
        if (empl != null) {
            session().clear();
            session ("username", empl.username);
            return redirect (routes.Authentication.index());
        }
        return ok (ix.ncats.views.html.errordef.render
                   (401, "User \""+username+"\" not authorized!"));
    }
    
    public static Result login () {
        return ok (ix.ncats.views.html.login.render());
    }

    public static Result logout () {
        session().clear();
        flash ("logout", "You've been logged out!");
        return redirect (routes.Authentication.login());
    }

    @Security.Authenticated(Secured.class)
    public static Result index () {
        return ok ("You've reached a secure area!");
    }
}
