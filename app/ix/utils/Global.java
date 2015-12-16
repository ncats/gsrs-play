package ix.utils;

import ix.core.NamedResource;
import ix.core.controllers.v1.RouteFactory;
import ix.core.plugins.IxContext;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.reflections.Reflections;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import scala.collection.JavaConverters;

public class Global extends GlobalSettings {
    static final Logger.ALogger AccessLogger = Logger.of("access");
    public static final Logger.ALogger PersistFailLogger = Logger.of("persistFail");
    public static final Logger.ALogger TransformFailLogger = Logger.of("transformFail");
    
    
    
    static Global _instance;
    public static Global getInstance () {
        return _instance;
    }
   
    // lookup of class name to resource
    private Map<String, String> names = new TreeMap<String, String>();
    private Set<Class<?>> resources;
    private IxContext ctx;

    public static Date epoch;

    protected void init (Application app) throws Exception {
        ctx = app.plugin(IxContext.class);
        if (ctx == null) {
            throw new IllegalStateException
                ("Plugin "+IxContext.class.getName()+" is not loaded!");
        }

        /*
        ServerConfig config = new ServerConfig ();
        config.setName("archive");
        config.setDataSource(DB.getDataSource("archive"));
        config.addPackage("models.*");
        config.setDdlGenerate(true);
        config.setDdlRun(true);

        archiveEbeanServer = EbeanServerFactory.create(config);
        Logger.info("## EbeanServer['archive'] = "+archiveEbeanServer);
        */
    }

  
    
    @Override
    public void onStart (Application app) {
        try {
            init (app);
            epoch = new Date ();
            Logger.info("Global instance "+this+" started at "+epoch);
            _instance = this;
        }
        catch (Exception ex) {
            Logger.trace("Can't initialize app!", ex);
        }
        
        /**
         * default/global entities factory
         */
        Reflections reflections = new Reflections ("ix");
        resources = reflections.getTypesAnnotatedWith(NamedResource.class);

        Logger.info(resources.size()+" named resources...");
        for (Class c : resources) {
            NamedResource res = 
                (NamedResource)c.getAnnotation(NamedResource.class);
            Logger.info("+ "+c.getName()+"\n  => "
                        +ctx.context()+ctx.api()+"/"+res.name()
                        +"["+res.type().getName()+"]");
            names.put(res.type().getName(), res.name());
            RouteFactory.register(res.name(), c);
        }

        //Logger.debug("IDG routes: "+ix.idg.Routes.routes().getClass());
        /*
        EbeanPlugin eb = new EbeanPlugin (app);
        EbeanServer server = Ebean.getServer(eb.defaultServer());
        SqlQuery query = 
            server.createSqlQuery("select distinct term from ix_core_value");
        List<SqlRow> rows = query.findList();
        for (SqlRow r : rows) {
            Logger.info(r.getString("term"));
        }
        */

        /*
        Logger.info("## starting app: secret=\""
                    +app.configuration().getString("application.secret")+"\"");
        */
    }

