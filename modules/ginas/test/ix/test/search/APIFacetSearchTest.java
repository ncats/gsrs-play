package ix.test.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasClassServerTest;
import ix.core.models.Role;
import ix.core.util.StopWatch;
import ix.core.util.StreamUtil;
import ix.ginas.modelBuilders.AbstractSubstanceBuilder;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.test.performance.LoadRecordPerformanceTest;
import ix.test.query.builder.SimpleQueryBuilder;
import ix.test.server.GinasTestServer.User;
import ix.test.server.RestSession;
import ix.test.server.RestSubstanceSearcher;
import ix.test.server.RestSubstanceSearcher.APIFacetResult;
import ix.test.server.RestSubstanceSearcher.RestSearchResult;
import ix.test.server.SubstanceAPI;
import ix.test.server.SubstanceSearcher.FacetType;
import ix.utils.Tuple;
import ix.utils.Util;

public class APIFacetSearchTest extends AbstractGinasClassServerTest{

    public static RestSession session;
    public static SubstanceAPI api;
    
    private final static String bucketCodeSystem = "BUCKET";
    private final static String[] bucketCodeValues = new String[]{
            "BUCKET1", 
            "BUCKET2"
    };
    
    private final static String codeSystem = "ACODE";
    private final static String[] codeValues = new String[]{
            "VALUE1", 
            "VALUE2", 
            "VALUE3",
            "VALUE4",
            "VALUE5",
            "VALUE6",
            "VALUE7",
            "VALUE8",
            "VALUE9",
            "VALUE10",
            "VALUE11"
            
    };
    
    
    private static Map<String,Integer> facetValueCounter;
    
    
    private final static String smallerCodeSystem = "SMALLERCODE";
    private final static String[] smallerCodeValues = new String[]{
            "TEST1", 
            "TEST2", 
            "TEST3",
            "TEST4",
            "TEST5",
            "TEST6",
            "TEST7",
            "TEST8",
            "TEST9"
    };
    
    
    private static Map<String,Integer> smallerFacetValueCounter;
    
    private final static int size=1000;
    
