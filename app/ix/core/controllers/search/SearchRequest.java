package ix.core.controllers.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queries.FilterClause;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import ix.core.search.SearchOptions;
import ix.core.search.SearchOptions.FacetLongRange;
import ix.core.search.SearchOptions.SearchTermFilter;
import ix.core.search.SearchResult;
import ix.core.search.text.TextIndexer;
import ix.utils.Tuple;
import ix.utils.Util;
import play.mvc.Http.Request;

public class SearchRequest {
	private SearchOptions options;
	private final Collection<?> subset;
	private String query;
	
	public SearchOptions getOptions() {
		return options;
	}

	public void setOptions(SearchOptions options) {
		this.options = options;
	}

	public Collection<?> getSubset() {
		return subset;
	}


	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public static class Builder {
		private Collection<?> subset;
		private String query = "*:*"; //defaults to "all" query
		private SearchOptions.Builder opBuilder = new SearchOptions.Builder();
		
		public Builder kind(Class<?> kind) {
			opBuilder.kind(kind);
			return this;
		}

		public Builder top(int top) {
			opBuilder.top(top);
			return this;
		}

		public Builder skip(int skip) {
			opBuilder.skip(skip);
			return this;
		}

		public Builder fetch(int fetch) {
			opBuilder.fetch(fetch);
			return this;
		}

		public Builder fdim(int fdim) {
			opBuilder.fdim(fdim);
			return this;
		}

		public Builder sideway(boolean sideway) {
			opBuilder.sideway(sideway);
			return this;
		}

		public Builder filter(String filter) {
			opBuilder.filter(filter);
			return this;
		}

		public Builder facets(List<String> facets) {
			opBuilder.facets(facets);
			return this;
		}
		
		public Builder addFacet(String facetName, String facetValue){
			this.opBuilder.addfacet(facetName, facetValue);
			return this;
			
		}
		

		public Builder longRangeFacets(List<FacetLongRange> longRangeFacets) {
			opBuilder.longRangeFacets(longRangeFacets);
			return this;
		}

		public Builder order(List<String> order) {
			opBuilder.order(order);
			return this;
		}

		public Builder expand(List<String> expand) {
			opBuilder.expand(expand);
			return this;
		}

		public Builder termFilters(List<SearchTermFilter> termFilters) {
			opBuilder.termFilters(termFilters);
			return this;
		}

		public Builder withRequest(Request req) {
			opBuilder.withRequest(req);
			return this;
		}

		public Builder withParameters(Map<String, String[]> params) {
			opBuilder.withParameters(params);
			return this;
		}

		public Builder options(SearchOptions options) {
			opBuilder.from(options);
			return this;
		}

		public Builder subset(Collection<?> subset) {
			this.subset = subset;
			return this;
		}

		public Builder query(String query) {
			this.query = query;
			return this;
		}

		public SearchRequest build() {
			return new SearchRequest(this);
		}
	}

	private SearchRequest(Builder builder) {
		this.options = builder.opBuilder.build();
		this.subset = builder.subset;
		this.query = builder.query;
	}

	
	/**
	 * Extracts a lucene query from the contained query text,
	 * using the provided {@link QueryParser}. This only
	 * extracts a query from the text, and does not include
	 * kind and subset information. If the query text is null,
	 * this returns a {@link MatchAllDocsQuery}.
	 * @param parser
	 * @return
	 * @throws ParseException
	 */
	public Query extractLuceneQuery(QueryParser parser) throws ParseException{
	    Query query = null;
        if (this.query == null) {
            query = new MatchAllDocsQuery();
        } else {
            query = parser.parse(this.query);
        }
        return query;
	}
	
