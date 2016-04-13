package ix.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import ix.core.models.DataVersion;
import ix.ginas.models.v1.Moiety;
import play.Logger;

public class EntityUtils {

    
    public static List getAnnotatedValues (Object entity, Class... annotation)
        throws Exception {
        List values = new ArrayList ();
        for (Field f : getFields (entity, annotation)) {
            Object v = f.get(entity);
            if (v != null)
                values.add(v);
        }
        return values;
    }
    public static List<Field> getFields (Object entity, Class... annotation) {
        List<Field> fields = new ArrayList<Field>();
        for (Field f : entity.getClass().getFields()) {
            for (Class c : annotation) {
                if (f.getAnnotation(c) != null) {
                    fields.add(f);
                    break;
                }
            }
        }
        return fields;
    }
    
    public static List<Field> getFieldsForClass (Class entity, Class... annotation) {
        List<Field> fields = new ArrayList<Field>();
        for (Field f : entity.getFields()) {
            for (Class c : annotation) {
                if (f.getAnnotation(c) != null) {
                    fields.add(f);
                    break;
                }
            }
        }
        return fields;
    }
    public static Object getId (Object entity) throws Exception {
    	if(entity instanceof Moiety){
    		return ((Moiety)entity).getUUID();
    	}
        Field f = getIdField (entity);
        Object id = null;
        if (f != null) {
            id = f.get(entity);
            if (id == null) { // now try bean method
                try {
                    Method m = entity.getClass().getMethod
                        ("get"+getBeanName (f.getName()));
                    id = m.invoke(entity, new Object[0]);
                }
                catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return id;
    }
    public static Object getVersion (Object entity) throws Exception {
        Field f = getVersionField (entity);
        Object version = null;
        if (f != null) {
            version = f.get(entity);
            if (version == null) { // now try bean method
                try {
                    Method m = entity.getClass().getMethod
                        ("get"+getBeanName (f.getName()));
                    version = m.invoke(entity, new Object[0]);
                }
                catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return version;
    }

    public static Field getVersionField (Object entity) throws Exception {
        List<Field> fields = getFields (entity, DataVersion.class);
        return fields.isEmpty() ? null : fields.iterator().next();
    }
    public static Field getIdField (Object entity) throws Exception {
        List<Field> fields = getFields (entity, Id.class);
        return fields.isEmpty() ? null : fields.iterator().next();
    }
    public static Field getIdFieldForClass (Class entity) throws Exception {
        List<Field> fields = getFieldsForClass (entity, Id.class);
        return fields.isEmpty() ? null : fields.iterator().next();
    }
    static String getBeanName (String field) {
        return Character.toUpperCase(field.charAt(0))+field.substring(1);
    }

	public static String getIdForBeanAsString(Object entity){
	    Object id=getIdForBean(entity);
	    if(id!=null)return id.toString();
	    return null;
	}
	
	public static String getVersionForBeanAsString(Object entity){
		try{
		    Object version=EntityUtils.getVersion(entity);
		    if(version!=null)return version.toString();
		}catch(Exception e){
			Logger.warn(e.getMessage());
		}
	    return null;
	}

	public static Object getIdForBean(Object entity){
	    if (!entity.getClass().isAnnotationPresent(Entity.class)) {
	        return null;
	    }
	    try {
	        Object id = EntityUtils.getId(entity);
	        if (id != null) return id;
	    }
	    catch (Exception ex) {
	        Logger.trace("Unable to fetch ID for "+entity, ex);
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

}
