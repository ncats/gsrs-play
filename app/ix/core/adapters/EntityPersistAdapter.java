package ix.core.adapters;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

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
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityCallable;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.models.Backup;
import ix.core.models.BaseModel;
import ix.core.models.Edit;
import ix.core.models.Indexable;
import ix.core.plugins.IxContext;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.processors.BackupProcessor;
import ix.seqaln.SequenceIndexer;
import ix.utils.EntityUtils;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import tripod.chem.indexer.StructureIndexer;
//import javax.annotation.PreDestroy;

public class EntityPersistAdapter extends BeanPersistAdapter {
   
	private static EntityPersistAdapter _instance =null;
	
	

    private Map<String, List<Hook>> preInsertCallback = 
        new HashMap<String, List<Hook>>();
    private Map<String, List<Hook>> postInsertCallback = 
        new HashMap<String, List<Hook>>();
    private Map<String, List<Hook>> preUpdateCallback = 
        new HashMap<String, List<Hook>>();
    private Map<String, List<Hook>> postUpdateCallback = 
        new HashMap<String, List<Hook>>();
    private Map<String, List<Hook>> preDeleteCallback = 
        new HashMap<String, List<Hook>>();
    private Map<String, List<Hook>> postDeleteCallback = 
        new HashMap<String, List<Hook>>();
    private Map<String, List<Hook>> postLoadCallback = 
        new HashMap<String, List<Hook>>();
    
    private Map<Class, EntityProcessor> extraProcessors=new HashMap<Class,EntityProcessor>();
    
    
    
    private TextIndexerPlugin plugin = 
            Play.application().plugin(TextIndexerPlugin.class);
    //public static SequenceIndexer _seqIndexer = Play.application().plugin(SequenceIndexerPlugin.class).getIndexer();
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

        alreadyLoaded = new ConcurrentHashMap<String,String>(10000);

