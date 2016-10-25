package ix.core.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ix.core.controllers.RequestOptions;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import play.Logger;
import play.mvc.Http;

import static ix.core.search.ArgumentAdapter.*;


public class SearchOptions implements RequestOptions {
	public static class TermFilter {
		String field;
		String term;

		public TermFilter(String field, String term) {
			this.field = field;
			this.term = term;
		}

		public String getField() {
			return this.field;
		}

		public String getTerm() {
			return this.term;
		}
	}

	public static class FacetLongRange {
		public String field;
		public Map<String, long[]> range = new TreeMap<String, long[]>();

		public FacetLongRange(String field) {
			this.field = field;
		}

		public void add(String title, long[] range) {
			this.range.put(title, range);
		}
	}

	public static final int DEFAULT_TOP = 10;
	public static final int DEFAULT_FDIM = 10;

	// default number of elements to fetch while blocking
	public static final int DEFAULT_FETCH_SIZE = 100; // 0 means all

	private Class<?> kind; // filter by type

	private int top = DEFAULT_TOP;
	private int skip;
	private int fetch = DEFAULT_FETCH_SIZE;
	private int fdim = DEFAULT_FDIM; // facet dimension
	// whether drilldown (false) or sideway (true)
	private boolean sideway = true;
	
	private boolean wait = false;
	private String filter;

	/**
	 * Facet is of the form: DIMENSION/VALUE...
	 */
	private List<String> facets = new ArrayList<String>();
	private List<FacetLongRange> longRangeFacets = new ArrayList<FacetLongRange>();
	private List<String> order = new ArrayList<String>();
	private List<String> expand = new ArrayList<String>();
	private List<TermFilter> termFilters = new ArrayList<TermFilter>();

	public SearchOptions() {
	}

	public SearchOptions(Class<?> kind) {
		this.setKind(kind);
	}

	public SearchOptions(Class<?> kind, int top, int skip, int fdim) {
		this.setKind(kind);
		this.setTop(Math.max(1, top));
		this.skip = Math.max(0, skip);
		this.setFdim(Math.max(1, fdim));
	}

	public SearchOptions(Map<String, String[]> params) {
		parse(params);
	}

	public void setFacet(String facet, String value) {
		facets.clear();
		addFacet(facet, value);
	}

	public void addFacet(String facet, String value) {
		facets.add(facet + "/" + value);
		queryParams.resetCache();
	}

	public int max() {
		return skip + getTop();
	}
	
	
	
	public Map<String,String[]> asQueryParms(){
		return queryParams.get();
	}
	
	private CachedSupplier<Map<String, ArgumentAdapter>> argumentAdapters = CachedSupplier.of(()->{
     	return Stream.of(
		     	ofList("order", a->setOrder(a), ()->getOrder()),
		     	ofList("expand", a->expand=a, ()->expand),
		     	ofList("facet", a->facets=a, ()->facets),
		     	ofInteger("top", a->setTop(a), ()->getTop()),
		     	ofInteger("skip", a->skip=a, ()->skip),
		     	ofInteger("fdim", a->setFdim(a), ()->getFdim()),
		     	ofInteger("fetch", a->setFetch(a), ()->getFetch()),
		     	ofBoolean("sideway", a->setSideway(a), ()->isSideway(),true),
		     	ofBoolean("wait", a->setWait(a), ()->isWait(),false),
		     	ofSingleString("filter", a->filter=a, ()->filter),
		     	ofSingleString("kind", a->{
		     		try{
		     			setKind(Class.forName(a));
		     		}catch(Exception e){
		     			Logger.error("Unknown kind:" + a, e);
		     		}
		     	}, ()->{
		     		if(getKind()==null)return null;
		     		return getKind().getName();	
		     	})
     	)
     	.collect(Collectors.toMap(a->a.name(), a->a));
     });
	
	private CachedSupplier<Map<String,String[]>> queryParams = CachedSupplier.of(()->{
		return argumentAdapters.get()
		.values()
		.stream()
		.collect(Collectors.toMap(a->a.name(), a->a.get()));
	});
	
	public SearchOptions parse(Map<String, String[]> params) {
		params.forEach((k,v)->{
			argumentAdapters.get()
					.getOrDefault(k, doNothing())
					.accept(v);
		});
		queryParams.resetCache();
		return this;
	}

