package ix.core.search;

import java.util.List;

public interface SearchAnalyzer<K> {
	
	void addWithQuery(K o, String q);
	List<FieldFacet> getFieldFacets();
	default boolean isEnabled(){
		return true;	
	}
	
}
