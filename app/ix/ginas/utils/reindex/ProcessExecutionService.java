package ix.ginas.utils.reindex;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.avaje.ebean.QueryIterator;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.models.BackupEntity;
import ix.core.plugins.IxContext;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.EntityFetcher;
import ix.core.util.BlockingSubmitExecutor;
import ix.core.util.CloseableIterator;
import ix.core.util.EntityUtils;
import ix.core.util.IOUtil;
import ix.core.util.StreamUtil;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.StreamUtil.ThrowableFunction;
import ix.ginas.models.v1.Substance;
import play.Application;
import play.Logger;
import play.Play;
import play.db.ebean.Model;

/**
 * Created by katzelda on 5/16/16.
 */
public class ProcessExecutionService {

    private static final Model.Finder<Long,BackupEntity> finder = new Model.Finder(Long.class, BackupEntity.class);

    private final ExecutorService executor;

    private static final boolean DELETE_FIRST = true;
    private static final boolean DO_NOT_DELETE_FIRST = false;

    
    public ProcessExecutionService(int numThread, int capacityPerThread) {
        this.executor = BlockingSubmitExecutor.newFixedThreadPool(numThread, capacityPerThread);
    }
    
    public static interface EntityStreamSupplier<T> extends Supplier<Stream<T>>{
    	public default long getTotal(){
    		return -1;
    	}
    	
    	   	
    	public static <T> EntityStreamSupplier<T> ofIterator(Supplier<Iterator<T>> source){
    		return ()->{
    			Iterator<T> ci=source.get();
    			
    			
    			Stream<T> stream = StreamUtil.forIterator(ci);
        		
        		stream.onClose(()->{
        			if(ci instanceof Closeable){
        				try{
        					((Closeable)ci).close();
        				}catch(IOException e){
        					//not sure about this
        					throw new RuntimeException(e);
        				}
        			}
        		});
        		return stream;	
    		};
    	}
    	
    	public default <U> EntityStreamSupplier<U> map(ThrowableFunction<T,U> map){
    		EntityStreamSupplier<T> me = this;
    		return new EntityStreamSupplier<U>(){
    			@Override
    			public long getTotal(){
    				return me.getTotal();
    			}
				@Override
				public Stream<U> get() {
					return me.get().map(map);
				}
    		};
    	}
    	
    	public default EntityStreamSupplier<T> before(Runnable run){
    		EntityStreamSupplier<T> me = this;
    		return new EntityStreamSupplier<T>(){
    			@Override
    			public long getTotal(){
    				return me.getTotal();
    			}
				@Override
				public Stream<T> get() {
					run.run();
					return me.get();
				}
    		};
    	}
    	
    	public default EntityStreamSupplier<T> total(long tot){
    		EntityStreamSupplier<T> me = this;
    		return new EntityStreamSupplier<T>(){
    			@Override
    			public long getTotal(){
    				return tot;
    			}
				@Override
				public Stream<T> get() {
					return me.get();
				}
    		};
    	}
    	
    	public default EntityStreamSupplier<T> total(Supplier<Long> tot){
    		EntityStreamSupplier<T> me = this;
    		return new EntityStreamSupplier<T>(){
    			@Override
    			public long getTotal(){
    				return tot.get();
    			}
				@Override
				public Stream<T> get() {
					return me.get();
				}
    		};
    	}
    	
    	public default EntityStreamSupplier<T> filter(Predicate<T> filter){
    		EntityStreamSupplier<T> me = this;
    		return new EntityStreamSupplier<T>(){
    			@Override
				public Stream<T> get() {
					return me.get().filter(filter);
				}
    		};
    	}
    	
    	
    }
    
    
    public <T,U extends T> void process(EntityStreamSupplier<U> streamSupplier, Consumer<T> consumer, ProcessListener listener) throws IOException{

        listener.newProcess();
    	try(Stream<U> stream = streamSupplier.get()){
    		long total = streamSupplier.getTotal();
    		if(total>=0){
    			listener.totalRecordsToProcess((int) total);
    		}
    		
    		stream.forEach(t->{
    			executor.submit(()->{
    				try{
    					consumer.accept(t);
    					listener.recordProcessed(t);
    				}catch(Exception e){
    					e.printStackTrace();
    					listener.error(e);
    				}
    			});
    		});
    		
    		executor.shutdown();
            executor.awaitTermination(1, TimeUnit.DAYS);
    		
    	}catch(InterruptedException e){
            listener.error(e);
        }catch(Throwable t){
            listener.error(t);
            throw t;
        }finally {
            listener.doneProcess();
        }
    }
    