    void dumpRoute () {
        Collection list = JavaConverters.asJavaCollectionConverter
            (play.api.Play.current().routes().toList()).asJavaCollection();
        for (Object l : list) {
            ix.Routes$ route = (ix.Routes$)l;
            String prefix = route.prefix();
            Logger.debug("PREFIX: "+prefix);
            for (java.lang.reflect.Method m : route.getClass().getMethods()) {
                if (m.getName().indexOf("_Routes") > 0) {
                    try {
                        Object obj = m.invoke(route);
                        Logger.debug("++ "+m+" => "+obj+" ["+obj.getClass()+"]");
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                //Logger.debug("+"+m);
            }

            if (prefix.length() > 0) {
                // now check to see any of the paths matches with {prefix}, if
                // so then we contract /{prefix}/{prefix}/{path}
                // to /{prefix}/{path}

                // method, url, action
                List<scala.Tuple3<String,String,String>> seq =
                    JavaConverters.seqAsJavaListConverter
                    (route.documentation()).asJava();
                //Logger.debug("route: "+seq);
                for (scala.Tuple3 t : seq) {
                    // skip the /{prefix} prefix
                    Logger.debug(" .."+t._1()+" \""+t._2()+"\" "+t._3());
                    String path = t._2().toString().substring(prefix.length());
                    if (path.startsWith(prefix)) {
                        Logger.debug(path+" => "+t._2());
                    }
                }
            }
        }
    }
    
    @Override
    public void onStop (Application app) {
        Logger.info("## stopping");
    }

    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{GzipFilter.class,julienrf.play.jsonp.JsonpJava.class};
    }  
    
    @Override
    public play.api.mvc.Handler onRouteRequest (Http.RequestHeader req) {
        
    	if(Play.application().configuration()
			.getBoolean("ix.ginas.debug.showheaders", false)){
	    	Logger.debug("HEADERS ON REQUEST ===================");
	    	String allheaders="";
	    	for(String head:req.headers().keySet()){
	    		allheaders+=head + "\t" + req.getHeader(head) + "\n";
	    		
	    	}
	    	Logger.debug(allheaders);
    	}
    	    	
        String real = req.getHeader("X-Real-IP");
        play.api.mvc.Handler h = super.onRouteRequest(req);
        
        AccessLogger.info("{} {} {} \"{}\"", req.remoteAddress(),
                          real != null ? real : "", req.method(), req.uri());
        return h;
    }

    public boolean debug (int level) { 
        return ctx.debug(level); 
    }

    public IxContext context () { return ctx; }

    public static IxContext getContext () {
        return getInstance().context();
    }

    /**
     * Return the registered NamedResource given a class name
     */
    public static String getResource (Class<?> cls) {
        Global g = getInstance ();
        String name = g.names.get(cls.getName());
        
        if (name == null) {
            // climb up the inheritance ladder to find the first matches
            for (Class c = cls.getSuperclass(); 
                 name == null && c != null; c = c.getSuperclass()) {
                name = g.names.get(c.getName());
                //Logger.debug(c.getName() +" => "+name);
            }
        }

        return name;
    }

    public static boolean DEBUG (int level) {
        return getInstance().debug(level);
    }

    public static String getRef (Object instance) {
        if (instance == null)
            return null;

        Global g = getInstance ();
        Class cls = instance.getClass();
        if (null == cls.getAnnotation(Entity.class)) {
            Logger.error(instance+" isn't an Entity!");
            throw new IllegalArgumentException ("Instance is not an Entity");
        }

        String name = getResource (cls);
        if (name == null) {
            //Logger.warn("Class "+cls.getName()+" isn't a NamedResource!");
            /*
            throw new IllegalArgumentException
                ("Class "+cls.getName()+" isn't a NamedResource!");
            */
            return null;
        }

        try {
            Field fid = null;
            for (Field f : cls.getFields()) {
                if (f.getAnnotation(Id.class) != null) {
                    fid = f;
                    break;
                }
            }
            
            if (fid == null) {
                Logger.trace("Entity doesn't have id field: "+instance);
                throw new IllegalArgumentException
                    ("Entity type does not have id field!");
            }
            Object id = fid.get(instance);
            return getNamespace()+"/"+name+"("+id+")";
        }
        catch (Exception ex) {
            Logger.trace("Unable to invoke getId", ex);
            throw new IllegalArgumentException (ex);
        }
    }

    public static String getHost () {
        try {
            Http.Request req = Controller.request();
            String h = _instance.ctx.host();
            if (h == null) {
                h = (req.secure()? "https":"http") + "://"+req.host();
            }
            return h;
        }
        catch (Exception ex) {
            // no available http context..
            Logger.trace("No available HTTP context!", ex);
        }
        return "";
    }
    
    public static String getNamespace () {
        return getHost ()+_instance.ctx.context()+_instance.ctx.api();
    }

    public static String getRef (Class<?> type, Object id) {
        Global g = getInstance ();
        try {
            String name = g.getResource(type);
            if (name == null)
                throw new IllegalArgumentException
                    ("Class "+type+" isn't a NamedResource!");
            return getNamespace()+"/"+name+"("+id+")";
        }
        catch (Exception ex) {
            throw new IllegalArgumentException (ex);
        }
    }
    
    public static String getRef (String type, Object id) {
        Global g = getInstance ();
        try {
            String name = g.getResource(Class.forName(type));
            if (name == null)
                throw new IllegalArgumentException
                    ("Class "+type+" isn't a NamedResource!");
            return getNamespace()+"/"+name+"("+id+")";
        }
        catch (Exception ex) {
            throw new IllegalArgumentException (ex);
        }
    }
 // For CORS
    private class ActionWrapper extends Action.Simple {
    public ActionWrapper(Action<?> action) {
    this.delegate = action;
    }

    @Override
    public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
    Promise<Result> result = this.delegate.call(ctx);
    Http.Response response = ctx.response();
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET");   // Only allow POST
    response.setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
    response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");         // Ensure this header is also allowed!  
    
    return result;
    }
    }

    @Override
    public Action<?> onRequest(Http.Request request,
    java.lang.reflect.Method actionMethod) {
    return new ActionWrapper(super.onRequest(request, actionMethod));
    }
    
    @Override
    public Promise<Result> onHandlerNotFound(RequestHeader request) {
        if(!request.path().endsWith("/"))return super.onHandlerNotFound(request);
        //return Promise.<Result>((request.path().substring(0, request.path().length()-1));
        return Promise.<Result>pure(Controller.movedPermanently(request.path().substring(0, request.path().length()-1)));
    }
    
}
