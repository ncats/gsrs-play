package ix.test.load;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
import ix.test.server.SubstanceLoader;
import ix.test.server.SubstanceSearcher;

/**
 * Created by katzelda on 4/29/16.
 */
public class FacetCountsTest extends AbstractGinasServerTest{

    @Test
    public void facetCountsGetUpdatedWhenDataIsLoaded() throws IOException{
        GinasTestServer.User admin = ts.createAdmin("admin2", "adminPass");

        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            SubstanceSearcher searcher = new SubstanceSearcher(session);

            loader.loadJson(new File("test/testdumps/rep90_part1.ginas"));

            SubstanceSearcher.SearchResult results = searcher.substructure("C1=CC=CC=C1");


            assertEquals(9, results.numberOfResults());

            Map<String, Map<String,Integer>> partialFacets = new HashMap<>();

            partialFacets.put("Record Status", new HashMap<String,Integer>(){{
                                                    put("Validated (UNII)", 9);
            }});

            partialFacets.put("Substance Type", new HashMap<String,Integer>(){{
                put("Chemical", 9);
            }});
            partialFacets.put("Stereochemistry", new HashMap<String,Integer>(){{
                put("ACHIRAL", 7);
                put("ABSOLUTE", 1);
                put("RACEMIC", 1);
            }});

            partialFacets.put("Molecular Weight", new HashMap<String,Integer>(){{
                put("0:200", 5);
                put("200:400", 3);
                put("400:600", 1);
                put("800:1000", 1);
                put(">1000", 1);
            }});

            partialFacets.put("GInAS Tag", new HashMap<String,Integer>(){{
//                put("NOMEN", 9); //Accidentally added before, removed now
//                put("WARNING", 9); //Removed due to changing how warnings are stored
                put("MI", 2);
                put("WHO-DD", 2);
                put("INCI", 1);
            }});


            TestFacetUtil.assertFacetsMatch(partialFacets, results);

            loader.loadJson(new File("test/testdumps/rep90_part2.ginas"));

            SubstanceSearcher.SearchResult results2 = searcher.substructure("C1=CC=CC=C1");
            assertEquals(17, results2.numberOfResults());            
            TestFacetUtil.assertFacetsMatch(TestFacetUtil.createExpectedRep90Facets(), results2);
        }
    }
}
