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

    static final ConcurrentMap<String, Class> registry = 
        new ConcurrentHashMap<String, Class>();

    public static <T  extends EntityFactory> void register 
        (String context, Class<T> factory) {
        Class old = registry.putIfAbsent(context, factory);
        if (old != null) {
            Logger.warn("Context \""+context
                        +"\" now maps to "+factory.getClass());
        }
    }

    public static Result listResources () {
        Set<String> resources = new TreeSet<String>(registry.keySet());
        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(resources));
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

    static Method getMethod (String context, 
                             String method, Class<?>... types) {
        Class factory = registry.get(context);
        if (factory != null) {
            try {
                return factory.getMethod(method, types);
            }
            catch (Exception ex) {
                Logger.trace("Unknown method \""+method
                             +"\" in class "+factory.getClass(), ex);
            }
        }
        return null;
    }

    
    public static Result count (String context) {
        try {
            Method m = getMethod (context, "count");
            if (m != null) 
                return (Result)m.invoke(null);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.debug("Unknown context: "+context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result get (String context, Long id, String expand) {
        try {
            Method m = getMethod (context, "get", Long.class, String.class);
            if (m != null)
                return (Result)m.invoke(null, id, expand);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.debug("Unknown context: "+context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result edits (String context, Long id) {
        try {
            Method m = getMethod (context, "edits", Long.class);
            if (m != null)
                return (Result)m.invoke(null, id);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.debug("Unknown context: "+context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result field (String context, Long id, String field) {
        try {
            Method m = getMethod (context, "field", Long.class, String.class);
            if (m != null)
                return (Result)m.invoke(null, id, field);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.debug("Unknown context: "+context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result page (String context, int top, int skip, 
                               String expand, String filter) {
        try {
            Method m = getMethod (context, "page", 
                                  int.class, int.class, 
                                  String.class, String.class);
            if (m != null)
                return (Result)m.invoke(null, top, skip, expand, filter);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.debug("Unknown context: "+context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result create (String context) {
        try {
            Method m = getMethod (context, "create"); 
            if (m != null)
                return (Result)m.invoke(null);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.debug("Unknown context: "+context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result update (String context, Long id, String field) {
        try {
            Method m = getMethod (context, "update", Long.class, String.class);
            if (m != null)
                return (Result)m.invoke(null, id, field);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.debug("Unknown context: "+context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }
}
