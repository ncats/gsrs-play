package ix.core.search;

import ix.core.search.FieldBasedSearchAnalyzer.DefaultKVPair;
import ix.core.search.FieldBasedSearchAnalyzer.KVPair;

public interface KVPair{
	public String getKey();
	public String getValue();
}