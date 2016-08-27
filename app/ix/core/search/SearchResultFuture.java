package ix.core.search;

import java.util.List;
import java.util.concurrent.FutureTask;

public class SearchResultFuture extends FutureTask<List>{
	public SearchResultFuture(final SearchResult result){
		super(new WaitForSearchCallable(result));
	}
    public SearchResultFuture(final SearchResult result, int numberOfRecords){
        super(new WaitForSearchCallable(result, numberOfRecords));
    }
}