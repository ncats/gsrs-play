package ix.test.server;



import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import ix.core.search.SearchResultContext;
import ix.ginas.models.v1.Substance;
import ix.test.query.builder.SimpleQueryBuilder;
import ix.test.query.builder.SubstanceCondition;
import ix.test.server.BrowserSession.WrappedWebRequest;
import ix.utils.Tuple;
import play.libs.ws.WSResponse;

/**
 * Abstracts all the html parsing and ginas specific url knowledge
 * to make a prorammatic way to query a running ginas instance to search
 * for substances.
 *
 * Created by katzelda on 4/5/16.
 */
public class SubstanceSearcher {

    private final BrowserSession session;
    private static final Pattern SUBSTANCE_LINK_HREF_PATTERN = Pattern.compile("/ginas/app/substance/([a-z0-9\\-]+)");
    private static final Pattern TOTAL_PATTERN = Pattern.compile("[^0-9]([0-9][0-9]*)[^0-9]*h3[^0-9]*pagination");
   
    private static final Pattern  SEARCH_KEY_PATTERN = Pattern.compile("ginas/app/api/v1/status\\(([0-9a-f]+)\\)");

    private String defaultSearchOrder =null;
    
    public SubstanceSearcher(BrowserSession session) {
        Objects.requireNonNull(session);

        this.session = session;
    }
    
    public void setSearchOrder(String order){
    	this.defaultSearchOrder=order;
    }
    
    
    

    /**
     * Get the UUIDs of all the loaded substances that have this substructure.
     * Assumes the page size for searches is 16.
     * @param smiles a kekulized SMILES string of the substructure to search for.
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    public SearchResult substructure(String smiles) throws IOException {
    	return substructure(smiles, 16, true);
    }
    
    /**
     * Get the UUIDs of all the loaded substances that have this substructure.
     * @param smiles a kekulized SMILES string of the substructure to search for.
     * @param rows the number of rows per page to return
     * @return a Set of UUIDs that match. will never be null but may be empty.
     *
     * @throws IOException if there is a problem parsing the results.
     */
    public SearchResult substructure(String smiles, int rows, boolean wait) throws IOException {

        //TODO have to kekulize


    	
        int page=1;

        Set<String> substances = new LinkedHashSet<>();

        Tuple<String,Set<String>> tmp=null;
        HtmlPage firstPage=null;
        String keyString=null;
        do {
            try {
                HtmlPage htmlPage = getSubstructurePage(smiles,rows,page, wait);
                tmp= getSubstancesFrom(htmlPage);
                if (firstPage == null) {
                    firstPage = htmlPage;
                    keyString = tmp.k();
                }

                

                page++;
                //we check the return value of the add() call
                //because when we get to the page beyond the end of the results
                //it returns the first page again
                //so we can check to see if we've already added these
                //records (so add() will return false)
                //which will break us out of the loop.
            }catch(FailingHttpStatusCodeException e){
            	
                //Code looks like it's been improved
                //to throw an exception if you page too far
                //so swallow that exception.
                if(e.getResponse().getContentAsString().contains("Bogus page")){
                    break;
                }
                throw new IllegalStateException(e);
            }catch(Exception e){
            	e.printStackTrace();
            }
        }while(substances.addAll(tmp.v()));

        SearchResult results = new SearchResult(keyString, substances);
        if(results.numberOfResults() >0){
            parseFacets(results, firstPage);
        }
        return results;
    }
    
    public HtmlPage getPage(WrappedWebRequest wwr, int page) throws IOException{
    	return session.submit(wwr.setQueryParameter("page", page+"").get());
    }
    
    public HtmlPage getSubstructurePage(String smiles, int rows, int page, boolean wait) throws IOException{
    	// Added "wait" so that it doesn't return before it's
    	// completely ready
    	
    	
    	// This may be a problem, as URLEncoder may over encode some smiles strings
    	WrappedWebRequest root=session.newGetRequest("ginas/app/substances")
    		.addQueryParameter("type", "Substructure")
    		.addQueryParameter("q", smiles)
    		.addQueryParameter("wait", wait+"")
    		.addQueryParameter("rows", rows+"");
    	
    	if(defaultSearchOrder!=null){
    		root=root.addQueryParameter("order",defaultSearchOrder);
    	}
    	try{
    		return getPage(root, page);
    	}catch(Exception e){
    		//e.printStackTrace();
    		//System.out.println("Something went wrong with request:" + root.setQueryParameter("page", page+"").get());
    		
//    		
//    		try {
//				Thread.sleep(1_000);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
    		throw e;
    	}
    }
    
    
    public SearchResult getSubstructureSearch(String smiles, int rows, int page, boolean wait) throws IOException{
    	return new SearchResult(getSubstancesFrom(getSubstructurePage(smiles,rows,page, wait)));
    }
    
    public SearchResult nameSearch(String query) throws IOException{
    	String q = new SimpleQueryBuilder()
    			.where()
    			.condition(SubstanceCondition.name(query).phrase())
    			.build();
    	return query(q);
    }
    
