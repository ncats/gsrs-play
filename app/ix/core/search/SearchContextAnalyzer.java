package ix.core.search;

import java.util.List;

public interface SearchContextAnalyzer {
	
	public void updateFieldQueryFacets(Object o, String q);

	public List<FieldFacet> getFieldFacets();
	
}
