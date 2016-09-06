package ix.core.search.text;

import java.util.function.Consumer;

import org.apache.lucene.index.IndexableField;

public interface DynamicFieldMaker{
	//createIndexableFields
	public void create(Consumer<IndexableField> ixFields, 
					   String name, 
					   String value, 
					   String full);
}