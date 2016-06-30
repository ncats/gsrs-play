package ix.test.ix.test.server;



import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 4/5/16.
 */
public class SubstanceSearch {

    private final BrowserSession session;

    private static final Pattern SUBSTANCE_LINK_PATTERN = Pattern.compile("<a href=\"/ginas/app/substance/([a-z0-9]+)\"");
    private static final Pattern TOTAL_PATTERN = Pattern.compile("[^0-9]([0-9][0-9]*)[^0-9]*h3[^0-9]*pagination");
///ginas/app/img/c37bea80-14ec-4144-8379-60c92d422713.svg?size=200&amp;context=ghtjouloym
    private static final Pattern STRUCTURE_IMG_URL = Pattern.compile("src=.(/ginas/app/img/[^\'\"]+)");
    
    private static final Pattern ROW_PATTERN = Pattern.compile("(un)?checked\\s+(\\S+(\\s+\\S+)?)\\s+(\\d+)");

    public SubstanceSearch(BrowserSession session) {
        Objects.requireNonNull(session);

        this.session = session;
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

        Set<String> temp;
        HtmlPage firstPage=null;
        do {
            try {
                HtmlPage htmlPage = getSubstructurePage(smiles,rows,page, wait);
                temp = getSubstancesFrom(htmlPage);
                if (firstPage == null) {
                    firstPage = htmlPage;
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
        }while(substances.addAll(temp));

        SearchResult results = new SearchResult(substances);
        if(results.numberOfResults() >0){
            parseFacets(results, firstPage);
        }
        return results;
    }
    
    public HtmlPage getPage(String rootUrl, int page) throws MalformedURLException, IOException{
    	return session.submit(session.newGetRequest(rootUrl + "&page=" + page));
    }
    
    public HtmlPage getSubstructurePage(String smiles, int rows, int page, boolean wait) throws MalformedURLException, IOException{
    	// Added "wait" so that it doesn't return before it's
    	// completely ready
    	// This may be a problem, as URLEncoder may over encode some smiles strings
    	String rootUrl = "ginas/app/substances?type=Substructure&q="+URLEncoder.encode(smiles, "UTF-8") + "&wait=" + wait + "&rows=" + rows;
    	return getPage(rootUrl, page);
    }
    
    
    public SearchResult getSubstructureSearch(String smiles, int rows, int page, boolean wait) throws MalformedURLException, IOException{
    	return new SearchResult(getSubstancesFrom(getSubstructurePage(smiles,rows,page, wait)));
    }
    
    
    
    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    public SearchResult all() throws IOException {

        String rootUrl = "ginas/app/substances?wait=true";
        int page=1;

        Set<String> substances = new LinkedHashSet<>();

        Set<String> temp;
        HtmlPage firstPage=null;
        do {
        	 HtmlPage htmlPage=null;
        	 try{
        		 htmlPage = session.submit(session.newGetRequest(rootUrl + "&page=" + page));
        	 }catch(Exception e){
             	break;
             }
            //stop if the paging throws an error
           
            	temp = getSubstancesFrom(htmlPage);
            
            if(firstPage ==null){
                firstPage = htmlPage;
            }
            page++;

            Matcher m=TOTAL_PATTERN.matcher(htmlPage.asXml());
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
        
        
        
        SearchResult results = new SearchResult(substances);
        if(results.numberOfResults() >0){
            parseFacets(results, firstPage);
        }
        return results;
    }

    private void parseFacets(SearchResult results, HtmlPage html) throws IOException{


        Map<String, Map<String,Integer>> map = new LinkedHashMap<>();

        Scanner scanner = new Scanner(html.asText());

        String line;
        do{
            line = scanner.nextLine();
        }while(line !=null && !line.contains("Record Status"));
        //can't get the Pattern matching to work as expected
        if(line ==null ){
            throw new IOException("no facets found");
        }
        String facetName = line.trim();

        map.put(facetName, new HashMap<String, Integer>());
        while(scanner.hasNextLine() && line !=null){

            line = scanner.nextLine();
            if(line ==null){
                continue;
            }
            String trimmed = line.trim();
            if(trimmed.isEmpty()){
                continue;
            }

            if(trimmed.contains("Substructure Query:")){
                break;
            }
            Matcher rowMatcher = ROW_PATTERN.matcher(trimmed);
            if(rowMatcher.find()){

                map.get(facetName).put(rowMatcher.group(2), Integer.parseInt(rowMatcher.group(4)));
            }else{

                facetName = trimmed;
                map.put(facetName, new HashMap<String, Integer>());
            }

        }

        for(Map.Entry<String, Map<String, Integer>> next : map.entrySet()){
            if(!next.getValue().isEmpty()){
                results.setFacet(next.getKey(), next.getValue());
            }
        }



    }


    private Set<String> getSubstancesFrom(HtmlPage page){
        Set<String> substances = new LinkedHashSet<>();

        Matcher matcher = SUBSTANCE_LINK_PATTERN.matcher(page.asXml());
        while(matcher.find()){
            substances.add(matcher.group(1));
        }

        return substances;
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


    public static class SearchResult{
        private final Set<String> uuids;

        private final Map<String, Map<String, Integer>> facetMap = new LinkedHashMap<>();

        public SearchResult(Set<String> uuids){
            Objects.requireNonNull(uuids);
            this.uuids = Collections.unmodifiableSet(new LinkedHashSet<>(uuids));

        }

        public Set<String> getUuids(){
            return uuids;
        }

        public int numberOfResults(){
            return uuids.size();
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


}
