package ix.core.search;

import java.util.Iterator;
import java.util.stream.Stream;

import ix.core.util.ConfigHelper;
import play.Logger;

/**
 * Structure searching
 */
public abstract class SearchResultProcessor<T, R> implements ResultProcessor<T, R> {
	
    protected Iterator<T> results;
    
    final SearchResultContext context = new SearchResultContext (); 
    boolean wait=false;
    
    public SearchResultProcessor () {}
    
    
	@Override
	public void setWait(boolean wait) {
		this.wait=wait;
	}


	@Override
	public boolean isWait() {
		return wait;
	}


	@Override
	public SearchResultContext getContext() {
		return context;
	}


	@Override
	public void setResults(Iterator<T> results) {
		this.results=results;
	}
	@Override
	public Iterator<T> getResults() {
		return this.results;
	}


	@Override
	public Stream<R> map(T result) {
		try{
			R r=instrument(result);
			if(r==null)return Stream.empty();
			return Stream.of(r);
		}catch(Exception e){
			Logger.error("error processing record", e);
			return Stream.empty();
		}
	}
	
	protected abstract R instrument(T result) throws Exception;


	
}