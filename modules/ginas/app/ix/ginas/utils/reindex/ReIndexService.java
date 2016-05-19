package ix.ginas.utils.reindex;

import com.avaje.ebean.QueryIterator;
import com.typesafe.config.ConfigUtil;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.models.BackupEntity;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.util.BlockingSubmitExecutor;
import ix.core.util.CloseableIterator;
import ix.core.util.IOUtil;
import play.Play;
import play.db.ebean.Model;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by katzelda on 5/16/16.
 */
public class ReIndexService {

    Model.Finder<Long,BackupEntity> finder = new Model.Finder(Long.class, BackupEntity.class);

    private final ExecutorService executor;

    private static boolean DELETE_FIRST = true;
    private static boolean DO_NOT_DELETE_FIRST = false;

    public ReIndexService(int numThread, int capacityPerThread) {
        this.executor = BlockingSubmitExecutor.newFixedThreadPool(numThread, capacityPerThread);
    }

    public void reindex(ReindexQuery query, ReIndexListener listener)throws IOException{
        try(CloseableIterator<BackupEntity> iter = query.query(listener)){
            while (iter.hasNext()) {
                BackupEntity o = iter.next();

                //this temp variable is because we have to
                //make sure the data array is eagerly fetched.
                //For some reason ebean isn't properly lazy loading
                //this when we try to use it and get NullPointerExceptions downstream
                //if we don't also fetch it here...
                byte[] tmp = o.data;

                executor.submit(new Worker(o, listener, DELETE_FIRST));

            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.DAYS);
        }catch(InterruptedException e){
            listener.error(e);
        }catch(Throwable t){
            listener.error(t);
            throw t;
        }
    }

    public void reindexAll(ReIndexListener listener){
        listener.newReindex();
        File ginasIx = new File(Play.application().configuration().getString("ix.home"));

        Play.application().plugin(SequenceIndexerPlugin.class).onStop();
        Play.application().plugin(StructureIndexerPlugin.class).onStop();

        TextIndexerPlugin.prepareTestRestart();
        Play.application().plugin(TextIndexerPlugin.class).onStop();

        IOUtil.deleteRecursivelyQuitely(new File(ginasIx, "sequence"));
        File structureDir = new File(ginasIx, "structure");
        IOUtil.deleteRecursivelyQuitely(structureDir);
        IOUtil.deleteRecursivelyQuitely(new File(ginasIx, "text"));

        structureDir.mkdirs();

        Play.application().plugin(SequenceIndexerPlugin.class).onStart();
        Play.application().plugin(StructureIndexerPlugin.class).onStart();
        Play.application().plugin(TextIndexerPlugin.class).onStart();


        listener.totalRecordsToIndex(finder.findRowCount());


        //QueryIterator must be in try-wtih-resource
        //so it is properly closed if it errors out early.
        try (QueryIterator<BackupEntity> iter = finder.findIterate()){
            while (iter.hasNext()) {
                BackupEntity o = iter.next();


                //this temp variable is because we have to
                //make sure the data array is eagerly fetched.
                //For some reason ebean isn't properly lazy loading
                //this when we try to use it and get NullPointerExceptions downstream
                //if we don't also fetch it here...
                byte[] tmp = o.data;

                executor.submit(new Worker(o, listener, DO_NOT_DELETE_FIRST));

            }


            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.DAYS);
        }catch(InterruptedException e){
            listener.error(e);
        }catch(RuntimeException e){
            listener.error(e);
            throw e;
        }finally{
            listener.doneReindex();


        }
    }


    private static class Worker implements Runnable {
        private final BackupEntity oreal;

        private final ReIndexListener listener;
        private final boolean deleteFirst;
        public Worker(BackupEntity bm,ReIndexListener listener, boolean deleteFirst) {
            this.oreal = bm;
            this.listener = listener;
            this.deleteFirst = deleteFirst;
        }

        @Override
        public void run() {
            try {
                Object instantiated = oreal.getInstantiated();
                EntityPersistAdapter.getInstance().deepreindex(instantiated, deleteFirst);
                listener.recordReIndexed(instantiated);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
