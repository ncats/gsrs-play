package ix.core.search.text;

import java.util.function.Consumer;

import org.apache.lucene.index.IndexableField;

public interface DynamicFieldIndexerPassiveProvider{
	public void produceDynamicFacets(String name, String value, String path, Consumer<IndexableField> ixFields);
}