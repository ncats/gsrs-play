package ix.core.controllers.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.Experimental;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.v1.RouteFactory;
import ix.core.models.ETag;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.SearchOptions;
import ix.core.search.SearchResult;
import ix.core.search.SearchResultContext;
import ix.core.search.SearchResultContext.SearchResultContextOrSerialized;
import ix.core.search.SuggestResult;
import ix.core.search.text.FacetMeta;
import ix.core.search.text.TextIndexer;
import ix.core.search.text.TextIndexer.TermVectors;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.EntityUtils.Key;
import ix.core.util.Java8Util;
import ix.core.util.pojopointer.PojoPointer;
import ix.utils.Global;
import ix.utils.Util;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Result;

public class SearchFactory extends EntityFactory {
    static CachedSupplier<Model.Finder<Long, ETag>> etagDb = Util.finderFor(Long.class, ETag.class);

    private static CachedSupplier<TextIndexerPlugin> textIndexerPlugin= 
    		CachedSupplier.of(()->Play.application().plugin(TextIndexerPlugin.class));
   

    static TextIndexer getTextIndexer(){
        return textIndexerPlugin.get().getIndexer();
    }
    
    public static SearchResult search(TextIndexer indexer, SearchRequest searchRequest) throws IOException{
    	String q = searchRequest.getQuery();
    	
    	//If this search is an etag search, fetch that existing etag
    	//and apply the existing query that is specified by that 
    	//etag.
    	//This may or may not be what we want 
    	//TODO: investigate this
    	if (q != null && (q.startsWith("etag:") || q.startsWith("ETag:"))) {
            String id = q.substring(5, 21);
            try {
                ETag etag = etagDb.get().where().eq("etag", id).findUnique();
                if (etag.query != null) {
                    if (etag.filter != null) {
                        String[] facets = etag.filter.split("&");
                        for (int i = facets.length; --i >= 0; ) {
                            if (facets[i].length() > 0) {
                            	searchRequest.getOptions().getFacets().add
                                    (0, facets[i].replaceAll("facet=", ""));
                            }
                        }
                    }
                    searchRequest.setQuery(etag.query); // rewrite the query
                }else {
                    Logger.warn("ETag "+id+" is not a search!");
                }
            }catch (Exception ex) {
                Logger.trace("Can't find ETag "+id, ex);
            }
        }
        
        return indexer.search(searchRequest.getOptions(), searchRequest.getQuery(), searchRequest.getSubset());
    }
    
    /**
     * A more explicit form of {@link #search(TextIndexer, SearchRequest)} which
     * just contains the options explicitly as arguments rather than as a wrapped
     * {@link SearchRequest}. This is not the preferred mechanism, as it makes
     * discoverability more difficult, but it is kept for legacy purposes. 
     *
     *@deprecated
     */
    @Deprecated
    public static SearchResult
        search (TextIndexer indexer, 
        		Class<?> kind, 
        		Collection<?> subset,
                String q, 
                int top, 
                int skip, 
                int fdim,
                Map<String, String[]> queryParams) throws IOException {
    	
        SearchRequest request = new SearchRequest.Builder()
        			.top(top)
        			.kind(kind)
        			.skip(skip)
        			.fdim(fdim)
        			.withParameters(queryParams)
        			.query(q)
        			.subset(subset)
        			.build();
        
        return search(indexer, request);
    }
    
    /**
     * Preferred mechanism to do searching.
     * @param request
     * @return
     * @throws IOException
     */
    public static SearchResult search(SearchRequest request) throws IOException{
    	return search(getTextIndexer(), request);
    }
        
    public static Result searchREST (String q, int top, int skip, int fdim) {
        return searchREST (null, q, top, skip, fdim);
    }
    
    public static SearchResult search (Class<?> kind, String q, int top, int skip, int fdim) throws IOException {
        SearchRequest req = new SearchRequest.Builder()
                .top(top)
                .skip(skip)
                .fdim(fdim)
                .kind(kind)
                .withRequest(request()) // I don't like this, 
                                        // I like being explicit
                .query(q)
                .build();               

        SearchResult result = search(req);
        return result;

    }
        
