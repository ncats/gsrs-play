package ix.utils;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;

import play.GlobalSettings;
import play.Application;
import play.Logger;
import play.db.DB;
import play.libs.Json;
import play.mvc.Http;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Ebean;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import javax.persistence.Entity;

import ix.core.search.TextIndexer;
import ix.core.NamedResource;
import ix.core.controllers.RouteFactory;

import org.reflections.Reflections;


public class Global extends GlobalSettings {
    public static final String PROPS_HOME = "inxight.home";
    public static final String PROPS_DEBUG = "inxight.debug";

    static Global instance;
    public static Global getInstance () {
        return instance;
    }

    private File home = new File (".");
    private TextIndexer textIndexer;
    private int debug;

    // lookup of class name to resource
    private Map<String, String> names = new TreeMap<String, String>();
    private Set<Class<?>> resources;
    private String context;
    private String api;

    protected void init (Application app) throws Exception {
        String h = app.configuration().getString(PROPS_HOME);
        if (h != null) {
            home = new File (h);
            if (!home.exists())
                home.mkdirs();
        }

        if (!home.exists())
            throw new IllegalArgumentException
                (PROPS_HOME+" \""+h+"\" is not accessible!");
        Logger.info("## "+PROPS_HOME+": \""+home.getCanonicalPath()+"\"");

        Integer level = app.configuration().getInt(PROPS_DEBUG);
        if (level != null)
            this.debug = level;
        Logger.info("## "+PROPS_DEBUG+": "+debug); 

        textIndexer = TextIndexer.getInstance(home);
        
        DatabaseMetaData meta = DB.getConnection().getMetaData();
        Logger.info("## Database vendor: "+meta.getDatabaseProductName()
                    +" "+meta.getDatabaseProductVersion());

        context = app.configuration().getString("application.context");
        if (context == null) {
            context = "";
        }
        else {
            int pos = context.length();
            while (--pos > 0 && context.charAt(pos) == '/')
                ;
            context = context.substring(0, pos+1);
        }
        Logger.info("## Application context: "+context);

        api = app.configuration().getString("application.api");
        if (api == null) {
            api = "/api";
        }
        Logger.info("## Application api context: {context}"+api);

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
        if (instance == null) {
            try {
                init (app);
            }
            catch (Exception ex) {
                Logger.trace("Can't initialize app!", ex);
            }

            Logger.info("Global instance "+this);
            instance = this;
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
            Logger.info("+ "+c.getName()+"\n  => "+context+"/"+res.name()
                        +"["+res.type().getName()+"]");
            names.put(res.type().getName(), res.name());
            RouteFactory.register(res.name(), c);
        }

        /*
        Logger.info("## starting app: secret=\""
                    +app.configuration().getString("application.secret")+"\"");
        */
    }

    @Override
    public void onStop (Application app) {
        Logger.info("## stopping");
        if (textIndexer != null)
            textIndexer.shutdown();
    }

    /*
    @Override
    public play.api.mvc.Handler onRouteRequest (Http.RequestHeader req) {
        Logger.debug("route: path="+req.path()+" method="+req.method());
        return super.onRouteRequest(req);
    }
    */

    public TextIndexer getTextIndexer () { return textIndexer; }
    public boolean debug (int level) { 
        return debug >= level; 
    }

    public static boolean DEBUG (int level) {
        return getInstance().debug(level);
    }

    public static String getResource (String type) {
        return getInstance().context+"/"+getInstance().names.get(type);
    }

    public static String getRef (Object instance) {
        Global g = getInstance ();
        Class cls = instance.getClass();
        if (null == cls.getAnnotation(Entity.class))
            throw new IllegalArgumentException ("Instance is not an Entity");

        String name = g.names.get(cls.getName());
        if (name == null)
            throw new IllegalArgumentException
                ("Class "+cls.getName()+" isn't a NamedResource!");

        try {
            Method m = cls.getMethod("getId");
            if (m == null)
                throw new IllegalArgumentException
                    ("Entity type does not have getId method!");
            Object id = m.invoke(instance);
            return g.context+g.api+"/"+name+"("+id+")";
        }
        catch (Exception ex) {
            Logger.trace("Unable to invoke getId", ex);
            throw new IllegalArgumentException (ex);
        }
    }

    public static String getRef (String type, long id) {
        Global g = getInstance ();
        String name = g.names.get(type);
        if (name == null)
            throw new IllegalArgumentException
                ("Class "+type+" isn't a NamedResource!");
        return g.context+g.api+"/"+name+"("+id+")";
    }
}
