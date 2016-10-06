package ix.test;

import java.io.File;
import java.util.Optional;

/**
 * Created by katzelda on 5/27/16.
 */
public interface ClassFilter {
    /**
     * Should the given class name be allowed.
     *
     * @param className the class name to check.
     * @param root the source root directory of this class.
     * @param classFile the file path to this class object.
     *
     * @return an Optional which will either contain a non-null Class object
     * if the class should be included; or an empty optional otherwise.
     * Will never return null.
     */
    Optional<Class<?>> test(String className, File root, File classFile);
}
