package ix.core.utils.executor;

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

import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
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
import ix.test.models3.Wat;
import ix.core.controllers.search.SearchFactory;
import play.Application;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

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
    	
    	
    	public static <T> EntityStreamSupplier<T> nothing(){
            return ()->Stream.empty();
        }
    	   	
    	public static <T> EntityStreamSupplier<T> ofIterator(Supplier<Iterator<T>> source){
    		return ()->{
    			Iterator<T> ci=source.get();
    			
    			
    			Stream<T> stream = StreamUtil.forIterator(ci);
        		
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
    		e.printStackTrace();
    		
            listener.error(e);
        }catch(Throwable t){
        	t.printStackTrace();
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
    	
    	    	
    	process(streamSupplier, CommonConsumers.REINDEX_COMPLETE() ,listener);
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
    	buildProcess(Object.class)
    		.consumer(CommonConsumers.REINDEX_FAST())
    		.streamSupplier(CommonStreamSuppliers.allBackups())
    		.before(ProcessExecutionService::nukeEverything)
    		.listener(listener)
    		.build()
    		.execute();
    }
    
    
    public class Process<T>{
        private Consumer<T> consumer = CommonConsumers.doNothing();
        private EntityStreamSupplier<T> supplier = CommonStreamSuppliers.doNothing();
        private ProcessListener listener= ProcessListener.doNothingListener();
    	
    	public Process(EntityStreamSupplier<T> supplier, Consumer<T> consumer,ProcessListener listener){
    		this.supplier=supplier;
    		this.consumer=consumer;
    		this.listener=listener;
    	}
    	
    	public void execute() throws IOException{
    	    System.out.println("Going to execute");
    		process(supplier,consumer,listener);
    	}
    	
    }
    
    public class ProcessBulder<T>{
    	private Consumer<T> consumer = CommonConsumers.doNothing();
    	private EntityStreamSupplier<T> supplier = CommonStreamSuppliers.doNothing();
    	private ProcessListener listener = ProcessListener.doNothingListener();
    	
    	public ProcessBulder(){}
    	public ProcessBulder(Class<T> cls){}
    	
    	public ProcessBulder<T> consumer(Consumer<T> consumer){
    		this.consumer=consumer;
    		return this;
    	}
    	
    	public ProcessBulder<T> streamSupplier(EntityStreamSupplier<T> supplier){
    		this.supplier=supplier;
    		return this;
    	}
    	
    	public ProcessBulder<T> before(Runnable r){
    		this.supplier=supplier.before(r);
    		return this;
    	}
    	
    	public ProcessBulder<T> before(Before b){
    		return before((Runnable)b);
    	}
    	
    	public ProcessBulder<T> filter(Predicate<T> filter){
    	    this.supplier=supplier.filter(filter);
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
    
    public <T> ProcessExecutionService.ProcessBulder<T> buildProcess(Class<T> cls){
    	return new ProcessExecutionService.ProcessBulder<T>();
    }
    
    
    
    public static interface Before extends Runnable{
    	
    	public static <T> Before removeIndexFor(Class<T> cls){
    		return ()->{
    			try{
    				SearchFactory.getTextIndexer().removeAllType(cls);
    			}catch(Exception e){
    				throw new RuntimeException(e);
    			}
    		};
    	}
    }
    
    
    
    
    
    
    public static class CommonConsumers{
    	public static <T> Consumer<T> REINDEX_FAST(){
    	    return (t->EntityPersistAdapter.getInstance().deepreindex(t, DO_NOT_DELETE_FIRST));
    	}
    		
        public static <T> Consumer<T> REINDEX_COMPLETE(){
    		return t->EntityPersistAdapter.getInstance().deepreindex(t, DELETE_FIRST);
    	};
    	
        public static <T> Consumer<T> POST_UPDATES(){
            return t->EntityPersistAdapter.getInstance().postUpdateBeanDirect(t, null, false);
    	};
    	
        public static <T> Consumer<T> POST_INSERTS(){
            return t->EntityPersistAdapter.getInstance().postInsertBeanDirect(t);
    	};
    	
        public static <T> Consumer<T> POST_DELETES(){
    		return t->EntityPersistAdapter.getInstance().postDeleteBeanDirect(t);
    	}
    	
        public static <T> Consumer<T> POST_LOADS(){ 
            return t->EntityPersistAdapter.getInstance().postLoad(t, null);
    	}

        public static <T> Consumer<T> doNothing() {
            return (t)->{};
        }
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
    	
    	/**
         * Create an {@link EntityStreamSupplier} for the given type,
         * which will use the native Ebean finder to find and fetch
         * all instances matching the provided {@link Expression}.
         * 
         * @param cls
         * @return
         */
        public static <T> EntityStreamSupplier<T> allWhere(Class<T> cls, Expression e){         
            return allFromQuery(cls, f->f.query().where(e));
        }
        
        /**
         * Create an {@link EntityStreamSupplier} for the given type,
         * which will use the native Ebean {@link Finder}. The provided {@link Function}
         * will extract a query from that {@link Finder} to use.
         * 
         * @param cls
         * @return
         */
        public static <T> EntityStreamSupplier<T> allFromQuery(Class<T> cls, Function<Finder<?,T>,Query<T>> qp){         
            Model.Finder<?,T> mfinder = (Finder<?, T>) EntityUtils.getEntityInfoFor(cls).getInherittedRootEntityInfo().getFinder();
            
            return EntityStreamSupplier.ofIterator(()->qp.apply(mfinder).findIterate())
                                       .total(()->(long)mfinder.findRowCount())
                                       .map(o->(T)o);
        }
        
        public static <T> EntityStreamSupplier<T> allFrom(Class<T> cls, Function<Finder<?,T>,ExpressionList<T>> e){         
            return allFromQuery(cls,e.andThen(el->el.query()));
        }
    	
    	
    	
    	/**
    	 * Create an {@link EntityStreamSupplier} for the given type,
    	 * which will use the native Ebean finder to find and fetch
    	 * all instances. In addition, use an {@link EntityFetcher}
    	 * to fetch each returned entity in the stream. This extra
    	 * step is done as Ebean sometimes lazy-loads the records
    	 * in ways that can be problematic for some common consumers.
    	 * 
    	 * @param cls
    	 * @param datasource Ebean datasource to use
    	 * @return
    	 */
    	public static <T> EntityStreamSupplier<T> allForDeep(Class<T> cls, String datasource){    		
    		return allFor(cls,datasource)
    			       .map(o->(T)EntityWrapper.of(o).getKey().getFetcher(datasource).call());
    	}
    	
    	/**
    	 * Create an {@link EntityStreamSupplier} for the given type,
    	 * which will use the native Ebean finder to find and fetch
    	 * all instances. 
    	 * 
    	 * @param cls
    	 * @param datasource Ebean datasource to use
    	 * @return
    	 */
    	public static <T> EntityStreamSupplier<T> allFor(Class<T> cls, String datasource){    		
    		Model.Finder<?,?> mfinder = EntityUtils.getEntityInfoFor(cls).getInherittedRootEntityInfo().getFinder(datasource);
    		return EntityStreamSupplier.ofIterator(()->mfinder.findIterate())
    								   .total(()->(long)mfinder.findRowCount())
    								   .map(o->(T)o);
    	}
    	
    	public static <T> EntityStreamSupplier<T> allBackups(){
    		return EntityStreamSupplier.ofIterator(()->finder.findIterate())
			        .total((long)finder.findRowCount())
			        .map(o->(T)o.getInstantiated());
    	}
    	
    	public static <T> EntityStreamSupplier<T> doNothing(){
            return EntityStreamSupplier.nothing();
        }
    	
    }
}
