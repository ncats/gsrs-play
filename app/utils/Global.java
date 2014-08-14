package utils;

import java.io.*;
import java.util.*;

import play.GlobalSettings;
import play.Application;
import play.Logger;

import search.TextIndexer;

public class Global extends GlobalSettings {

    static Global instance;
    public static Global getInstance () {
        return instance;
    }

    private File home = new File (".");
    private TextIndexer textIndexer;

    protected void init (Application app) throws Exception {
        String h = app.configuration().getString("crosstalk.home");
        if (h != null) {
            home = new File (h);
            if (!home.exists())
                home.mkdirs();
        }

        if (!home.exists())
            throw new IllegalArgumentException
                ("crosstalk.home \""+h+"\" is not accessible!");

        Logger.info("## home: \""+home.getCanonicalPath()+"\"");
        textIndexer = TextIndexer.getInstance(home);
    }

    @Override
    public void onStart (Application app) {
        if (instance == null) {
            try {
                init (app);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

            Logger.info("Global instance "+this);
            instance = this;
        }
        /*
        Logger.info("## starting app: secret=\""
                    +app.configuration().getString("application.secret")+"\"");
        */
    }

    @Override
    public void onStop (Application app) {
        Logger.info("## stopping");
        textIndexer.shutdown();
    }

    public TextIndexer getTextIndexer () { return textIndexer; }
}
