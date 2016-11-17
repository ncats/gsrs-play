package ix.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import ix.core.Experimental;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.pojopointer.PojoPointer;
import ix.utils.Tuple;
import play.Play;

/**
 * Basic static utility helper class to get configuration parameters from
 * the config file, either lazily, or directly through play.
 * 
 * <p>
 * The advantage of this helper class is it's possible
 * to overwrite the play configuration, and instead use
 * some other value. Static calls to {@link Play#application()},
 * (which are used sometimes in the codebase) make it difficult
 * to write some targeted tests, as a started application must
 * be present before the class can initialize, even if the 
 * specific config variable being set will never be used in the
 * area being tested. To avoid that, the use of a lazy-loading
 * method {@link #supplierOf(String, Object)} allows the creation
 * of a {@link CachedSupplier} for the specific variable needed.
 * </p>
 * 
 * <p>
 * In addition, {@link #setConfigurationResolver(BiFunction)} can
 * be used to overwrite the default configuration, or even probe
 * into what configuration parameters are being retrieved.
 * </p>
 * 
 * 
 * @author peryeata
 *
 */
@Experimental
public class ConfigHelper {
    private final static CachedSupplier<ConcurrentHashMap<String, Object>> configVars = CachedSupplier.of(() -> {
        return new ConcurrentHashMap<String, Object>();
    });

    public static <T> CachedSupplier<T> supplierOf(String path, T d) {
        return CachedSupplier.of(() -> {
            return getOrDefault(path, d);
        });
    }

    public static int getInt(String path, int def) {
        return getOrDefault(path, def);
    }

    public static boolean getBoolean(String path, boolean def) {
        return getOrDefault(path, def);
    }

    public static long getLong(String path, long def) {
        return getOrDefault(path, def);
    }

    public static <T> T getOrDefault(String path, T def) {
        Class<?> cls;
        if (def == null) {
            cls = Object.class;
        } else {
            cls = def.getClass();
        }

        return (T) configVars.get().computeIfAbsent(path, p -> {
            return resolver.apply(Tuple.of(path, cls), def);
        });
    }

    /**
     * Explicitly set a configuration variable
     * 
     * @param path
     * @param value
     */
    public static <T> void setConfig(String path, T value) {
        configVars.get().put(path, value);
    }

    /**
     * Explicitly sets the configuration fetcher
     * 
     * @param resolve
     */
    public static void setConfigurationResolver(BiFunction<Tuple<String, Class<?>>, Object, Object> resolve) {
        resolver = resolve;
    }

    private final static BiFunction<Tuple<String, Class<?>>, Object, Object> PLAY_CONFIG_RESOLVER = ((f, d) -> {
        String key = f.k();
        Class<?> cls = f.v();

        if (cls.isAssignableFrom(Long.class)) {
            return Play.application().configuration().getLong(key, (Long) d);
        }
        if (cls.isAssignableFrom(Boolean.class)) {
            return Play.application().configuration().getBoolean(key, (Boolean) d);
        }

        Object o = EntityWrapper.of(Play.application().configuration().asMap())
                .at(PojoPointer.fromURIPath(key.replace(".", "/"))) // This is a
                                                                    // silly way
                .map(e -> (Object) e.getValue()) // ... but it works
                .orElse(d);
        
        
        return o;

    });
    public static BiFunction<Tuple<String, Class<?>>, Object, Object> resolver = PLAY_CONFIG_RESOLVER;

}
