package ix.core.plugins;

import java.io.File;
import java.io.IOException;

import play.Logger;
import play.Play;
import play.Plugin;
import play.Application;

import ix.core.search.TextIndexer;

public class TextIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private TextIndexer indexer;
    private boolean closed=false;
    private static int initCount=0;

    public TextIndexerPlugin (Application app) {
        this.app = app;
    }

    public void onStart () {
    	initCount++;
        Logger.info("Loading plugin "+getClass().getName()+"...");
        ctx = app.plugin(IxContext.class);
        if (ctx == null){
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        }
        try {
        	File storage=ctx.text();
        	
        	//Sometimes tests may hold on to folders they shouldn't
        	//Here, we side-step the issue by changing the directory
        	if(Play.isTest()){
        		String newStorage=storage.getAbsolutePath() + initCount;
        		Logger.info("Making new text index folder for test:" + newStorage);
        		storage = new File(newStorage);
        		storage.mkdirs();
        	}
            indexer = TextIndexer.getInstance(storage);
        }
        catch (IOException ex) {
            Logger.trace("Can't initialize text indexer", ex);
        }
    }

    public void onStop () {
        //We don't want to shutdown during testing
        //because the indexes get messed up
        //TODO find root cause of this issue
       // if (indexer != null && !Play.isTest()) {
        if (indexer != null) {
            indexer.shutdown();
            Logger.info("Plugin " + getClass().getName() + " stopped!");
        }
        

        closed=true;
        indexer=null;
    }
    

    public boolean enabled () { return !closed; }
    public TextIndexer getIndexer () { return indexer; }
}
