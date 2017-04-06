package ix.core.search;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.CacheStrategy;
import ix.core.search.LazyList.NamedCallable;
import ix.core.search.text.TextIndexer.Facet;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.core.util.TimeUtil;
import ix.utils.Global;
import ix.utils.Util.QueryStringManipulator;
import play.mvc.Controller;

@CacheStrategy(evictable = false)
public class SearchResult {

	private String key;
	private String query;
	final private List<Facet> facets = new ArrayList<Facet>();
	final private List<FieldedQueryFacet> suggestFacets = new ArrayList<FieldedQueryFacet>();

	private final LazyList<Key,Object> matches = new LazyList<>(o -> (EntityWrapper.of(o)).getKey());
	
	
	private List<?> result; // final result when there are no more updates
	                        // (largely unnecessary now)

	private int count;
	private SearchOptions options;
	final long timestamp = TimeUtil.getCurrentTimeMillis();
	final AtomicLong stop = new AtomicLong();
	
	Comparator<Key> idComparator = null;
	
	private String generatingUrl;

	private final List<SoftReference<SearchResultDoneListener>> listeners = new ArrayList<>();


	public SearchResult(SearchOptions options, String query) {
		this.options = options;
		this.query = query;
	}

	
	public boolean hasError(){
		return false;
	}

	public Optional<Throwable> getThrowable(){
		return Optional.empty();
	}
	
	/**
     * Returns a list of FieldFacets which help to explain why and how
     * matches were done for this particular query.
     * 
     * @return
     */
    public List<FieldedQueryFacet> getFieldFacets() {
        return suggestFacets;
    }

	public void setRank(Comparator<Key> idCompare) {
		Objects.requireNonNull(idCompare);
		idComparator = idCompare;
	}

	public void addListener(SearchResultDoneListener listener) {
		synchronized (listeners) {
			listeners.add(new SoftReference<>(listener));
		}
	}

