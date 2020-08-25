package ix.test.server;



import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import ix.test.server.BrowserSession.WrappedWebRequest;
import ix.utils.Tuple;
import ix.utils.Util;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

/**
 * Abstracts all the html parsing and ginas specific url knowledge
 * to make a programmatic way to query a running ginas instance to search
 * for substances. This SubstanceSearcher is specifically for browser-piloting.
 *
 * Created by katzelda on 4/5/16.
 */
public class BrowserSubstanceSearcher implements SubstanceSearcher {

    
    private final BrowserSession session;
    private  final Pattern SUBSTANCE_LINK_HREF_PATTERN;
    private static final Pattern TOTAL_PATTERN = Pattern.compile("[^0-9]([0-9][0-9]*)[^0-9]*h3[^0-9]*pagination");
   
    private final Pattern  SEARCH_KEY_PATTERN;

    private String defaultSearchOrder =null;
    private final String applicationContext;

    public BrowserSubstanceSearcher(BrowserSession session) {
        Objects.requireNonNull(session);

        this.session = session;

        applicationContext = session.getTestSever().getApplicationContext();
        SUBSTANCE_LINK_HREF_PATTERN = Pattern.compile("/"+applicationContext + "/substance/([a-z0-9\\-]+)");
        SEARCH_KEY_PATTERN = Pattern.compile(applicationContext + "/api/v1/status\\(([0-9a-f]+)\\)");
    }

    @Override
    public AbstractSession getSession() {
        return session;
    }
    
    /* (non-Javadoc)
     * @see ix.test.server.SubstanceSearcherIFace#setSearchOrder(java.lang.String)
     */
    @Override
    public void setSearchOrder(String term, SearchOrderDirection dir){
        Objects.requireNonNull(term);
    	this.defaultSearchOrder=dir.formatQuery(term);
    }
    
    /* (non-Javadoc)
     * @see ix.test.server.SubstanceSearcherIFace#substructure(java.lang.String)
     */
    @Override
    public SearchResult substructure(String smiles) throws IOException {
    	return substructure(smiles, 16, true);
    }
    
    @Override
    public SearchResult similarity(String smiles, double cutoff) throws IOException {
        return similarity(smiles, cutoff, 16, true);
    }
    
    @Override
    public SearchResult flex(String smiles) throws IOException {
        return structureSearch(smiles,0.8, "Flex", 16, true);
    }
    
    @Override
    public SearchResult exact(String smiles) throws IOException {
        return structureSearch(smiles,0.8, "Exact", 16, true);
    }
    
    public SearchResult similarity(String smiles, double cutoff, int rows, boolean wait) throws IOException {
        return structureSearch(smiles, 0.8, "Similarity", rows, wait);
    }
    
    public SearchResult substructure(String smiles, int rows, boolean wait) throws IOException {
        return structureSearch(smiles, 0.8, "Substructure", rows, wait);
    }
    
