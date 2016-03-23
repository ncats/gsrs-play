package ix.core.plugins;

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

    public TextIndexerPlugin (Application app) {
        this.app = app;
    }

    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        try {
            indexer = TextIndexer.getInstance(ctx.text());
        }
        catch (IOException ex) {
            Logger.trace("Can't initialize text indexer", ex);
            ex.printStackTrace();
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
    }
    

    public boolean enabled () { return !closed; }
    public TextIndexer getIndexer () { return indexer; }
}
