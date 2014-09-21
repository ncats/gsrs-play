package crosstalk.core.controllers;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.avaje.ebean.*;
import com.avaje.ebean.event.BeanPersistListener;

import crosstalk.core.models.*;

public class RouteFactory extends Controller {
    static final public Model.Finder<Long, Resource> resFinder = 
        new Model.Finder(Long.class, Resource.class);
    static final public Model.Finder<Long, Acl> aclFinder = 
        new Model.Finder(Long.class, Acl.class);
    static final public Model.Finder<Long, Principal> palFinder =
        new Model.Finder(Long.class, Principal.class);

    public static void register (String resource) {
    }

    public static Result get (String ns, String resource) {
        Resource res = resFinder
            .where(Expr.eq("name", ns))
            .findUnique();
        if (res == null) {
            return badRequest ("No such namespace: "+ns);
        }
        
        // now see if this request has proper permission
        if (res.isPublic()) { // ok
        }
        else {
            return forbidden ("You don't have permission to access resource!");
        }
                  
        ObjectMapper mapper = new ObjectMapper ();
        return ok(mapper.valueToTree(res));
    }
}
