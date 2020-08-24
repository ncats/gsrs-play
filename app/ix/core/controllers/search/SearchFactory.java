package ix.core.controllers.search;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Supplier;

import ix.core.plugins.Workers;
import ix.core.util.GinasPortalGun;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.Experimental;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.v1.RouteFactory;
import ix.core.models.ETag;
import ix.core.plugins.IxCache;
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
import ix.utils.CallableUtil.TypedCallable;
import ix.utils.Global;
import ix.utils.Util;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

public class SearchFactory extends EntityFactory {
    static CachedSupplier<Model.Finder<Long, ETag>> etagDb = Util.finderFor(Long.class, ETag.class);

    private static CachedSupplier<TextIndexerPlugin> textIndexerPlugin= 
    		CachedSupplier.of(()->Play.application().plugin(TextIndexerPlugin.class));
   

    public static TextIndexer getTextIndexer(){
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
        
        try {
        return indexer.search(searchRequest.getOptions(), searchRequest.getQuery(), searchRequest.getSubset());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("error searching", e);
        }
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
        
    public static F.Promise<Result> searchREST (String q, int top, int skip, int fdim) {
        return searchREST (SearchRequest.Builder.class, null, q, top, skip, fdim);
    }
    
    private static SearchRequest getSearchRequest(Class<? extends SearchRequest.Builder> requestBuilderClass, Class<?> kind, String q, int top, int skip, int fdim) throws IOException {
        SearchRequest.Builder builder;
        try {
            builder = requestBuilderClass.newInstance();
        }catch(Exception e){
            throw new IOException("error instantiating new request builder", e);
        }
        SearchRequest req = builder
                .top(top)
                .skip(skip)
                .fdim(fdim)
                .kind(kind)
                .withRequest(request()) // I don't like this, 
                                        // I like being explicit,
                                        // but it's ok for now
                .query(q)
                .build();      
        return req;
    }
    
    
    public static SearchResult search (Class<? extends SearchRequest.Builder> requestBuilderClass, Class<?> kind, String q, int top, int skip, int fdim) throws IOException {
        SearchRequest req = getSearchRequest(requestBuilderClass, kind,q,top,skip,fdim);
        SearchResult result = search(req);
        return result;

    }
        
    public static F.Promise<Result> searchREST (Class<? extends SearchRequest.Builder> requestBuilderClass, Class<?> kind, String q, int top, int skip, int fdim) {
        if (Global.DEBUG(1)) {
            Logger.debug("SearchFactory.search: kind="
                         +(kind != null ? kind.getName():"")+" q="
                         +q+" top="+top+" skip="+skip+" fdim="+fdim);
        }
        return Workers.WorkerPool.DB_EXPENSIVE_READ_ONLY.newJob(new F.Function0<SearchResultPair>() {
            @Override
            public SearchResultPair apply() {
                SearchResult result = null;
                try {
                    result = search(requestBuilderClass, kind,q,top,skip,fdim);
                } catch (IOException e) {
                   return new SearchResultPair(SearchResult.createErrorResult(e), Collections.emptyList() );
                }

                List<Object> results = new ArrayList<Object>();

                result.copyTo(results, 0, top, true); //this looks wrong, because we're not skipping
                //anything, but it's actually right,
                //because the original request did the skipping.
                //This mechanism should probably be worked out
                //better, as it's not consistent.
                return new SearchResultPair(result, results);
            }

        }).andThen(Workers.WorkerPool.DB_WRITE, pair -> {
//
                SearchResult result = pair.searchResult;
                if (result.hasError()) {
                    return (Result) badRequest(result.getThrowable().get().getMessage());
                }
                //the etag only saves the subset result?
                    String key = GinasPortalGun.getBestKeyForCurrentRequest() + "REST";
                    result.getMatches();
                    IxCache.getOrElse(getTextIndexer().lastModified(), key, ()->result);

                 return saveAsEtag(pair.resultList, result);
            }
        )
                .toPromise();

    }

    private static class SearchResultPair{
        SearchResult searchResult;
        List<Object> resultList;

        public SearchResultPair( SearchResult searchResult, List<Object> resultList) {
            this.resultList = resultList;
            this.searchResult = searchResult;
        }
    }
    private static Result saveAsEtag(List<Object> results, SearchResult result) {
        final ETag etag = new ETag.Builder()
                .fromRequest(request())
                .options(result.getOptions())
                .count(results.size())
                .total(result.getCount())

                .sha1OfRequest("q", "facet")
                .build();

        if(request().queryString().get("export") ==null) {
            etag.save();
        }
        etag.setContent(results);
        etag.setSponosredResults(result.getSponsoredMatches());
        etag.setFacets(result.getFacets());
        etag.setFieldFacets(result.getFieldFacets());
        etag.setSelected(result.getOptions().getFacets(), result.getOptions().isSideway());


        EntityMapper mapper = getEntityMapper();
        return Java8Util.ok(mapper.valueToTree(etag));
    }

    public static Result searchRESTFacets (Class<SearchRequest.Builder> searchRequestBuilderClass, Class<?> kind, String q, String facetField, int fdim, int fskip, String ffilter) {
      
        try {
             
            SearchRequest sq = getSearchRequest(searchRequestBuilderClass, kind, q, Integer.MAX_VALUE, 0,fdim);
            
            
            String fkey = sq.getDefiningSetSha1() + "/" + "facet/" + facetField;
            
            TextIndexer indexer= Play
                    .application()
                    .plugin(TextIndexerPlugin.class)
                    .getIndexer();
            
            TermVectors vec = IxCache.getOrElse(indexer.lastModified(),fkey, TypedCallable.of(()->{
                try{

                    Query query = sq.extractFullFacetQuery(indexer, facetField);
                    return indexer
                        .getTermVectors(kind, facetField, (Filter)null,query);
                }catch(Exception e){
                    e.printStackTrace();
                    throw e;
                }
            }, TermVectors.class));
            
            
            
            FacetMeta fm=vec.getFacet(fdim, fskip, ffilter, Global.getHost() + Controller.request().uri());       
            
            EntityMapper mapper = getEntityMapper ();
            return Java8Util.ok (mapper.valueToTree(fm));
        }catch (Exception ex) {
            ex.printStackTrace();
            return RouteFactory._apiBadRequest(ex);
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
                    node.set(f, mapper.valueToTree(results));
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
        SearchResultContextOrSerialized possibleContext=SearchResultContext.getContextForKey(key);
        
        if (possibleContext != null) {
            if(!possibleContext.hasFullContext()){
                return redirect(possibleContext.getSerialized().generatingPath);
            }
            SearchResultContext context = possibleContext.getContext();
            
            
            SearchRequest sr = new SearchRequest.Builder()
                    .fdim(10)
                    .withParameters(Util.reduceParams(request().queryString(), 
                                    "fdim", "fskip", "ffilter", "q", "facet", "sideway"))
                    .query(request().getQueryString("q")) //TODO: Refactor this
                    .build();
            
            String fkey = context.getKey() + "/facets/" + sr.getDefiningSetSha1() + "/" +field; 
            
            TextIndexer indexer= Play
                    .application()
                    .plugin(TextIndexerPlugin.class)
                    .getIndexer();
            
            
            TermVectors vec = IxCache.getOrElse(indexer.lastModified(), fkey, TypedCallable.of(()->{
                //All of this only to get the "kind" filter needed
                //TODO: Could be added to the search result context as a high-level field
                
                
                Object first=context.getResults()
                        .stream()
                        .findFirst()
                        .get();
    
                Class<Object> kind = (Class<Object>) EntityWrapper.of(first)
                    .getEntityInfo()
                    .getInherittedRootEntityInfo()
                    .getEntityClass();
                
                
                Query q=sr.extractFullFacetQuery(indexer, field);
                
                return indexer
                        .getTermVectors(kind, field, (List<Object>)context.getResultsAsList(),q);
                
            }, TermVectors.class));
            
            SearchOptions so =sr.getOptions();
            
            FacetMeta fm=vec.getFacet(so.getFdim(), so.getFskip(), so.getFfilter(), Global.getHost() + Controller.request().uri());       
            
            EntityMapper em = EntityFactory.getEntityMapper();
            
            return Java8Util.ok(em.valueToTree(fm));
        }
        return RouteFactory._apiNotFound("No key found: "+key+"!");
    }
    
    
    //TODO: Needs evaluation
    @Experimental
    public static Result getSearchResultContextResults(String key, int top, int skip, int fdim, String field) {
        SearchResultContextOrSerialized possibleContext=SearchResultContext.getContextForKey(key);
    	
    	if (possibleContext != null) {
    		if(!possibleContext.hasFullContext()){
    			return redirect(possibleContext.getSerialized().generatingPath);
    		}
    		SearchResultContext ctx=possibleContext.getContext();
    		
    		
    		EntityMapper em=EntityFactory.getEntityMapper();
    		
    		SearchRequest searchRequest = new SearchRequest.Builder()
				    				.top(top)
				    				.skip(skip)
				    				.fdim(fdim)
				    				.withParameters(Util.reduceParams(request().queryString(), 
				    				        "facet", "sideway"))
				    				.query(request().getQueryString("q")) //TODO: Refactor this
				    				.build();
    		
    		
    		
    		SearchResult results = ctx.getAdapted(searchRequest);
    		
    		PojoPointer pp = PojoPointer.fromURIPath(field);
    		
    		List<Object> resultSet = new ArrayList<Object>();
    		
    		SearchOptions so = searchRequest.getOptions();
    		
    		results.copyTo(resultSet, so.getSkip(), so.getTop(), true);
    		
    		

    		int count = resultSet.size();
    		
    		
    		Object ret=EntityWrapper.of(resultSet)
    					 .at(pp)
    					 .get()
    					 .getValue();
    		
    		final ETag etag = new ETag.Builder()
            		.fromRequest(request())
            		.options(searchRequest.getOptions())
    				.count(count)
    				.total(results.getCount())
    				.sha1(Util.sha1(ctx.getKey()))
    				.build();
    		
            etag.save(); //Always save?
            
            etag.setFacets(results.getFacets());
            etag.setContent(ret);
            etag.setFieldFacets(results.getFieldFacets());
            //TODO Filters and things
    		
            return Java8Util.ok (em.valueToTree(etag));
    	}
        return notFound ("No key found: "+key+"!");
    }

}
