package ix.core.adapters;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;

import ix.core.EntityProcessor;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.factories.EntityProcessorFactory;
import ix.core.java8Util.Java8ForOldEbeanHelper;
import ix.core.models.Edit;
import ix.core.plugins.IxCache;
import ix.core.plugins.IxContext;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.processors.BackupProcessor;
import ix.core.processors.IndexingProcessor;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.ginas.utils.reindex.ReIndexListener;
import ix.seqaln.SequenceIndexer;
import play.Application;
import play.Logger;
import play.Play;
import tripod.chem.indexer.StructureIndexer;

public class EntityPersistAdapter extends BeanPersistAdapter implements ReIndexListener{

    private class MyLock{
        private Counter count = new Counter();
        private ReentrantLock lock = new ReentrantLock();

        private volatile boolean preUpdateWasCalled=false;
        private volatile boolean postUpdateWasCalled=false;

        private final Key thekey;

        public MyLock(Key thekey) {
            this.thekey = thekey;
        }
        
        public boolean isLocked(){
        	return this.lock.isLocked();
        }
        
        public boolean tryLock(){
        	return this.lock.tryLock();
        }
        

        public void acquire(){
            synchronized (count){
                count.increment();
            }
            while(true){
                try {
                    if(lock.tryLock(1, TimeUnit.MINUTES)){
                        break;
                    }else{
                    	Logger.warn("still waiting for lock with key " + thekey);
                        System.out.println("still waiting for lock with key " + thekey);
                    }
                } catch (InterruptedException e) {
                   throw new RuntimeException(e);
                }
            }
            preUpdateWasCalled=false;
            postUpdateWasCalled=false;
            
        }

        public void release(){
            synchronized (count) {
                count.decrementAndGet();
            }
            lock.unlock();
            synchronized (count) {
                int value = count.intValue();
                if(value ==0){
                    //no more blocking records?
                    //remove ourselves from the map to free memory
                    lockMap.remove(thekey);
                }
            }
        }
        
        public void markPreUpdateCalled(){
        	preUpdateWasCalled=true;
        }
        
        public void markPostUpdateCalled(){
        	postUpdateWasCalled=true;
        }
        
        public boolean hasPreUpdateBeenCalled(){
        	return preUpdateWasCalled;
        }
        
        public boolean hasPostUpdateBeenCalled(){
        	return postUpdateWasCalled;
        }
    }

    private static class Counter{
        private int count=0;

        public void increment(){
            count++;
        }

        public int decrementAndGet(){
            return --count;
        }

        public int intValue(){
            return count;
        }
    }

	private static EntityPersistAdapter _instance =null;
	
	private Application application;
	
	private Map<EntityInfo, EntityProcessor> entityProcessors = new ConcurrentHashMap<>();
    
    //Do we need both?
    private Map<Key, MyLock> lockMap= new ConcurrentHashMap<>();
    private ConcurrentHashMap<Key, Edit> editMap= new ConcurrentHashMap<>();
    
    private TextIndexerPlugin textIndexerPlugin;
    
    
    private StructureIndexerPlugin strucProcessPlugin;
    private SequenceIndexerPlugin seqProcessPlugin;
    
    public boolean isReindexing=false;
    
    private ConcurrentHashMap<Key, Integer> alreadyLoaded= 
           new ConcurrentHashMap<>(10000);;
    
    
    public static EntityPersistAdapter getInstance(){
    	return _instance;
    }
    


    /**
     * Preparing the edit ...
     * 
     * @param ew
     * @return
     */
    private Edit createAndPushEditForWrappedEntity(EntityWrapper ew){
    	Objects.requireNonNull(ew);
    	String oldJSON = ew.toFullJson();
    	
    	Edit e = new Edit(ew.getEntityClass(),ew.getKey().getIdString());
    	e.oldValue=oldJSON;
    	e.path=null;
    	
    	if(ew.getVersion().isPresent()){
    		e.version=ew.getVersion().get().toString();
    		//TODO: consider this
    		//e.comments = e.version + " comment";
    	}
    	storeEditForUpdate(ew.getKey(),e);
    	return e;
    }

    
    /**
     * Used to apply the change operation during a locked edit.
     * Empty optional is used to cancel an operation, and a non-empty optional
     * will be used just to pass through.
     *  
     * @author peryeata
     *
     * @param <T>
     */
    public interface ChangeOperation<T>{
        Optional apply(T obj) throws Exception; //Can't be of T type, unfortunately ... may return different thing
    }

