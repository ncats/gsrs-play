package ix.ncats.controllers;

import java.util.List;

import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import play.mvc.Result;

/**
 * interface for rendering a result page
 */
public interface ResultRenderer<T> {
    Result render (SearchResultContext context,
                   int page, int rows, int total, int[] pages,
                   List<TextIndexer.Facet> facets, List<T> results);
    int getFacetDim ();
}