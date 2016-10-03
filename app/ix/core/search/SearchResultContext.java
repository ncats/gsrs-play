package ix.core.search;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;


import play.mvc.Call;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.CacheStrategy;
import ix.core.plugins.IxCache;
import ix.core.search.FieldedQueryFacet.MATCH_TYPE;
import ix.utils.Util;
import play.Logger;

@CacheStrategy(evictable=false)
public class SearchResultContext {
    public enum Status {
        Pending,	//show  +
        Running,	//show  +
        Determined, //don't show +
        Done,		//don't show +
        Failed		//don't show +
    }

    
    

    public static interface StatusChangeListener{
    	void onStatusChange(SearchResultContext.Status newStatus, SearchResultContext.Status oldStatus);
    }
    
    private List<SoftReference<SearchResultContext.StatusChangeListener>> listeners = new ArrayList<>();
    
    private SearchResultContext.Status _status = Status.Pending;
    String mesg;
    Long start;
    Long stop;
    List<FieldedQueryFacet> fieldFacets=null;
    Collection results = new LinkedBlockingDeque();

    List sponsoredResults = new ArrayList();

    String id = Util.randvar (10);
    Integer total;
    String key;
    
    
    
    public static class SearchResultContextDeterminedFuture extends FutureTask<Void>{
    	public SearchResultContextDeterminedFuture(final SearchResultContext context){
    		super(new WaitForDeterminedCallable(context));
    	}
    }
    
    private static class WaitForDeterminedCallable implements Callable<Void>, SearchResultContext.StatusChangeListener{
    	private SearchResultContext context;
    	private final CountDownLatch latch;

    	public WaitForDeterminedCallable(final SearchResultContext context){
    		Objects.requireNonNull(context);
    		this.context = context;
    		this.context.addListener(this);
            latch = new CountDownLatch(1);
    	}
    	
    	public void onStatusChange(SearchResultContext.Status newStatus, SearchResultContext.Status oldStatus){
    		if(newStatus == Status.Determined || 
    			newStatus==Status.Done || 
    			newStatus == Status.Failed){
    			
    			while(latch.getCount()>0){
                    latch.countDown();
                }
    		}
    		
    	}
		@Override
		public Void call() throws Exception {
			if(latch.getCount()>0 && !context.isDetermined()){
				latch.await();
			}
			context.removeListener(this);
			return null;
		}
    }
    
    SearchResultContext () {}
    
    public SearchResultContext (SearchResult result) {
    	fieldFacets=result.getFieldFacets();
        start = result.getTimestamp();      
        
        if (result.finished()) {
            setStatus(Status.Done);
            stop = result.getStopTime();
        }else if (result.size() > 0){
        	setStatus(Status.Determined);
        }
        
        if (_status != Status.Done) {
            mesg = String.format
                ("Loading...%1$d%%",
                 (int)(Math.ceil(100.*result.size()/((double)result.count()))));
        }
        
        results = result.getMatches();
        total = result.count();
        this.key=result.getKey();

        sponsoredResults =result.getSponsoredMatches();
    }

    /**
     * Returns the set of records which matched a predefined 
     * set of fields directly
     * @return
     */
    @JsonIgnore
    public List getExactMatches(){
        return sponsoredResults;
    }
    
    /**
     * Returns query narrowing suggestions and their counts
     * @return
     */
    public List<FieldedQueryFacet> getFieldFacets(){
    	return fieldFacets;
    }
    
    /**
     * Returns true if getExactMatches() would return a list of size >0
     * @return
     */
    public boolean hasExactMatches(){
    	return sponsoredResults.size()>0;
    }
    
    
    /**
     * Returns the suggested FieldQueryFacets grouped by match type, 
     * in order they show in easy grouping
     * @return
     */
    @JsonIgnore
    public LinkedHashMap<FieldedQueryFacet.MATCH_TYPE, List<FieldedQueryFacet>> getFieldFacetsMap(){
    	Map<MATCH_TYPE,Integer> place= new HashMap<MATCH_TYPE,Integer>();
    	place.put(MATCH_TYPE.FULL, 0);
    	place.put(MATCH_TYPE.WORD_STARTS_WITH, 1);
    	place.put(MATCH_TYPE.WORD, 2);
    	place.put(MATCH_TYPE.CONTAINS, 3);
    	place.put(MATCH_TYPE.NO_MATCH, 4);
    	
    	LinkedHashMap<FieldedQueryFacet.MATCH_TYPE, List<FieldedQueryFacet>> grouped = new LinkedHashMap<>();
    	
    	fieldFacets.stream()
    			   .collect(Collectors.groupingBy(FieldedQueryFacet::getMatchType,Collectors.toList()))
    			   .entrySet()
    			   .stream()
    			   .sorted((e1,e2)->-(place.get(e2.getKey())-place.get(e1.getKey())))
    			   .forEach(e1->{
    				   grouped.put(e1.getKey(), e1.getValue());
    			   });
    	return grouped;
    	
    }

