package ix.core.plugins;

import java.io.IOException;

import play.Application;
import play.Logger;
import play.Plugin;
import tripod.chem.indexer.StructureIndexer;

public class StructureIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private StructureIndexer indexer;
    private boolean closed=false;

    public StructureIndexerPlugin (Application app) {
        this.app = app;
    }

    @Override
    public void onStart () {
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        
        try {
            indexer = StructureIndexer.open(ctx.structure());
            closed=false;
            Logger.info("Plugin "+getClass().getName()+" started!");
        }
        catch (IOException ex) {
            throw new RuntimeException
                ("Can't initialize structure indexer", ex);
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
    public StructureIndexer getIndexer () { return indexer; }

}
