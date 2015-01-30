package ix.core;

import java.lang.annotation.*;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface NamedResource {
    String name ();
    Class type ();
    String description () default "";
}
