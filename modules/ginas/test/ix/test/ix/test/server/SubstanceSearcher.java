package ix.test.ix.test.server;



import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ix.core.search.SearchResultContext;
import ix.ginas.models.v1.Substance;
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

    private static final Pattern SUBSTANCE_LINK_PATTERN = Pattern.compile("<a href=\"/ginas/app/substance/([a-z0-9\\-]+)\"");
    private static final Pattern TOTAL_PATTERN = Pattern.compile("[^0-9]([0-9][0-9]*)[^0-9]*h3[^0-9]*pagination");
///ginas/app/img/c37bea80-14ec-4144-8379-60c92d422713.svg?size=200&amp;context=ghtjouloym
    private static final Pattern STRUCTURE_IMG_URL = Pattern.compile("src=.(/ginas/app/img/[^\'\"]+)");
    
    private static final Pattern ROW_PATTERN = Pattern.compile("[<]label[^>]*[>]([^<]*).*?badge[^>]*[>]([^<]*)", Pattern.DOTALL);

    //"/ginas/app/api/v1/status/ceb8ca9e14006df02a6d2cee8c38e664640f2036"

    private static final Pattern  SEARCH_KEY_PATTERN = Pattern.compile("ginas/app/api/v1/status/([0-9a-f]+)");

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

        Object[] tmp;
        HtmlPage firstPage=null;
        String keyString=null;
        do {
            try {
                HtmlPage htmlPage = getSubstructurePage(smiles,rows,page, wait);
                tmp= getSubstancesFrom(htmlPage);
                if (firstPage == null) {
                    firstPage = htmlPage;
                    keyString = (String)tmp[0];
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
                throw e;
            }
        }while(substances.addAll( (Set<String>) tmp[1]));

        SearchResult results = new SearchResult(keyString, substances);
        if(results.numberOfResults() >0){
            parseFacets(results, firstPage);
        }
        return results;
    }
    
    public HtmlPage getPage(String rootUrl, int page) throws IOException{
    	return session.submit(session.newGetRequest(rootUrl + "&page=" + page));
    }
    
    public HtmlPage getSubstructurePage(String smiles, int rows, int page, boolean wait) throws IOException{
    	// Added "wait" so that it doesn't return before it's
    	// completely ready
    	// This may be a problem, as URLEncoder may over encode some smiles strings
    	String rootUrl = "ginas/app/substances?type=Substructure&q="+URLEncoder.encode(smiles, "UTF-8") + "&wait=" + wait + "&rows=" + rows;
    	
    	if(defaultSearchOrder!=null){
    		rootUrl+="&order=" + defaultSearchOrder;
    	}
    	return getPage(rootUrl, page);
    }
    
    
    public SearchResult getSubstructureSearch(String smiles, int rows, int page, boolean wait) throws IOException{
    	return new SearchResult(getSubstancesFrom(getSubstructurePage(smiles,rows,page, wait)));
    }
    public SearchResult nameSearch(String query) throws IOException{
        return query("root_names_name:\"" + query + "\"");
    }
    public SearchResult exactNameSearch(String query) throws IOException{
        return query("root_names_name:\"^" + query + "$\"");
    }
    public SearchResult codeSearch(String query) throws IOException{
        return query("root_codes_code:\"" + query + "\"");
    }
    public SearchResult exactSearch(String query) throws IOException{
        return query("^"+query + "$");
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

        String rootUrl = "ginas/app/substances?wait=true";
        if(queryOrNull !=null){
        	String encodedQueryOrNull = URLEncoder.encode(queryOrNull,"UTF-8");
            rootUrl +="&q=\"" + encodedQueryOrNull + "\"";
        }
        if(defaultSearchOrder!=null){
    		rootUrl+="&order=" + defaultSearchOrder;
    	}
        int page=1;

        Set<String> substances = new LinkedHashSet<>();

        Set<String> temp;
        HtmlPage firstPage=null;
        String keyString = null;
        do {
        	 HtmlPage htmlPage=null;
        	 try{
        		 
        		 htmlPage = session.submit(session.newGetRequest(rootUrl + "&page=" + page));
        	 }catch(Exception e){
             	break;
             }
            //stop if the paging throws an error
           String htmlText = htmlPage.asXml();
            	temp = getSubstancesFrom(htmlText);
            
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

    private Set<String> getSubstancesFrom(String html){
        Set<String> substances = new LinkedHashSet<>();
        Matcher matcher = SUBSTANCE_LINK_PATTERN.matcher(html);
        while(matcher.find()){
            substances.add(matcher.group(1));
        }


        return substances;
    }

    private Object[] getSubstancesFrom(HtmlPage page){
        String htmlText = page.asXml();
        Set<String> substances =  getSubstancesFrom(htmlText);

        return new Object[]{getKeyFrom(htmlText), substances};

    }
    
    public static Set<String> getStructureImagesFrom(HtmlPage page){
        Set<String> substances = new LinkedHashSet<>();

        String txt=page.asXml();
        System.out.println(txt);
        Matcher matcher = STRUCTURE_IMG_URL.matcher(txt);
        while(matcher.find()){
            substances.add(matcher.group(1));
        }

        return substances;
    }


    public class SearchResult{
        private final Set<String> uuids;
        private final Map<String, Map<String, Integer>> facetMap = new LinkedHashMap<>();

        private final String searchKey;
        public SearchResult(Object[] array){
            this((String)array[0], (Set<String>) array[1]);
        }
        public SearchResult(String searchKey, Set<String> uuids){
            Objects.requireNonNull(uuids);
            Objects.requireNonNull(searchKey);

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
            System.out.println("search key = " + searchKey);
            SearchResultContext src = SearchResultContext.getSearchResultContextForKey(searchKey);

            return src.getResults().stream()
                                .map( o -> (Substance) o);

        }
        public InputStream export(String format){
            WSResponse resp = SubstanceSearcher.this.session.get("ginas/app/setExport?id="+searchKey + "&format="+format);
            return resp.getBodyAsStream();
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
