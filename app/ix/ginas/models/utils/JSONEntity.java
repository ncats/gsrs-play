package ix.ginas.models.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JSONEntity {
        // Used to denote members of GINAS JSON Schema

    String name() default "";
    String title() default "";
    String format() default "";
    boolean isReadOnly() default false; // property is read only
    boolean isRequired() default false; // property is required
    boolean isFinal() default false; // additionalProperties = false
    int minItems() default 0;
    int maxItems() default 0;
    String values() default ""; // name of String[] which defines allowed values
    String defaultValue() default "";
    String itemsTitle() default ""; // for List<String> classes only
    String itemsFormat() default ""; // for List<String> classes only
    boolean isUniqueItems() default false; // whether items must be unique
    
    String literalInjection() default ""; // last resort for getting our way (patternProperties)
    boolean expandInPlace() default false; // DEPRECATED
}
