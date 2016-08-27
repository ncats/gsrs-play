package ix.ncats.controllers;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.CacheStrategy;
import ix.core.search.FieldFacet;
import ix.core.search.SearchResult;
import ix.utils.Util;

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
    List<FieldFacet> fieldFacets=null;
    Collection results = new LinkedBlockingDeque();
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
    
    SearchResultContext () {
    }
    
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
    }
    
    public List<FieldFacet> getFieldFacets(){
    	return fieldFacets;
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
    
    public String getUrl(){
    	return routes.App.status(this.getKey()).toString();
    }
}