    /**
     * This is the same as performChange, except that it takes in the object
     * to be changed rather than the key to retrieve that object.
     * 
     * Note that the actual object will still be fetched using the key
     * from the database. This is because it may be stale at this point.
     * 
     * @param t
     * @param changeOp
     * @return
     */
    @Deprecated
    public static <T> EntityWrapper performChangeOn(T t,ChangeOperation<T> changeOp){
    	EntityWrapper<T> wrapped = EntityWrapper.of(t);
    	return performChange(wrapped.getKey(),changeOp);
    }
    
    
    @Deprecated
    public static <T> EntityWrapper performChange(Key key, ChangeOperation<T> changeOp){
    	return EntityPersistAdapter.getInstance().change(key, changeOp);
    }
    
    
    
    public <T> EntityWrapper change(Key key, ChangeOperation<T> changeOp){
         Objects.requireNonNull(key);
         Objects.requireNonNull(changeOp);
         
         
         
         MyLock lock = lockMap.computeIfAbsent(key, new Function<Key, MyLock>() {
             @Override
             public MyLock apply(Key key) {
                 return new MyLock(key); //This should work, but feels wrong
             }
         });



         Edit e=null;
         lock.acquire(); //acquire the lock (blocks)
         try{
             EntityWrapper<T> ew = (EntityWrapper<T>)key.fetch().get(); //supplies the object to be edited,
             //you could have a different supplier
             //for this, but it's nice to be sure
             //that the object can't be stale
             e=createAndPushEditForWrappedEntity(ew); //Doesn't block, or even check for 
                                                      //existence of an active edit
             								   		 //let's hope it works anyway!
             
             Optional op = changeOp.apply((T)ew.getValue()); //saving happens here
             												//So should anything with the edit
             												//inside of a post Update hook
             EntityWrapper saved=null;
             
             
             //didn't work, according to change operation
             //Either there was an error, or the decision
             //to change was cancelled
             if(!op.isPresent()){
             	return null; 
             }else{
             	saved = EntityWrapper.of(op.get());
 			}
 			e.kind = saved.getKind();
 			e.newValue = saved.toFullJson();
 			e.comments= ew.getChangeReason().orElse(null);
 			e.save();
 			
             return saved;
         }catch(Exception ex){
             ex.printStackTrace();
             throw new IllegalStateException(ex);
         }finally{
             if(e !=null) {
                 popEditForUpdate(key); //When we're all done, release it
             }
             lock.release(); //release the lock
         }
     }

    
    private void storeEditForUpdate(Key k, Edit e){
    	if(editMap.containsKey(k))throw new IllegalStateException("Concurrent edit may have occured, bailing out");
    	editMap.put(k,e);
    }
    
    
    public Edit popEditForUpdate(Key key){
    	return editMap.remove(key);	
    }
    public boolean isEditPresentUpdate(Key key){
    	return editMap.containsKey(key);
    }
    
    public int getEditUpdateCount(){
    	return editMap.size();
    }

    public EntityPersistAdapter () {
    	this(Play.application());
    }
    
    public EntityPersistAdapter (Application app){
    	this.application=app;
    	textIndexerPlugin = app.plugin(TextIndexerPlugin.class);
    	strucProcessPlugin=app.plugin(StructureIndexerPlugin.class);
        seqProcessPlugin=app.plugin(SequenceIndexerPlugin.class);
    	_instance=this;
    }
    

    boolean debug (int level) {
        IxContext ctx = application.plugin(IxContext.class);
        return ctx.debug(level);
    }