    public void reindex(ReindexQuery query, ProcessListener listener)throws IOException{
    	EntityStreamSupplier<Object> streamSupplier = EntityStreamSupplier
    								.ofIterator(()->query.query(listener))
    								.map(o->o.getInstantiated());
    	
    	    	
    	process(streamSupplier, CommonConsumers.REINDEX_COMPLETE ,listener);
    }
    
    
    /**
     * Deletes cache, etc for faster re-indexing
     */
    private static void nukeEverything(){
    	Application app = Play.application();
        Logger.info("SHUTTING DOWN");
        // Util.debugSpin(3000);

        File ginasIx = app.plugin(IxContext.class).home();

        Logger.info("#################### Deleting indexes");
        Logger.info("#################### stopping seq indexer");
        app.plugin(SequenceIndexerPlugin.class).onStop();
        Logger.info("stopping structure indexer");
        app.plugin(StructureIndexerPlugin.class).onStop();

        TextIndexerPlugin.prepareTestRestart();
        Logger.info("stopping text indexer");
        app.plugin(TextIndexerPlugin.class).onStop();
        Logger.info("deleting sequence dir");
        IOUtil.deleteRecursivelyQuitely(new File(ginasIx, "sequence"));
        Logger.info("deleting structure dir");
        File structureDir = new File(ginasIx, "structure");
        IOUtil.deleteRecursivelyQuitely(structureDir);
        Logger.info("deleting text indexer storage root dir");
        IOUtil.deleteRecursivelyQuitely(TextIndexerPlugin.getStorageRootDir());

        Logger.info("deleting ginas ix dir");
        try {
            IOUtil.printDirectoryStructure(ginasIx);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        structureDir.mkdirs();

        app.plugin(SequenceIndexerPlugin.class).onStart();
        app.plugin(StructureIndexerPlugin.class).onStart();
        app.plugin(TextIndexerPlugin.class).onStart();
    }
    
    

    public void reindexAll(ProcessListener listener) throws Exception{
		process(CommonStreamSuppliers.allBackups().before(ProcessExecutionService::nukeEverything), CommonConsumers.REINDEX_FAST, listener);
    }
    
    
    public class Process<T>{
    	private Consumer<T> consumer;
    	private EntityStreamSupplier<T> supplier;
    	private ProcessListener listener;
    	
    	public Process(EntityStreamSupplier<T> supplier, Consumer<T> consumer,ProcessListener listener){
    		this.supplier=supplier;
    		this.consumer=consumer;
    		this.listener=listener;
    	}
    	
    	public void execute() throws IOException{
    		process(supplier,consumer,listener);
    	}
    	
    }
    
    public class ProcessBulder<T>{
    	private Consumer<T> consumer;
    	private EntityStreamSupplier<T> supplier;
    	private ProcessListener listener;
    	
    	public ProcessBulder(){}
    	
    	public ProcessBulder<T> consumer(Consumer<T> consumer){
    		this.consumer=consumer;
    		return this;
    	}
    	
    	public ProcessBulder<T> streamSupplier(EntityStreamSupplier<T> supplier){
    		this.supplier=supplier;
    		return this;
    	}
    	
    	public ProcessBulder<T> listener(ProcessListener listener){
    		this.listener=listener;
    		return this;
    	}
    	
    	public Process<T> build(){
    		return new Process(supplier, consumer,listener);    		
    	}
    	
    }
    
    
    
    public static class CommonConsumers{
    	public static Consumer<Object> REINDEX_FAST = t->{
    		
    		EntityPersistAdapter.getInstance().deepreindex(t, DO_NOT_DELETE_FIRST);
    	};
    	
    	public static Consumer<Object> REINDEX_COMPLETE = t->{
    		EntityPersistAdapter.getInstance().deepreindex(t, DELETE_FIRST);
    	};
    	
    	public static Consumer<Object> POST_UPDATES = t->{
    		EntityPersistAdapter.getInstance().postUpdateBeanDirect(t, null, false);
    	};
    	
    	public static Consumer<Object> POST_INSERTS = t->{
    		EntityPersistAdapter.getInstance().postInsertBeanDirect(t);
    	};
    	
    	public static Consumer<Object> POST_DELETES = t->{
    		EntityPersistAdapter.getInstance().postDeleteBeanDirect(t);
    	};
    	
    	public static Consumer<Object> POST_LOADS = t->{
    		EntityPersistAdapter.getInstance().postLoad(t, null);
    	};
    }
    
    public static class CommonStreamSuppliers{
    	/**
    	 * Create an {@link EntityStreamSupplier} for the given type,
    	 * which will use the native Ebean finder to find and fetch
    	 * all instances. In addition, use an {@link EntityFetcher}
    	 * to fetch each returned entity in the stream. This extra
    	 * step is done as Ebean sometimes lazy-loads the records
    	 * in ways that can be problematic for some common consumers.
    	 * 
    	 * @param cls
    	 * @return
    	 */
    	public static <T> EntityStreamSupplier<T> allForDeep(Class<T> cls){    		
    		return allFor(cls)
    			       .map(o->(T)EntityWrapper.of(o).getKey().getFetcher().call());
    	}
    	
    	/**
    	 * Create an {@link EntityStreamSupplier} for the given type,
    	 * which will use the native Ebean finder to find and fetch
    	 * all instances.
    	 * 
    	 * @param cls
    	 * @return
    	 */
    	public static <T> EntityStreamSupplier<T> allFor(Class<T> cls){    		
    		Model.Finder<?,?> mfinder = EntityUtils.getEntityInfoFor(cls).getInherittedRootEntityInfo().getFinder();
    		return EntityStreamSupplier.ofIterator(()->mfinder.findIterate())
    								   .total(()->(long)mfinder.findRowCount())
    								   .map(o->(T)o);
    	}
    	
    	
    	public static EntityStreamSupplier<Object> allBackups(){
    		return EntityStreamSupplier.ofIterator(()->finder.findIterate())
			        .total((long)finder.findRowCount())
			        .map(o->o.getInstantiated());
    	}
    	
    }
}
