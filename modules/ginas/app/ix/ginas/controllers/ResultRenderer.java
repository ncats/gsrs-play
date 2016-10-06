package ix.ginas.controllers;

import ix.core.search.SearchResultContext;
import play.twirl.api.Html;

public interface ResultRenderer<T> {
	public Html render(T t, SearchResultContext ctx);
}