    public boolean isRegisterFor (Class<?> cls) {
    	EntityInfo<?> emeta = EntityUtils.getEntityInfoFor(cls);
        if(!emeta.isEntity())return false;

        EntityProcessorFactory epf=EntityProcessorFactory
        		.getInstance(this.application);
        
        if(emeta.hasBackup()){
        	epf.register(emeta, BackupProcessor.getInstance(),false);
        }
        if(emeta.shouldIndex()){
        	epf.register(emeta, IndexingProcessor.getInstance(),false);
        }

        EntityProcessor epp=epf.getSingleResourceFor(emeta);
        addEntityProcessor(emeta,epp);
        return true;
    }
    
    private void addEntityProcessor(EntityInfo<?> emeta, EntityProcessor<?> ep){
    	entityProcessors.put(emeta, ep);
    }
    
    @Override
    public boolean preInsert (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        
        operate(bean,Java8ForOldEbeanHelper.processorCallableFor(PrePersist.class),true);
        return true;
    }
    @Override
    public boolean preUpdate (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        return preUpdateBeanDirect(bean);
    }
    
    public boolean preUpdateBeanDirect(Object bean){
    	EntityWrapper ew = EntityWrapper.of(bean);
    	MyLock ml = lockMap.get(ew.getKey());
        if(ml!=null && ml.hasPreUpdateBeenCalled()){
         	return true; // true?
        }
        
        operate(bean,Java8ForOldEbeanHelper.processorCallableFor(PreUpdate.class),true);
        
        if(ml!=null){
         	ml.markPreUpdateCalled();
        }
        return true;
    }
    
    @Override
    public void postInsert (BeanPersistRequest<?> request) {
        
        InxightTransaction it = InxightTransaction.getTransaction(request.getTransaction());
        final Object bean = request.getBean();
        it.addPostCommitCall(new Callable(){
			@Override
			public Object call() throws Exception {
				postInsertBeanDirect(bean);
				return null;
			}
        }); 
        
    }

    public void postInsertBeanDirect(Object bean){
    	operate(bean,Java8ForOldEbeanHelper.processorCallableFor(PostPersist.class),false);
    }
    
    public static SequenceIndexer getSequenceIndexer(){
    	return getInstance().sequenceIndexer();
    }
    
    public static StructureIndexer getStructureIndexer(){
    	return getInstance().structureIndexer();
    }
    
    public StructureIndexer structureIndexer(){
    	if (strucProcessPlugin == null || !strucProcessPlugin.enabled()) {
    		strucProcessPlugin=application.plugin(StructureIndexerPlugin.class);
		}
		return strucProcessPlugin.getIndexer();
    }
    
    public SequenceIndexer sequenceIndexer(){
		if (seqProcessPlugin == null || !seqProcessPlugin.enabled()) {
			seqProcessPlugin = application.plugin(SequenceIndexerPlugin.class);
		}
		return seqProcessPlugin.getIndexer();
	}
    
   
    
    
	private void makeIndexOnBean(Object bean) throws java.io.IOException {
		Java8ForOldEbeanHelper.makeIndexOnBean(this, EntityWrapper.of(bean));
	}
	
	private void deleteIndexOnBean(Object bean) throws Exception {
		Java8ForOldEbeanHelper.deleteIndexOnBean(this,EntityWrapper.of(bean));
	}

    @Override
    public void postUpdate (BeanPersistRequest<?> request) {
    	final Object bean = request.getBean();
    	
    	InxightTransaction it = InxightTransaction.getTransaction(request.getTransaction());
        
    	final Object oldValues = request.getOldValues();
        it.addPostCommitCall(new Callable(){
			@Override
			public Object call() throws Exception {
				postUpdateBeanDirect(bean,oldValues);
				return null;
			}
        });
    }
    
