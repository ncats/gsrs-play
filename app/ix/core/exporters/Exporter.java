package ix.core.exporters;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import com.hazelcast.nio.IOUtil;

public interface Exporter<T> extends Closeable{
	void export(T obj) throws IOException;

	default void exportForEachAndClose(Iterator<T> it) throws IOException{
		try {
			it.forEachRemaining(t -> {
				try {
					export(t);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			});

		}finally{
			IOUtil.closeResource(this);
		}
	}
	
	default void exportForEachAndClose(Iterable<T> it) throws IOException{
		exportForEachAndClose(it.iterator());
	}
	
}