    public static Result searchREST (Class<?> kind, String q, int top, int skip, int fdim) {
        if (Global.DEBUG(1)) {
            Logger.debug("SearchFactory.search: kind="
                         +(kind != null ? kind.getName():"")+" q="
                         +q+" top="+top+" skip="+skip+" fdim="+fdim);
        }

        try {
        					
        	
            SearchResult result = search(kind,q,top,skip,fdim);
            
            List<Object> results = new ArrayList<Object>();
            
            result.copyTo(results, 0, top, true); //this looks wrong, because we're not skipping
            									  //anything, but it's actually right,
                        						  //because the original request did the skipping.
             									  //This mechanism should probably be worked out.
                                                  //better
            
            final ETag etag = new ETag.Builder()
            		.fromRequest(request())
    				.options(result.getOptions())
    				.count(results.size()) 
    				.total(result.getCount())
    				.sha1OfRequest("q", "facet")
    				.build();
            
            
            etag.save();
            etag.setContent(results);
            etag.setFacets(result.getFacets());
            etag.setSelected(result.getOptions().getFacets(), result.getOptions().isSideway());
            
            
            EntityMapper mapper = getEntityMapper ();
            return Java8Util.ok (mapper.valueToTree(etag));
        }
        catch (IOException ex) {
            return badRequest (ex.getMessage());
        }
    }
    
    public static Result searchRESTFacets (Class<?> kind, String q, String facetField, int fdim, int fskip, String ffilter) {
      
        try {
                            
            System.out.println("Searching for:" + q);
            SearchResult result = search(kind, q, Integer.MAX_VALUE, 0,fdim);
            
            List<Key> keyResults = new ArrayList<Key>();
            
            result.copyKeysTo(keyResults, 0, Integer.MAX_VALUE, true); 
            
            TermVectors vec=Play
                    .application()
                    .plugin(TextIndexerPlugin.class)
                    .getIndexer()
                    .getTermVectorsFromKeys(kind, facetField, keyResults);
            
            FacetMeta fm=vec.getFacet(fdim, fskip, ffilter, Global.getHost() + Controller.request().uri());       
            
            EntityMapper mapper = getEntityMapper ();
            return Java8Util.ok (mapper.valueToTree(fm));
        }catch (Exception ex) {
            return badRequest (ex.getMessage());
        }
    }
    

    public static Result suggest (String q, int max) {
        return suggestField (null, q, max);
    }

