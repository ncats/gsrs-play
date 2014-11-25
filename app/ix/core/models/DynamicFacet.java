package ix.core.models;

import java.lang.annotation.*;

/**
 * This class-level annotation provides a way to specify dynamic
 * facet categories by defining the two fields one for the facet
 * label and the other for its value.
 */
@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface DynamicFacet {
    String label (); // field that specifies the facet label
    String value (); // field that specifies the facet value
}
