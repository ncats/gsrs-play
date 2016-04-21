package ix.core.util;

import java.io.Closeable;
import java.util.Iterator;

/**
 * An Iterator that wraps somekind of resource that
 * must be closed.  This can be used in a Java 7 try-with-resource.
 *
 *
 * Created by katzelda on 4/21/16.
 */
public interface CloseableIterator<T> extends Iterator<T>, Closeable{
}
