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
import play.cache.Cache;
import play.libs.Akka;
import play.db.ebean.Model;
import play.db.ebean.Transactional;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import tripod.chem.indexer.StructureIndexer;

import ix.core.plugins.IxContext;
import ix.core.models.XRef;
import ix.core.models.Payload;
import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.models.Structure;
import ix.core.controllers.PayloadFactory;
import ix.core.controllers.ProcessingJobFactory;
import ix.utils.Util;

public class StructureIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private ActorSystem system;
    private StructureIndexer indexer;

    public StructureIndexerPlugin (Application app) {
        this.app = app;
    }

    @Override
    public void onStart () {
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        
        system = ActorSystem.create("StructureIndexer");
        Logger.info("Plugin "+getClass().getName()
                    +" initialized; Akka version "+system.Version());

        try {
            indexer = new StructureIndexer (ctx.structure());
        }
        catch (IOException ex) {
            Logger.trace("Can't initialize structure indexer", ex);
        }
    }

    @Override
    public void onStop () {
        if (indexer != null)
            indexer.shutdown();
        if (system != null)
            system.shutdown();
        Logger.info("Plugin "+getClass().getName()+" stopped!");
    }

    public boolean enabled () { return true; }
}
