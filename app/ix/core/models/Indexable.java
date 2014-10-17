package ix.core.models;

import java.lang.annotation.*;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Indexable {
    boolean indexed () default true;
    boolean taxonomy () default false;
    boolean facet () default false;
    boolean suggest () default false;
    String pathsep () default "/"; // path separator for
    int[] intRanges () default {};
    long[] longRanges () default {};
    double[] doubleRanges () default {};
    // if empty, use the instance variable name
    String name () default "";
}
