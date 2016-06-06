package ix.test.load;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import ix.test.ix.test.server.BrowserSession;
import ix.test.ix.test.server.ConfigUtil;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceLoader;
import ix.test.ix.test.server.SubstanceSearch;
import ix.test.util.TestUtil;
import ix.utils.Util;
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
    
    
    private void substructureSearchShouldWait(BrowserSession session) throws IOException, AssertionError{
    	SubstanceSearch searcher = new SubstanceSearch(session);
    	SubstanceSearch.SearchResult results =searcher.getSubstructureSearch("CC1=CC=CC=C1", 1, 15,true);
        assertTrue("15th page of substructure search should have the same substance every time",results.getUuids().contains("0a707d18"));
        assertTrue("15th page of substructure search should have no other substances",results.getUuids().size()==1);        
    }
    
    private void substructureSearchShouldWaitAndLaterPagesShouldReturn(BrowserSession session) throws IOException, AssertionError{
    	SubstanceSearch searcher = new SubstanceSearch(session);
    	SubstanceSearch.SearchResult resultsFirst =searcher.getSubstructureSearch("CC1=CC=CC=C1", 2, 3,true);
    	SubstanceSearch.SearchResult resultsLater =searcher.getSubstructureSearch("CC1=CC=CC=C1", 2, 6,true);
    	assertTrue("6th page on substructure search should have 2 entries", resultsLater.getUuids().size()==2);
    }
    private void substructureHighlightingShouldShowOnLastPage(BrowserSession session) throws IOException, AssertionError{
    	SubstanceSearch searcher = new SubstanceSearch(session);
    	SubstanceSearch.SearchResult resultsFirst =searcher.getSubstructureSearch("C1=CC=CC=C1", 1, 1,false);
    	HtmlPage lastResults =searcher.getSubstructurePage("C1=CC=CC=C1", 1, 10,true);
    	Set<String> img_urls=SubstanceSearch.getStructureImagesFrom(lastResults);
    	assertEquals(1,img_urls.size());
    	RestSession rs=ts.newRestSession(session.getUser());
    	
    	String withHighlight=img_urls.iterator().next();
    	String withoutHighlight=withHighlight.replace("context", "contextfake");
    	
    	String sha1WithHighlight=Util.sha1(rs.get(withHighlight).getBody());
    	String sha1WithoutHighlight=Util.sha1(rs.get(withoutHighlight).getBody());
    	//System.out.println("Highlight sha1:" +sha1WithHighlight);
    	//System.out.println("No Highlight sha1:" +sha1WithoutHighlight);
    	
    	assertNotEquals(sha1WithHighlight,sha1WithoutHighlight);
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
    public void substructureSearchOnRep90ShouldReturnDeterministicResults() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);


            runRepTests(session);
            substructureSearchShouldWait(session);
            
        }
    }
    
    
    @Test
    public void substructureSearchOnRep90ShouldAllowPaging() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);


            runRepTests(session);
            substructureSearchShouldWaitAndLaterPagesShouldReturn(session);
            
        }
    }
    
    @Test
    public void substructureSearchOnRep90ShouldPreserveHighlightingAfterPaging() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);


            runRepTests(session);
            try{
            	substructureHighlightingShouldShowOnLastPage(session);
            }catch(Throwable t){
            	t.printStackTrace();
            	throw t;
            }
            
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