	public void removeAndConsumeRangeFilters(BiConsumer<String, long[]> cons) {
		//Map<String, List<Filter>> filters = new HashMap<String, List<Filter>>();
		List<String> remove = new ArrayList<String>();
		for (String f : this.facets) {
			int pos = f.indexOf('/');
			if (pos > 0) {
				String facet = f.substring(0, pos);
				String value = f.substring(pos + 1);
				// options.longRangeFacets.stream()
				for (SearchOptions.FacetLongRange flr : this.getLongRangeFacets()) {
					if (facet.equals(flr.field)) {
						long[] range = flr.range.get(value);
						if (range != null) {
							// add this as filter..
							cons.accept(facet, range);
						}
						remove.add(f);
					}
				}
			}
		}
		this.facets.removeAll(remove);
	}

	public List<String> getFacets() {
		return this.facets;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("SearchOptions{kind=" + (getKind() != null ? getKind().getName() : "") + ",top="
				+ getTop() + ",skip=" + skip + ",fdim=" + getFdim() + ",fetch=" + getFetch() + ",sideway=" + isSideway() + ",filter="
				+ filter + ",facets={");
		for (Iterator<String> it = facets.iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext())
				sb.append(",");
		}
		sb.append("},order={");
		for (Iterator<String> it = getOrder().iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext())
				sb.append(",");
		}
		sb.append("},expand={");
		for (Iterator<String> it = expand.iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext())
				sb.append(",");
		}
		sb.append("}}");
		return sb.toString();
	}

	private DrillAndPath parseDrillAndPath(String dd) {
		int pos = dd.indexOf('/');
		if (pos > 0) {
			String facet = dd.substring(0, pos);
			String value = dd.substring(pos + 1);
			String[] drill = value.split("/");
			for (int i = 0; i < drill.length; i++) {
				drill[i] = drill[i].replace("$$", "/");
			}
			return new DrillAndPath(facet, drill);
		}
		return null;
	}

	public Map<String, List<DrillAndPath>> getDrillDownsMap() {
		Map<String, List<DrillAndPath>> providedDrills = new HashMap<String, List<DrillAndPath>>();
		// the first term is the drilldown dimension
		this.facets.stream().map(dd -> parseDrillAndPath(dd)).filter(Objects::nonNull).forEach(
				dp -> providedDrills.computeIfAbsent(dp.getDrill(), t -> new ArrayList<DrillAndPath>()).add(dp));
		return providedDrills;
	}

	public class DrillAndPath {
		private String drill;
		private String[] paths;

		public DrillAndPath(String drill, String[] path) {
			this.drill = drill;
			this.paths = path;
		}

		public String asLabel() {
			return String.join("/", getPaths());
		}

		public String getDrill() {
			return drill;
		}

		public String[] getPaths() {
			return paths;
		}

	}

	public EntityInfo<?> getKindInfo() {
		try {
			return EntityUtils.getEntityInfoFor(this.getKind());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public int getTop() {
		return this.top;
	}

	@Override
	public int getSkip() {
		return this.skip;
	}

	@Override
	public String getFilter() {
		return this.filter;
	}

	public static class Builder {
		
		private Class<?> kind;
		private int top=DEFAULT_TOP;
		private int skip=0;
		private int fetch=DEFAULT_FETCH_SIZE;
		private int fdim=DEFAULT_FDIM;
		private boolean sideway=true;
		
		private String filter;
		
		private List<String> facets = new ArrayList<>();
		private List<FacetLongRange> longRangeFacets = new ArrayList<>();
		private List<String> order = new ArrayList<>();
		private List<String> expand = new ArrayList<>();
		private List<TermFilter> termFilters = new ArrayList<>();
		
		private Map<String,String[]> params;
		
		/**
		 * Creates a clone of the given {@link SearchOptions}, clobbering any previous
		 * calls to {@link #top(int)}, {@link #skip(int)} or any other option 
		 * creating method. The only methods not clobbered by this are
		 * {@link #withParameters(Map)}, {@link #withRequest(play.mvc.Http.Request)}
		 * which are applied after the creation.
		 * @param so
		 * @return
		 */
		public Builder from(SearchOptions so) {
			top(so.getTop());
			skip(so.skip);
			fetch(so.getFetch());
			fdim(so.getFdim());
			sideway(so.isSideway());
			filter(so.filter);
			kind(so.getKind());
			facets(new ArrayList<String>(so.getFacets()));
			order(new ArrayList<String>(so.getOrder()));
			expand(new ArrayList<String>(so.expand));
			termFilters(new ArrayList<TermFilter>(so.termFilters));
			longRangeFacets(new ArrayList<FacetLongRange>(so.longRangeFacets));
			return this;
		}

		public Builder kind(Class<?> kind) {
			this.kind = kind;
			return this;
		}

		public Builder top(int top) {
			this.top = top;
			return this;
		}

		public Builder skip(int skip) {
			this.skip = skip;
			return this;
		}

		public Builder fetch(int fetch) {
			this.fetch = fetch;
			return this;
		}

		public Builder fdim(int fdim) {
			this.fdim = fdim;
			return this;
		}

		public Builder sideway(boolean sideway) {
			this.sideway = sideway;
			return this;
		}

		public Builder filter(String filter) {
			this.filter = filter;
			return this;
		}

		public Builder facets(List<String> facets) {
			this.facets = facets;
			return this;
		}
		public Builder facets(String ... facets) {
			this.facets = Arrays.asList(facets);
			return this;
		}

		public Builder longRangeFacets(List<FacetLongRange> longRangeFacets) {
			this.longRangeFacets = longRangeFacets;
			return this;
		}

		public Builder order(List<String> order) {
			this.order = order;
			return this;
		}

		public Builder expand(List<String> expand) {
			this.expand = expand;
			return this;
		}

		public Builder termFilters(List<TermFilter> termFilters) {
			this.termFilters = termFilters;
			return this;
		}
		
		public Builder withRequest(Http.Request req) {
			return withParameters(req.queryString());
		}
		
		public Builder withParameters(Map<String,String[]> params) {
			this.params=params;
			return this;
		}

		public SearchOptions build() {
			return new SearchOptions(this);
		}
	}

	private SearchOptions(Builder builder) {
		this.setKind(builder.kind);
		this.setTop(builder.top);
		this.skip = builder.skip;
		this.setFetch(builder.fetch);
		this.setFdim(builder.fdim);
		this.setSideway(builder.sideway);
		this.filter = builder.filter;
		this.facets = builder.facets;
		this.setLongRangeFacets(builder.longRangeFacets);
		this.setOrder(builder.order);
		this.expand = builder.expand;
		this.termFilters = builder.termFilters;
		if(builder.params!=null){
			this.parse(builder.params);
		}
	}

	public List<TermFilter> getTermFilters() {
		return termFilters;
	}
	
	public void addTermFilters(List<TermFilter> termFilters) {
        this.termFilters.addAll(termFilters);
    }
	public void addTermFilter(TermFilter termFilter) {
        this.termFilters.add(termFilter);
    }

	public List<String> getOrder() {
		return order;
	}

	public List<String> setOrder(List<String> order) {
		this.order = order;
		queryParams.resetCache();
		return order;
	}

	public List<FacetLongRange> getLongRangeFacets() {
		return longRangeFacets;
	}
	public void addLongRangeFacets(List<FacetLongRange> longRangeFacets) {
        this.longRangeFacets.addAll(longRangeFacets);
    }

	public void setLongRangeFacets(List<FacetLongRange> longRangeFacets) {
		this.longRangeFacets = longRangeFacets;
		queryParams.resetCache();
	}

	public boolean isSideway() {
		return sideway;
	}

	public boolean setSideway(boolean sideway) {
		this.sideway = sideway;
		queryParams.resetCache();
		return sideway;
	}

	public Class<?> getKind() {
		return kind;
	}

	public void setKind(Class<?> kind) {
		this.kind = kind;
		queryParams.resetCache();
	}

	public int getFdim() {
		return fdim;
	}

	public int setFdim(int fdim) {
		this.fdim = fdim;
		queryParams.resetCache();
		return fdim;
	}

	public int getFetch() {
		return fetch;
	}

	public int setFetch(int fetch) {
		this.fetch = fetch;
		queryParams.resetCache();
		return fetch;
	}

	public int setTop(int top) {
		this.top = top;
		queryParams.resetCache();
		return top;
	}

	public boolean isWait() {
		return wait;
	}

	public boolean setWait(boolean wait) {
		this.wait = wait;
		return wait;
	}

}
