package ix.core.adapters;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
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

import ix.core.java8Util.Java8ForOldEbeanHelper;
import ix.core.util.Java8Util;
import org.apache.lucene.store.AlreadyClosedException;

import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;

import ix.core.EntityProcessor;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityCallable;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.models.Backup;
import ix.core.models.BaseModel;
import ix.core.models.Edit;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.plugins.IxCache;
import ix.core.plugins.IxContext;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.processors.BackupProcessor;
import ix.seqaln.SequenceIndexer;
import ix.utils.EntityUtils;
import ix.utils.TimeProfiler;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import tripod.chem.indexer.StructureIndexer;

public class EntityPersistAdapter extends BeanPersistAdapter{

    private static class MyLock{
        private Counter count = new Counter();
        private ReentrantLock lock = new ReentrantLock();


        private final String refId;

        public MyLock(String refId) {
            this.refId = refId;
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
    
    private static Map<String, MyLock> lockMap;
    
    private TextIndexerPlugin textIndexerPlugin =
            Play.application().plugin(TextIndexerPlugin.class);
    private static StructureIndexerPlugin strucProcessPlugin;
    private static SequenceIndexerPlugin seqProcessPlugin;
    
    private static ConcurrentHashMap<String, String> alreadyLoaded;
    
    public static EntityPersistAdapter getInstance(){
    	return _instance;
    }
    private static ConcurrentHashMap<String, Edit> editMap;


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

    public static Edit storeEditForPossibleUpdate(Object bean){
    	if(bean==null)return null;
    	EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
    	String oldJSON = mapper.toJson(bean);
    	Class cls = bean.getClass();
        Object id = EntityUtils.getIdForBean(bean);
    	Edit e = new Edit(cls,id);
    	e.oldValue=oldJSON;
    	e.version=EntityUtils.getVersionForBeanAsString(bean);
    	storeEditForUpdate(cls,id,e);
    	return e;
    }

    public interface ChangeOperation<T>{
        void apply(T obj) throws Exception;
    }

    public static <T> void performChange(String id, Supplier<T> objSupplier, ChangeOperation<T> changeOp){
       // Objects.requireNonNull(id);
        Objects.requireNonNull(objSupplier);
        Objects.requireNonNull(changeOp);

        MyLock lock = lockMap.computeIfAbsent(id, k -> new MyLock(k));

        lock.acquire();
        T bean = objSupplier.get();
        Edit e=null;
        try{
            e=storeEditForPossibleUpdate(bean);
            if(e ==null){
                return;
            }
            changeOp.apply(bean);
        }catch(Exception ex){
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }finally{
            if(e !=null) {
                popEditForUpdate(e.getClass(), e.refid);
            }
            lock.release();
        }
    }

//    public static void performChange(Object bean, final Callable change){
//    	Edit e=storeEditForPossibleUpdate(bean);
//    	if(e==null)return;
//    	try{
//    		change.call();
//    	}catch(Exception t){
//    		t.printStackTrace();
//    		throw new IllegalStateException(t);
//    	}finally{
//    		popEditForUpdate(e.getClass(),e.refid);
//    	}
//    }
    
    
    private static void storeEditForUpdate(Class c, Object id, Edit e){
    	String s1=c.getName() + ":" + id;
    	editMap.put(s1,e);
    }
    
    public static Edit popEditForUpdate(Class c, Object id){
    	String s1=c.getName() + ":" + id;
    	Edit e= editMap.get(s1);
    	if(e!=null){
    		editMap.remove(s1);
    	}
    	
    	return e;
    }
    public static int getEditUpdateCount(){
    	return editMap.size();
    }

    public EntityPersistAdapter () {
    	List<Object> ls= Play.application().configuration().getList("ix.core.entityprocessors",null);
    	if(ls!=null){
    		for(Object o:ls){
    			if(o instanceof Map){
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
//                register (PrePersist.class, cls, m, preInsertCallback);
//                register (PostPersist.class, cls, m, postInsertCallback);
//                register (PreUpdate.class, cls, m, preUpdateCallback);
//                register (PostUpdate.class, cls, m, postUpdateCallback);
//                register (PreRemove.class, cls, m, preDeleteCallback);
//                register (PostRemove.class, cls, m, postDeleteCallback);
//                register (PostLoad.class, cls, m, postLoadCallback);
//
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
    
    
    public static long persistcount=0;
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
		if (textIndexerPlugin != null){
			try{
				textIndexerPlugin.getIndexer().add(bean);
			}catch(Exception e){
				System.err.println(e.getMessage());
			}
		}

		List<Field> sequenceFields = getSequenceIndexableField(bean);
		if (sequenceFields != null && sequenceFields.size()>0) {
			String _id = EntityUtils.getIdForBeanAsString(bean);
			for(Field seq:sequenceFields){
				String indexSequence;
				try {
					indexSequence = (String) seq.get(bean);
					if (indexSequence != null && indexSequence.length() > 0) {
						getSequenceIndexer().add(_id, indexSequence);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		List<Field> structureFields = getStructureIndexableField(bean);
		if (structureFields != null && structureFields.size()>0) {
			String _id = EntityUtils.getIdForBeanAsString(bean);
			for(Field seq:structureFields){
				String structure;
				try {
					structure = (String) seq.get(bean);
					getStructureIndexer().add(_id, structure);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void deleteIndexOnBean(Object bean) throws Exception {
		if (textIndexerPlugin != null)
            textIndexerPlugin.getIndexer().remove(bean);
		String _id = EntityUtils.getIdForBeanAsString(bean);
		List<Field> sequenceFields = getSequenceIndexableField(bean);
		if (sequenceFields != null && sequenceFields.size()>0) {
			try{
				getSequenceIndexer().remove(_id);
			}catch(AlreadyClosedException e){
				System.err.println("Unable to remove index, due to concurrent modification, retrying once");
				getSequenceIndexer().remove(_id);
			}
		}
		List<Field> structureFields = getStructureIndexableField(bean);
		if (structureFields != null && structureFields.size()>0) {
			getStructureIndexer().remove(null, _id);
		}
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
    	EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
        String beanID=null;
        Class cls = bean.getClass();
        if (Edit.class.isAssignableFrom(cls)) {
            // don't touch this class
            return;
        }
        
                try {
                    Object id = EntityUtils.getId(bean);
                    
                    if (id != null) {
                    	beanID=id+"";
                    	Edit edit=EntityPersistAdapter.popEditForUpdate(cls, id);
                    	//TP: Are these done 2 places now?
                    	//won't edits be stored twice?
                    	//Also, this isn't sufficient to capture everything.
                    	//It seems that it only grabs the previous values
                    	//that are top-level. If an object inside a collection,
                    	//or with some other identifier changes internally,
                    	//that info is lost.
                    	if(edit==null){
                    		 edit = new Edit (cls, id);
                    		 edit.oldValue = mapper.toJson(oldvalues);
                    		 edit.version = EntityUtils.getVersionForBeanAsString(oldvalues);
                    	}else{
                    		
                    	}
                    	edit.kind = cls.getName();
                    	edit.newValue = mapper.toJson(bean);
               	     	edit.save();                        
                    }
                    else {
                        Logger.warn("Entity bean ["+cls.getName()+"]="+id
                                    +" doesn't have Id annotation!");
                    }
                }
                catch (Exception ex) {
                    Logger.trace("Can't retrieve bean id", ex);
                }
            
        
        if (debug (2)) {
            Logger.debug(">> Old: "+mapper.valueToTree(oldvalues)
                         +"\n>> New: "+mapper.valueToTree(bean));
        }

        try {

            List<Hook> methods = postUpdateCallback.get(cls);
            if (methods != null) {
                for (Hook m : methods) {
                    try {
                        m.invoke(bean);
                    }
                    catch (Exception ex) {
                        Logger.trace
                            ("Can't invoke method "
                             +m.getName()+"["+cls+"]", ex);
                    }
                }
            }
            deleteIndexOnBean(bean);
            makeIndexOnBean(bean);
            
        }
        catch (Exception ex) {
            Logger.warn("Can't update bean index "+bean, ex);
        }
        //This invalidates the cache for this bean
        String kindIDKey=bean.getClass().getName() + "._id" + ":" + beanID;
        
        IxCache.removeAllChildKeys(kindIDKey);
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
                }
                catch (Exception ex) {
                	
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
                }
                catch (Exception ex) {
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
                }
                catch (Exception ex) {
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
    	//reindex(bean);
    	
    	if(bean instanceof Model){
	    	EntityFactory.recursivelyApply((Model)bean, new EntityCallable(){
				@Override
				public void call(Object m, String path) {
						
					reindex(m, deleteFirst);
				}
	    	});
    	}
    	
    }
    
//    public static long reindexCount=0;
//    public Stack<Long> times= new Stack<Long>();
public void reindex(Object bean){
    reindex(bean, true);
}
    public void reindex(Object bean, boolean deleteFirst){
    	
    	String _id=null;
    	if(bean instanceof BaseModel){
    		_id=((BaseModel)bean).fetchGlobalId();
    	}else{
    		_id=EntityUtils.getIdForBeanAsString(bean);
    	}
        if(alreadyLoaded.containsKey(bean.getClass()+_id)){
            return;
        }
//        long ocount=reindexCount;
        try {
//        	long start=System.currentTimeMillis();
        	//times.push();
            if(_id!=null) {
                alreadyLoaded.put(bean.getClass() + _id, _id);
            }
            if(deleteFirst) {
                deleteIndexOnBean(bean);
            }
            makeIndexOnBean(bean);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(ocount==reindexCount){
//        	System.out.println(bean.getClass().getName());
//        }
//        reindexCount++;
        
    }
    public static List<Field> getSequenceIndexableField(Object entity){

        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return null;
        }
        List<Field> flist = new ArrayList<Field>();
        try {

            for (Field f : entity.getClass().getFields()) {
                Indexable ind= f.getAnnotation(Indexable.class);
                if (ind != null) {
                    if(ind.sequence()){
                    	flist.add(f);
                    }
                }
               
            }

        }catch (Exception ex) {
            Logger.trace("Unable to search for sequence indexes for "+entity, ex);
        }
        return flist;
    }
    public static List<Field> getStructureIndexableField(Object entity){

        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return null;
        }
        List<Field> flist = new ArrayList<Field>();
        try {
                
            for (Field f : entity.getClass().getFields()) {
                Indexable ind= f.getAnnotation(Indexable.class);
                if (ind != null) {
                    if(ind.structure()){
                    	flist.add(f);
                    }
                }
               
            }
        }catch (Exception ex) {
            Logger.trace("Unable to search for structure indexes for "+entity, ex);
        }
        return flist;
    }


    public static void doneReindexing(){
        alreadyLoaded.clear();
    }





}
