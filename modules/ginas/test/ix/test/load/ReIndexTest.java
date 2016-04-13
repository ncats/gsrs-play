package ix.test.load;

import ix.test.ix.test.server.*;
import ix.test.util.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by katzelda on 4/12/16.
 */
public class ReIndexTest {
    //http://localhost:9000/dev/ginas/app/_updateIndex/_monitor

    @Rule
    public GinasTestServer ts = new GinasTestServer();


    private GinasTestServer.User admin;

    @Before
    public void createAdmin(){
        admin = ts.createAdmin("admin2", "adminPass");
    }



    @Test
    public void reindexAfterDeleteShouldRestoreSearch()throws IOException {
            try(BrowserSession session = ts.newBrowserSession(admin)) {

                SubstanceLoader loader = new SubstanceLoader(session);

                File f = new File("test/testdumps/rep90.ginas");

                loader.loadJson(f);

            }

            ts.stop(true);

            File home = ConfigUtil.getValueAsFile("ix.home");
            //Delete the ginas.ix/text, ginas.ix/sequence and ginas.ix/structure folders
            TestUtil.tryToDeleteRecursively(new File(home, "text"));
            TestUtil.tryToDeleteRecursively(new File(home, "sequence"));
            TestUtil.tryToDeleteRecursively(new File(home, "structure"));

            ts.start();
            try(BrowserSession session = ts.newBrowserSession(admin)){

                SubstanceReIndexer reIndexer = new SubstanceReIndexer(session);

                reIndexer.reindex();

                SubstanceSearch searcher = new SubstanceSearch(session);

                SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

                assertEquals(17, results.numberOfResults());
                assertFacetsMatch(createExpectedRep90Facets(), results);
            }

    }

    public static void assertFacetsMatch(Map<String, Map<String, Integer>> expectedFacets, SubstanceSearch.SearchResult actualResults){
        Map<String, Map<String, Integer>> actual = actualResults.getAllFacets();


        assertEquals(expectedFacets, actual);
    }

    private Map<String, Map<String, Integer>> createExpectedRep90Facets() {
        Map<String, Map<String, Integer>> expectedFacets = new HashMap<>();

        expectedFacets.put("Record Status", new HashMap<String, Integer>(){{
            put("Validated (UNII)", 17);
        }});
        expectedFacets.put("Substance Class", new HashMap<String, Integer>(){{
            put("Chemical", 17);
        }});
        expectedFacets.put("Molecular Weight", new HashMap<String, Integer>(){{
            put("200:400", 10);
            put("0:200", 9);
            put("400:600", 2);
            put(">1000", 2);
            put("800:1000", 1);
        }});
        expectedFacets.put( "GInAS Tag", new HashMap<String, Integer>(){{
            put("NOMEN", 17);
            put("WARNING", 17);
            put("WHO-DD", 6);
            put("MI", 6);
            put("INCI", 2);
            put("INN", 1);
            put("HSDB", 1);
            put("MART.", 1);
            put("FCC", 1);
            put("FHFI", 1);
        }});

        //Stereochemistry={ACHIRAL=13, ABSOLUTE=3, RACEMIC=1},
        expectedFacets.put("Stereochemistry", new HashMap<String, Integer>(){{

            put("ACHIRAL", 13);
            put("ABSOLUTE", 3);
            put("RACEMIC", 1);
        }});
        //Structure Hash={NNQ793F142LD=5, 1GMA5YNPSNF6=1, 9QJCPY53NHZV=1, L6RUGLWCMMP4=1, NNQ1X6A91CVX=1, NV2AC53S72NK=1, PA437XKNCWR2=1, VU8BQZFPPYTZ=1, YPCZM11BTJ54=1, Z3T91W4NXAHP=1 }
        expectedFacets.put("Structure Hash", new HashMap<String, Integer>(){{
            put("NNQ793F142LD", 5);
            put("1GMA5YNPSNF6", 1);
            put("9QJCPY53NHZV", 1);
            put("L6RUGLWCMMP4", 1);
            put("NNQ1X6A91CVX", 1);
            put("NV2AC53S72NK", 1);
            put("PA437XKNCWR2", 1);
            put("VU8BQZFPPYTZ", 1);
            put("YPCZM11BTJ54", 1);
            put("Z3T91W4NXAHP", 1);

        }});

        return expectedFacets;
    }
}