    public SearchResult structureSearch(String smiles, double cutoff, String type, int rows, boolean wait) throws IOException{

        //TODO have to kekulize

        int page=1;

        Set<String> substances = new LinkedHashSet<>();

        Tuple<String,Set<String>> tmp=null;
        HtmlPage firstPage=null;
        String keyString="";
        do {
            try {
                HtmlPage htmlPage = getChemicalSearchPage(smiles,cutoff,type ,rows,page, wait);
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
        }while(tmp !=null && substances.addAll(tmp.v()));

        SearchResult results = new SearchResult(keyString, substances,this, session.getUser());
        if(results.numberOfResults() >0){
            parseFacets(results, firstPage);
        }
        return results;
    }
    
    public HtmlPage getPage(WrappedWebRequest wwr, int page) throws IOException{
    	return session.submit(wwr.setQueryParameter("page", page+"").get());
    }
    
    public HtmlPage getExactPage(String smiles, int rows, int page, boolean wait) throws IOException{
        return getChemicalSearchPage(smiles,0.8,"Exact",rows,page,wait);
    }
    public HtmlPage getFlexPage(String smiles, int rows, int page, boolean wait) throws IOException{
        return getChemicalSearchPage(smiles,0.8,"Flex",rows,page,wait);
    }
    public HtmlPage getSubstructurePage(String smiles, int rows, int page, boolean wait) throws IOException{
        return getChemicalSearchPage(smiles,0.8,"Substructure",rows,page,wait);
    }
    public HtmlPage getSimilarityPage(String smiles, double cutoff, int rows, int page, boolean wait) throws IOException{
        return getChemicalSearchPage(smiles,cutoff,"Similarity",rows,page,wait);
    }
    
    public HtmlPage getChemicalSearchPage(String smiles, double cutoff, String type, int rows, int page, boolean wait) throws IOException{
        // Added "wait" so that it doesn't return before it's
        // completely ready
        
        
        // This may be a problem, as URLEncoder may over encode some smiles strings
        WrappedWebRequest root=session.newGetRequest(session.getHttpResolver().get("substances"))
            .addQueryParameter("type", type)
            .addQueryParameter("q", smiles)
            .addQueryParameter("cutoff", cutoff +"")
            .addQueryParameter("wait", wait+"")
            .addQueryParameter("rows", rows+"");
        
        if(defaultSearchOrder!=null){
            root=root.addQueryParameter("order",defaultSearchOrder);
        }
        return getPage(root, page);
    }
    
    
    public SearchResult getSubstructureSearch(String smiles, int rows, int page, boolean wait) throws IOException{
        HtmlPage htmlpage=getSubstructurePage(smiles,rows,page, wait);
        Tuple<String,Set<String>> set = getSubstancesFrom(htmlpage);
        //String html=htmlpage.asXml();
        //System.out.println(html);
    	return new SearchResult(set.k(),set.v(),this, session.getUser());
    }
    
    
    /* (non-Javadoc)
     * @see ix.test.server.SubstanceSearcherIFace#query(java.util.UUID)
     */
    @Override
    public SearchResult query(UUID uuid) throws IOException {
        return query(uuid.toString());
    }
    
    /* (non-Javadoc)
     * @see ix.test.server.SubstanceSearcherIFace#query(java.lang.String)
     */
    @Override
    public SearchResult query(String queryString) throws IOException {
        return performSearch(queryString);
    }
    
    /* (non-Javadoc)
     * @see ix.test.server.SubstanceSearcherIFace#all()
     */
    @Override
    public SearchResult all() throws IOException {
        return performSearch(null);
    }
    
    private SearchResult performSearchRequest(WrappedWebRequest req) throws IOException {

        int page=1;

     //   System.out.println("query url is " + rootUrl);
        Set<String> substances = new LinkedHashSet<>();
        Set<String> specialMatches = new LinkedHashSet<>();
        Set<String> temp;
        
        HtmlPage firstPage=null;
        String keyString = null;
        do {
            HtmlPage htmlPage = null;
            try {
                htmlPage = session.submit(req.setQueryParameter("page", page + "").get());
            } catch (Exception e) {
                if(keyString ==null){
                    throw e;
                }
               break;
            }
            temp = getSubstancesFrom(htmlPage).v();
            specialMatches.addAll(getSpecialMatchesFrom(htmlPage));
            // stop if the paging throws an error
            String htmlText = htmlPage.asXml();

            if (firstPage == null) {
                firstPage = htmlPage;
                keyString = getKeyFrom(htmlText);
            }
            page++;

            Matcher m = TOTAL_PATTERN.matcher(htmlText);
            String total = null;
            if (m.find()) {
                total = m.group(1);
            }
            // we check the return value of the add() call
            // because when we get to the page beyond the end of the results
            // it returns the first page again
            // so we can check to see if we've already added these
            // records (so add() will return false)
            // which will break us out of the loop.
        } while (substances.addAll(temp));
        
        
        SearchResult results = new SearchResult.Builder()
                                        .searcher(this)
                                        .username(this.session.getUser())
                                        .searchKey(keyString)
                                        .uuids(substances)
                                        .specialUuids(specialMatches)
                                        .build();
        if(results.numberOfResults() >0){
            parseFacets(results, firstPage);
        }
        return results;
    }
    /**
     * Get the UUIDs of all the loaded substances
     * @return a Set of UUIDs that match. will never be null but may be empty.
     * @throws IOException if there is a problem parsing the results.
     */
    private SearchResult performSearch(String queryOrNull) throws IOException {

        WrappedWebRequest req=session.newGetRequest(session.getHttpResolver().get("/substances"))
        	.addQueryParameter("wait", "true");
        	
        if(queryOrNull !=null){
        	req=req.addQueryParameter("q", queryOrNull);
        }
        
        if(defaultSearchOrder!=null){
        	req=req.addQueryParameter("order", defaultSearchOrder);
    	}
        return performSearchRequest(req);
    }

    private String getKeyFrom(String htmlText) {
        Matcher m = SEARCH_KEY_PATTERN.matcher(htmlText);
        if(!m.find()){
            return "";
        }
        return m.group(1);
    }


    private void parseFacets(SearchResult results, HtmlPage html) throws IOException{
    	Map<String, Map<String,Integer>> map = new LinkedHashMap<>();

//    	System.out.println("==================================");
//
//    	System.out.println("html =\n" + html.asXml());
//        System.out.println("==================================");

        /*html.querySelectorAll("div.panel-default")
    		.stream()
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
    		});     */

        html.querySelectorAll("div[ng-controller=FilterController]")
                .stream()
                .filter(n->n.asXml().contains("facetDisplay"))
                .forEach(c->{
                    String facetName = c.querySelector("div>h3>span[ng-bind=facetDisplay]").asText().trim();
//                    System.out.println("FOUND FACET NAME " + facetName);
                    Map<String,Integer> counts= new HashMap<>();
                            c.querySelectorAll("ul.list-group div[ng-hide]") //Each facet value
                                    .stream()
                                    .map(d->new List[]{
                                            //<span class ="badge pull-right ng-binding">
                                            d.querySelectorAll("div label.ng-binding").stream().map(n-> n.asText().trim()).collect(Collectors.toList()),
                                            d.querySelectorAll("div span.pull-right.ng-binding").stream().map(n-> n.asText().trim()).collect(Collectors.toList())
                                    })
                                    .filter(s-> s[0] !=null && !s[0].isEmpty())
//                                    .peek(a-> System.out.println(Arrays.toString(a)))
                                    .forEach( s->{
                                        Iterator<String> keyIter = (Iterator<String>) s[0].iterator();
                                        Iterator<String> valueIter = (Iterator<String>) s[1].iterator();
                                        while(keyIter.hasNext() && valueIter.hasNext()){
                                            String k = keyIter.next();
                                            String v = valueIter.next();
                                            if(k !=null && !k.isEmpty() && v !=null && !v.isEmpty()){
                                                counts.put(k, Integer.parseInt(v));
                                            }

                                        }
                                    });

//                    System.out.println("counts = " + counts);
                    map.put(facetName, counts);
    		});

        for(Map.Entry<String, Map<String, Integer>> next : map.entrySet()){
            if(!next.getValue().isEmpty()){
                results.setFacet(next.getKey(), next.getValue());
            }
        }
    }
    
    /**
     * Finds all the matching substance truncated uuids
     * in the specified {@link DomNode}, returning
     * a set of those identifiers.
     * @param dn
     * @return
     */
    private Set<String> getSubstanceMatchesIn(DomNode dn){
        return dn.querySelectorAll("a[href*=\""+session.getTestSever().getApplicationContext() + "/substance/\"]")
                .stream()
                .map(a->a.getAttributes().getNamedItem("href").getNodeValue())
                .map(Util.getMatchingGroup(SUBSTANCE_LINK_HREF_PATTERN, 1))
                .filter(o->o.isPresent())
                .map(o->o.get())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private Tuple<String,Set<String>> getSubstancesFrom(HtmlPage page){
//        System.out.println(page.asXml());
        Set<String> substances =  getSubstanceMatchesIn(page);
        String htmlText = page.asXml();
        return Tuple.of(getKeyFrom(htmlText), substances);

    }
    
    /**
     * Returns all matching substance truncated uuids
     * in the dom element associated with the special
     * matches section (somtimes called "sponsored").
     * @param page
     * @return
     */
    private Set<String> getSpecialMatchesFrom(HtmlPage page){
        Set<String> set = new LinkedHashSet<>();
        DomNode dn = page.querySelector(".specialmatches");
        if(dn!=null){
            set.addAll(getSubstanceMatchesIn(dn));
        }
        return set;
    }

    public Set<String> getStructureImagesFrom(HtmlPage page){
        Set<String> substances = page.querySelectorAll("img[src*=\"" + applicationContext + "/img\"]")
        .stream()
        .map(m->m.getAttributes().getNamedItem("src").getNodeValue())
        .collect(Collectors.toCollection(LinkedHashSet::new));
        
        if(substances.isEmpty()){
            page.querySelectorAll("rendered")
                .stream()
                .map(m->{
                    String id=m.getAttributes().getNamedItem("id").getNodeValue();
                    String ctx= Optional.ofNullable(m.getAttributes().getNamedItem("ctx"))
                            .map(n->n.getNodeValue())
                            .orElse(null);
                    return new String[]{id,ctx};
                    
                })
                .forEach(s->{
                    substances.add(applicationContext + "/img/" + s[0] + ".svg?context=" + s[1]);
                });     
        }
        
        
        return substances;
    }


    public WebExportRequest getExport(String format, String key){
    	return new WebExportRequest(key,format,BrowserSubstanceSearcher.this.session);
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
    static class SortByValueComparator<T extends Comparable<? super T>, V> implements Comparator<T>{
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


    @Override
    public SearchResult facet(String name, String value) throws IOException {
        WrappedWebRequest req=session.newGetRequest(session.getHttpResolver().get("substances"))
                .addQueryParameter("wait", "true");
               
        req=req.addQueryParameter("facet", name  + "/" + value);
        if(defaultSearchOrder!=null){
                req=req.addQueryParameter("order", defaultSearchOrder);
        }
        return this.performSearchRequest(req);
    }

    @Override
    public SubstanceSearchRequest request() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