    /**
     * Performs a name search, trusting that the query term is
     * formatted exactly as it needs to be, with any included
     * quotes or wildcard characters
     * @param query
     * @return
     * @throws IOException
     */
    public SearchResult nameRawSearch(String query) throws IOException{
    	String q = new SimpleQueryBuilder()
    			.where()
    			.condition(SubstanceCondition.name(query).raw())
    			.build();
    	return query(q);
    }
    
    public SearchResult exactNameSearch(String query) throws IOException{
    	String q = new SimpleQueryBuilder()
    			.where()
    			.condition(SubstanceCondition.name(query).exact())
    			.build();
        return query(q);
    }
    public SearchResult codeSearch(String query) throws IOException{
    	String q = new SimpleQueryBuilder()
    			.where()
    			.condition(SubstanceCondition.code(query).phrase())
    			.build();
        return query(q);
    }
    public SearchResult exactSearch(String query) throws IOException{
    	String q = new SimpleQueryBuilder()
    			.where()
    			.globalMatchesExact(query)
    			.build();
        return query(q);
    }
    
    
    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    public SearchResult query(UUID uuid) throws IOException {
        return query(uuid.toString());
    }
    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    public SearchResult query(String queryString) throws IOException {
        return performSearch(queryString);
    }
    
    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    public SearchResult all() throws IOException {
        return performSearch(null);
    }
    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    private SearchResult performSearch(String queryOrNull) throws IOException {

        WrappedWebRequest req=session.newGetRequest("ginas/app/substances")
        	.addQueryParameter("wait", "true");
        	
        if(queryOrNull !=null){
        	req=req.addQueryParameter("q", queryOrNull);
        }
        
        if(defaultSearchOrder!=null){
        	req=req.addQueryParameter("order", defaultSearchOrder);
    	}
        int page=1;

     //   System.out.println("query url is " + rootUrl);
        Set<String> substances = new LinkedHashSet<>();

        Set<String> temp;
        HtmlPage firstPage=null;
        String keyString = null;
        do {
        	 HtmlPage htmlPage=null;
        	 try{
        		 htmlPage = session.submit(req.setQueryParameter("page", page+"").get());
        	 }catch(Exception e){
//                 e.printStackTrace();
             	break;
             }
        	temp = getSubstancesFrom(htmlPage).v();
            //stop if the paging throws an error
            String htmlText = htmlPage.asXml();
            
            
            if(firstPage ==null){
                firstPage = htmlPage;
                keyString = getKeyFrom(htmlText);
            }
            page++;

            Matcher m=TOTAL_PATTERN.matcher(htmlText);
            String total = null;
            if(m.find()){
            	total=m.group(1);

            }
            
            //we check the return value of the add() call
            //because when we get to the page beyond the end of the results
            //it returns the first page again
            //so we can check to see if we've already added these
            //records (so add() will return false)
            //which will break us out of the loop.
        }while(substances.addAll(temp));
        
        
        
        SearchResult results = new SearchResult(keyString, substances);
        if(results.numberOfResults() >0){
            parseFacets(results, firstPage);
        }
        return results;
    }

    private String getKeyFrom(String htmlText) {
        Matcher m = SEARCH_KEY_PATTERN.matcher(htmlText);
        if(!m.find()){
            //throw new IllegalStateException("could not find search key for " + htmlText);
            return "";
        }
        return m.group(1);
    }


    private void parseFacets(SearchResult results, HtmlPage html) throws IOException{
    	Map<String, Map<String,Integer>> map = new LinkedHashMap<>();

    	html.querySelectorAll("div.panel-default")
    		.parallelStream() //Not necessary, but kinda cool
    		.filter(n->n.asXml().contains("toggleFacet"))
    		.forEach(c->{
    			String facetName = c.querySelector("h3").asText().trim();
    			Map<String,Integer> counts=
    				c.querySelectorAll("div.row.list-group-item") //Each facet value
    				.stream()
    				.map(d->new String[]{
    					d.querySelector("label").asText().trim(),
    					d.querySelector("span.badge").asText().trim()
    					})
    				.collect(Collectors.toMap(s->s[0], s->Integer.parseInt(s[1])));
    			map.put(facetName, counts);
    		});        

        for(Map.Entry<String, Map<String, Integer>> next : map.entrySet()){
            if(!next.getValue().isEmpty()){
                results.setFacet(next.getKey(), next.getValue());
            }
        }



    }
//
//    private Set<String> getSubstancesFrom(String html){
//        Set<String> substances = new LinkedHashSet<>();
//        
//        Matcher matcher = SUBSTANCE_LINK_PATTERN.matcher(html);
//        while(matcher.find()){
//            substances.add(matcher.group(1));
//        }
//
//
//        return substances;
//    }

    public static Function<String, Optional<String>> getMatchingGroup(Pattern p, int group){
    	return (s)->{
    		Matcher m = p.matcher(s);
    		if(!m.find()){
    			return Optional.empty();
    		}else{
    			return Optional.of(m.group(group));
    		}
    	};
    }
    