	public Query extractFullQuery(QueryParser parser, FacetsConfig facetsConfig) throws ParseException{
	    Query query = extractLuceneQuery(parser);
	    if (options.getFacets().isEmpty()) {
	        return query;
	    }else{
    	    DrillDownQuery ddq = new DrillDownQuery(facetsConfig, query);
            options.getDrillDownsMap().values()
                                      .stream()
                                      .flatMap(t->t.stream())
                                      .forEach(dp->{
                                          ddq.add(dp.getDrill(), dp.getPaths());
                                      });
            return ddq;
	    }
	}
	public Tuple<Query,Filter> extractFullFacetQueryAndFilter(QueryParser parser, FacetsConfig facetsConfig, String facet) throws ParseException{
	    if(!options.isSideway() || options.getFacets().isEmpty()){
	        return Tuple.of(extractFullQuery(parser,facetsConfig),null);
	    }
	    
	    
	    Query query = extractLuceneQuery(parser);
	    

	    
	    List<Filter> nonStandardFacets = new ArrayList<Filter>();

	    DrillDownQuery ddq = new DrillDownQuery(facetsConfig, query);
	    options.getDrillDownsMap().values()
                    	    .stream()
                    	    .flatMap(t->t.stream())
                    	    .filter(dp->!dp.getDrill().equals(facet))
                    	    .filter(dp->{
                    	    	if(dp.getDrill().startsWith("^")){
                		    		nonStandardFacets.add(new TermsFilter(new Term(TextIndexer.TERM_VEC_PREFIX + dp.getDrill().substring(1), dp.getPaths()[0])));
                		    		return false;
                		    	}else if(dp.getDrill().startsWith("!")){
                		    		System.out.println("Found a not!" + dp.getDrill());
                		    		BooleanFilter f = new BooleanFilter();
                		    		TermsFilter tf = new TermsFilter(new Term(TextIndexer.TERM_VEC_PREFIX + dp.getDrill().substring(1), dp.getPaths()[0]));
                		    		f.add(new FilterClause(tf, BooleanClause.Occur.MUST_NOT));
                		    		nonStandardFacets.add(f);
                		    		return false;
                		    	}
                		    	return true;
                    	    })
                    	    .forEach(dp->{
                    	        ddq.add(dp.getDrill(), dp.getPaths());
                    	    });
	    Filter filter = null;
	    
	    if(!nonStandardFacets.isEmpty()){
			filter = new ChainedFilter(nonStandardFacets.toArray(new Filter[0])
					                  ,ChainedFilter.AND);	
		}
	    return Tuple.of(ddq,filter);
    }
	
	public Query extractFullFacetQuery(QueryParser parser, FacetsConfig facetsConfig, String facet) throws ParseException{
	    return extractFullFacetQueryAndFilter(parser,facetsConfig,facet).k();
    }
	
	
	
    public Map<String, String[]> asQueryParams() {
        Map<String,String[]> map = options.asQueryParams();
        map.put("q", new String[]{query});
        return map;
    }
    
    public static enum RequestUniquenessLevel{
        SET("q", "facet", "filter", "kind", "termfilter", "sideway"),
        SET_ORDER("q", "facet", "filter", "kind", "termfilter", "order","sideway"),
        SET_ORDER_STATS("q", "facet", "filter", "kind", "termfilter", "order", "fdim","sideway"),
        SET_ORDER_STATS_PAGE("q", "facet", "filter", "kind", "termfilter", "order", "fdim", "top","skip","expand","sideway");
        
        private String[] params;
        private RequestUniquenessLevel(String ... params){
            this.params=params;
        }
        
        public String[] getParams(){
            return this.params;
        }
        
    }
    
    public String getSha1Hash(RequestUniquenessLevel level){
        int chash = Optional.ofNullable(this.subset).orElse(new HashSet()).hashCode();
        return Util.sha1("search" + chash, asQueryParams(), level.getParams());
    }
    
    
    /**
     * This is a sha1 hash of the pieces of this
     * request which are considered to fundamentally
     * alter the <i>set</i> of results returned.
     * 
     * <p>
     * More specifically, this request will result
     * in a collection of entities. It may also encode 
     * certain post-processing and reporting options.
     * If 2 requests (done before any data changes)
     * would return (and <i>must</i> always return) the same 
     * total <b>set</b> of results, then those two requests 
     * should have the same {@link #getDefiningSetSha1()} 
     * result. Note that a canonical encoding of a request
     * isn't always possible, so there are cases where
     * equivalent requests will receive different sha1
     * hashes.
     * </p>
     * 
     * @return
     */
    public String getDefiningSetSha1(){
        return getSha1Hash(RequestUniquenessLevel.SET);
    }
    
    /**
     * This is a sha1 hash of the pieces of this request which are considered to
     * fundamentally alter the <i>order</i>, <i>set</i>, or <i>statistics</i> 
     * of results returned.
     * 
     * <p>
     * More specifically, this request will result in a collection of entities.
     * It may also encode certain post-processing and reporting options. If 2
     * requests (done before any data changes) would return (and <i>must</i>
     * always return) the same total <b>set</b> and <i>order</i> and 
     * <i>facets, or other statistical meta data</i> of results,
     * then those two requests should have the same
     * {@link #getOrderedSetSha1()} result. Note that a canonical encoding of a
     * request isn't always possible, so there are cases where equivalent
     * requests will receive different sha1 hashes.
     * </p>
     * 
     * @return
     */
    public String getOrderedSetSha1(){
        return getSha1Hash(RequestUniquenessLevel.SET_ORDER_STATS);
    }

    public SearchResult execute() throws IOException {
        return SearchFactory.search(this);
    }
    public SearchResult execute(TextIndexer t) throws IOException {
        return SearchFactory.search(t,this);
    }
}