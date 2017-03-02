package ix.core.util;

import java.lang.reflect.Field;

/**
 * Created by katzelda on 3/1/17.
 */
public class ReflectionUtil {

    public  static <T>  T getFieldValue(Object obj, String fieldName){
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(obj);
        }catch(NoSuchFieldException  | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }
}