    private Tuple<String,Set<String>> getSubstancesFrom(HtmlPage page){
        String htmlText = page.asXml();
        Set<String> substances =  page.querySelectorAll("a[href*=\"ginas/app/substance/\"]")
        	.stream()
        	.map(a->a.getAttributes().getNamedItem("href").getNodeValue())
        	.map(getMatchingGroup(SUBSTANCE_LINK_HREF_PATTERN, 1))
        	.filter(o->o.isPresent())
        	.map(o->o.get())
        	.collect(Collectors.toSet());
        
        return Tuple.of(getKeyFrom(htmlText), substances);

    }
    
    public static Set<String> getStructureImagesFrom(HtmlPage page){
        Set<String> substances = page.querySelectorAll("img[src*=\"ginas/app/img\"]")
        .stream()
        .map(m->m.getAttributes().getNamedItem("src").getNodeValue())
        .collect(Collectors.toSet());
        
        return substances;
    }
    
    public class WebExportRequest{
    	private String format;
    	private String key;
    	private long timeout;
    	
    	public WebExportRequest(){
    		
    	}
    	public WebExportRequest(String key, String format, long timeout){
    		this.format=format;
    		this.key=key;
    		this.timeout=timeout;
    	}
    	
    	public WebExportRequest setTimeout(long t){
    		this.timeout=t;
    		return this;
    	}
    	public WebExportRequest setKey(String key){
    		this.key=key;
    		return this;
    	}
    	public WebExportRequest setFormat(String format){
    		this.format=format;
    		return this;
    	}
    	
    	public InputStream getInputStream(){
    		return getWSResponse()
    			.getBodyAsStream();
    	}
    	public WSResponse getWSResponse(){
    		String url=getMeta().at("/url").asText();
    		return SubstanceSearcher.this.session.get(url, timeout);
    	}
    	
    	public JsonNode getMeta(){
        	WSResponse resp = SubstanceSearcher.this.session.get("ginas/app/setExport?id="+key + "&format="+format, timeout);
            return resp.asJson();
        }
    	
    	public boolean isReady(){
    		return getMeta().at("/isReady").asBoolean();
    	}
    }
    
    
    public WebExportRequest getExport(String format, String key){
    	return new WebExportRequest(key,format,SubstanceSearcher.this.session.timeout);
    }
    


    public class SearchResult{
        private final Set<String> uuids;
        private final Map<String, Map<String, Integer>> facetMap = new LinkedHashMap<>();

        private final String searchKey;
        public SearchResult(Tuple<String, Set<String>> set){
            this(set.k(), set.v());
        }
        public SearchResult(String searchKey, Set<String> uuids){
            Objects.requireNonNull(uuids);
            try{
            	Objects.requireNonNull(searchKey);
            }catch(Exception e){
            	e.printStackTrace();
            	throw e;
            	
            }

            this.searchKey = searchKey;
            this.uuids = Collections.unmodifiableSet(new LinkedHashSet<>(uuids));

        }
        public String getKey(){
            return searchKey;
        }
        public Set<String> getUuids(){
            return uuids;
        }

        public int numberOfResults(){
            return uuids.size();
        }


        public Stream<Substance> getSubstances(){
            SearchResultContext src = SearchResultContext.getSearchResultContextForKey(searchKey);

            return src.getResults().stream()
                                .map( o -> (Substance) o);

        }
        public InputStream export(String format){
            return getExport(format,searchKey).getInputStream();
        }
        
        

        public Map<String, Integer> getFacet(String facetName){
            return facetMap.get(facetName);
        }

        public void setFacet(String facetName, Map<String, Integer> countMap){
            Objects.requireNonNull(facetName);
            Objects.requireNonNull(countMap);

            Map<String, Integer> copy = new TreeMap<>(new SortByValueComparator(countMap, Order.DECREASING));
            copy.putAll(countMap);

            facetMap.put(facetName, Collections.unmodifiableMap(copy));


        }

        public Map<String, Map<String, Integer>> getAllFacets(){
            return Collections.unmodifiableMap(facetMap);
        }


    }

    enum Order implements Comparator<Integer>{
        INCREASING{
            @Override
            public int compare(Integer o1, Integer o2) {
                if(o1 ==null && o2==null){
                    return 0;
                }
                if(o2 ==null){
                    return -1;
                }
                if(o1 ==null){
                    return 1;
                }
                return Integer.compare(o1,o2);
            }
        },
        DECREASING{
            @Override
            public int compare(Integer o1, Integer o2) {
                //note parameter order is swapped
                return INCREASING.compare(o2, o1);
            }
        };
    }
    private static class SortByValueComparator<T extends Comparable<? super T>, V> implements Comparator<T>{
        private final Map<T, V> countMap;
        private Comparator<V> order;
        public SortByValueComparator(Map<T, V> countMap, Comparator<V> order) {
            this.countMap = countMap;
            this.order = order;
        }
        @Override
        public int compare(T s1, T s2) {
            int valueCmp= order.compare(countMap.get(s1), countMap.get(s2));
            if(valueCmp !=0){
                return valueCmp;
            }
            //values are equal, sort by key?
            return s1.compareTo(s2);
        }
    }
}
