package ix.core.controllers.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ix.core.search.SearchOptions;
import ix.core.search.SearchOptions.FacetLongRange;
import ix.core.search.SearchOptions.TermFilter;
import play.mvc.Http.Request;

public class SearchRequest {
	private SearchOptions options;
	private Collection<?> subset;
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

	public void setSubset(Collection<?> subset) {
		this.subset = subset;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public static class Builder {
		private Collection<?> subset;
		private String query;
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

		public Builder termFilters(List<TermFilter> termFilters) {
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
}