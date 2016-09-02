package ix.core.adapters;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import ix.core.search.EntityFetcher;
import ix.core.search.text.EntityUtils;
import ix.core.search.text.EntityUtils.EntityInfo;
import ix.core.search.text.EntityUtils.EntityWrapper;
import ix.core.util.Java8Util;
import ix.seqaln.SequenceIndexer;
import ix.utils.TimeProfiler;
import ix.utils.Tuple;
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
    
    private static ConcurrentHashMap<String, Integer> alreadyLoaded;
    
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

    /**
     * Preparing the edit ...
     * 
     * @param bean
     * @return
     */
    public static Edit storeEditForPossibleUpdate(Object bean){
    	if(bean==null)return null;
    	EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
    	String oldJSON = mapper.toJson(bean);
    	EntityInfo ei = EntityUtils.getEntityInfoFor(bean);
    	Tuple<String,String> f_id=ei.getFieldAndId(bean);
    	
    	Edit e = new Edit(ei.getClazz(),f_id.v());
    	e.oldValue=oldJSON;
    	e.version=ei.getVersionAsStringFor(bean);
    	
    	
    	storeEditForUpdate(f_id,e);
    	return e;
    }

    public interface ChangeOperation<T>{
        void apply(T obj) throws Exception;
    }

    public static <T> void performChange(String id, Supplier<T> objSupplier, ChangeOperation<T> changeOp){
       // Objects.requireNonNull(id);
        Objects.requireNonNull(objSupplier);
        Objects.requireNonNull(changeOp);

        MyLock lock = lockMap.computeIfAbsent(id, new Function<String, MyLock>() {
            @Override
            public MyLock apply(String key) {
                return new MyLock(key);
            }});

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
            	//This part wasn't right before ... should be ok now
            	//Still, when do we actually need to pop?
                popEditForUpdate(EntityUtils.getEntityInfoFor(bean).getFieldAndId(bean));
            }
            lock.release();
        }
    }

    
    private static void storeEditForUpdate(Tuple<String,String> fieldAndId, Edit e){
    	String s1=fieldAndId.k() + ":" + fieldAndId.v();
    	editMap.put(s1,e);
    }
    
    
    public static Edit popEditForUpdate(Tuple<String,String> fieldAndId){
    	String s1=fieldAndId.k() + ":" + fieldAndId.v();
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
    	EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
    	EntityWrapper<?> ew= new EntityWrapper(bean);
    	
    	if (ew.ignorePostUpdateHooks()) {
            return;
        }
    	
        Tuple<String,String> fieldAndId = ew.getIdAndFieldName();
                try {
                    if (fieldAndId.v() != null) {
                    	Edit edit=EntityPersistAdapter.popEditForUpdate(fieldAndId);
                    	//TP: Are these done 2 places now?
                    	//won't edits be stored twice?
                    	//Also, this isn't sufficient to capture everything.
                    	//It seems that it only grabs the previous values
                    	//that are top-level. If an object inside a collection,
                    	//or with some other identifier changes internally,
                    	//that info is lost.
                    	if(edit==null){
                    		 edit = new Edit (ew.getClazz(), fieldAndId.v());
                    		 edit.oldValue = mapper.toJson(oldvalues);
                    		 edit.version = ew.getVersion().orElse(null);
                    	}else{
                    		
                    	}
                    	edit.kind = ew.getKind();
                    	edit.newValue = mapper.toJson(bean);
               	     	edit.save();                        
                    }
                    else {
                        Logger.warn("Entity bean ["+ew.getKind()+"]="+fieldAndId.v()
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
        
        IxCache.removeAllChildKeys(ew.getGlobalKey());
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
    	Java8ForOldEbeanHelper.deepreindex(this,new EntityWrapper(bean),deleteFirst);
    }
    
	public void reindex(Object bean){
	    reindex(EntityWrapper.of(bean), true);
	}
	
    public void reindex(EntityWrapper ew, boolean deleteFirst){
    	
        try {
        	if(ew.hasGlobalKey()){
	        	String key=ew.getGlobalKey(); // Is this one the best to use?
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
