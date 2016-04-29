package ix.test.load;

import ix.test.ix.test.server.BrowserSession;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.SubstanceSearch;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by katzelda on 4/29/16.
 */
public class FacetCountsTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer();
    private Map<String, Map<String, Integer>> allFacets;

    @Test
    public void facetCountsInitiallyZero() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(ts.getFakeUser1())){
            SubstanceSearch searcher = new SubstanceSearch(session);

            SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

            assertEquals(0, results.numberOfResults());
            System.out.println(allFacets);
            assertTrue(allFacets.isEmpty());
        }
    }
}
