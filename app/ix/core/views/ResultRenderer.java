package ix.core.views;

import ix.core.search.SearchResultContext;
import play.twirl.api.Html;

public interface ResultRenderer<T> {
	public Html render(T t, SearchResultContext ctx);
	
	public default Html render(T t){
	    return render(t,null);
	}
	
}