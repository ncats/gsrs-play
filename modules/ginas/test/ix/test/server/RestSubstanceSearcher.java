package ix.test.server;



import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.util.CachedSupplier;
import ix.core.util.StreamUtil;
import ix.ginas.models.v1.Substance;
import ix.utils.Tuple;
import play.libs.ws.WSRequestHolder;

/**
 * REST-based form of a {@link SubstanceSearcher}.
 * 
 */
public class RestSubstanceSearcher implements SubstanceSearcher{
    private String defaultSearchOrder =null;
    private static final long REST_TIMEOUT=60_000; //1 min

    private RestSession session;

    private static final String BASE_URL = "ginas/app/";

    private static final String API_STRUCTURE_SEARCH = BASE_URL + "api/v1/substances/structureSearch";
    private static final String API_TEXT_SEARCH = BASE_URL + "api/v1/substances/search";
    private static final String API_SEQUENCE_SEARCH = BASE_URL + "api/v1/substances/sequenceSearch";
    private static final String API_ALL_BROWSE = BASE_URL + "api/v1/substances/sequenceSearch";

    public RestSubstanceSearcher(RestSession session){
        this.session=session;
    }


    @Override
    public void setSearchOrder(String term, SearchOrderDirection dir) {
        Objects.requireNonNull(term);
        defaultSearchOrder=dir.formatQuery(term);
    }

    @Override
    public RestSearchResult substructure(String smiles) throws IOException {
        JsonNode jsn= this.session.getRequest(API_STRUCTURE_SEARCH)
                .setQueryParameter("q", smiles)
                .setQueryParameter("type", "Substructure")
                .get()
                .get(REST_TIMEOUT)
                .asJson();
        return processAsyncRequest(jsn);
    }
    
    private RestSearchResult processAsyncRequest(JsonNode jsn) throws JsonParseException, JsonMappingException, IOException{
        //wait for async
        while(!jsn.get("finished").asBoolean()){
            jsn=this.session.getRequest(jsn.get("url").asText())
                    .get()
                    .get(REST_TIMEOUT)
                    .asJson();
        }
        String key = jsn.get("key").asText();
        
        JsonNode results =this.session.getRequest(jsn.get("results").asText())
                .get()
                .get(REST_TIMEOUT)
                .asJson();
        return resultsFromFirstResultNode(results,key);
    }
    
    @Override
    public RestSearchResult similarity(String smiles, double cutoff) throws IOException {
        JsonNode jsn= this.session.getRequest(API_STRUCTURE_SEARCH)
                .setQueryParameter("q", smiles)
                .setQueryParameter("type", "Similarity")
                .setQueryParameter("cutoff", cutoff+"")
                .get()
                .get(REST_TIMEOUT)
                .asJson();
        return processAsyncRequest(jsn);
    }


    @Override
    public SearchResult flex(String smiles) throws IOException {
        JsonNode jsn= this.session.getRequest(API_STRUCTURE_SEARCH)
                            .setQueryParameter("q", smiles)
                            .setQueryParameter("type", "Flex")
                            .get()
                            .get(REST_TIMEOUT)
                            .asJson();
        
        return processAsyncRequest(jsn);
    }


    @Override
    public RestSearchResult exact(String smiles) throws IOException {
        JsonNode jsn= this.session.getRequest(API_STRUCTURE_SEARCH)
                .setQueryParameter("q", smiles)
                .setQueryParameter("type", "Exact")
                .get()
                .get(REST_TIMEOUT)
                .asJson();
        return processAsyncRequest(jsn);
    }
    
