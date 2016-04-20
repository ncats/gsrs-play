package ix.ginas.utils;
import java.util.*;
import java.util.concurrent.*;

import javax.persistence.Entity;

import com.avaje.ebean.Page;
import com.avaje.ebean.PagingList;
import com.avaje.ebean.QueryIterator;
import ix.core.util.BlockingSubmitExecutor;
import org.reflections.Reflections;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.models.BackupEntity;
import play.db.ebean.Model;

public class RebuildIndex  {

	public static int PAGE_SIZE=10;
	

    private final int pageSize;


    private final ExecutorService executor;

    private ReIndexListener listener;

    public RebuildIndex(ReIndexListener listener){
        this(listener, PAGE_SIZE);
    }
    public RebuildIndex(ReIndexListener listener, int pageSize){
        Objects.requireNonNull(listener);
        this.pageSize = pageSize;
        this.listener = listener;
        this.executor =  BlockingSubmitExecutor.newFixedThreadPool(5,pageSize);
    }

    public void reindex(Class<?> type){
        reindex(type, null);
    }
    public void reindex(Class<?> type, Date since){
        listener.newReindex();
        Model.Finder<Long,BackupEntity> finder = new Model.Finder(Long.class, BackupEntity.class);

        listener.totalRecordsToIndex(finder.findRowCount());


        //QueryIterator must be in try-wtih-resource
        //so it is properly closed if it errors out early.
        try (QueryIterator<BackupEntity> iter = finder.findIterate()){
              while (iter.hasNext()) {
                    BackupEntity o = iter.next();

                    //TODO: make part of query
                    if (!o.isOfType(type)) continue;
                    if (since != null) {
                        if (!o.modified.after(since)) {
                            continue;
                        }
                    }
                  //this temp variable is because we have to
                  //make sure the data array is eagerly fetched.
                  //For some reason ebean isn't properly lazy loading
                  //this when we try to use it and get NullPointerExceptions downstream
                  //if we don't also fetch it here...
                  byte[] tmp = o.data;

                    executor.submit(new Worker(o, listener));

                }


            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.DAYS);
        }catch(InterruptedException e){
            listener.error(e);
        }finally{
            listener.doneReindex();
        }

    }


    /**
     * Listener for reindexing progress.
     */
    public interface ReIndexListener{
        /**
         * Starting a new re-indexing process.
         */
        void newReindex();

        /**
         * Finished a re-indexing process.
         */
        void doneReindex();

        /**
         * The following object was
         * successfully re-indexed.
         * @param o the object that was re-indexed.
         */
        void recordReIndexed(Object o);

        /**
         * An error occurred.
         * @param t the {@link Throwable} that caused the error.
         */
        void error(Throwable t);

        /**
         * The total number of records that are to be indexed.
         * This method will be called before {@link #doneReindex()}
         * but is not guarenteed to be be called before
         * {@link #recordReIndexed(Object)} because some implementations
         * may compute the total asynchronously.
         *
         * @param total the total number of records to be indexed.
         */
        void totalRecordsToIndex(int total);
    }

	private static class Worker implements Runnable {
        private final BackupEntity oreal;

        private final ReIndexListener listener;
        public Worker(BackupEntity bm,ReIndexListener listener) {
            this.oreal = bm;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                Object instantiated = oreal.getInstantiated();
                EntityPersistAdapter.getInstance().deepreindex(instantiated);
                listener.recordReIndexed(instantiated);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static Set<Class<?>> getEntityClasses (String[] models) throws Exception {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for(String load: models) {
            load = load.trim();
            if (load.endsWith(".*")) {
                Reflections reflections = new Reflections
                    (load.substring(0, load.length()-2));
                Set<Class<?>> resources =
                    reflections.getTypesAnnotatedWith(Entity.class);
                for (Class<?> c : resources) {
                	classes.add((Class<? extends Entity>) c);
                }
            }
            else {
            	Reflections reflections = new Reflections
                        (load.substring(0, load.lastIndexOf(".")));
            	Set<Class<?>> resources =
                        reflections.getTypesAnnotatedWith(Entity.class);
                    for (Class<?> c : resources) {
                    	if(c.getName().equalsIgnoreCase(load))
                    		classes.add(c);
                    }
            }
        }
        return classes;
        
    }
}