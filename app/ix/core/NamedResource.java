package ix.core;

import ix.core.controllers.search.SearchRequest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface NamedResource {
    String name ();
    Class type ();
    String description () default "";
    boolean adminOnly() default false;
    boolean allowSearch() default true;

    Class<? extends SearchRequest.Builder> searchRequestBuilderClass() default SearchRequest.Builder.class;
}