    private RestSearchResult resultsFromFirstResultNode(JsonNode results, String key) throws JsonParseException, JsonMappingException, IOException{
        Set<String> subUUIDs = new LinkedHashSet<>();
        
        
        
        List<Map<String,Object>> facets = 
                (new ObjectMapper())
                .readValue(results.get("facets").toString(), 
                        new TypeReference<List<Map<String,Object>>>() { });
        
        Map<String,Map<String,Integer>> facetCounts= facets
                .stream()
                .map(m->Tuple.of((String)m.get("name"),(List<Map<String,Object>>)m.get("values")))
                .map(t->{
                   Map<String,Integer> resp=t.v().stream()
                        .map(m->Tuple.of((String)m.get("label"), (Integer)m.get("count")))
                        .collect(Tuple.toMap());
                   return Tuple.of(t.k(),resp);
                })
                .collect(Tuple.toMap());

        iterateThroughEachResult(results, s->{
            subUUIDs.add(s.get("uuid").asText());
        });


        SearchResult sr= new SearchResult.Builder()
                .searchKey(key)
                .uuids(subUUIDs)
                .facetMap(facetCounts)
                .build();
        return new RestSearchResult(sr, results,new SubstanceAPI(this.session));
    }
    
    //TODO: Refactor into interface pattern
    public class RestSearchResult extends SearchResult{
        
        private final JsonNode firstPage;
        private final SubstanceAPI api;
        private final SearchResult sr;
        
        public String getKey() {
            return sr.getKey();
        }

        public Set<String> getUuids() {
            return sr.getUuids();
        }

        public Set<String> getSpecialUuids() {
            return sr.getSpecialUuids();
        }

        public int numberOfResults() {
            return sr.numberOfResults();
        }

        public Stream<Substance> getSubstances() {
            return sr.getSubstances();
        }

        public int hashCode() {
            return sr.hashCode();
        }

        public InputStream export(String format) {
            return sr.export(format);
        }

        @Override
        public BrowserSubstanceSearcher.WebExportRequest newExportRequest(String format) {
            return sr.newExportRequest(format);
        }

        public Map<String, Integer> getFacet(String facetName) {
            return sr.getFacet(facetName);
        }

        public void setFacet(String facetName, Map<String, Integer> countMap) {
            sr.setFacet(facetName, countMap);
        }

        public Map<String, Map<String, Integer>> getAllFacets() {
            return sr.getAllFacets();
        }

        public boolean equals(Object obj) {
            return sr.equals(obj);
        }

        public String toString() {
            return sr.toString();
        }

        public RestSearchResult(SearchResult sr, JsonNode firstPage, SubstanceAPI api){
            this.sr=sr;
            this.api=api;
            this.firstPage=firstPage;
        }
        
        public APIFacetResult getFacetResult(String name){
            return APIFacetResult.fromSearchResult(api, this.firstPage, name);
        }

        //TODO: move to new interface as well
        public Integer getTotal() {
            return firstPage.at("/total").asInt();
        }
        
        //TODO: move to new interface also
        public SubstanceSearchRequest getRefiningSearcher(){
            String start = firstPage.at("/uri").asText();
            return new RestSubstanceSearchRequest(start,RestSubstanceSearcher.this);
        }
    }
    

    //TODO: Consolidate with other interfaces
    public static interface FacetResult{
        
        public String getFacetName();
        
        default Set<String> getFacetLabels(){
            return getFacetCounts().stream().map(t->t.k()).collect(Collectors.toSet());
        }
        default Map<String,Integer> getFacetMap(){
            return getFacetCounts().stream().collect(Tuple.toMap());
        }
        public Set<Tuple<String, Integer>> getFacetCounts();
        
        public FacetResult getFilteredFacet(String filter);
        
        default Integer getFacetCountForValue(String value){
            return this.getFacetMap().get(value);
        }
        
        
    }
    
    public static class APIFacetResult implements FacetResult{
        private JsonNode facetResponse;
        private final SubstanceAPI api;
        
        private static Set<Tuple<String,Integer>> extractValues(JsonNode values){
            return StreamUtil.forIterable(values)
                    .map(j->Tuple.of(j.at("/label").asText(), j.at("/count").asInt()))
                    .collect(Collectors.toSet());
        }
        
        private final CachedSupplier<Set<Tuple<String,Integer>>> allValues = CachedSupplier.of(()->{

            JsonNode facetWrap=facetResponse;
            Set<Tuple<String,Integer>> values = new HashSet<>();
            while(true){
                values.addAll(extractValues(facetWrap.at("/content")));
                JsonNode nextPage = facetWrap.at("/nextPageUri");
                if(nextPage==null || nextPage.isMissingNode()){
                    break;
                }
                facetWrap=APIFacetResult.this.api.getSession().getAsJson(nextPage.asText());
            }
            return values;
        });
        