    public String getId () { return id; }
    public SearchResultContext.Status getStatus () { return _status; }
    public void setStatus (SearchResultContext.Status status) { 
    	SearchResultContext.Status ostat=this._status;
    	this._status = status;
    	notifyChange(_status,ostat);
    	
    }
    public String getMessage () { return mesg; }
    public void setMessage (String mesg) { this.mesg = mesg; }
    public Integer getCount () { return results.size(); }
    public Integer getTotal () { 
    	if(total !=null){
    		return total;
    	}else{
    		if(isDetermined()){
    			return results.size();
    		}
    	}
    	return null;
    }
    public void setTotal (int i) { 
    	total = i;
    }
    
    public Long getStart () { return start; }
    public Long getStop () { return stop; }
    
    public boolean isFinished () {
        return _status == Status.Done || _status == Status.Failed;
    }
    
    public boolean isDetermined () {
        return isFinished () || _status == Status.Determined;
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Collection getResults () { return results; }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Collection getResultsAsList () {return (results!=null)?new ArrayList<>(results):null; }
    
    protected void add (Object obj) { results.add(obj); }
    
    
    public void addListener(SearchResultContext.StatusChangeListener listener){
    	listeners.add(new SoftReference<>(listener));
    }
    
    public void removeListener(SearchResultContext.StatusChangeListener listener){
    	Iterator<SoftReference<SearchResultContext.StatusChangeListener>> iter =listeners.iterator();
    	while(iter.hasNext()){
    		SoftReference<SearchResultContext.StatusChangeListener> l = iter.next();
    		SearchResultContext.StatusChangeListener actualListener = l.get();
    		//if get() returns null then the object was garbage collected
    		if(actualListener ==null || listener.equals(actualListener)){
    			iter.remove();
    			//keep checking in the unlikely event that
    			//a listener was added twice?
    		}
    	}
    }
    
    private void notifyChange(SearchResultContext.Status newStatus, SearchResultContext.Status oldStatus){
        Iterator<SoftReference<SearchResultContext.StatusChangeListener>> iter = listeners.iterator();
        List<SearchResultContext.StatusChangeListener> tocall = new ArrayList<SearchResultContext.StatusChangeListener>();
        while(iter.hasNext()){
        	SearchResultContext.StatusChangeListener l = iter.next().get();
            if(l ==null){
                iter.remove();
            }else{
            	tocall.add(l);
            }
        }
        tocall.forEach(l -> l.onStatusChange(newStatus, oldStatus));
    }
    public void setKey(String key){
    	this.key=key;
    }
    public String getKey(){
    	return key;
    }
    
    /**
     * Get a future which will return only when
     * {@link #isDetermined()}} is true.
     *
     * @return a Future will never be null, but get() will return null when completed
     */
    @JsonIgnore
    public Future<Void> getDeterminedFuture(){
    	SearchResultContext.SearchResultContextDeterminedFuture future= new SearchResultContextDeterminedFuture(this);
        ForkJoinPool.commonPool().submit(future);
        return future;
    }
    
    public String toJson(){
    	ObjectMapper om = new ObjectMapper();
    	return om.valueToTree(this).toString();
    }
    
    
    
    //TODO: rewrite this to allow moving to core
    public String getUrl(){
    	return getCall().toString();
    }
    
    @JsonIgnore
    public Call getCall(){
    	return ix.core.controllers.search.routes.SearchFactory.getSearchResultContext(this.getKey());
    }
    
    public static SearchResultContext getSearchResultContextForKey(String key){
    	SearchResultContext context=null;
        try {
            Object value = IxCache.get(key);
            if (value != null) {
            	if(value instanceof SearchResultContext){
                    context = (SearchResultContext)value;
            	}else if(value instanceof SearchResult){
            		SearchResult result = (SearchResult)value;
            		context = new SearchResultContext (result);
                    Logger.debug("status: key="+key+" finished="+context.isFinished());
            	}
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
	    if(context!=null){
	    	context.setKey(key);
	    }
	    return context;
    }
}