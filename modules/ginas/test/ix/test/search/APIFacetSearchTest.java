package ix.test.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ix.core.util.RunOnly;
import ix.test.query.builder.SuppliedQueryBuilder;
import ix.test.server.*;
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
            "VALUE1_A",
            "VALUE2_A",
            "VALUE3_A",
            "VALUE4_A",
            "VALUE5_A",
            "VALUE6_A",
            "VALUE7_A",
            "VALUE8_A",
            "VALUE9_A",
            "VALUE10_A",
            "VALUE11_A"
            
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
        TreeSet<String> seen = new TreeSet<>();
        long timeElapsed = StopWatch.timeElapsed(()->{
            
            
            StreamUtil
                .cycle(codeValues)
                .limit(size)
//                .parallel()
                .forEach(v->{
                    String bucket=bucketPuller.get();
                    String smaller=smallerPuller.get();
                    seen.add(v);
                    AbstractSubstanceBuilder<?,?> sb = new SubstanceBuilder();
                    if(Math.random()>.5){
                        sb=((SubstanceBuilder)sb).asChemical()
                             .setStructure(smilesPuller.get());
                    }
                    sb.generateNewUUID();
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
        
        facetValueCounter=new TreeMap<>(tmpvalueCounter.entrySet().stream().map(Tuple::of).map(Tuple.vmap(i->i.get()))
                    .collect(Tuple.toMap()));
        smallerFacetValueCounter=new TreeMap<>(tmpSmallerValueCounter.entrySet().stream().map(Tuple::of).map(Tuple.vmap(i->i.get()))
                .collect(Tuple.toMap()));
        
        System.out.println("Load time:" + timeElapsed);
        System.out.println("seen = " + seen);
        //Just trigger a basic lucene caching
        //otherwise timing is off for other tests
        api.fetchSubstancesSearch();
        
    }
    
    
    @Test
    public void topFacetsShowOnSearchAll() throws IOException{
        
        RestSubstanceSubstanceSearcher searcher = new RestSubstanceSubstanceSearcher(session);
        
        
        SearchResult result = searcher.all();
        Map<String, Integer> facetValueCounts= result.getFacet(smallerCodeSystem);
        
        
        assertEquals(size,result.numberOfResults());
        
        
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
    public void topFacetsArePagable() throws IOException{
        RestSubstanceSubstanceSearcher searcher = new RestSubstanceSubstanceSearcher(session);
        


        Map<String, Map<String, Integer>> allFacets = searcher.query(
                SimpleQueryBuilder.searchAll(),  h -> h.setQueryParameter("fdim", "20")).getAllFacets();

        Map<String,Integer> mapi= allFacets.get(codeSystem);

        assertEquals(facetValueCounter.keySet(),mapi.keySet());
        assertEquals(facetValueCounter,mapi);
    }
    
    @Test
    public void topFacetsAreFilterable() throws IOException{
        RestSubstanceSubstanceSearcher searcher = new RestSubstanceSubstanceSearcher(session);
        String contains = "VALUE1";
         Map<String, Integer> filteredFacetResult = searcher.queryFacets(SuppliedQueryBuilder.searchAll(), codeSystem, r -> r.setQueryParameter("fdim", "20")).getFilteredFacet(contains);

//        assertEquals(1, filteredFacetResult.size());





        
        Map<String,Integer> filtered=facetValueCounter.entrySet()
                .stream()
                .filter(e->e.getKey().contains(contains))
                .collect(Util.toMap());
        
        assertEquals(filtered.keySet(), filteredFacetResult.keySet());
        assertEquals(filtered, filteredFacetResult);
    }
    
    @Test
    public void facetCountsUpdatedOnSearch() throws IOException{
        RestSubstanceSubstanceSearcher rsearch= new RestSubstanceSubstanceSearcher(session);
        
        rsearch.request();
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult searchResult = rsearch
                        .request(SimpleQueryBuilder.exactSearch(bucketCodeValues[0]))
                            .setQueryParameter("fdim", "20")
                            .submit();
        
        Map<String, Integer> fr = searchResult.getFacet(bucketCodeSystem);


        String labels= fr.keySet().stream().collect(Collectors.joining());
        assertEquals(bucketCodeValues[0], labels); //the only label is just the one
                                                   //we searched for
        

        int tsum = searchResult.getFacet(codeSystem).values().stream()
                                .mapToInt(Integer::intValue)
            .sum();

        assertEquals(searchResult.getTotal(), tsum);
        assertNotEquals(size, tsum);
    }
    
    @Test
    public void facetCountsUpdatedOnSidewaysFacetSearch() throws IOException{
        RestSubstanceSubstanceSearcher rsearch= new RestSubstanceSubstanceSearcher(session);
        
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult sr = (RestSubstanceSubstanceSearcher.RestExportSupportSearchResult) rsearch.request()
                                .addFacet(bucketCodeSystem, bucketCodeValues[0])
                                .setFacetType(FacetType.SIDEWAYS)
                                .setQueryParameter("fdim", "20")
                                .submit();
        
        Map<String, Integer> fr= sr.getFacet(bucketCodeSystem);
        Set<String> labels= fr.keySet();
        Set<String> expected=Util.toSet(bucketCodeValues);
        
        //Unlike the search case, this should include the counts of the facets
        //that it would have if there were no other selected
        //That is, it is a "sideways" selection, instead of a "drilldown"
        //by default
        assertEquals(expected, labels); 
        
        
        Map<String, Integer> fr2= sr.getFacet(codeSystem);
        int tsum = fr2.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
        assertEquals(sr.getTotal(), tsum);
        assertNotEquals(size, tsum);
    }
    
    @Test
    public void facetCountsUpdatedOnDrillDownFacetSearch() throws IOException{
        RestSubstanceSubstanceSearcher rsearch= new RestSubstanceSubstanceSearcher(session);
        
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult sr =  rsearch.request()
                             .addFacet(bucketCodeSystem, bucketCodeValues[0])
                             .setFacetType(FacetType.DRILLDOWN)
                                .setQueryParameter("fdim", "20")
                             .submit();
        

        
        assertEquals(Util.toSet(bucketCodeValues[0]), sr.getFacet(bucketCodeSystem).keySet());
        
        

        int tsum = sr.getFacet(codeSystem).values().stream()
                                    .mapToInt(Integer::intValue)
            .sum();
        assertEquals(sr.getTotal(), tsum);
        assertNotEquals(size, tsum);
    }
    
    
    @Test
    public void facetCountsUpdatedOnSidewaysStructureAndFacetSearch() throws IOException{
        RestSubstanceSubstanceSearcher rsearch= new RestSubstanceSubstanceSearcher(session);
        
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult baseResult = rsearch.substructure("C");
        
        int expectedAmount= baseResult.getFacet(bucketCodeSystem)
                                          .get(bucketCodeValues[0]);
        
        
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult facetedResult = baseResult.refiningRequest()
                        .addFacet(bucketCodeSystem, bucketCodeValues[0])
                        .setFacetType(FacetType.SIDEWAYS)
                        .submit();
        
        
        assertEquals(expectedAmount, facetedResult.getTotal());
        
        assertEquals(
                baseResult.getFacet(bucketCodeSystem),
                     facetedResult.getFacet(bucketCodeSystem)
                    );
        
    }
    
    @Test
    public void facetCountsUpdatedOnDrillDownStructureAndFacetSearch() throws IOException{
        RestSubstanceSubstanceSearcher rsearch= new RestSubstanceSubstanceSearcher(session);
        
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult baseResult = rsearch.substructure("C");
        
        int expectedAmount= baseResult.getFacet(bucketCodeSystem)
                                          .get(bucketCodeValues[0]);
        
        
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult facetedResult = baseResult.refiningRequest()
                        .addFacet(bucketCodeSystem, bucketCodeValues[0])
                        .setFacetType(FacetType.DRILLDOWN)
                        .submit();
        
        
        assertEquals(expectedAmount, facetedResult.getTotal());
        
        
        
        assertEquals(
                    baseResult.getFacet(bucketCodeSystem)
                                .entrySet().stream()
                                .filter(e-> e.getKey().equals(bucketCodeValues[0]))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                                ,
                     facetedResult.getFacet(bucketCodeSystem)
                    );
        
    }
    
    @Test
    public void facetCountsUpdatedOnSidewaysStructureAndTextSearch() throws IOException{
        RestSubstanceSubstanceSearcher rsearch= new RestSubstanceSubstanceSearcher(session);
        
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult baseResult = rsearch.substructure("C");
        
        int expectedAmount= baseResult.getFacet(bucketCodeSystem)
                                          .get(bucketCodeValues[0]);
        
        //TestUtil.waitForParam("ok");
        
        
        RestSubstanceSubstanceSearcher.RestExportSupportSearchResult subQueryResult = baseResult.refiningRequest(SimpleQueryBuilder.exactSearch(bucketCodeValues[0])).submit();
        
        assertEquals(expectedAmount, subQueryResult.getTotal());
        
        assertEquals(
                    baseResult.getFacet(bucketCodeSystem).entrySet()
                                    .stream().filter(e-> e.getKey().equals(bucketCodeValues[0]))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),

                     subQueryResult.getFacet(bucketCodeSystem)
                    );
        
    }
    
    
}
