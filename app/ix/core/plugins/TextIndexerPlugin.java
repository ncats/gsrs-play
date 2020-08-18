package ix.core.plugins;

import java.io.File;
import java.io.IOException;

import ix.core.search.text.IndexerServiceFactory;
import ix.core.search.text.Lucene4IndexServiceFactory;
import ix.core.util.ConfigHelper;
import org.apache.lucene.store.AlreadyClosedException;

import ix.core.search.text.TextIndexer;
import play.Logger;
import play.Play;
import play.Plugin;
import play.Application;

public class TextIndexerPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private TextIndexer indexer;
    private boolean closed=false;
    private static int initCount=0;

    private static boolean updateStoarageCount = true;
    private static IndexerServiceFactory indexerServiceFactory;
    private static File storageDir;

    public TextIndexerPlugin (Application app) {
        this.app = app;
    }



    public static synchronized File getStorageRootDir(){
        return storageDir;
    }
    public synchronized void onStart () {

        Logger.info("Loading plugin "+getClass().getName()+"...");
        ctx = app.plugin(IxContext.class);
        if (ctx == null){
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        }
        //Sometimes tests may hold on to folders they shouldn't
        //Here, we side-step the issue by changing the directory
        if(updateStoarageCount){
            initCount++;
        }
        //always update the storage count from now
        //on because it will usually be a restart
        updateStoarageCount=true;
        try {
            storageDir = getStorageDir(ctx);
            //we have to use reflection here be because GSRSDependency is in ginas module!
            Class<IndexerServiceFactory> factory = ConfigHelper.getClass("gsrs.textIndexer.serviceFactory");

                                                            ;

            indexerServiceFactory = factory.newInstance();
            indexer = TextIndexer.getInstance(storageDir, indexerServiceFactory.createForDir(storageDir));

            if(indexer==null){
            	throw new IllegalStateException("Trouble getting textindexer");
            }
        }
        catch (Exception ex) {
            Logger.trace("Can't initialize text indexer", ex);
        }
    }

    public static IndexerServiceFactory getIndexerServiceFactory(){
        return indexerServiceFactory;
    }

    private static synchronized File getStorageDir(IxContext ctx){
        File storage=ctx.text();

        if(Play.isTest()){
            String newStorage=storage.getAbsolutePath() + initCount;

            Logger.info("Making new text index folder for test:" + newStorage);
            storage = new File(newStorage);
            storage.mkdirs();

        }
        return storage;
    }

    public static synchronized void prepareTestRestart(){
        updateStoarageCount = false;
    }

    public synchronized void onStop () {
        //We don't want to shutdown during testing
        //because the indexes get messed up
        //TODO find root cause of this issue
       // if (indexer != null && !Play.isTest()) {
        if (indexer != null) {

            if(Play.isTest()){
                try{
                    indexer.deleteAll();
                }catch(AlreadyClosedException e){
                    //ignore?
                }
            }
            indexer.shutdown();
            Logger.info("Plugin " + getClass().getName() + " stopped!");
        }
        
        closed=true;
        indexer=null;
    }
    

    public synchronized boolean enabled () { return !closed; }
    
    public synchronized TextIndexer getIndexer () { 
    	return indexer; 
    }


}
