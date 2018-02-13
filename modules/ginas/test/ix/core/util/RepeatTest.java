package ix.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a test with this to tell
 * the runner how many times to repeat this test.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatTest {
    /**
     * How many times to repeat the test (default is 1).
     * @return the number of times as an int.
     */
    int times() default 1;
}
