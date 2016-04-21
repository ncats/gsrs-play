package ix.test.load;

import ix.ginas.controllers.GinasApp;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.SubstanceSearch;
import ix.test.util.TestNamePrinter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by katzelda on 4/13/16.
 */
public abstract class AbstractLoadDataSetTest {



    public GinasTestServer ts = new GinasTestServer();


    protected GinasTestServer.User admin;

    @Rule
    public RuleChain chain = RuleChain.outerRule( new TestNamePrinter())
                                                    .around(ts);

    @Before
    public void createAdmin(){
        admin = ts.createAdmin("admin2", "adminPass");
    }



    protected void assertFacetsMatch(Map<String, Map<String, Integer>> expectedFacets, SubstanceSearch.SearchResult actualResults){
        Map<String, Map<String, Integer>> actual = actualResults.getAllFacets();

        Map<String, Map<String, Integer>> filteredExpected = filterVisibleFacets(expectedFacets);

        assertEquals(filteredExpected, actual);
    }

    /**
     * Sometimes, we change which facets are calculated and how they are named by modifying
     * code in {@code GinasApp } so this method checks that class to remove any facets
     * that are currently not computed and translates the name accordingly.
     *
     * This way, we don't have to change our test code everytime we change production code.
     *
     * * @param expectedFacets the expected facet counts.
     * @return a new Map where some of the facets might be filtered out.  The original map is not modified.
     */
    private Map<String,Map<String,Integer>> filterVisibleFacets(Map<String, Map<String, Integer>> expectedFacets) {

        Map<String,Map<String,Integer>> filtered = new HashMap<>();
        for(String name : GinasApp.CHEMICAL_FACETS){
            String translatedName = GinasApp.translateFacetName(name);

            filtered.put(translatedName, expectedFacets.get(translatedName));
        }

        return filtered;
    }

    protected Map<String, Map<String, Integer>> createExpectedRep90Facets() {
        Map<String, Map<String, Integer>> expectedFacets = new HashMap<>();

        expectedFacets.put("Record Status", new HashMap<String, Integer>(){{
            put("Validated (UNII)", 17);
        }});
        expectedFacets.put("Substance Type", new HashMap<String, Integer>(){{
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
