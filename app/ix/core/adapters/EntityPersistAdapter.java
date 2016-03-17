package ix.core.adapters;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.models.Edit;
import ix.core.models.Indexable;
import ix.core.plugins.IxContext;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.seqaln.SequenceIndexer;
import ix.utils.EntityUtils;
import play.Logger;
import play.Play;
import tripod.chem.indexer.StructureIndexer;
//import javax.annotation.PreDestroy;

public class EntityPersistAdapter extends BeanPersistAdapter {
   

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
    private static StructureIndexerPlugin strucProcessPlugin=Play.application().plugin(StructureIndexerPlugin.class);
    private static SequenceIndexerPlugin seqProcessPlugin=Play.application().plugin(SequenceIndexerPlugin.class);
    
    private static ConcurrentHashMap<String, String> alreadyLoaded = new ConcurrentHashMap<String,String>();
    
    private static ConcurrentHashMap<String, Edit> editMap = new ConcurrentHashMap<String,Edit>();
    
    
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
        		
        		registerProcessor(cls,ep,preInsertCallback,PrePersist.class);
        		registerProcessor(cls,ep,postInsertCallback,PostPersist.class);
        		registerProcessor(cls,ep,preUpdateCallback,PreUpdate.class);
        		registerProcessor(cls,ep,postUpdateCallback,PostUpdate.class);
        		registerProcessor(cls,ep,preDeleteCallback,PreRemove.class);
        		registerProcessor(cls,ep,postDeleteCallback,PostRemove.class);
        		registerProcessor(cls,ep,postLoadCallback,PostLoad.class);
        		
        	}
        }
        return registered;
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
        String name = bean.getClass().getName();
        List<Hook> methods = preUpdateCallback.get(name);
        if (methods != null) {
            for (Hook m : methods) {
                try {
                    m.invoke(bean);
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
    public void postUpdate (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
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
                    	edit.oldValue = mapper.toJson
                                (request.getOldValues());
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
            Logger.debug(">> Old: "+mapper.valueToTree(request.getOldValues())
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
        if(UPDATE_INDEX){
                reindex(bean);
        }
    }
    
    public void reindex(Object bean){
        String _id=EntityUtils.getIdForBeanAsString(bean);
        if(alreadyLoaded.containsKey(bean.getClass()+_id)){
            return;
        }
        
        try {      
        	makeIndexOnBean(bean);
            if(_id!=null)
            	alreadyLoaded.put(bean.getClass()+_id,_id);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
