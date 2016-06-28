package ix.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Results;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Utility class  to help transition the inxight
 * code base from Java 7 to Java 8 without upgrading
 * any of the depencdencies.
 *
 * Created by katzelda on 5/10/16.
 */
public final class Java8Util {

    private Java8Util(){
        //can not instantiate
    }

    /**
     * Wrapper around Play's Results.ok() method that takes a JsonNode.
     * For some reason the Java 8 compiler type inferencing gets confused
     * and we get a compiler error about an ambigious method call since there are
     * several Results.ok( Object) methods and it can't tell which one to use.
     * By using this intermediate method, the compiler doesn't have to infer anything.
     * @param content the JsonNode to wrap.
     * @return a 200 Status object.
     */
    public static Results.Status ok(JsonNode content) {
        return Results.ok(content);
    }


    public static <K, V> List<V> createNewListIfAbsent(Map<K, List<V>> map, K key){
        return map.computeIfAbsent( key, k-> new ArrayList<V>());
    }

}
