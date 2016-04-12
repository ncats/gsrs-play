package ix.test.load;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ix.test.ix.test.server.*;
import ix.test.util.TestNamePrinter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;
/**
 * Created by katzelda on 4/4/16.
 */
public class LoadDataSet {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private GinasTestServer ts = new GinasTestServer();

    @Rule
    public RuleChain chain = RuleChain.outerRule( new TestNamePrinter())
                                                    .around(ts);

    private GinasTestServer.User admin;

    @Before
    public void createAdmin(){
        admin = ts.createAdmin("admin2", "adminPass");
    }


    public static void assertFacetsMatch(Map<String, Map<String, Integer>> expectedFacets, SubstanceSearch.SearchResult actualResults){
        Map<String, Map<String, Integer>> actual = actualResults.getAllFacets();

        for(Map.Entry<String, Map<String, Integer>> expectedEntry : expectedFacets.entrySet()){
            String key=expectedEntry.getKey();
            System.out.println(key);

            Map<String, Integer> actualSubMap = actual.get(key);
            System.out.println("\t" + expectedEntry.getValue());
            System.out.println("\t" + actualSubMap);
        }
        assertEquals(expectedFacets, actual);
    }


    @Test
    public void loadMultipleFiles() throws IOException {

        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);


            loader.loadJson(new File("test/testdumps/rep90_part1.ginas"));

            loader.loadJson(new File("test/testdumps/rep90_part2.ginas"));

            SubstanceSearch searcher = new SubstanceSearch(session);

            SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

            assertEquals(17, results.numberOfResults());


            assertFacetsMatch(createExpectedRep90Facets(), results);
        }
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
        return expectedFacets;
    }

    @Test
    public void loadAsAdmin() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);


            SubstanceSearch searcher = new SubstanceSearch(session);

            SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

            assertEquals(17, results.numberOfResults());
            assertFacetsMatch(createExpectedRep90Facets(), results);
        }
    }

    @Test
    public void loadedDataPersistedAcrossRestarts() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)) {

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);

        }

        ts.restart();
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceSearch searcher = new SubstanceSearch(session);

            SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

            assertEquals(17, results.numberOfResults());
            assertFacetsMatch(createExpectedRep90Facets(), results);
        }

    }

    @Test
    public void nonAdminCanNotLoad() throws IOException{
        GinasTestServer.User normalUser = ts.createNormalUser("peon", "pass");

        try(BrowserSession session = ts.newBrowserSession(normalUser)) {

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            expectedException.expectMessage("401 Unauthorized");
            loader.loadJson(f);
        }
    }

    //Delete the ginas.ix/text, ginas.ix/sequence and ginas.ix/structure folders
    @Test
    public void noDataLoadedShouldReturnZeroResults() throws IOException {

        SubstanceSearch searcher = new SubstanceSearch(ts.notLoggedInBrowserSession());

        SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

        assertEquals(0, results.numberOfResults());

        assertEquals(Collections.emptyMap(), results.getAllFacets());
    }


}
