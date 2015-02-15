package ix.core.plugins;

import java.io.IOException;

import play.Logger;
import play.Plugin;
import play.Application;

import ix.core.search.TextIndexer;

public class TextIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private TextIndexer indexer;

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
        }
    }

    public void onStop () {
        if (indexer != null) {
            indexer.shutdown();
            Logger.info("Plugin "+getClass().getName()+" stopped!");
        }
    }

    public boolean enabled () { return true; }
    public TextIndexer getIndexer () { return indexer; }
}
