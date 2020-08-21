package ix.test.load;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ix.test.server.*;
import org.junit.Test;

import ix.AbstractGinasServerTest;

/**
 * Created by katzelda on 4/29/16.
 */
public class FacetCountsTest extends AbstractGinasServerTest{

    @Test
    public void facetCountsGetUpdatedWhenDataIsLoaded() throws IOException{
        GinasTestServer.User admin = ts.createAdmin("admin2", "adminPass");

        try(RestSession session = ts.newRestSession(admin);
            BrowserSession browserSession = ts.newBrowserSession(admin)
        ){

            SubstanceLoader loader = new SubstanceLoader(browserSession);

            RestSubstanceSubstanceSearcher searcher = new RestSubstanceSubstanceSearcher(session);

            loader.loadJson(new File("test/testdumps/rep90_part1.ginas"));

            SearchResult results = searcher.substructure("C1=CC=CC=C1");


            assertEquals(10, results.numberOfResults());

            Map<String, Map<String,Integer>> partialFacets = new HashMap<>();

            partialFacets.put("Record Status", new HashMap<String,Integer>(){{
                                                    put("approved", 9);
            }});

            partialFacets.put("Substance Class", new HashMap<String,Integer>(){{
                put("Chemical", 9);
            }});
            partialFacets.put("StereoChemistry", new HashMap<String,Integer>(){{
                put("ACHIRAL", 7);
                put("ABSOLUTE", 1);
                put("RACEMIC", 1);
            }});

            //molecular weights taken out of test because rest api doesn't have it ?

//            partialFacets.put("Molecular Weight", new HashMap<String,Integer>(){{
//                put("0:200", 5);
//                put("200:400", 3);
//                put("400:600", 1);
//                put("800:1000", 1);
//                put(">1000", 1);
//            }});

            partialFacets.put("GInAS Tag", new HashMap<String,Integer>(){{
//                put("NOMEN", 9); //Accidentally added before, removed now
//                put("WARNING", 9); //Removed due to changing how warnings are stored
                put("MI", 2);
                put("WHO-DD", 2);
                put("INCI", 1);
            }});


            System.out.println("facets = " + results.getAllFacets());
            TestFacetUtil.assertFacetsMatch(partialFacets, results);

            loader.loadJson(new File("test/testdumps/rep90_part2.ginas"));

            SearchResult results2 = searcher.substructure("C1=CC=CC=C1");
            assertEquals(17, results2.numberOfResults());            
            TestFacetUtil.assertFacetsMatch(TestFacetUtil.createExpectedRep90Facets(), results2);
        }
    }
}
