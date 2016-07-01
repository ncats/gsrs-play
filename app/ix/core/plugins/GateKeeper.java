package ix.core.plugins;

import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Created by katzelda on 5/26/16.
 */
public interface GateKeeper extends Closeable {
    boolean remove(String key);

    boolean removeAllChildKeys(String key);

    <T> T getSinceOrElse(String key, long creationTime, Callable<T> generator) throws Exception;

    <T> T getSinceOrElse(String key, long creationTime, Callable<T> generator, int seconds) throws Exception;

    <T> T getOrElseRaw(String key, Callable<T> generator, int seconds) throws Exception;

    Object get(String key);

    Object getRaw(String key);

    <T> T getOrElse(String key, Callable<T> generator, int seconds) throws Exception;

    void put(String key, Object value, int expiration);

    void putRaw(String key, Object value);

    void putRaw(String key, Object value, int expiration);

    boolean contains(String key);

    void put(String key, Object value);

    <T> T getOrElseRaw(String key, Callable<T> generator) throws Exception;

    <T> T getOrElse(String key, Callable<T> generator) throws Exception;

    @Override
    void close();

    Element getRawElement(String key);

    Stream<Element> elements(int top, int skip);

    Statistics getStatistics ();

    void clear();
}