        private APIFacetResult(SubstanceAPI api, JsonNode response){
            this.facetResponse=response;
            this.api=api;
        }

        @Override
        public String getFacetName() {
            return facetResponse.at("/facetName").asText();
        }

        @Override
        public Set<Tuple<String, Integer>> getFacetCounts() {
            return allValues.get();
        }
        
        public static APIFacetResult fromSearchResult(SubstanceAPI api, JsonNode result, String facet){
            JsonNode jsn = StreamUtil.forIterable(result.at("/facets"))
                                .filter(f->f.at("/name").asText().equals(facet))
                                .findFirst()
                                .map(f->f.at("/_self").asText())
                                .map(u->api.getSession().getAsJson(u))
                                .get();
            return new APIFacetResult(api, jsn);      
        }

        @Override
        public APIFacetResult getFilteredFacet(String filter) {
           WSRequestHolder wsr = api.getSession().getRequest(facetResponse.at("/uri").asText())
                           .setQueryParameter("ffilter", filter);
           JsonNode jsn = api.getSession().getAsJson(wsr);
           return new APIFacetResult(api,jsn);
        }
    }
    
    
    private void iterateThroughEachResult(JsonNode jsn , Consumer<JsonNode> subjson){
        while(true){
            JsonNode content = jsn.get("content");

            for(JsonNode sub: content){
                subjson.accept(sub);
            }

            JsonNode nextPage=jsn.get("nextPageUri");
            if(nextPage!=null && !nextPage.isNull() && !nextPage.isMissingNode()){
                jsn=this.session.getRequest(nextPage.asText())
                        .get()
                        .get(REST_TIMEOUT)
                        .asJson();
            }else{
                break;
            }
        }
    }


    @Override
    public SearchResult query(String queryString) throws IOException {
        return this.request().setQuery(queryString).submit();
    }


    
    @Override
    public SearchResult facet(String name, String value) throws IOException {
        return this.request().addFacet(name, value).submit();
    }


    @Override
    public RestSubstanceSearchRequest request() {
        return new RestSubstanceSearchRequest(API_TEXT_SEARCH,this);
    }
       
    
    public static class RestSubstanceSearchRequest implements SubstanceSearchRequest{
        private String baseURI;
        private final RestSubstanceSearcher searcher;
        
        private Set<Tuple<String,String>> facets = new HashSet<>();
        private String q=null;
        private FacetType ftype = FacetType.SIDEWAYS;
        
        public RestSubstanceSearchRequest(String baseURI,RestSubstanceSearcher searcher){
            this.baseURI=baseURI;
            this.searcher=searcher;
        }
        
        public RestSubstanceSearchRequest setBaseUri(String uri){
            this.baseURI=uri;
            return this;
        }
        
        
        @Override
        public SearchResult submit() throws IOException {
            WSRequestHolder req= searcher.session.getRequest(baseURI);
            
            req=facets.stream().reduce(req, (r,t)->{
                return r.setQueryParameter("facet", t.k() + "/" +  t.v());
            },(r1,r2)->r1);
            
            req=req.setQueryParameter(ftype.key(), ftype.value()+"");
            
            
            if(q!=null){
                req=req.setQueryParameter("q", q);
            }
            
            JsonNode jsn = req.get()
                    .get(REST_TIMEOUT)
                    .asJson();
            return searcher.resultsFromFirstResultNode(jsn, ""); //No search result key for text from rest
        }

        @Override
        public SubstanceSearchRequest addFacet(String name, String value) {
            facets.add(Tuple.of(name, value));
            return this;
        }

        @Override
        public SubstanceSearchRequest setQuery(String q) {
            this.q=q;
            return this;
        }

        @Override
        public SubstanceSearchRequest setFacetType(FacetType ft) {
            ftype=ft;
            return this;
        }
    }

}
