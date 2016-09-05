package ix.core.models;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
/**
 * Adding this annotation to a field or method is meant to specify that
 * that is the field/method which signals whether the data has been validated.
 * 
 * Right now, this only makes sense on a method/field which returns a boolean.
 * 
 * 
 * 
 * TODO: When more data, beyond whether the record is / is not validated is desired,
 * what should be done? Should any class wishing to have this information accessible
 * return a specified "ValidationState" object? Or should the annotations be expanded,
 * and several fields used?
 * 
 * @author peryeata
 *
 */
public @interface DataValidated {}
