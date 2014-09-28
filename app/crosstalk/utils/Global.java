package crosstalk.utils;

import java.io.*;
import java.util.*;

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

import crosstalk.core.search.TextIndexer;
import crosstalk.core.controllers.*;
import crosstalk.ncats.controllers.*;

public class Global extends GlobalSettings {

    static Global instance;
    public static Global getInstance () {
        return instance;
    }

    private File home = new File (".");
    private TextIndexer textIndexer;
    private int debug;

    protected void init (Application app) throws Exception {
        String h = app.configuration().getString("crosstalk.home");
        if (h != null) {
            home = new File (h);
            if (!home.exists())
                home.mkdirs();
        }

        Integer level = app.configuration().getInt("crosstalk.debug");
        if (level != null)
            this.debug = level;
        Logger.info("## crosstalk.debug="+debug); 

        if (!home.exists())
            throw new IllegalArgumentException
                ("crosstalk.home \""+h+"\" is not accessible!");

        Logger.info("## home: \""+home.getCanonicalPath()+"\"");
        textIndexer = TextIndexer.getInstance(home);

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
        RouteFactory.register("keywords", KeywordFactory.class);
        RouteFactory.register("publications", PublicationFactory.class);
        RouteFactory.register("organizations", OrganizationFactory.class);
        RouteFactory.register("investigators", InvestigatorFactory.class);
        RouteFactory.register("grants", GrantFactory.class);
        RouteFactory.register("projects", ProjectFactory.class);
        RouteFactory.register("mesh", MeshFactory.class);

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
}
