package ix.core.adapters;

import ix.core.models.Edit;
import ix.core.models.Indexable;
import ix.core.models.Structure;
import ix.core.plugins.IxContext;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.seqaln.SequenceIndexer;

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
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import play.Logger;
import play.Play;
import tripod.chem.indexer.StructureIndexer;

import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
//import javax.annotation.PreDestroy;

public class EntityPersistAdapter extends BeanPersistAdapter {
    private TextIndexerPlugin plugin = 
        Play.application().plugin(TextIndexerPlugin.class);

    private Map<String, List<Method>> preInsertCallback = 
        new HashMap<String, List<Method>>();
    private Map<String, List<Method>> postInsertCallback = 
        new HashMap<String, List<Method>>();
    private Map<String, List<Method>> preUpdateCallback = 
        new HashMap<String, List<Method>>();
    private Map<String, List<Method>> postUpdateCallback = 
        new HashMap<String, List<Method>>();
    private Map<String, List<Method>> preDeleteCallback = 
        new HashMap<String, List<Method>>();
    private Map<String, List<Method>> postDeleteCallback = 
        new HashMap<String, List<Method>>();
    private Map<String, List<Method>> postLoadCallback = 
        new HashMap<String, List<Method>>();
    
    public static SequenceIndexer _seqIndexer = Play.application()
            .plugin(SequenceIndexerPlugin.class).getIndexer();
    public static StructureIndexer _strucIndexer =
            Play.application().plugin(StructureIndexerPlugin.class).getIndexer();
    
    private static ConcurrentHashMap<String, String> alreadyLoaded = new ConcurrentHashMap<String,String>();
    
    private static boolean UPDATE_INDEX = false;
    
    public EntityPersistAdapter () {
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
        return registered;
    }

    void register (Class annotation,
                   Class cls, Method m, Map<String, List<Method>> registry) {
        if (m.isAnnotationPresent(annotation)) {
            Logger.info("Method \""+m.getName()+"\"["+cls.getName()
                        +"] is registered for "+annotation.getName());
            List<Method> methods = registry.get(cls.getName());
            if (methods == null) {
                registry.put(cls.getName(), methods = new ArrayList<Method>());
            }
            methods.add(m);
        }
    }
    
    @Override
    public boolean preInsert (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();
        List<Method> methods = preInsertCallback.get(name);
        if (methods != null) {
            for (Method m : methods) {
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
            List<Method> methods = postInsertCallback.get(name);
            if (methods != null) {
                for (Method m : methods) {
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

            if (plugin != null)
                plugin.getIndexer().add(bean);
        }
        catch (java.io.IOException ex) {
            Logger.trace("Can't index bean "+bean, ex);
        }
    }

    @Override
    public boolean preUpdate (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();

        List<Method> methods = preUpdateCallback.get(name);
        if (methods != null) {
            for (Method m : methods) {
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
        ObjectMapper mapper = new ObjectMapper ();
        
        Class cls = bean.getClass();
        if (Edit.class.isAssignableFrom(cls)) {
            // don't touch this class
            return;
        }

        for (Field f : cls.getFields()) {
            if (f.getAnnotation(Id.class) != null) {
                try {
                    Object id = f.get(bean);
                    if (id != null) {
                        Edit edit = new Edit (cls, id);
                        edit.oldValue = mapper.writeValueAsString
                            (request.getOldValues());
                        edit.newValue = mapper.writeValueAsString(bean);
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
            }
        }
        
        if (debug (2)) {
            Logger.debug(">> Old: "+mapper.valueToTree(request.getOldValues())
                         +"\n>> New: "+mapper.valueToTree(bean));
        }

        try {
            String name = cls.getName();
            List<Method> methods = postUpdateCallback.get(name);
            if (methods != null) {
                for (Method m : methods) {
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

            if (plugin != null)
                plugin.getIndexer().update(bean);
        }
        catch (java.io.IOException ex) {
            Logger.warn("Can't update bean index "+bean, ex);
        }
    }

    @Override
    public boolean preDelete (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();
        
        List<Method> methods = preDeleteCallback.get(name);
        if (methods != null) {
            for (Method m : methods) {
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

        List<Method> methods = postDeleteCallback.get(name);
        if (methods != null) {
            for (Method m : methods) {
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
            if (plugin != null)
                plugin.getIndexer().remove(bean);
        }
        catch (Exception ex) {
            Logger.trace("Can't remove bean "+bean+" from index!", ex);
        }
    }

    @Override
    public void postLoad (Object bean, Set<String> includedProperties) {
        String name = bean.getClass().getName();
        List<Method> methods = postLoadCallback.get(name);
        if (methods != null) {
            for (Method m : methods) {
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
        String _id=getIdForBeanAsString(bean);
        if(alreadyLoaded.containsKey(bean.getClass()+_id)){
            return;
        }
        
        
        try {      
            plugin.getIndexer().update(bean);
            if(bean instanceof Structure){
                if(_strucIndexer==null){
                    _strucIndexer=Play.application().plugin(StructureIndexerPlugin.class).getIndexer();
                }
                _strucIndexer.add(_id, ((Structure)bean).molfile);
            }
                
            Field seq=getSequenceIndexableField(bean);
            if(seq!=null){
                String indexSequence;
                try {
                    indexSequence = (String) seq.get(bean);
                    if(indexSequence!=null && indexSequence.length()>0){
                        //System.out.println("Indexing sequence:" + _id + "\t" + _seqIndexer + "\t" + indexSequence);
                        if(_seqIndexer==null){
                            _seqIndexer=Play.application()
                                .plugin(SequenceIndexerPlugin.class).getIndexer();
                        }
                                        
                        _seqIndexer.add(_id, indexSequence);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            alreadyLoaded.put(bean.getClass()+_id,_id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Object getIdForBean(Object entity){
        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return null;
        }
        try {
            Object id = null;
            Field f=getIdFieldForBean(entity);
            id = f.get(entity);
            if (id != null) {
                return id;
            }
        }
        catch (Exception ex) {
            Logger.trace("Unable to update index for "+entity, ex);
        }
        return null;
    }
    public static Field getIdFieldForBean(Object entity){
        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return null;
        }
        try {
            for (Field f : entity.getClass().getFields()) {
                if (f.getAnnotation(Id.class) != null) {
                    return f;
                }
            }
        } catch (Exception ex) {
            Logger.trace("Unable to update index for "+entity, ex);
        }
        return null;
    }
    public static Method getIdSettingMethodForBean(Object entity){
        Field f=getIdFieldForBean(entity);
        for(Method m:entity.getClass().getMethods()){
            if(m.getName().toLowerCase().equals("set" + f.getName().toLowerCase())){
                return m;
            }
        }
        return null;
    }
    public static String getIdForBeanAsString(Object entity){
        Object id=getIdForBean(entity);
        if(id!=null)return id.toString();
        return null;
    }
    public static String setIdForBean(Object entity){
        Object id=getIdForBean(entity);
        if(id!=null)return id.toString();
        return null;
    }
    public static Field getSequenceIndexableField(Object entity){
        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return null;
        }
        try {
                
            for (Field f : entity.getClass().getFields()) {
                Indexable ind= f.getAnnotation(Indexable.class);
                
                if (ind != null) {
                    //System.out.println("Looking at" + f);
                    if(ind.sequence()){
                        return f;
                    }
                }
               
            }
        }
        catch (Exception ex) {
            Logger.trace("Unable to update index for "+entity, ex);
        }
        return null;
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
