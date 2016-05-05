package ix.ginas.datasource;

import java.util.Iterator;
import java.util.List;

public interface DataSet<K> extends Iterable<K>{
	public Iterator<K> iterator();
	public boolean contains(K k);
}
