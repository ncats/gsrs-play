package ix.test.server;



import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.utils.Tuple;

/**
 * REST-based form of a {@link SubstanceSearcherIFace}.
 * 
 */
public class RestSubstanceSearcher implements SubstanceSearcherIFace{
    private String defaultSearchOrder =null;
    private static final long REST_TIMEOUT=500; //ms

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
    public void setSearchOrder(String order) {
        defaultSearchOrder=order;
    }

    @Override
    public SearchResult substructure(String smiles) throws IOException {
        JsonNode jsn= this.session.getRequest(API_STRUCTURE_SEARCH)
                .setQueryParameter("q", smiles)
                .setQueryParameter("type", "Substructure")
                .get()
                .get(REST_TIMEOUT)
                .asJson();

        //wait for async
        while(!jsn.get("finished").asBoolean()){
            jsn=this.session.getRequest(jsn.get("url").asText())
                    .get()
                    .get(REST_TIMEOUT)
                    .asJson();
        }

        String key = jsn.get("key").asText();

        return resultsFromFirstResultNode(jsn,key);
    }
    
    private SearchResult resultsFromFirstResultNode(JsonNode jsn, String key) throws JsonParseException, JsonMappingException, IOException{
        Set<String> subUUIDs = new LinkedHashSet<>();
        JsonNode results =this.session.getRequest(jsn.get("results").asText())
                .get()
                .get(REST_TIMEOUT)
                .asJson();
        
        
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


        return new SearchResult.Builder()
                .searchKey(key)
                .uuids(subUUIDs)
                .facetMap(facetCounts)
                .build();
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

    
    //TODO: Should implement?
    @Override
    public SearchResult substructure(String smiles, int rows, boolean wait) throws IOException {
        throw new UnsupportedOperationException(this.getClass() + " doesn't support sync calls to substructure search yet");
    }

    @Override
    public SearchResult query(String queryString) throws IOException {
        JsonNode jsn= this.session.getRequest(API_TEXT_SEARCH)
                .setQueryParameter("q", queryString)
                .get()
                .get(REST_TIMEOUT)
                .asJson();
        
        return resultsFromFirstResultNode(jsn, null); //No search result key for text from rest
                                                      //api (yet)
    }

}
