package ix.core.search.text;

import java.util.function.Consumer;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexableField;

import ix.core.models.Indexable;

public interface PrimitiveFieldMaker{
	public void create(
			Consumer<IndexableField> fields, 
			String name, 
			Object value, 
			String full,
			Indexable indexable);
}