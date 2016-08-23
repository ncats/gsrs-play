package ix.core.controllers.v1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.Id;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import be.objectify.deadbolt.java.actions.Dynamic;
import ix.core.NamedResource;
import ix.core.NamedResourceFilter;
import ix.core.UserFetcher;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.models.Acl;
import ix.core.models.Group;
import ix.core.models.Namespace;
import ix.core.models.Principal;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.core.util.Java8Util;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.utils.EntityUtils;
import ix.utils.Global;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class RouteFactory extends Controller {
    private static final int MAX_POST_PAYLOAD = 1024*1024*10;
	static public Model.Finder<Long, Namespace> resFinder;
    static public Model.Finder<Long, Acl> aclFinder;
    static public Model.Finder<Long, Principal> palFinder;

    static final ConcurrentMap<String, Class> _registry = new ConcurrentHashMap<String, Class>();
    static final Set<String> _uuid = new TreeSet<String>();
    
    private static NamedResourceFilter resourceFilter=null;
    
    static{

        init();
    	
    }

    public static void init() {
        String resproc= Play.application().configuration().getString("ix.core.resourcefilter",null);

        if(resproc!=null){
            Class processorCls;
            try {
                processorCls = Class.forName(resproc);
                resourceFilter=(NamedResourceFilter) processorCls.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        resFinder =
                new Model.Finder(Long.class, Namespace.class);

        aclFinder =
                new Model.Finder(Long.class, Acl.class);

        palFinder =
                new Model.Finder(Long.class, Principal.class);


    }

    public static <T  extends EntityFactory> void register 
        (String context, Class<T> factory) {
        Class old = _registry.putIfAbsent(context, factory);
        if (old != null) {
            Logger.warn("Context \""+context
                        +"\" now maps to "+factory.getClass());
        }
        
        NamedResource named  = factory.getAnnotation(NamedResource.class);
        if (named != null) {
            try {
                Class cls = named.type();
                Field id = EntityUtils.getIdFieldForClass(cls);

                if (id == null) { // possible?
                    Logger.error("Fatal error: Entity "+cls.getName()
                                 +" for factory "+factory.getClass()
                                 +" doesn't have any Id annotation!");
                }
                else {
                    Class c = id.getType();
                    if (UUID.class.isAssignableFrom(c)) {
                        Logger.debug("## "+cls.getName()
                                     +" is globally unique!");
                        _uuid.add(context);
                    }
                }
            }
            catch (Exception ex) {
                Logger.error("Can't access named resource type", ex);
            }
        }
    }
    
   

    public static Result listResources () {
        Set<String> resources = new TreeSet<String>(_registry.keySet());
        List<String> urls = new ArrayList<String>();
        ObjectMapper mapper = new ObjectMapper();      
        ArrayNode nodes = mapper.createArrayNode();
        for (String res : resources) {
            ObjectNode n = mapper.createObjectNode();
            Class<?> cls=_registry.get(res);
            NamedResource named  = (NamedResource)cls.getAnnotation(NamedResource.class);
            if(resourceFilter!=null){
            	if(!resourceFilter.isVisible(cls)){
            		continue;
            	}
            }
            n.put("name", res);
            n.put("kind", named.type().getName());
            n.put("href", Global.getHost()+request().uri()+"/"+res);
            n.put("description", named.description());
            nodes.add(n);
        }

        return ok (nodes);
    }

    public static Result get (String ns, String resource) {
        Namespace res = resFinder
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
        return Java8Util.ok(mapper.valueToTree(res));
    }

    static Method getMethod (String context, 
                             String method, Class<?>... types) {
        Class factory = _registry.get(context);
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
        Logger.warn("Context {} has not method count()",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result search (String context, String q, 
                                 int top, int skip, int fdim) {
        Class factory = _registry.get(context);
        if (factory != null) {
            NamedResource res = 
                (NamedResource)factory.getAnnotation(NamedResource.class);
            return SearchFactory.search(res.type(), q, top, skip, fdim);
        }
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result get (String context, Long id, String expand) {
        try {
            Method m = getMethod (context, "get", Long.class, String.class);
            if (m != null)
                return (Result)m.invoke(null, id, expand);
        }catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method get(Long,String)",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result doc (String context, Long id) {
        try {
            Method m = getMethod (context, "doc", Long.class);
            if (m != null)
                return (Result)m.invoke(null, id);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method doc(Long)", context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_SEARCH, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getUUID (String context, String uuid, String expand) {
        try {
            Method m = getMethod (context, "get", UUID.class, String.class);
            if (m != null)
                return (Result)m.invoke(null, EntityFactory.toUUID(uuid),
                                        expand);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method get(UUID,String)",context);
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

    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result editsUUID (String context, String id) {
        try {
            Method m = getMethod (context, "edits", UUID.class);
            if (m != null)
                return (Result)m.invoke(null, EntityFactory.toUUID(id));
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method edits(UUID)",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }
    
    @Dynamic(value = IxDynamicResourceHandler.CAN_APPROVE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	public static Result approveUUID (String context, String id) {
    	try {
            Method m = getMethod (context, "approve", UUID.class);
            if (m != null)
                return (Result)m.invoke(null, EntityFactory.toUUID(id));
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method for approving",context);
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
        Logger.warn("Context {} has no method field(Long,String)",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result fieldUUID (String context, String uuid, String field) {
        try {
            Method m = getMethod (context, "field", UUID.class, String.class);
            if (m != null) {
                return (Result)m.invoke
                    (null, EntityFactory.toUUID(uuid), field);
            }
            else {
                Logger.error
                    ("Context \""+context
                     +"\" doesn't have method \"field(UUID, String)\"!");
            }
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method field(UUID,String)",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }
    
    public static Result page (String context, int top,
                               int skip, String filter) {
        
        try {
            Method m = getMethod (context, "page", 
                                  int.class, int.class, String.class);
            if (m != null)
                return (Result)m.invoke(null, top, skip, filter);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method page(int,int,String)",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
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
        Logger.warn("Context {} has no method create()",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_REGISTER, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result validate (String context) {
        try {
            Method m = getMethod (context, "validate"); 
            if (m != null)
                return (Result)m.invoke(null);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method validate()",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
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
        Logger.warn("Context {} has no method update(Long,String)",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result updateEntity (String context) {
        try {
            Method m = getMethod (context, "updateEntity");
            if (m != null)
                return (Result)m.invoke(null);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method updateEntity()",context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    @Dynamic(value = IxDynamicResourceHandler.CAN_UPDATE, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_POST_PAYLOAD)
    public static Result updateUUID (String context, String id, String field) {
        try {
            Method m = getMethod (context, "update", UUID.class, String.class);
            if (m != null)
                return (Result)m.invoke
                    (null, EntityFactory.toUUID(id), field);
        }
        catch (Exception ex) {
            Logger.trace("["+context+"]", ex);
            return internalServerError (context);
        }
        Logger.warn("Context {} has no method update(UUID,String)", context);
        return badRequest ("Unknown Context: \""+context+"\"");
    }

    public static Result _getUUID (String uuid, String expand) {
        for (String context : _uuid) {
            Result r = getUUID (context, uuid, expand);
            if (r.toScala().header().status() < 400)
                return r;
        }
        return notFound ("Unknown id "+uuid);
    }
    
    public static Result _fieldUUID (String uuid, String field) {
        for (String context : _uuid) {
            Result r = fieldUUID (context, uuid, field);
            if (r.toScala().header().status() < 400)
                return r;
        }
        return notFound ("Unknown id "+uuid);
    }
    public static Result checkPreFlight(String path) {
        response().setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
        response().setHeader("Access-Control-Allow-Methods", "POST, PUT, GET");   // Only allow POST
        response().setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");         // Ensure this header is also allowed!  
        return ok();
    }
    
    
    
    
    public static Result profile(){
    	UserProfile p=UserFetcher.getActingUserProfile(false);
    	if(p!=null){
    		ObjectMapper om=new ObjectMapper();
        	return Java8Util.ok(om.valueToTree(p));
    	}
    	return notFound("No user logged in");
    }
    
    public static Result profileResetKey(){
    	UserProfile p=UserFetcher.getActingUserProfile(false);
    	if(p!=null){
    		p.regenerateKey();
    		p.save();
    		ObjectMapper om=new ObjectMapper();
        	return Java8Util.ok(om.valueToTree(p));
    	}
    	return notFound("No user logged in");
    }
    
    private static JsonNode getError(Throwable t, int status){
    	Map m=new HashMap();
    	m.put("message", t.getMessage());
    	m.put("status", status);
    	ObjectMapper om = new ObjectMapper();
    	return om.valueToTree(m);
    }
    
    public static Result _apiBadRequest(Throwable t){
    	return badRequest(getError(t, play.mvc.Http.Status.BAD_REQUEST));
    }
    public static Result _apiInternalServerError(Throwable t){
    	return internalServerError(getError(t, play.mvc.Http.Status.INTERNAL_SERVER_ERROR));
    }
    public static Result _apiUnauthorized(Throwable t){
    	return internalServerError(getError(t, play.mvc.Http.Status.UNAUTHORIZED));
    }
    
    

}
