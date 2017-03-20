package ix.test.server;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import ix.core.plugins.IxCache;
import ix.core.search.SearchResultContext;
import ix.ginas.models.v1.Substance;
import ix.test.server.BrowserSubstanceSearcher.Order;
import ix.test.server.BrowserSubstanceSearcher.SortByValueComparator;
import ix.utils.Util;

public class SearchResult {
	private final Set<String> uuids;
	private final Set<String> specialUuids = new HashSet<>();
	private final Map<String, Map<String, Integer>> facetMap = new LinkedHashMap<>();
	private final BrowserSubstanceSearcher searcher;

	private String searchKey;

    private final GinasTestServer.User username;
	
	//TODO: Refactor
	public SearchResult(){
	    this("",new HashSet<String>(),null, null);
	}

	public SearchResult(String searchKey, Set<String> uuids, BrowserSubstanceSearcher searcher, GinasTestServer.User user) {
		Objects.requireNonNull(uuids);
		try {
			Objects.requireNonNull(searchKey);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;

		}

		this.searchKey = searchKey;
		this.uuids = Collections.unmodifiableSet(new LinkedHashSet<>(uuids));
		this.searcher=searcher;
        this.username = user;
	}

	private SearchResult(Builder builder) {
		this.uuids = builder.uuids;
		this.specialUuids.addAll(builder.specialUuids);
		this.facetMap.putAll(builder.facetMap);
		this.searcher = builder.searcher;
        this.username = builder.username;
		this.searchKey = builder.searchKey;
	}

	public String getKey() {
		return searchKey;
	}

	public Set<String> getUuids() {
		return uuids;
	}
	
	public Set<String> getSpecialUuids(){
	    return this.specialUuids;
	}

	public int numberOfResults() {
		return uuids.size();
	}

	public Stream<Substance> getSubstances() {

//        String adaptedKey = "!"+ searchKey + "#"+ Util.sha1(username);
//
//        SearchResultContext src = new SearchResultContext((ix.core.search.SearchResult) IxCache.getRaw(adaptedKey));
//        Collection results =  src.getResults();

        return GinasTestServer.doAsUser(username, ()-> {
            SearchResultContext src = SearchResultContext.getSearchResultContextForKey(searchKey);
            if (src == null) {
                throw new NullPointerException("null searchResultContext for " + searchKey);
            }
            //src.getCall()
            Collection results = src.getResults();
            if (results == null) {
                throw new NullPointerException("null results for " + searchKey + src.getMessage());
            }
            return results.stream().map(o -> (Substance) o);
        });
	}

    public BrowserSubstanceSearcher.WebExportRequest newExportRequest(String format) {
        return searcher.getExport(format, searchKey);
    }

	public InputStream export(String format) {
		return newExportRequest(format).getInputStream();
	}

	public Map<String, Integer> getFacet(String facetName) {
		return facetMap.get(facetName);
	}

	public void setFacet(String facetName, Map<String, Integer> countMap) {
		Objects.requireNonNull(facetName);
		Objects.requireNonNull(countMap);

		Map<String, Integer> copy = new TreeMap<>(new SortByValueComparator(countMap, Order.DECREASING));
		copy.putAll(countMap);

		facetMap.put(facetName, Collections.unmodifiableMap(copy));

	}

	public Map<String, Map<String, Integer>> getAllFacets() {
		return Collections.unmodifiableMap(facetMap);
	}

	public static class Builder {
		private Set<String> uuids;
		private Set<String> specialUuids= new HashSet<>();
		private Map<String, Map<String, Integer>> facetMap = new HashMap<>();
		private BrowserSubstanceSearcher searcher;
		private String searchKey;


        private GinasTestServer.User  username;


		public Builder uuids(Set<String> uuids) {
			this.uuids = uuids;
			return this;
		}

        public Builder username(GinasTestServer.User  username){
            this.username = username;
            return this;
        }

		public Builder specialUuids(Set<String> specialUuids) {
			this.specialUuids = specialUuids;
			return this;
		}

		public Builder facetMap(Map<String, Map<String, Integer>> facetMap) {
			this.facetMap = facetMap;
			return this;
		}

		public Builder searcher(BrowserSubstanceSearcher searcher) {
			this.searcher = searcher;
			return this;
		}

		public Builder searchKey(String searchKey) {
			this.searchKey = searchKey;
			return this;
		}

		public SearchResult build() {
			return new SearchResult(this);
		}
	}


}