	public void removeListener(SearchResultDoneListener listener) {
		synchronized (listeners) {
			Iterator<SoftReference<SearchResultDoneListener>> iter = listeners.iterator();
			while (iter.hasNext()) {
				SoftReference<SearchResultDoneListener> l = iter.next();
				SearchResultDoneListener actualListener = l.get();
				//if get() returns null then the object was garbage collected
				if (actualListener == null || listener.equals(actualListener)) {
					iter.remove();
					//keep checking in the unlikely event that
					//a listener was added twice?
				}
			}
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getQuery() {
		return query;
	}

	public SearchOptions getOptions() {
		return options;
	}

	public List<Facet> getFacets() {
		return facets;
	}

	public Facet getFacet(String name) {
		return facets.stream().filter(n -> n.getName().equalsIgnoreCase(name)).findAny().orElse(null);

	}

	public int size() {
		return matches.size();
	}

	public Object get(int index) {
		throw new UnsupportedOperationException("get(index) is no longer supported; please use copyTo()");
	}

	/**
	 * Copies from the search results to the specified list
	 * with specified offset (for the master results), and
	 * total count of records to be copied.
	 * 
	 * Note: This method will block and wait for the "correct" 
	 * answer only if the final <pre>wait</pre> parameter is 
	 * <pre>true</pre>. Otherwise it will return whatever
	 * is available.
	 * 
	  * @param list 
	 * 	Destination list to copy into
	 * @param start
	 * 	Offset starting location from master list
	 * @param count
	 * 	Total number of records to be copied
	 * @param wait
	 * 	set to true for blocking for correct answer,
	 *  false will return available records immediately
	 * @return
	 */
	public int copyTo(List list, int start, int count, boolean wait) {

		if(this.count ==0 || count ==0){
			return 0;
		}
		// It may be that the page that is being fetched is not yet
		// complete. There are 2 options here then. The first is to
		// return whatever is ready now immediately, and report the
		// number of results (that is what had been done before).

		// The second way is to wait for the fetching to be completed
		// which is what is demonstrated below. 
		List matches;
		if (wait) {
			try {
				matches = this.getMatchesFuture(start + count).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				//interrupted...make empty list?
				matches = Collections.emptyList();
			}
		} else {
			matches = getMatches();
		}

		if (start >= matches.size()) {
			return 0;
		}
	
		
		Iterator it = matches.listIterator(start);

		int i = 0;
		for (; i < count && it.hasNext(); ++i) {
			list.add(it.next());
		}

		return i;

	}
	
	/**
     * Copies from the search results {@link Key}s to the specified list
     * with specified offset (for the master results), and
     * total count of records to be copied.
     * 
     * Note: This method will block and wait for the "correct" 
     * answer only if the final <pre>wait</pre> parameter is 
     * <pre>true</pre>. Otherwise it will return whatever
     * is available.
     * 
      * @param list 
     *  Destination list to copy keys into
     * @param start
     *  Offset starting location from master list
     * @param count
     *  Total number of records to be copied
     * @param wait
     *  set to true for blocking for correct answer,
     *  false will return available records immediately
     * @return
     */
    public int copyKeysTo(List<Key> list, int start, int count, boolean wait) {

        // It may be that the page that is being fetched is not yet
        // complete. There are 2 options here then. The first is to
        // return whatever is ready now immediately, and report the
        // number of results (that is what had been done before).

        // The second way is to wait for the fetching to be completed
        // which is what is demonstrated below. 
        List<Object> matches;
        if (wait) {
            try {
                matches = this.getMatchesFuture(start + count).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                //interrupted...make empty list?
                matches = Collections.emptyList();
            }
        } else {
            matches = getMatches();
        }

        if (start >= matches.size()) {
            return 0;
        }
        
        LazyList<Key,Object> keyList = LazyList.of(matches, (t)->EntityWrapper.of(t).getKey());
        
        List<NamedCallable<Key,Object>> allKeys= keyList.getInternalList();
        Iterator<NamedCallable<Key,Object>> it = allKeys.listIterator(start);
        int i = 0;
        for (; i < count && it.hasNext(); ++i) {
            list.add(it.next().getName());
        }

        return i;

    }
	

	/**
	 * Copies from the search results to the specified list
	 * with specified offset (for the master results), and
	 * total count of records to be copied.
	 * 
	 * Note: It may be that the search is still running, 
	 * and haven't fully returned yet. In that case, this 
	 * method will not wait for the results, but will return
	 * immediately with any records that are ready so far.
	 * 
	 * To have a blocking search which waits for the "correct"
	 * answer, use: <pre>copyTo(list,start,count, true)</pre>
	 *
	 * 
	 * @param list 
	 * 	Destination list to copy into
	 * @param start
	 * 	Offset starting location from master list
	 * @param count
	 * 	Total number of records to be copied
	 * @return
	 */
	// fill the given list with value starting at start up to start+count
	public int copyTo(List list, int start, int count) {
		return copyTo(list, start, count, false);
	}

	/**
	 * Get the result of {@link #getMatches()}}
	 * as a Future which runs in a background thread
	 * and will block the call to Future#get() until
	 * at the list is fully populated.
	 *
	 * @return a Future will never be null.
	 */
	public Future<List> getMatchesFuture() {
		SearchResultFuture future = new SearchResultFuture(this);
		// new Thread(future).start();
		ForkJoinPool.commonPool().submit(future);
		return future;
	}

	/**
	 * Get the result of {@link #getMatches()}}
	 * as a Future which runs in a background thread
	 * and will block the call to Future#get() until
	 * at least numberOfRecords is fetched.
	 * @param numberOfRecords the minimum number of records (or the list is done populating)
	 *                        in the List to wait to get populated
	 *                        before Future#get() unblocks.
	 * @return a Future will never be null.
	 */
	public Future<List> getMatchesFuture(int numberOfRecords) {
		SearchResultFuture future = new SearchResultFuture(this, numberOfRecords);
		ForkJoinPool.commonPool().submit(future);
		// new Thread(future).start();
		return future;
	}

	/**
	 * Get the result of {@link #getMatches()}}
	 * as a Future which runs in a background thread
	 * and will block the call to Future#get() until
	 * at least numberOfRecords is fetched.
	 * @param numberOfRecords the minimum number of records (or the list is done populating)
	 *                        in the List to wait to get populated
	 *                        before Future#get() unblocks.
	 *
	 * @param executorService the ExecutorService to submit the Future to.
	 * @return a Future will never be null.
	 */
	public Future<List> getMatchesFuture(int numberOfRecords, ExecutorService executorService) {
		SearchResultFuture future = new SearchResultFuture(this, numberOfRecords);
		executorService.submit(future);
		return future;
	}

	/**
	 * Returns a list of matches for which some special criteria are 
	 * met. For example, an exact match on a specific designated field.
	 * @return
	 */
	public List getSponsoredMatches() {
		LazyList<Key,Object> lazylist = new LazyList<>(c -> EntityWrapper.of(c).getKey());
		for (NamedCallable<Key,Object> nc : sponsored.values()) {
			lazylist.addCallable(nc);
		}
		return lazylist;
	}

	public List getMatches() {
		if (result != null)
			return result; // return if ready
		boolean finished = finished();


        if (idComparator != null) {
            matches.sortByNames(idComparator);
        }
		if (finished) {
		    
			result = matches;
		}
		
		return matches;
	}

	public boolean isEmpty() {
		return matches.isEmpty();
	}

	/**
	 * This is just a delegate to {@link #getCount()}
	 * @deprecated
	 * @return
	 */
	@Deprecated
	public int count() {
		return getCount();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long elapsed() {
		return stop.get() - timestamp;
	}

	public long getStopTime() {
		return stop.get();
	}

	public boolean finished() {
		return stop.get() >= timestamp;
	}

	private final Map<Key, NamedCallable<Key,Object>> sponsored = new HashMap<>();

	public void addNamedCallable(NamedCallable<Key,Object> c) {
		if (!sponsored.containsKey(c.getName())) {
			matches.addCallable(c);
			processAddition(c);
		}
	}
	
	@JsonIgnore
	public String getFacetURI(String facetName){
	    try{
            String base=Controller.request().uri().split("\\?")[0] + "/@facets";

            Map<String, String[]> params=new HashMap<>(Controller.request().queryString());
            QueryStringManipulator qManip = new QueryStringManipulator(params);


            qManip.toggleInclusion("field", facetName);

            String newQueryString =qManip.toQueryString();

            if(newQueryString.length()<=0){
                System.out.println(newQueryString);
                return Global.getHost() + base;
            }
            return Global.getHost() + base + "?" + newQueryString;
        }catch(Exception e){
            e.printStackTrace();
            throw e;

        }
	}
	
	
	/**
	 * Creates the url necessary for toggling the given facet name and
	 * value for this SearchResult.
	 * 
	 * <p>
	 * Needs an active HTTP context
	 * </p>
	 *  
	 * @param facetName
	 * @param facetValue
	 * @return
	 */
	public String getFacetToggleURI(String facetName, String facetValue){
		try{
			String base=Controller.request().uri().split("\\?")[0];

			Map<String, String[]> params=new HashMap<>(Controller.request().queryString());
			QueryStringManipulator qManip = new QueryStringManipulator(params);

			String fselect= facetName + "/" + facetValue;

			qManip.toggleInclusion("facet", fselect);

			String newQueryString =qManip.toQueryString();

			if(newQueryString.length()<=0){
				System.out.println(newQueryString);
				return Global.getHost() + base;
			}
			return Global.getHost() + base + "?" + newQueryString;
		}catch(Exception e){
			e.printStackTrace();
			throw e;

		}
				
	}

	public void addSponsoredNamedCallable(NamedCallable<Key,Object> c) {
		//System.out.println("Sponsored record: " + c.getName());
		sponsored.put(c.getName(), c);
		matches.addCallable(c);
		processAddition(c);
	}

	protected void add(Object obj) {
		matches.add(obj);
		processAddition(() -> obj);
	}

	private void processAddition(NamedCallable o) {
		notifyAdd(o);
	}

	private void notifyAdd(Object o) {

		notifyListeners(l -> l.added(o));

	}

	public void done() {
		stop.set(TimeUtil.getCurrentTimeMillis());
		notifyListeners(l -> l.searchIsDone());
	}

	/**
	 * Notify all listeners that haven't yet been
	 * GC'ed by performing the given consumer
	 * on each one.
	 *
	 * @param consumer the Consumer function to perform
	 *                 on each listener; can not be null.
	 *
	 * @throws NullPointerException if consumer is null.
	 */
	private void notifyListeners(Consumer<SearchResultDoneListener> consumer) {
		synchronized (listeners) {
			Iterator<SoftReference<SearchResultDoneListener>> iter = listeners.iterator();
			while (iter.hasNext()) {
				SearchResultDoneListener l = iter.next().get();
				//if object pointed to by soft reference
				//has been GC'ed then it is null
				if (l == null) {
					iter.remove();
				} else {
					consumer.accept(l);
				}

			}
		}
	}

	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void addFacet(Facet f) {
		this.facets.add(f);
	}

	public void addFieldQueryFacet(FieldedQueryFacet ff) {
		if (ff.getDisplayField() != null) {
			this.suggestFacets.add(ff);
		}
	}

	/**
	 * A Builder for {@link SearchResult}. This builder
	 * uses fields which will be passed through to build
	 * a {@link SearchResult}.
	 * @author peryeata
	 *
	 */
	public static class BasicBuilder extends AbstractBuilder<BasicBuilder> {


		protected BasicBuilder getThis(){
			return this;
		}

	}

	/**
	 * A Builder for {@link SearchResult}. This builder
	 * uses fields which will be passed through to build
	 * a {@link SearchResult}.
	 * @author peryeata
	 *
	 */
	public static  abstract class AbstractBuilder<B extends AbstractBuilder<B>> {
		private String key;
		private String query;
		private List<Facet> facets;
		private List<FieldedQueryFacet> suggestFacets;
		private LazyList<Key,Object> matches;
		private List<?> result;
		private int count =-1;
		private SearchOptions options;
		private long stop =-1;
		private Comparator<Key> idComparator;
		private List<SoftReference<SearchResultDoneListener>> listeners;
		private Map<Key, NamedCallable<Key,Object>> sponsored;

		protected abstract  B getThis();

		public B key(String key) {
			this.key = key;
			return getThis();
		}

		public B query(String query) {
			this.query = query;
			return getThis();
		}

		public B facets(List<Facet> facets) {
			this.facets = facets;
			return getThis();
		}

		public B suggestFacets(List<FieldedQueryFacet> suggestFacets) {
			this.suggestFacets = suggestFacets;
			return getThis();
		}

		public B matches(LazyList<Key,Object> matches) {
			this.matches = matches;
			return getThis();
		}

		public B result(List<?> result) {
			this.result = result;
			return getThis();
		}

		public B count(int count) {
			if(count < 0){
				throw new IllegalArgumentException("count can not be negative");
			}
			this.count = count;
			return getThis();
		}

		public B options(SearchOptions options) {
			this.options = options;
			return getThis();
		}

		public B stop(long stop) {
			this.stop = stop;
			return getThis();
		}

		public B idComparator(Comparator<Key> idComparator) {
			this.idComparator = idComparator;
			return getThis();
		}

		public B listeners(List<SoftReference<SearchResultDoneListener>> listeners) {
			this.listeners = listeners;
			return getThis();
		}

		public B sponsored(Map<Key, NamedCallable<Key,Object>> sponsored) {
			this.sponsored = sponsored;
			return getThis();
		}


		public SearchResult build() {
			return new SearchResult(this);
		}
	}

	public static class EmptyBuilder extends AbstractBuilder<EmptyBuilder>{

		@Override
		protected EmptyBuilder getThis() {
			return this;
		}

		public SearchResult build() {
			return new EmptySearchResult(this);
		}
	}

	private SearchResult(AbstractBuilder builder) {
		this.key = builder.key;
		this.query = builder.query;
		
		this.result = builder.result;
		this.count = Math.max(0, builder.count);
		this.options = builder.options;
		if(builder.stop>0){
		    this.stop.set(builder.stop);
		}
		this.idComparator = builder.idComparator;
		if(builder.sponsored!=null){
			this.sponsored.putAll(builder.sponsored);
		}
		if(builder.suggestFacets!=null){
			this.suggestFacets.addAll(builder.suggestFacets);
		}
		if(builder.matches!=null){
			this.matches.addAll(builder.matches);
		}
		if(builder.facets!=null){
			this.facets.addAll(builder.facets);
		}
	}
	
	/**
     * Static method for adapting a {@link SearchResultContext} into a {@link SearchResult}.
     * Typically, a {@link SearchResultContext} comes first, and a search is performed to
     * restrict the context to a specific result. This is a lazy case, where the results
     * from a {@link SearchResultContext}, as well as some basic {@link SearchOptions}
     * are used to create a simple {@link SearchResult}.
     * @param ctx The SearchResultContext to use, which will be used for its key and
     *            match results
     * @param options The SearchOptions to use, this is useful for a few processing cases,
     *                like top/skip etc.
     * @return
     */
    public static SearchResult fromContext(SearchResultContext ctx, SearchOptions options){
        @SuppressWarnings("unchecked")
        LazyList<Key,Object> ll = (LazyList<Key,Object>)LazyList
                            .of(ctx.getResults(),o->EntityWrapper.of(o).getKey());
        
        
        return createBuilder()
                    .options(options)
                    .stop(TimeUtil.getCurrentTimeMillis())
                    .matches(ll)
                    .result(ll)
                    .key(ctx.getKey() + "/result")
                    .count(ll.size())
                    .build();
    }


	public static AbstractBuilder<?> createBuilder(){
		return new BasicBuilder();
	}

	public static AbstractBuilder<?> createEmptyBuilder(SearchOptions options){
		return new EmptyBuilder()

				.options(options)
				.stop(TimeUtil.getCurrentTimeMillis())
				;
	}

	private static class EmptySearchResult extends SearchResult{
		public EmptySearchResult(AbstractBuilder builder) {
			super(builder);
		}

		public EmptySearchResult(SearchOptions options, String query) {
			super(options, query);
		}

		@Override
		public int copyTo(List list, int start, int count, boolean wait) {
			return 0;
		}

		@Override
		public List getMatches() {
			return Collections.emptyList();
		}
	}


	public static SearchResult createErrorResult(Throwable t){
		return new ErrorSearchResult(new EmptyBuilder(), t);
	}

	private static class ErrorSearchResult extends EmptySearchResult{

		private final Throwable error;

		public ErrorSearchResult(AbstractBuilder builder, Throwable error) {
			super(builder);
			this.error = error;
		}

		public ErrorSearchResult(SearchOptions options, String query, Throwable error) {
			super(options, query);
			this.error = error;
		}

		@Override
		public boolean hasError() {
			return error !=null;
		}

		@Override
		public Optional<Throwable> getThrowable() {
			return Optional.ofNullable(error);
		}
	}
}