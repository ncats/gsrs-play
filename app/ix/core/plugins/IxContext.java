package ix.core.plugins;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;

import play.Logger;
import play.Plugin;
import play.Application;
import play.db.DB;

public class IxContext extends Plugin {
    public static final String PROPS_NS = "ix";
    public static final String PROPS_HOME = PROPS_NS+".home";
    public static final String PROPS_DEBUG = PROPS_NS+".debug";

    private final Application app;
    private File home = new File (".");
    private int debug;
    private String context;
    private String api;
    private String host;

    public IxContext (Application app) {
        this.app = app;
    }

    private void init () throws Exception {
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

        DatabaseMetaData meta = DB.getConnection().getMetaData();
        Logger.info("## Database vendor: "+meta.getDatabaseProductName()
                    +" "+meta.getDatabaseProductVersion());

        host = app.configuration().getString("application.host");
        if (host == null || host.length() == 0) {
            host = null;
        }
        else {
            int pos = host.length();
            while (--pos > 0 && host.charAt(pos) == '/')
                ;
            host = host.substring(0, pos+1);
        }
        Logger.info("## Application host: "+host);

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
        if (api == null)
            api = "/api";
        else if (api.charAt(0) != '/')
            api = "/"+api;
        Logger.info("## Application api context: "
                    +((host != null ? host : "") + context+api));
    }

    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");        
        try {
            init ();
        }
        catch (Exception ex) {
            Logger.trace("Can't initialize app", ex);
        }
    }

    public void onStop () {
        Logger.info("Stopping plugin "+getClass().getName());
    }

    public boolean enabled () { return true; }
    public File home () { return home; }
    public boolean debug (int level) { return debug >= level; }
    public String context () { return context; }
    public String api () { return api; }
    public String host () { return host; }
}