        editMap = new ConcurrentHashMap<String,Edit>();
    }

    
    public static void storeEditForUpdate(Class c, Object id, Edit e){
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
    
    private static boolean UPDATE_INDEX = false;
    
    public EntityPersistAdapter () {
    	List<Object> ls= Play.application().configuration().getList("ix.core.entityprocessors",null);
    	if(ls!=null){
    		for(Object o:ls){
    			if(o instanceof Map){
	    			Map m = (Map)o;
	    			String entityClass=(String) m.get("class");
	    			String processorClass=(String) m.get("processor");
	    			String debug="Setting up processors for [" + entityClass + "] ... ";
	    			try {
	    				
						Class entityCls = Class.forName(entityClass);
						Class processorCls = Class.forName(processorClass);
						extraProcessors.put(entityCls, (EntityProcessor) processorCls.newInstance());
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
    public static class EntityProcessorHook implements Hook{
    	private EntityProcessor ep;
    	private Method useMethod;
    	public EntityProcessorHook(EntityProcessor ep, Class<?> hookAnnotation){
    		this.ep=ep;
    		try{
	    		if(hookAnnotation.equals(PrePersist.class)){
					useMethod=ep.getClass().getMethod("prePersist",Object.class);
					
	    		}else if(hookAnnotation.equals(PostPersist.class)){
					useMethod=ep.getClass().getMethod("postPersist",Object.class);
					
	    		}else if(hookAnnotation.equals(PreUpdate.class)){
					useMethod=ep.getClass().getMethod("preUpdate",Object.class);
					
	    		}else if(hookAnnotation.equals(PostUpdate.class)){
					useMethod=ep.getClass().getMethod("postUpdate",Object.class);
					
	    		}else if(hookAnnotation.equals(PreRemove.class)){
					useMethod=ep.getClass().getMethod("preRemove",Object.class);
					
	    		}else if(hookAnnotation.equals(PostRemove.class)){
					useMethod=ep.getClass().getMethod("postRemove",Object.class);
					
	    		}else if(hookAnnotation.equals(PostLoad.class)){
					useMethod=ep.getClass().getMethod("postLoad",Object.class);
	    		}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		
    	} 
    	
		@Override
		public void invoke(Object o) throws Exception {
			if(useMethod!=null){
				useMethod.invoke(ep, o);
			}
		}

		@Override
		public String getName() {
			return useMethod.getName();
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
                register (PrePersist.class, cls, m, preInsertCallback);
                register (PostPersist.class, cls, m, postInsertCallback);
                register (PreUpdate.class, cls, m, preUpdateCallback);
                register (PostUpdate.class, cls, m, postUpdateCallback);
                register (PreRemove.class, cls, m, preDeleteCallback);
                register (PostRemove.class, cls, m, postDeleteCallback);
                register (PostLoad.class, cls, m, postLoadCallback);
            }
        }
        for(Class c:extraProcessors.keySet()){
        	if(c.isAssignableFrom(cls)){
        		EntityProcessor ep =extraProcessors.get(c);
        		Logger.info("Adding processor hooks... " + ep.getClass().getName() + " for "+ cls.getName());
        		addEntityProcessor(cls,ep);
        	}
        }
        if(cls.isAnnotationPresent(Backup.class)){
        	addEntityProcessor(cls,BackupProcessor.getInstance());
        }
        
        return registered;
    }
    private void addEntityProcessor(Class cls, EntityProcessor ep){
    	registerProcessor(cls,ep,preInsertCallback,PrePersist.class);
		registerProcessor(cls,ep,postInsertCallback,PostPersist.class);
		registerProcessor(cls,ep,preUpdateCallback,PreUpdate.class);
		registerProcessor(cls,ep,postUpdateCallback,PostUpdate.class);
		registerProcessor(cls,ep,preDeleteCallback,PreRemove.class);
		registerProcessor(cls,ep,postDeleteCallback,PostRemove.class);
		registerProcessor(cls,ep,postLoadCallback,PostLoad.class);
    }
    
    void registerProcessor(Class cls, EntityProcessor ep, Map<String, List<Hook>> registry, Class<?> hookAnnotation){
    	List<Hook> methods = registry.get(cls.getName());
    	if (methods == null) {
            registry.put(cls.getName(), methods = new ArrayList<Hook>());
        }
        methods.add(new EntityProcessorHook(ep,hookAnnotation));
    }

    void register (Class annotation,
                   Class cls, Method m, Map<String, List<Hook>> registry) {
        if (m.isAnnotationPresent(annotation)) {
            Logger.info("Method \""+m.getName()+"\"["+cls.getName()
                        +"] is registered for "+annotation.getName());
            List<Hook> methods = registry.get(cls.getName());
            if (methods == null) {
                registry.put(cls.getName(), methods = new ArrayList<Hook>());
            }
            methods.add(new InstanceMethodHook(m));
        }
    }
    public static class Counter{
    	private Map<String,Long> count= new LinkedHashMap<String,Long>();
    	public void addTo(String key, long c){
    		Long o=count.get(key);
    		if(o==null){
    			o=0l;
    		}
    		o=o+c;
    		count.put(key,o);
    	}
    	public void increment(String key){
    		addTo(key,1);
    	}
    	public long get(String key){
    		return count.get(key);
    	}
    	public Set<String> getKeys(){
    		return count.keySet();
    	}
    }
    public static class TimeProfiler{
    	private Counter numberCount = new Counter();
    	private Counter timeCount = new Counter();
    	private Map<String,Long> startTimes = new HashMap<String,Long>();
    	
    	public void addTime(Object o){
    		numberCount.increment(o.toString());
    		startTimes.put(o.toString(), System.currentTimeMillis());
    	}
    	public void stopTime(Object o){
    		timeCount.addTo(o.toString(), (System.currentTimeMillis()-startTimes.get(o.toString())));
    		startTimes.remove(o.toString());
    	}
    	public void printResults(){
    		System.out.println("===========");
    		for(String s:numberCount.getKeys()){
    			double average=0;
    			average+=timeCount.get(s)/(0.0+numberCount.get(s));
    			
    			System.out.println(s + "\t" + 
    					numberCount.get(s) + "\t" + 
    					timeCount.get(s) + "\t" 
    					+ average);
    		}
    		System.out.println("==========");
    	}
    	
    }
    public static TimeProfiler timeProfile= new TimeProfiler();
    public static long persistcount=0;
    @Override
    public boolean preInsert (BeanPersistRequest<?> request) {
    	
        Object bean = request.getBean();
        
        String name = bean.getClass().getName();
        
        List<Hook> methods = preInsertCallback.get(name);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }
                catch (Exception ex) {
                    Logger.trace("Can't invoke method "
                                 +name+"["+m.getName()+"]", ex);
                    return false;
                }
            }
        }
        persistcount++;
        timeProfile.addTime(name);
        return true;
    }

    @Override
    public void postInsert (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();
        try {
            List<Hook> methods = postInsertCallback.get(name);
            if (methods != null) {
                for (Hook m : methods) {
                    try {
                        m.invoke(bean);
                    }
                    catch (Exception ex) {
                        Logger.trace
                            ("Can't invoke method "
                             +m.getName()+"["+name+"]", ex);
                    }
                }
            }
            makeIndexOnBean(bean);
            
        }
        catch (java.io.IOException ex) {
            Logger.trace("Can't index bean "+bean, ex);
        }
        timeProfile.stopTime(name);
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
		if (plugin != null){
			plugin.getIndexer().add(bean);
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
		if (plugin != null)
            plugin.getIndexer().remove(bean);
		String _id = EntityUtils.getIdForBeanAsString(bean);
		List<Field> sequenceFields = getSequenceIndexableField(bean);
		if (sequenceFields != null && sequenceFields.size()>0) {
			getSequenceIndexer().remove(_id);
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
    	String name = bean.getClass().getName();
        List<Hook> methods = preUpdateCallback.get(name);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }catch (Exception ex) {
                    Logger.trace("Can't invoke method "
                                 +m.getName()+"["+name+"]", ex);
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public void postUpdate (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        postUpdateBeanDirect(bean,request.getOldValues());
    }
    
    public void postUpdateBeanDirect(Object bean, Object oldvalues){
    	EntityMapper mapper = EntityMapper.FULL_ENTITY_MAPPER();
        
        Class cls = bean.getClass();
        if (Edit.class.isAssignableFrom(cls)) {
            // don't touch this class
            return;
        }
        
                try {
                    Object id = EntityUtils.getId(bean);
                    
                    if (id != null) {
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
                    	}
                    	edit.oldValue = mapper.toJson(oldvalues);
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
            String name = cls.getName();
            List<Hook> methods = postUpdateCallback.get(name);
            if (methods != null) {
                for (Hook m : methods) {
                    try {
                        m.invoke(bean);
                    }
                    catch (Exception ex) {
                        Logger.trace
                            ("Can't invoke method "
                             +m.getName()+"["+name+"]", ex);
                    }
                }
            }
            deleteIndexOnBean(bean);
            makeIndexOnBean(bean);
        }
        catch (Exception ex) {
            Logger.warn("Can't update bean index "+bean, ex);
        }
    } 

    @Override
    public boolean preDelete (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();
        
        List<Hook> methods = preDeleteCallback.get(name);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    if (m != null) {
                        m.invoke(bean);
                    }
                }
                catch (Exception ex) {
                    Logger.trace("Can't invoke method "
                                 +m.getName()+"["+name+"]", ex);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void postDelete (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();

        List<Hook> methods = postDeleteCallback.get(name);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }
                catch (Exception ex) {
                    Logger.trace("Can't invoke method "
                                 +m.getName()+"["+name+"]", ex);
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
    public void postLoad (Object bean, Set<String> includedProperties) {
        String name = bean.getClass().getName();
        List<Hook> methods = postLoadCallback.get(name);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
                }
                catch (Exception ex) {
                    Logger.trace("Can't invoke method "
                                 +m.getName()+"["+name+"]", ex);
                }
            }
        }
//        if(UPDATE_INDEX){
//               reindex(bean);
//        }
    }
    
    public void deepreindex(Object bean){
//    	long start=System.nanoTime();
//    	final long[] l= new long[]{0};
    	if(bean instanceof Model){
	    	EntityFactory.recursivelyApply((Model)bean, new EntityCallable(){
				@Override
				public void call(Object m, String path) {
//					long start2=System.nanoTime();
					reindex(m);
//					l[0]+=(System.nanoTime()-start2);
				}
	    	});
    	}
//    	long duration = (System.nanoTime()-start);
//    	System.out.println("Reindexing took:" + duration + " with " + l[0] + " for actual indexing");
//    	System.out.println("Basic I/O fraction:" + ((duration-l[0]+0.0)/(duration+0.0)));
    	
    }
    
//    public static long reindexCount=0;
//    public Stack<Long> times= new Stack<Long>();
    public void reindex(Object bean){
    	
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
            if(_id!=null)
                alreadyLoaded.put(bean.getClass()+_id,_id);
            deleteIndexOnBean(bean);
            makeIndexOnBean(bean);
            
//            times.pop();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(ocount==reindexCount){
//        	System.out.println(bean.getClass().getName());
//        }
//        reindexCount++;
        
    }
    public static List<Field> getSequenceIndexableField(Object entity){
    	List<Field> flist = new ArrayList<Field>();
        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return null;
        }
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
    	List<Field> flist = new ArrayList<Field>();
        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return null;
        }
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
    

    public static boolean isUpdatingIndex() {
        return UPDATE_INDEX;
    }

    public static void setUpdatingIndex(boolean update) {
        if(update!=UPDATE_INDEX){
            alreadyLoaded.clear();
            UPDATE_INDEX = update;
        }
    }
}
