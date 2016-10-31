package ix.test.load;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ix.ginas.controllers.GinasApp;
import ix.test.server.SearchResult;

/**
 * Created by katzelda on 4/29/16.
 */
public final class TestFacetUtil {
    private TestFacetUtil(){
        //can not instantiate
    }


    public static void assertFacetsMatch(Map<String, Map<String, Integer>> expectedFacets, SearchResult actualResults){
        Map<String, Map<String, Integer>> actual = actualResults.getAllFacets();
        Map<String, Map<String, Integer>> filteredExpected = filterVisibleFacets(expectedFacets);

        //Note: we no longer assert that the facets are the same. If we assumed that, then adding
        //a facet would not be allowed without some deeper changes. Instead, we say
        //that the filtered set must be a subset
        
        Set<Entry<String,Map<String, Integer>>> missing=new HashSet<>(filteredExpected.entrySet());
        missing.removeAll(actual.entrySet());
        missing.removeIf(f->f.getValue()==null);
        
        
        Map<String, Map<String, Integer>> empty=new HashMap<>();
        
        assertEquals(empty.entrySet(),missing);
    }

    /**
     * Sometimes, we change which facets are calculated and how they are named by modifying
     * code in {@code GinasApp } so this method checks that class to remove any facets
     * that are currently not computed and translates the name accordingly.
     *
     * This way, we don't have to change our test code every time we change production code.
     *
     * * @param expectedFacets the expected facet counts.
     * @return a new Map where some of the facets might be filtered out.  The original map is not modified.
     */
    private static Map<String,Map<String,Integer>> filterVisibleFacets(Map<String, Map<String, Integer>> expectedFacets) {

        Map<String,Map<String,Integer>> filtered = new HashMap<>();
        for(String name : GinasApp.getDefaultFacets()){
            String translatedName = GinasApp.translateFacetName(name);
            filtered.put(translatedName, expectedFacets.get(translatedName));
        }

        return filtered;
    }

    public static Map<String, Map<String, Integer>> createExpectedRep90Facets() {
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
//            put("NOMEN", 17); //Note, this was removed, as it was only in the tags by accident
//            put("WARNING", 17);
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
