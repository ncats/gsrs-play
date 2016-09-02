package ix.core.search.text;

import java.util.function.Consumer;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexableField;

import ix.core.models.Indexable;

public interface PrimitiveFieldIndexerPassiveProvider{
	public void defaultIndex(Consumer<IndexableField> fields, 
			Indexable indexable, 
			String name, 
			String full,
			Object value, 
			Store store);
}