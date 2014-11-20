package ix.core.adapters;

import play.Logger;
import play.Play;
import java.util.Date;
import java.lang.reflect.*;
import com.avaje.ebean.event.*;
import javax.persistence.Entity;

import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.*;
import ix.core.plugins.*;

public class EntityPersistAdapter extends BeanPersistAdapter {
    TextIndexerPlugin plugin = 
        Play.application().plugin(TextIndexerPlugin.class);

    public EntityPersistAdapter () {
    }

    public boolean isRegisterFor (Class<?> cls) {
        return cls.isAnnotationPresent(Entity.class);
    }
    
    @Override
    public boolean preInsert (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        if (ETag.class.isAssignableFrom(bean.getClass())) {
            try {
                Method m = bean.getClass()
                    .getMethod("setModified", Date.class);
                m.invoke(bean, new Date ());
                Logger.debug("Updating "+((ETag)bean).etag);
            }
            catch (Exception ex) {
                Logger.trace("Can't create ETag", ex);
                return false;
            }
        }
        return true;
    }

    @Override
    public void postInsert (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        //Logger.debug("## indexing bean "+bean+"; global="+g);
        try {
            if (plugin != null)
                plugin.getIndexer().add(bean);
        }
        catch (java.io.IOException ex) {
            Logger.trace("Can't index bean "+bean, ex);
        }
    }

    boolean debug (int level) {
        IxContext ctx = Play.application().plugin(IxContext.class);
        return ctx.debug(level);
    }

    @Override
    public boolean preUpdate (BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public void postUpdate (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        if (debug (2)) {
            ObjectMapper mapper = new ObjectMapper ();
            Logger.debug(">> Old: "+mapper.valueToTree(request.getOldValues())
                         +"\n>> New: "+mapper.valueToTree(bean));
        }

        try {
            if (plugin != null)
                plugin.getIndexer().update(bean);
        }
        catch (java.io.IOException ex) {
            Logger.warn("Can't update bean index "+bean, ex);
        }
    }
}
