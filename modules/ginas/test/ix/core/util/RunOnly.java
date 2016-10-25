package ix.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Only run the following test in a test class.
 * This is like @Ignore but reversed.  Ignore everything
 * BUT this method.
 *
 * <p/>
 * This saves you the effort of manually marking
 * all other test methods with @Ignores and then remembering to
 * move them all back.
 *
 * Created by katzelda on 10/21/16.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunOnly {
}
