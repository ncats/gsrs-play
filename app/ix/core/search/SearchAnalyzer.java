package ix.core.search;

import java.util.List;

public interface SearchAnalyzer<K> {
	
	void addWithQuery(K o, String q);
	
	List<FieldedQueryFacet> getFieldFacets();
	default boolean isEnabled(){
		return true;	
	}
	
	default void markDone(){};
	
}
