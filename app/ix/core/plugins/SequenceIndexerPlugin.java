package ix.core.plugins;

import java.io.IOException;

import ix.seqaln.SequenceIndexer;
import play.Application;
import play.Logger;
import play.Plugin;

public class SequenceIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private SequenceIndexer indexer;
    private boolean closed=false;

    public SequenceIndexerPlugin (Application app) {
        this.app = app;
    }

    @Override
    public void onStart () {
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        
        try {
            indexer = SequenceIndexer.open(ctx.sequence());
            Logger.info("Plugin "+getClass().getName()+" started!");        
        }
        catch (IOException ex) {
        	System.out.println("Error loading sequence indexer");
        	ex.printStackTrace();
            throw new RuntimeException
                ("Can't initialize sequence indexer", ex);
        }
    }

    @Override
    public void onStop () {
        if (indexer != null)
            indexer.shutdown();
        Logger.info("Plugin "+getClass().getName()+" stopped!");
        closed=true;
    }

    public boolean enabled () { return !closed; }
    public SequenceIndexer getIndexer () { return indexer; }
}
