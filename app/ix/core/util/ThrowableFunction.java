package ix.core.util;

import java.util.function.Function;

/**
 * This is just an extension of the {@link Function} interface
 * which will wrap a checked exception as a {@link RuntimeException}.
 * This is used for simplifying the Lambda functions that can throw an
 * error.
 *
 */
@FunctionalInterface
public interface ThrowableFunction<T,V> extends Function<T,V>{

    @Override
    default V apply(T t){
        try {
            return applyThrowable(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public V applyThrowable(T t) throws Exception;
}
