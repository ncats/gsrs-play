package ix.core.adapters;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.Entity;
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
import ix.core.java8Util.Java8ForOldEbeanHelper;
import ix.core.models.Backup;
import ix.core.models.Edit;
import ix.core.models.Keyword;
import ix.core.plugins.IxCache;
import ix.core.plugins.IxContext;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.processors.BackupProcessor;
import ix.core.util.EntityUtils;
import ix.core.util.Java8Util;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.seqaln.SequenceIndexer;
import ix.utils.TimeProfiler;
import ix.utils.Tuple;
import ix.utils.Util;
import play.Logger;
import play.Play;
import tripod.chem.indexer.StructureIndexer;

public class EntityPersistAdapter extends BeanPersistAdapter{

    private static class MyLock{
        private Counter count = new Counter();
        private ReentrantLock lock = new ReentrantLock();


        private final String refId;

        public MyLock(String refId) {
            this.refId = refId;
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
            lock.lock();
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
                    EntityPersistAdapter.lockMap.remove(refId);
                }
            }
        }
    }

    private static class Counter{
        private int count;

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
	
	

    private Map<Class<?>, List<Hook>> preInsertCallback = new ConcurrentHashMap<>();
    private Map<Class<?>, List<Hook>> postInsertCallback = new ConcurrentHashMap<>();
    private Map<Class<?>, List<Hook>> preUpdateCallback = new ConcurrentHashMap<>();
    private Map<Class<?>, List<Hook>> postUpdateCallback = new ConcurrentHashMap<>();
    private Map<Class<?>, List<Hook>> preDeleteCallback = new ConcurrentHashMap<>();
    private Map<Class<?>, List<Hook>> postDeleteCallback = new ConcurrentHashMap<>();
    private Map<Class<?>, List<Hook>> postLoadCallback = new ConcurrentHashMap<>();
    
    private Map<Class<?>, List<EntityProcessor>> extraProcessors=new HashMap<>();
    
    
    //Do we need both?
    private static Map<Key, MyLock> lockMap;
    private static ConcurrentHashMap<Key, Edit> editMap;
    
    private TextIndexerPlugin textIndexerPlugin =
            Play.application().plugin(TextIndexerPlugin.class);
    private static StructureIndexerPlugin strucProcessPlugin;
    private static SequenceIndexerPlugin seqProcessPlugin;
    
    private static ConcurrentHashMap<Key, Integer> alreadyLoaded;
    
    public static EntityPersistAdapter getInstance(){
    	return _instance;
    }
    


    static{
        init();
    }

    public static void init(){

        strucProcessPlugin=Play.application().plugin(StructureIndexerPlugin.class);
        seqProcessPlugin=Play.application().plugin(SequenceIndexerPlugin.class);

        alreadyLoaded = new ConcurrentHashMap<>(10000);
        editMap = new ConcurrentHashMap<>();
        lockMap = new ConcurrentHashMap<>();
    }

    /**
     * Preparing the edit ...
     * 
     * @param bean
     * @return
     */
    public static Edit createAndPushEditForWrappedEntity(EntityWrapper ew){
    	Objects.requireNonNull(ew);
    	String oldJSON = ew.toFullJson();
    	
    	Edit e = new Edit(ew.getClazz(),ew.getKey().getIdString());
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
    public static <T> EntityWrapper performChangeOn(T t,ChangeOperation<T> changeOp){
    	EntityWrapper<T> wrapped = EntityWrapper.of(t);
    	return performChange(wrapped.getKey(),changeOp);
    }
    
    //This should do 2 things
    // #1, It should lock the object from being updated, blocking if necessary
    // #2, It should create and save the versioned Edit
    //
    //  This accepts a change operation, which should do the actual saving and changing
    
    
    // Returns the thing
    public static <T> EntityWrapper performChange(Key key, ChangeOperation<T> changeOp){
       // Objects.requireNonNull(id);
        Objects.requireNonNull(key);
        Objects.requireNonNull(changeOp);
        
        
        
        MyLock lock = lockMap.computeIfAbsent(key, new Function<Key, MyLock>() {
            @Override
            public MyLock apply(Key key) {
                return new MyLock(key.toString()); //This should work, but feels wrong
            }
        });

        if(lock.isLocked()){
        	System.out.println("Record " + key + " is locked. Waiting ...");
        }
        
        lock.acquire(); //acquire the lock (blocks)
        
        EntityWrapper<T> ew = key.fetch().get(); //supplies the object to be edited,
        										 //you could have a different supplier
        										 //for this, but it's nice to be sure
        										 //that the object can't be stale
        Edit e=null;
        try{
        	
            e=createAndPushEditForWrappedEntity(ew); //Doesn't block, or even check for 
                                                     //existence of an active edit
            								   		 //let's hope it works anyway!
            
            Optional op = changeOp.apply((T)ew.getValue()); //saving happens here
            												//So should anything with the edit
            												//inside of a post Update hook
            EntityWrapper saved=null;
            //didn't work, according to change operation
            if(!op.isPresent()){
            	System.out.println("Couldn't perform change.");
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

    
    private static void storeEditForUpdate(Key k, Edit e){
    	if(editMap.containsKey(k))throw new IllegalStateException("Concurrent edit may have occured, bailing out");
    	editMap.put(k,e);
    }
    
    
    public static Edit popEditForUpdate(Key key){
    	return editMap.remove(key);	
    }
    public static boolean isEditPresentUpdate(Key key){
    	return editMap.containsKey(key);
    }
    
    public static int getEditUpdateCount(){
    	return editMap.size();
    }

    public EntityPersistAdapter () {
    	List<Object> ls= Play.application().configuration().getList("ix.core.entityprocessors",null);
    	if(ls!=null){
    		for(Object o:ls){
    			if(o instanceof Map){ //TODO: This can be parsed with a little less strangeness
	    			Map m = (Map)o;
	    			String entityClass=(String) m.get("class");
	    			String processorClass=(String) m.get("processor");
	    			Map params=(Map) m.get("with");
	    			String debug="Setting up processors for [" + entityClass + "] ... ";
	    			try {
	    				
						Class<?> entityCls = Class.forName(entityClass);
						Class<?> processorCls = Class.forName(processorClass);
						
						EntityProcessor ep=null;
						if(params!=null){
							try{
								Constructor c=processorCls.getConstructor(Map.class);
								ep= (EntityProcessor) c.newInstance(params);
							}catch(Exception e){
								e.printStackTrace();
								Logger.warn("No Map constructor for " + processorClass);
							}
						}
						if(ep==null){
							ep = (EntityProcessor) processorCls.newInstance();
						}
						List<EntityProcessor> eplist=extraProcessors.get(entityCls);
						if(eplist==null){
							eplist=new ArrayList<EntityProcessor>();
							extraProcessors.put(entityCls, eplist);
						}
						eplist.add(ep);
						Logger.info(debug + "done");
					} catch (Exception e) {
						Logger.info(debug + "failed");
						e.printStackTrace();
					}
	    			
	    			
	    			
    			}
    			
    		}
    	}
    	_instance=this;
    }
    
    public static interface Hook{
    	public void invoke(Object o) throws Exception;
    	public String getName();
    }
    public static class InstanceMethodHook implements Hook{
    	public Method m;

    	public InstanceMethodHook(Method m){
    		this.m=m;
    	}

		@Override
		public void invoke(Object o) throws Exception {
			m.invoke(o);
		}

		@Override
		public String getName() {
			return m.getName();
		}
    }
    public static class StaticMethodHook implements Hook{
    	public Method m;

    	public StaticMethodHook(Method m){
    		this.m=m;
    	}

		@Override
		public void invoke(Object o) throws Exception {
			m.invoke(null,o);
		}

		@Override
		public String getName() {
			return m.getName();
		}
    }



    boolean debug (int level) {
        IxContext ctx = Play.application().plugin(IxContext.class);
        return ctx.debug(level);
    }

    public boolean isRegisterFor (Class<?> cls) {
        boolean registered = cls.isAnnotationPresent(Entity.class);
        if (registered) {
            for (Method m : cls.getMethods()) {
                Java8ForOldEbeanHelper.register (PrePersist.class, cls, m, preInsertCallback);
                Java8ForOldEbeanHelper.register (PostPersist.class, cls, m, postInsertCallback);
                Java8ForOldEbeanHelper.register (PreUpdate.class, cls, m, preUpdateCallback);
                Java8ForOldEbeanHelper.register (PostUpdate.class, cls, m, postUpdateCallback);
                Java8ForOldEbeanHelper.register (PreRemove.class, cls, m, preDeleteCallback);
                Java8ForOldEbeanHelper.register (PostRemove.class, cls, m, postDeleteCallback);
                Java8ForOldEbeanHelper.register (PostLoad.class, cls, m, postLoadCallback);
            }
        }
        for(Map.Entry<Class<?>,List<EntityProcessor>> entry : extraProcessors.entrySet()){
            Class<?> c = entry.getKey();
        	if(c.isAssignableFrom(cls)){
        		for(EntityProcessor ep :entry.getValue()){
        			Logger.info("Adding processor hooks... " + ep.getClass().getName() + " for "+ cls.getName());
        			addEntityProcessor(cls,ep);
        		}
        	}
        }
        if(cls.isAnnotationPresent(Backup.class)){
        	addEntityProcessor(cls,BackupProcessor.getInstance());
        }
        
        return registered;
    }
    
    private void addEntityProcessor(Class cls, EntityProcessor ep){


        Java8ForOldEbeanHelper.addPrePersistEntityProcessor(cls, preInsertCallback, ep);
        Java8ForOldEbeanHelper.addPostPersistEntityProcessor(cls, postInsertCallback, ep);
        Java8ForOldEbeanHelper.addPreUpdateEntityProcessor(cls, preUpdateCallback, ep);
        Java8ForOldEbeanHelper.addPostUpdateEntityProcessor(cls, postUpdateCallback, ep);
        Java8ForOldEbeanHelper.addPreRemoveEntityProcessor(cls, preDeleteCallback, ep);
        Java8ForOldEbeanHelper.addPostRemoveEntityProcessor(cls, postDeleteCallback, ep);
        Java8ForOldEbeanHelper.addPostLoadEntityProcessor(cls, postLoadCallback, ep);

    }
    


    void register (Class annotation,
                   Class cls, Method m, Map<String, List<Hook>> registry) {
        if (m.isAnnotationPresent(annotation)) {
            Logger.info("Method \""+m.getName()+"\"["+cls.getName()
                        +"] is registered for "+annotation.getName());
            Java8Util.createNewListIfAbsent(registry, cls.getName())
                    .add(new InstanceMethodHook(m));
        }
    }
    
    
    //public static long persistcount=0;
    @Override
    public boolean preInsert (BeanPersistRequest<?> request) {
    	
        Object bean = request.getBean();
        
        Class clazz = bean.getClass();
        
        TimeProfiler.addGlobalTime(clazz);
        if(bean instanceof Keyword){
        	TimeProfiler.addGlobalTime(((Keyword)bean).toString());
        }
        
        List<Hook> methods = preInsertCallback.get(clazz);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }
                catch (Exception ex) {
                	ex.printStackTrace();
					Logger.trace("Can't invoke method "
					        +clazz+"["+m.getName()+"]", ex);
                	throw new IllegalStateException(ex);
                }
            }
        }
        
        
        return true;
    }

    public void postInsertBeanDirect(Object bean){
    	Class clazz = bean.getClass();
        try {
            List<Hook> methods = postInsertCallback.get(clazz);
            if (methods != null) {
                for (Hook m : methods) {
                    try {
                        m.invoke(bean);
                    }
                    catch (Exception ex) {
                        Logger.trace
                            ("Can't invoke method "
                             +m.getName()+"["+clazz+"]", ex);
                    }
                }
            }
            makeIndexOnBean(bean);
        }
        catch (java.io.IOException ex) {
            Logger.trace("Can't index bean "+bean, ex);
        }
        TimeProfiler.stopGlobalTime(clazz);
        if(bean instanceof Keyword){
        	TimeProfiler.stopGlobalTime(((Keyword)bean).toString());
        }
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
    public static SequenceIndexer getSequenceIndexer(){
		if (seqProcessPlugin == null || !seqProcessPlugin.enabled()) {
			seqProcessPlugin = Play.application().plugin(SequenceIndexerPlugin.class);
		}
		return seqProcessPlugin.getIndexer();
	}
    
    public static StructureIndexer getStructureIndexer(){
    	if (strucProcessPlugin == null || !strucProcessPlugin.enabled()) {
    		strucProcessPlugin=Play.application().plugin(StructureIndexerPlugin.class);
		}
		return strucProcessPlugin.getIndexer();
    }
    
	private void makeIndexOnBean(Object bean) throws java.io.IOException {
		Java8ForOldEbeanHelper.makeIndexOnBean(this, EntityWrapper.of(bean));
	}
	
	private void deleteIndexOnBean(Object bean) throws Exception {
		Java8ForOldEbeanHelper.deleteIndexOnBean(this,EntityWrapper.of(bean));
	}
	
    @Override
    public boolean preUpdate (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        return preUpdateBeanDirect(bean);
    }
    
    public boolean preUpdateBeanDirect(Object bean){
    	Class clazz = bean.getClass();
        List<Hook> methods = preUpdateCallback.get(clazz);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }catch (Exception ex) {
                	ex.printStackTrace();
                    Logger.trace("Can't invoke method "
                                 +m.getName()+"["+clazz+"]", ex);
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public void postUpdate (BeanPersistRequest<?> request) {
    	
    	InxightTransaction it = InxightTransaction.getTransaction(request.getTransaction());
        final Object bean = request.getBean();
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
						if (!EntityPersistAdapter.isEditPresentUpdate(key)) { 
							Edit edit = new Edit(ew.getClazz(), key.getIdString());
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

        try {

            List<Hook> methods = postUpdateCallback.get(ew.getClazz());
            if (methods != null) {
                for (Hook m : methods) {
                    try {
                        m.invoke(bean);
                    }catch (Exception ex) {
                        Logger.trace
                            ("Can't invoke method "
                             +m.getName()+"["+ew.getClazz()+"]", ex);
                    }
                }
            }
            
            deleteIndexOnBean(bean);
            makeIndexOnBean(bean);
            
        }catch (Exception ex) {
            Logger.warn("Can't update bean index "+bean, ex);
        }
        
        IxCache.removeAllChildKeys(ew.getKey().toString());
    } 

    @Override
    public boolean preDelete (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        Class clazz = bean.getClass();
        
        List<Hook> methods = preDeleteCallback.get(clazz);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    if (m != null) {
                        m.invoke(bean);
                    }
                }catch (Exception ex) {
                	
                    Logger.trace("Can't invoke method "
                    		+m.getName()+"["+clazz+"]", ex);
                    throw new IllegalStateException(ex);
                }
            }
        }
        return true;
    }
    
    public void postDeleteBeanDirect (Object bean) {
    	Class clazz = bean.getClass();

        List<Hook> methods = postDeleteCallback.get(clazz);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }catch (Exception ex) {
                    Logger.trace("Can't invoke method "
                                 +m.getName()+"["+clazz+"]", ex);
                }
            }
        }

        try {
        	deleteIndexOnBean(bean);
        }
        catch (Exception ex) {
            Logger.trace("Can't remove bean "+bean+" from index!", ex);
        }
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

    @Override
    public void postLoad (Object bean, Set<String> includedProperties) {
        Class clazz = bean.getClass();
        List<Hook> methods = postLoadCallback.get(clazz);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }catch (Exception ex) {
                    Logger.trace("Can't invoke method "
                                 +m.getName()+"["+clazz+"]", ex);
                }
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
	            	if(alreadyLoaded.containsKey(key)){
	                    return;
	                }
	                alreadyLoaded.put(key, 0);
	            }
	            if(deleteFirst) {
	                deleteIndexOnBean(ew.getValue());
	            }
	            makeIndexOnBean(ew.getValue());
        	}
        } catch (Exception e) {
            Logger.error("Problem reindexing entity:" ,e);
        }
    }

    public static void doneReindexing(){
        alreadyLoaded.clear();
    }

	public TextIndexerPlugin getTextIndexerPlugin() {
		return textIndexerPlugin;
	}






}
