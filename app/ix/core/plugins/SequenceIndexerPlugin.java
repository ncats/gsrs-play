package ix.core.plugins;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

import play.Logger;
import play.Plugin;
import play.Application;

import ix.seqaln.SequenceIndexer;

public class SequenceIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private SequenceIndexer indexer;

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
            throw new RuntimeException
                ("Can't initialize sequence indexer", ex);
        }
    }

    @Override
    public void onStop () {
        if (indexer != null)
            indexer.shutdown();
        Logger.info("Plugin "+getClass().getName()+" stopped!");
    }

    public boolean enabled () { return true; }
    public SequenceIndexer getIndexer () { return indexer; }
}
