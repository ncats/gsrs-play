package ix.test.load;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ix.test.ix.test.server.*;
import ix.test.util.TestNamePrinter;
import ix.test.util.TestUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import org.apache.commons.io.FileUtils;
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
public class LoadDataSetTest extends AbstractLoadDataSetTest{


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    
    /**
     * Does a simple set of searches to be performed by all loads
     * @param session
     * @throws Exception
     */
    private void runRepTests(BrowserSession session) throws IOException, AssertionError{
    	SubstanceSearch searcher = new SubstanceSearch(session);
        SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");
        assertEquals(17, results.numberOfResults());
        SubstanceSearch.SearchResult all = searcher.all();
        assertEquals(90, all.numberOfResults());
        TestFacetUtil.assertFacetsMatch(TestFacetUtil.createExpectedRep90Facets(), results);
    }
    
    @Test
    public void loadMultipleFiles() throws IOException {

        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);


            loader.loadJson(new File("test/testdumps/rep90_part1.ginas"));

            loader.loadJson(new File("test/testdumps/rep90_part2.ginas"));

            runRepTests(session);
        }
    }

    //@Ignore
    @Test
    public void loadAsAdmin() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);


            runRepTests(session);
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
            runRepTests(session);
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


    @Test
    public void noDataLoadedShouldReturnZeroResults() throws IOException {

        SubstanceSearch searcher = new SubstanceSearch(ts.notLoggedInBrowserSession());

        SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

        assertEquals(0, results.numberOfResults());

        assertEquals(Collections.emptyMap(), results.getAllFacets());
    }

    @Test
    public void deleteLuceneIndexesButNOTDatabaseShouldReturnZeroResults() throws IOException{
        try(BrowserSession session = ts.newBrowserSession(admin)) {

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);

        }

        ts.stop(true);

        File home = ConfigUtil.getDefault().getValueAsFile("ix.home");
        //Delete the ginas.ix/text, ginas.ix/sequence and ginas.ix/structure folders
        TestUtil.tryToDeleteRecursively(new File(home, "text"));
        TestUtil.tryToDeleteRecursively(new File(home, "sequence"));
        TestUtil.tryToDeleteRecursively(new File(home, "structure"));

        ts.start();
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceSearch searcher = new SubstanceSearch(session);

            SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

            assertEquals(0, results.numberOfResults());
            assertTrue(results.getAllFacets().isEmpty());
        }
    }

}
