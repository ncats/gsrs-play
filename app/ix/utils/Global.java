package ix.utils;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import play.GlobalSettings;
import play.Application;
import play.Logger;
import play.db.ebean.EbeanPlugin;
import play.db.DB;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Controller;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import javax.persistence.Entity;

import ix.core.plugins.IxContext;
import ix.core.NamedResource;
import ix.core.controllers.v1.RouteFactory;

import org.reflections.Reflections;


public class Global extends GlobalSettings {
    static Global _instance;
    public static Global getInstance () {
        return _instance;
    }

    // lookup of class name to resource
    private Map<String, String> names = new TreeMap<String, String>();
    private Set<Class<?>> resources;
    private IxContext ctx;

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
            Logger.info("Global instance "+this);
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

    @Override
    public void onStop (Application app) {
        Logger.info("## stopping");
    }

    /*
    @Override
    public play.api.mvc.Handler onRouteRequest (Http.RequestHeader req) {
        Logger.debug("route: path="+req.path()+" method="+req.method());
        return super.onRouteRequest(req);
    }
    */

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
        
        if (name != null) {
            // climb up the inheritance ladder to find the first matches
            for (Class c = cls.getSuperclass(); 
                 name == null; c = c.getSuperclass()) {
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
            Logger.warn("Class "+cls.getName()+" isn't a NamedResource!");
            /*
            throw new IllegalArgumentException
                ("Class "+cls.getName()+" isn't a NamedResource!");
            */
            return null;
        }

        try {
            /*
            Method m = cls.getMethod("getId");
            if (m == null) {
                Logger.error("Entity doesn't have getId: "+instance);
                throw new IllegalArgumentException
                    ("Entity type does not have getId method!");
            }
            Object id = m.invoke(instance);
            */
            Field f = cls.getField("id");
            if (f == null) {
                Logger.trace("Entity doesn't have id field: "+instance);
                throw new IllegalArgumentException
                    ("Entity type does not have id field!");
            }
            Object id = f.get(instance);
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
}
