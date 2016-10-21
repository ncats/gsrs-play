package ix.core.search;

import java.util.concurrent.Future;
import java.util.function.BiFunction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.mvc.Call;


/**
 * Special wrapper around an existing {@link SearchResultContext} to pass
 * along additional context information, such as the {@link #top} {@link #skip}
 * for paging. This {@link FocusedSearchResultContext} allows a link to
 * be generated directly here, rather than to the parent unfocused context.
 * @author peryeata
 *
 */
class FocusedSearchResultContext extends SearchResultContext{
	SearchResultContext ctx;
	
	public String getGeneratingUrl() {
		return ctx.getGeneratingUrl();
	}
	public void setAdapter(BiFunction<SearchOptions, SearchResultContext, SearchResult> adapter) {
		ctx.setAdapter(adapter);
	}
	public SearchResult getAdapted(SearchOptions opt) {
		System.out.println("Getting adapted");
		return ctx.getAdapted(opt);
	}
	public Future<Void> getDeterminedFuture() {
		return ctx.getDeterminedFuture();
	}
	public String getId() {
		return ctx.getId();
	}
	public Status getStatus() {
		return ctx.getStatus();
	}
	public String getMessage() {
		return ctx.getMessage();
	}
	public Integer getCount() {
		return ctx.getCount();
	}
	public Integer getTotal() {
		return ctx.getTotal();
	}
	public Long getStart() {
		return ctx.getStart();
	}
	public Long getStop() {
		return ctx.getStop();
	}
	public boolean isFinished() {
		return ctx.isDetermined();
	}
	public boolean isDetermined() {
		return ctx.isDetermined();
	}
	
	public String getKey() {
		return ctx.getKey();
	}
	
	@Override
	@JsonIgnore
    public Call getResultCall(){
    	return this.getResultCall(top, skip, fdim, field);
    }
	
	@Override
	@JsonIgnore
    public Call getCall(){
    	return getCall(top, skip, fdim, field);
    }
	
	int top=10;
	int skip=0;
	int fdim=10;
	String field="";
	
	FocusedSearchResultContext(SearchResultContext sr, int top, int skip, int fdim, String field){
		this.ctx=sr;
		this.top=top;
		this.skip=skip;
		this.fdim=fdim;
		this.field=field;
	}
}