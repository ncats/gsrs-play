package models.core;

import play.Logger;
import java.util.Date;
import java.lang.reflect.*;
import com.avaje.ebean.event.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityPersistAdapter extends BeanPersistAdapter {

    public EntityPersistAdapter () {
    }

    public boolean isRegisterFor (Class<?> cls) {
        return ETag.class.isAssignableFrom(cls);
        /*
        try {
            modified = cls.getMethod("setModified", Date.class);
            Logger.debug("Listening to entity "+cls.getName());
        }
        catch (Exception ex) {
        }

        return modified != null;
        */
    }

    @Override
    public boolean preInsert (BeanPersistRequest<?> request) {
        Object bean = request.getBean();
        try {
            Method m = bean.getClass().getMethod("setModified", Date.class);
            m.invoke(bean, new Date ());
            Logger.debug("Updating "+((ETag)bean).etag);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    @Override
    public void postUpdate (BeanPersistRequest<?> request) {
        ObjectMapper mapper = new ObjectMapper ();
        Logger.debug(">> Old: "+mapper.valueToTree(request.getOldValues())
                     +"\n>> New: "+mapper.valueToTree(request.getBean()));
        
    }
}
