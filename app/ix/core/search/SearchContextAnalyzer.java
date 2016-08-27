package ix.core.search;

import java.util.List;

public interface SearchContextAnalyzer<K> {
	void updateFieldQueryFacets(K o, String q);
	List<FieldFacet> getFieldFacets();
	default boolean isEnabled(){
		return true;	
	}
	
}
