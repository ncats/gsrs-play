package ix.core.search;

import java.util.List;

public interface SearchContextAnalyzer<K> {
	
	public void updateFieldQueryFacets(K o, String q);

	public List<FieldFacet> getFieldFacets();
	
}
