package ix.core.adapters;

import play.Logger;
import play.Play;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.*;
import com.avaje.ebean.event.*;

import javax.persistence.Entity;
import javax.persistence.Id;
//import javax.annotation.PreDestroy;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.PreRemove;
import javax.persistence.PostRemove;

import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.*;
import ix.core.plugins.*;

public class EntityPersistAdapter extends BeanPersistAdapter {
    private TextIndexerPlugin plugin = 
        Play.application().plugin(TextIndexerPlugin.class);

    private Map<String, Method> preInsertCallback = 
        new HashMap<String, Method>();
    private Map<String, Method> postInsertCallback = 
        new HashMap<String, Method>();
    private Map<String, Method> preUpdateCallback = 
        new HashMap<String, Method>();
    private Map<String, Method> postUpdateCallback = 
        new HashMap<String, Method>();
    private Map<String, Method> preDeleteCallback = 
        new HashMap<String, Method>();
    private Map<String, Method> postDeleteCallback = 
        new HashMap<String, Method>();

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
                if (m.isAnnotationPresent(PrePersist.class)) {
                    preInsertCallback.put(cls.getName(), m);
                    Logger.info("Method "+m.getName()
                                +"["+cls.getName()
                                +"] is registered for PrePersist");
                }

                if (m.isAnnotationPresent(PostPersist.class)) {
                    postInsertCallback.put(cls.getName(), m);
                    Logger.info("Method "+m.getName()
                                +"["+cls.getName()
                                +"] is registered for PostPersist");
                }

                if (m.isAnnotationPresent(PreUpdate.class)) {
                    preUpdateCallback.put(cls.getName(), m);
                    Logger.info("Method "+m.getName()
                                +"["+cls.getName()
                                +"] is registered for PreUpdate");
                }

                if (m.isAnnotationPresent(PostUpdate.class)) {
                    postUpdateCallback.put(cls.getName(), m);
                    Logger.info("Method "+m.getName()
                                +"["+cls.getName()
                                +"] is registered for PostUpdate");
                }

                if (m.isAnnotationPresent(PreRemove.class)) {
                    preDeleteCallback.put(cls.getName(), m);
                    Logger.info("Method "+m.getName()
                                +"["+cls.getName()
                                +"] is registered for PreRemove");

                }

                if (m.isAnnotationPresent(PostRemove.class)) {
                    postDeleteCallback.put(cls.getName(), m);
                    Logger.info("Method "+m.getName()
                                +"["+cls.getName()
                                +"] is registered for PostRemove");
                }
            }
        }
        return registered;
    }
    
    @Override
    public boolean preInsert (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();
        Method m = preInsertCallback.get(name);
        if (m != null) {
            try {
                m.invoke(bean);
            }
            catch (Exception ex) {
                Logger.trace("Can't invoke method "
                             +name+"["+m.getName()+"]", ex);
            }
        }

        return true;
    }

    @Override
    public void postInsert (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();
        try {
            Method m = postInsertCallback.get(name);
            if (m != null) {
                try {
                    m.invoke(bean);
                }
                catch (Exception ex) {
                    Logger.trace
                        ("Can't invoke method "+m.getName()+"["+name+"]", ex);
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
        Method m = preUpdateCallback.get(name);
        try {
            if (m != null) {
                m.invoke(bean);
            }
        }
        catch (Exception ex) {
            Logger.trace("Can't invoke method "+m.getName()+"["+name+"]", ex);
        }
        return true;
    }

    @Override
    public void postUpdate (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
	ObjectMapper mapper = new ObjectMapper ();
	
	Class cls = bean.getClass();
	if (!Edit.class.isAssignableFrom(cls)) {
	    for (Field f : cls.getFields()) {
		if (f.getAnnotation(Id.class) != null) {
		    try {
			Object id = f.get(bean);
			if (id instanceof Long) {
			    Edit edit = new Edit (cls, (Long)id);
			    edit.oldValue = mapper.writeValueAsString
				(request.getOldValues());
			    edit.newValue = mapper.writeValueAsString(bean);
			    edit.save();
			}
			else {
			    Logger.warn("Entity bean ["+cls.getName()+"]="+id
					+" doesn't have id of type Long!");
			}
		    }
		    catch (Exception ex) {
			Logger.trace("Can't retrieve bean id", ex);
		    }
		}
	    }
	}
	
        if (debug (2)) {
            Logger.debug(">> Old: "+mapper.valueToTree(request.getOldValues())
                         +"\n>> New: "+mapper.valueToTree(bean));
        }

        String name = bean.getClass().getName();
        Method m = postUpdateCallback.get(name);
        try {
            if (m != null) {
                try {
                    m.invoke(bean);
                }
                catch (Exception ex) {
                    Logger.trace
                        ("Can't invoke method "+m.getName()+"["+name+"]", ex);
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
        Method m = preDeleteCallback.get(name);
        try {
            if (m != null) {
                m.invoke(bean);
            }
        }
        catch (Exception ex) {
            Logger.trace("Can't invoke method "+m.getName()+"["+name+"]", ex);
        }
        return true;
    }

    @Override
    public void postDelete (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        String name = bean.getClass().getName();
        Method m = postDeleteCallback.get(name);
        try {
            if (m != null)
                m.invoke(bean);
        }
        catch (Exception ex) {
            Logger.trace("Can't invoke method "+m.getName()+"["+name+"]", ex);
        }
    }
}
