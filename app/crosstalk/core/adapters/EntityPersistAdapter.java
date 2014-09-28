package crosstalk.core.adapters;

import play.Logger;
import java.util.Date;
import java.lang.reflect.*;
import com.avaje.ebean.event.*;
import javax.persistence.Entity;

import com.fasterxml.jackson.databind.ObjectMapper;

import crosstalk.core.models.*;
import crosstalk.utils.Global;

public class EntityPersistAdapter extends BeanPersistAdapter {

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
        Global g = Global.getInstance();
        Object bean = request.getBean();
        //Logger.debug("## indexing bean "+bean+"; global="+g);
        try {
            g.getTextIndexer().add(bean);
        }
        catch (java.io.IOException ex) {
            Logger.trace("Can't index bean "+bean, ex);
        }
    }

    @Override
    public boolean preUpdate (BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public void postUpdate (BeanPersistRequest<?> request) {
        Global g = Global.getInstance();
        Object bean = request.getBean();

        if (g.debug(2)) {
            ObjectMapper mapper = new ObjectMapper ();
            Logger.debug(">> Old: "+mapper.valueToTree(request.getOldValues())
                         +"\n>> New: "+mapper.valueToTree(bean));
        }

        try {
            g.getTextIndexer().update(bean);
        }
        catch (java.io.IOException ex) {
            Logger.warn("Can't update bean index "+bean, ex);
        }
    }
}