    @BeforeClass
    public static void LoadData(){
        User u = ts.createUser(Role.Admin);
        session = ts.newRestSession(u);
        api = new SubstanceAPI(session);
        
        Stream<String> strings= Stream.iterate(0, i->i+1)
              .map(i->LoadRecordPerformanceTest.toAlphabet(i, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        
        Stream<String> smilesStructures= LoadRecordPerformanceTest.uniqueReversable("SPONC")
                                            .map(s->"C" + s);
        
        Supplier<String> smilesPuller=StreamUtil.supplierFor(smilesStructures);
        Supplier<String> stringPuller=StreamUtil.supplierFor(strings);
        Supplier<String> bucketPuller=StreamUtil.supplierFor(StreamUtil.cycle(bucketCodeValues));
        Supplier<String> smallerPuller=StreamUtil.supplierFor(StreamUtil.cycle(smallerCodeValues));
        
        Map<String,AtomicInteger> tmpvalueCounter=new ConcurrentHashMap<>();
        Map<String,AtomicInteger> tmpSmallerValueCounter=new ConcurrentHashMap<>();
        
        System.out.println("loading");
        
        long timeElapsed = StopWatch.timeElapsed(()->{
            
            
            StreamUtil
                .cycle(codeValues)
                .limit(size)
                .parallel()
                .forEach(v->{
                    String bucket=bucketPuller.get();
                    String smaller=smallerPuller.get();
                    
                    AbstractSubstanceBuilder<?,?> sb = new SubstanceBuilder();
                    if(Math.random()>.5){
                        sb=((SubstanceBuilder)sb).asChemical()
                             .setStructure(smilesPuller.get());
                    }
                            
                    sb.addCode(codeSystem, v)
                    .addCode(bucketCodeSystem, bucket)
                    .addCode(smallerCodeSystem, smaller)
                    .addName("TESTSUBSTANCE " + stringPuller.get())
                    .andThen(s->{
                        tmpvalueCounter.computeIfAbsent(v,f-> new AtomicInteger(0))
                                    .incrementAndGet();
                        tmpSmallerValueCounter.computeIfAbsent(smaller, f->new AtomicInteger(0))
                                    .incrementAndGet();
                    })
                    .buildJsonAnd(js->{
                        api.submitSubstanceJson(js);
                    });
                });    
            
            
        });
        
        facetValueCounter=tmpvalueCounter.entrySet().stream().map(Tuple::of).map(Tuple.vmap(i->i.get()))
                    .collect(Tuple.toMap());
        smallerFacetValueCounter=tmpSmallerValueCounter.entrySet().stream().map(Tuple::of).map(Tuple.vmap(i->i.get()))
                .collect(Tuple.toMap());
        
        System.out.println("Load time:" + timeElapsed);
        
        //Just trigger a basic lucene caching
        //otherwise timing is off for other tests
        api.fetchSubstancesSearch();
        
    }
    
    
    @Test
    public void topFacetsShowOnSearchAll(){
        JsonNode jsn = api.fetchSubstancesSearchJSON();
        
        JsonNode facets= jsn.at("/facets");
        
        
        //This gobbledygook is just finding the facet which matches
        //the codesystem provided, and then converting the values
        //to a map of labels to counts
        Map<String, Integer> facetValueCounts=StreamUtil.forIterable(facets)
                                    .filter(t->t.at("/name").asText().equals(smallerCodeSystem))
                                    .limit(1)
                                    .map(t->t.at("/values"))
                                    .flatMap(t->StreamUtil.forIterable(t))
                                    .map(j->Tuple.of(j.at("/label").asText(), j.at("/count").asInt()))
                                    .collect(Tuple.toMap());
        
        
        assertEquals(size,jsn.at("/total").asInt());
        
        
        Map<String,Integer> limitMap = smallerFacetValueCounter.entrySet()
                .stream()
                .map(Tuple::of)
                .sorted((a,b)->{
                    return -a.v().compareTo(b.v());
                })
                .limit(10)
                .collect(Tuple.toMap());
        
        assertEquals(limitMap.keySet(),
                     facetValueCounts.keySet());
        
        assertEquals(limitMap,facetValueCounts);
    }
    
    public static Set<Tuple<String,Integer>> extractValues(JsonNode values){
        return StreamUtil.forIterable(values)
                .map(j->Tuple.of(j.at("/label").asText(), j.at("/count").asInt()))
                .collect(Collectors.toSet());
    }
    
    @Test
    public void topFacetsArePagable(){
        JsonNode jsn = api.fetchSubstancesSearchJSON();
        APIFacetResult facetResult = APIFacetResult.fromSearchResult(api, jsn, codeSystem);
        
        Map<String,Integer> mapi=facetResult.getFacetMap();
        assertEquals(facetValueCounter.keySet(),mapi.keySet());
        assertEquals(facetValueCounter,mapi);
    }
    
    @Test
    public void topFacetsAreFilterable(){
        JsonNode jsn = api.fetchSubstancesSearchJSON();
        APIFacetResult facetResult = APIFacetResult.fromSearchResult(api, jsn, codeSystem);
        
        String contains = "VALUE1";
        
        APIFacetResult filteredFacetResult=facetResult.getFilteredFacet(contains);
        
        Map<String,Integer> filtered=facetValueCounter.entrySet()
                .stream()
                .filter(e->e.getKey().contains(contains))
                .collect(Util.toMap());
        
        
        assertEquals(filtered.keySet(),filteredFacetResult.getFacetLabels());
        assertEquals(filtered,filteredFacetResult.getFacetMap());
    }
    
    @Test
    public void facetCountsUpdatedOnSearch() throws IOException{
        RestSubstanceSearcher rsearch= new RestSubstanceSearcher(session);
        
        RestSearchResult sr = (RestSearchResult) rsearch.exactSearch(bucketCodeValues[0]);
        
        
        APIFacetResult fr= sr.getFacetResult(bucketCodeSystem);
        String labels= fr.getFacetLabels().stream().collect(Collectors.joining());
        assertEquals(bucketCodeValues[0], labels); //the only label is just the one
                                                   //we searched for
        
        APIFacetResult fr2= sr.getFacetResult(codeSystem);
        int tsum = fr2.getFacetCounts().stream()
            .mapToInt(t->t.v())
            .sum();
        assertEquals(sr.getTotal().intValue(), tsum);
        assertNotEquals(size, tsum);
    }
    
    @Test
    public void facetCountsUpdatedOnSidewaysFacetSearch() throws IOException{
        RestSubstanceSearcher rsearch= new RestSubstanceSearcher(session);
        
        RestSearchResult sr = (RestSearchResult) rsearch.request()
                                .addFacet(bucketCodeSystem, bucketCodeValues[0])
                                .setFacetType(FacetType.SIDEWAYS)
                                .submit();
        
        APIFacetResult fr= sr.getFacetResult(bucketCodeSystem);
        Set<String> labels= fr.getFacetLabels();
        Set<String> expected=Util.toSet(bucketCodeValues);
        
        //Unlike the search case, this should include the counts of the facets
        //that it would have if there were no other selected
        //That is, it is a "sideways" selection, instead of a "drilldown"
        //by default
        assertEquals(expected, labels); 
        
        
        APIFacetResult fr2= sr.getFacetResult(codeSystem);
        int tsum = fr2.getFacetCounts().stream()
            .mapToInt(t->t.v())
            .sum();
        assertEquals(sr.getTotal().intValue(), tsum);
        assertNotEquals(size, tsum);
    }
    
    @Test
    public void facetCountsUpdatedOnDrillDownFacetSearch() throws IOException{
        RestSubstanceSearcher rsearch= new RestSubstanceSearcher(session);
        
        RestSearchResult sr = (RestSearchResult) rsearch.request()
                             .addFacet(bucketCodeSystem, bucketCodeValues[0])
                             .setFacetType(FacetType.DRILLDOWN)
                             .submit();
        
        
        APIFacetResult fr= sr.getFacetResult(bucketCodeSystem);
        Set<String> labels= fr.getFacetLabels();
        Set<String> expected=Util.toSet(bucketCodeValues[0]);
        
        assertEquals(expected, labels); 
        
        
        APIFacetResult fr2= sr.getFacetResult(codeSystem);
        int tsum = fr2.getFacetCounts().stream()
            .mapToInt(t->t.v())
            .sum();
        assertEquals(sr.getTotal().intValue(), tsum);
        assertNotEquals(size, tsum);
    }
    
    
    @Test
    public void facetCountsUpdatedOnSidewaysStructureAndFacetSearch() throws IOException{
        RestSubstanceSearcher rsearch= new RestSubstanceSearcher(session);
        
        RestSearchResult baseResult = (RestSearchResult) rsearch.substructure("C");
        
        Integer expectedAmount= baseResult.getFacetResult(bucketCodeSystem)
                                          .getFacetCountForValue(bucketCodeValues[0]);
        
        
        RestSearchResult facetedResult = (RestSearchResult) baseResult.getRefiningSearcher()
                        .addFacet(bucketCodeSystem, bucketCodeValues[0])
                        .setFacetType(FacetType.SIDEWAYS)
                        .submit();
        
        
        assertEquals(expectedAmount, facetedResult.getTotal());
        
        assertEquals(
                     baseResult.getFacetResult(bucketCodeSystem).getFacetMap(),
                     facetedResult.getFacetResult(bucketCodeSystem).getFacetMap()
                    );
        
    }
    
    @Test
    public void facetCountsUpdatedOnDrillDownStructureAndFacetSearch() throws IOException{
        RestSubstanceSearcher rsearch= new RestSubstanceSearcher(session);
        
        RestSearchResult baseResult = (RestSearchResult) rsearch.substructure("C");
        
        Integer expectedAmount= baseResult.getFacetResult(bucketCodeSystem)
                                          .getFacetCountForValue(bucketCodeValues[0]);
        
        
        RestSearchResult facetedResult = (RestSearchResult) baseResult.getRefiningSearcher()
                        .addFacet(bucketCodeSystem, bucketCodeValues[0])
                        .setFacetType(FacetType.DRILLDOWN)
                        .submit();
        
        
        assertEquals(expectedAmount, facetedResult.getTotal());
        
        
        
        assertEquals(
                    baseResult.getFacetResult(bucketCodeSystem)
                                .getFilteredFacet(bucketCodeValues[0])
                                .getFacetMap(),
                     facetedResult.getFacetResult(bucketCodeSystem).getFacetMap()
                    );
        
    }
    
    @Test
    public void facetCountsUpdatedOnSidewaysStructureAndTextSearch() throws IOException{
        RestSubstanceSearcher rsearch= new RestSubstanceSearcher(session);
        
        RestSearchResult baseResult = (RestSearchResult) rsearch.substructure("C");
        
        Integer expectedAmount= baseResult.getFacetResult(bucketCodeSystem)
                                          .getFacetCountForValue(bucketCodeValues[0]);
        
        //TestUtil.waitForParam("ok");
        
        
        RestSearchResult subQueryResult = (RestSearchResult) baseResult.getRefiningSearcher()
                        .setQuery(new SimpleQueryBuilder().where()
                                .globalMatchesExact(bucketCodeValues[0])
                                .build())
                        .submit();
        
        assertEquals(expectedAmount, subQueryResult.getTotal());
        
        assertEquals(
                    baseResult.getFacetResult(bucketCodeSystem)
                                .getFilteredFacet(bucketCodeValues[0])
                                .getFacetMap(),
                     subQueryResult.getFacetResult(bucketCodeSystem).getFacetMap()
                    );
        
    }
    
    
}
