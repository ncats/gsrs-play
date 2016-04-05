package ix.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Types annotated with "@SingleParent" are considered
 * to "belong to" some other entity, such that they should
 * be deleted when the parent is deleted.
 * 
 * This can be used as a signal to perform certain database actions.
 * Specifically, it can be used to simulate the effects of
 * JPA 2.0 "orphanRemoval", where being removed from a parent 
 * list will always trigger a deletion of the element.
 * 
 * https://docs.oracle.com/cd/E19798-01/821-1841/giqxy/
 * 
 * Unlike orphanRemoval, however, it is not necessary to know
 * the exact parent collection field that possesses this object.
 * 
 * @author peryeata
 *
 */
@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Target(value={ElementType.TYPE})
public @interface SingleParent {
    
}