    public static Result suggestField (String field, String q, int max) {
        try {
            ObjectMapper mapper = new ObjectMapper ();
            if (field != null) {
                List<SuggestResult> results =
                        getTextIndexer().suggest(field, q, max);
                return Java8Util.ok (mapper.valueToTree(results));
            }

            ObjectNode node = mapper.createObjectNode();
            for (String f : getTextIndexer().getSuggestFields()) {
                List<SuggestResult> results =
                        getTextIndexer().suggest(f, q, max);
                if (!results.isEmpty())
                    node.put(f, mapper.valueToTree(results));
            }
            //Logger.info(node.toString());
            return ok (node);
        }catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }
    }

    public static Result suggestFields () {
        ObjectMapper mapper = new ObjectMapper ();
        return Java8Util.ok (mapper.valueToTree(getTextIndexer().getSuggestFields()));
    }
    
    //TODO: Needs evaluation
    public static Result getSearchResultContext (String key) {
    	return getSearchResultContext(key,10,0,10,"");
    }
    

    
    //TODO: Needs evaluation
    public static Result getSearchResultContext (String key, int top, int skip, int fdim, String field) {
        System.out.println("getting context:" + key);
    	SearchResultContextOrSerialized possibleContext=SearchResultContext.getContextForKey(key);
    	if(possibleContext!=null){
	    	if (possibleContext.hasFullContext()) {
	    		SearchResultContext ctx=possibleContext.getContext()
	    											    .getFocused(top, skip, fdim, field);
	    		ObjectMapper mapper = new ObjectMapper ();
	            return Java8Util.ok (mapper.valueToTree(ctx));
	    	}else if(possibleContext.getSerialized() !=null){
	    		return redirect(possibleContext.getSerialized().generatingPath);
	    	}
    	}
        return RouteFactory._apiNotFound("No key found: "+key+"!");
    }
    
    @Experimental
    public static Result getSearchResultContextFacets(String key, String field) throws Exception {
        System.out.println("getting context facets:" + key);
        SearchResultContextOrSerialized possibleContext=SearchResultContext.getContextForKey(key);
        
        if (possibleContext != null) {
            if(!possibleContext.hasFullContext()){
                return redirect(possibleContext.getSerialized().generatingPath);
            }
            SearchResultContext context = possibleContext.getContext();
            
            
            SearchOptions so = new SearchOptions.Builder()
                    .fdim(10)
                    .fskip(0)
                    .ffilter("")
                    .withParameters(Util.reduceParams(request().queryString(), 
                                    "fdim", "fskip", "ffilter"))
                    .build();
            
            Object first=context.getResults()
                                .stream()
                                .findFirst()
                                .get();
            
            TermVectors vec=Play
                    .application()
                    .plugin(TextIndexerPlugin.class)
                    .getIndexer()
                    .getTermVectors(first.getClass(), field, context.getResultsAsList());
            
            FacetMeta fm=vec.getFacet(so.getFdim(), so.getFskip(), so.getFfilter(), Global.getHost() + Controller.request().uri());       
            
            EntityMapper em = EntityFactory.getEntityMapper();
            
            return Java8Util.ok(em.valueToTree(fm));
        }
        return RouteFactory._apiNotFound("No key found: "+key+"!");
    }
    
    
    //TODO: Needs evaluation
    @Experimental
    public static Result getSearchResultContextResults(String key, int top, int skip, int fdim, String field) {
        System.out.println("getting context:" + key);
        SearchResultContextOrSerialized possibleContext=SearchResultContext.getContextForKey(key);
    	
    	if (possibleContext != null) {
    		if(!possibleContext.hasFullContext()){
    			return redirect(possibleContext.getSerialized().generatingPath);
    		}
    		SearchResultContext ctx=possibleContext.getContext();
    		
    		
    		EntityMapper em=EntityFactory.getEntityMapper();
    		
    		//TODO: include facets and such by passing through
    		//to search result as well.
    		SearchRequest so = new SearchRequest.Builder()
				    				.top(top)
				    				.skip(skip)
				    				.fdim(fdim)
				    				.withParameters(Util.reduceParams(request().queryString(), 
				    				                "facet"))
				    				.build();
    		
    		
    		
    		SearchResult results = ctx.getAdapted(so);
    		
    		PojoPointer pp = PojoPointer.fromUriPath(field);
    		
    		List<Object> resultSet = new ArrayList<Object>();
    		
    		results.copyTo(resultSet, so.getOptions().getSkip(), so.getOptions().getTop(), true);
    		
    		

    		int count = resultSet.size();
    		
    		
    		Object ret=EntityWrapper.of(resultSet)
    					 .at(pp)
    					 .get()
    					 .getValue();
    		
    		final ETag etag = new ETag.Builder()
            		.fromRequest(request())
            		.options(so.getOptions())
    				.count(count)
    				.total(results.getCount())
    				.sha1(Util.sha1(ctx.getKey()))
    				.build();
    		
    		
    		
            etag.save(); //Always save?
            
            etag.setFacets(results.getFacets());
            etag.setContent(ret);
            //TODO Filters and things
    		
            return Java8Util.ok (em.valueToTree(etag));
    	}
        return notFound ("No key found: "+key+"!");
    }

}
