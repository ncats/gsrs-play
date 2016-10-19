package ix.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker for experimental elements which are not yet considered
 * to be fully implemented, or are very likely to change in the
 * near future. Care should be taken when using any element annotated
 * with {@link Experimental}.
 * @author peryeata
 *
 */
@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Target(value={ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PACKAGE})
public @interface Experimental {

}