    public void postUpdateBeanDirect(Object bean, Object oldvalues){
    	EntityWrapper<?> ew= EntityWrapper.of(bean);
    	MyLock ml = lockMap.get(ew.getKey());
        if(ml!=null && ml.hasPostUpdateBeenCalled()){
        	return;
        }
    	if (ew.ignorePostUpdateHooks()) {
            return;
        }
    	EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
                try {
                    if (ew.isEntity() && ew.hasKey()) {
                    	Key key = ew.getKey();
                    	// If we didn't already start an edit for this
                    	// then start one and save it. Otherwise just ignore
                    	// the edit piece.
						if (!isEditPresentUpdate(key)) { 
							Edit edit = new Edit(ew.getEntityClass(), key.getIdString());
							edit.oldValue = EntityWrapper.of(oldvalues).toFullJson();
							edit.version = ew.getVersion().orElse(null);
							edit.comments= ew.getChangeReason().orElse(null);
							edit.kind = ew.getKind();
							edit.newValue = ew.toFullJson(); 
							edit.save();
							
						}
                    }else {
                        Logger.warn("Entity bean ["+ew.getKind()+"]"
                                    +" doesn't have Id annotation!");
                    }
                }catch (Exception ex) {
                    Logger.trace("Can't retrieve bean id", ex);
                }
            
        
        if (debug (2)) {
            Logger.debug(">> Old: "+mapper.valueToTree(oldvalues)
                         +"\n>> New: "+mapper.valueToTree(bean));
        }
        operate(bean,Java8ForOldEbeanHelper.processorCallableFor(PostUpdate.class),true);
        
        
        IxCache.removeAllChildKeys(ew.getKey().toString());
        if(ml!=null){
        	ml.markPostUpdateCalled();
        }
    } 

    @Override
    public boolean preDelete (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        operate(bean,Java8ForOldEbeanHelper.processorCallableFor(PreRemove.class),true);
        return true;
    }

    @Override
    public void postDelete (BeanPersistRequest<?> request) {
    	
    	InxightTransaction it = InxightTransaction.getTransaction(request.getTransaction());
        final Object bean = request.getBean();
        it.addPostCommitCall(new Callable(){
			@Override
			public Object call() throws Exception {
				postDeleteBeanDirect(bean);
				return null;
			}
        });
        
    }
    
    
    public void postDeleteBeanDirect (Object bean) {
    	operate(bean,Java8ForOldEbeanHelper.processorCallableFor(PostRemove.class),false);
    }
    

    @Override
    public void postLoad (Object bean, Set<String> includedProperties) {
    	operate(bean,Java8ForOldEbeanHelper.processorCallableFor(PostLoad.class),false);
    }

    public void operate(Object bean, BiFunction<Object,EntityProcessor, Callable> function, boolean fail){
    	EntityInfo<?> emeta = EntityUtils.getEntityInfoFor(bean.getClass());
        EntityProcessor ep = entityProcessors.get(emeta);
        try {
 			if(ep!=null){
 				function.apply(bean, ep).call();
 			}
 		} catch (Exception ex) {
 			Logger.trace("Error invoking Entity Processor:" + ex.getMessage(), ex);
 			if(fail){
 				throw new IllegalStateException(ex);
 			}
 		}
    }

    public void deepreindex(Object bean){
        deepreindex(bean, true);
    }
    
    
    public void deepreindex(Object bean, boolean deleteFirst){
    	Java8ForOldEbeanHelper.deepreindex(this,EntityWrapper.of(bean),deleteFirst);
    }
    
	public void reindex(Object bean){
	    reindex(EntityWrapper.of(bean), true);
	}
	
    public void reindex(EntityWrapper ew, boolean deleteFirst){
    	
        try {
        	if(ew.hasKey()){
	        	Key key=ew.getKey();
	            if(key!=null) {
	            	//TODO: Investigate this
	            	if(isReindexing){
		            	if(alreadyLoaded.containsKey(key)){
		            		return;
		                }
		                alreadyLoaded.put(key, 0);
	            	}
	            }
	            if(deleteFirst) {
	                deleteIndexOnBean(ew.getValue());
	            }
	            makeIndexOnBean(ew.getValue());
        	}
        } catch (Exception e) {
            Logger.error("Problem reindexing entity:" ,e);
            e.printStackTrace();
        }
    }

	public TextIndexerPlugin getTextIndexerPlugin() {
		return textIndexerPlugin;
	}

	
	@Override
	public void newReindex() {
		isReindexing=true;
    	alreadyLoaded.clear();
	}

	@Override
	public void doneReindex() {
		isReindexing=false;
        alreadyLoaded.clear();
	}

	@Override
	public void recordReIndexed(Object o) {}

	@Override
	public void error(Throwable t) {}

	@Override
	public void totalRecordsToIndex(int total) {}

	@Override
	public void countSkipped(int numSkipped) {}

}
