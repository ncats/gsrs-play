package ix.ginas.exporters;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface Exporter<T> extends Closeable{
	void export(T obj) throws IOException;
	default void exportForEachAndClose(Iterator<T> it) throws IOException{
		it.forEachRemaining(t -> {
			try {
				export(t);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		this.close();
	}
	public String getExtension();
	
}