package ix.ginas.utils;
import java.util.*;
import java.util.concurrent.*;

import javax.persistence.Entity;

import com.avaje.ebean.Page;
import com.avaje.ebean.PagingList;
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

        PagingList<BackupEntity> pagingList = finder.query()
                //.setFirstRow(pageSize * page)
                //.setMaxRows(pageSize)
                .findPagingList(pageSize);


        Future<Integer> futureRowCount = pagingList.getFutureRowCount();
        List<BackupEntity> l;
        Page<BackupEntity> currentPage;
        int page = 0;

        boolean computedTotalAlready=false;
        try {
            do {

                currentPage = pagingList.getPage(page);
                l = currentPage.getList();
                for (BackupEntity o : l) {
                    //TODO: make part of query
                    if (!o.isOfType(type)) continue;
                    if (since != null) {
                        if (!o.modified.after(since)) {
                            continue;
                        }
                    }

                    executor.submit(new Worker(o, listener));

                }

                //this ugliness is so we don't call get() before we
                //to most other ways to write this will invoke get() before
                //we want which will cause it to block.
                if(!computedTotalAlready && futureRowCount.isDone()){
                    computedTotalAlready = true;
                    listener.totalRecordsToIndex(futureRowCount.get());
                }

                page++;
            } while (! l.isEmpty());

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.DAYS);
        }catch(InterruptedException | ExecutionException  e){
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