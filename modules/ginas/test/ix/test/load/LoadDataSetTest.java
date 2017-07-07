package ix.test.load;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import ix.ginas.utils.GinasGlobal;
import ix.test.server.BrowserSession;
import ix.test.server.ConfigUtil;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SearchResult;
import ix.test.server.SubstanceLoader;
import ix.test.server.BrowserSubstanceSearcher;
import ix.test.util.TestUtil;
import ix.utils.Util;
/**
 * Created by katzelda on 4/4/16.
 */
public class LoadDataSetTest extends AbstractLoadDataSetTest{


    private static final String TEST_TESTDUMPS_REP90_PART2_GINAS = "test/testdumps/rep90_part2.ginas";


	private static final String TEST_TESTDUMPS_REP90_PART1_GINAS = "test/testdumps/rep90_part1.ginas";


	public static final String TEST_TESTDUMPS_REP90_GINAS = "test/testdumps/rep90.ginas";
    
    
	@Rule
    public ExpectedException expectedException = ExpectedException.none();

    
    /**
     * Does a simple set of searches to be performed by all loads
     * @param session
     * @throws Exception
     */
    public static void runRepTests(BrowserSession session) throws IOException, AssertionError{
    	
    	
    	BrowserSubstanceSearcher searcher = new BrowserSubstanceSearcher(session);
    	SearchResult all = searcher.all();
        assertEquals(90, all.numberOfResults());
       
        SearchResult results = searcher.substructure("C1=CC=CC=C1");
        assertEquals(17, results.numberOfResults());
        TestFacetUtil.assertFacetsMatch(TestFacetUtil.createExpectedRep90Facets(), results);
        
    }
    
    
    private void substructureSearchShouldWait(BrowserSession session) throws IOException, AssertionError{
    	BrowserSubstanceSearcher searcher = new BrowserSubstanceSearcher(session);
    	searcher.setSearchOrder("Name Count");
    	SearchResult results1 =searcher.getSubstructureSearch("CC1=CC=CC=C1", 1, 1,false);
    	//System.out.println("This was the first uuid:" + results1.getUuids().toString());
    	
    	
    	try{
    	    Thread.sleep(1_000);
    	}catch(Exception e){}
    	
    	SearchResult results2 =searcher.getSubstructureSearch("CC1=CC=CC=C1", 1, 15,true);
    	
    	
    	
    	
    	//System.out.println("Found results size:" + results2.getUuids().toString());
    	assertEquals(1, results2.getUuids().size());
    	String findUUID="445d5a83";
		assertTrue(
				"15th page of substructure searchProteins should have the same substance every time: looking for \""
						+ findUUID
						+ "\" but found \""
						+ results2.getUuids().toString() + "\"", results2
						.getUuids().contains(findUUID));
        assertTrue("15th page of substructure searchProteins should have no other substances",results2.getUuids().size()==1);
    }
    
    private void substructureSearchShouldWaitAndLaterPagesShouldReturn(BrowserSession session) throws IOException, AssertionError{
    	BrowserSubstanceSearcher searcher = new BrowserSubstanceSearcher(session);
    	SearchResult resultsFirst =searcher.getSubstructureSearch("CC1=CC=CC=C1", 2, 3,true);
    	SearchResult resultsLater =searcher.getSubstructureSearch("CC1=CC=CC=C1", 2, 6,true);
    	assertTrue("6th page on substructure searchProteins should have 2 entries", resultsLater.getUuids().size()==2);
    }
    private void substructureHighlightingShouldShowOnLastPage(BrowserSession session) throws IOException, AssertionError{
    	BrowserSubstanceSearcher searcher = new BrowserSubstanceSearcher(session);
    	
    	
    	SearchResult resultsFirst =searcher.getSubstructureSearch("C1=CC=CC=C1", 1, 1,false);
    	HtmlPage lastResults =searcher.getSubstructurePage("C1=CC=CC=C1", 1, 10,true);
//    	System.out.println(lastResults.asXml());
    	Set<String> img_urls= BrowserSubstanceSearcher.getStructureImagesFrom(lastResults);
//    	try {
//            Thread.sleep(1200_000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            throw new IOException(e);
//        }
    	assertEquals(1,img_urls.size());
    	RestSession rs=ts.newRestSession(session.getUser());
    	
    	CountDownLatch cdl = new CountDownLatch(1);
    	
    	
    	
    	String withHighlight=img_urls.iterator().next();
    	String withoutHighlight=withHighlight.replace("context", "contextfake");
    	System.out.println("Testing1:" + withHighlight);
    	System.out.println("Testing2:" + withoutHighlight);
    	
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


            loader.loadJson(new File(TEST_TESTDUMPS_REP90_PART1_GINAS));

            loader.loadJson(new File(TEST_TESTDUMPS_REP90_PART2_GINAS));
			
            runRepTests(session);
        }
    }

    
    @Test 
    public void loadAsAdmin() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){
            SubstanceLoader loader = new SubstanceLoader(session);
            File f = new File(TEST_TESTDUMPS_REP90_GINAS);
            loader.loadJson(f);
            runRepTests(session);
        }
    }
    
    
    @Test  
    public void substructureSearchOnRep90ShouldReturnDeterministicResults() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File(TEST_TESTDUMPS_REP90_GINAS);

            loader.loadJson(f);


            runRepTests(session);
            substructureSearchShouldWait(session);
            
        }
    }
    
    
    @Test  
    public void substructureSearchOnRep90ShouldAllowPaging() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File(TEST_TESTDUMPS_REP90_GINAS);

            loader.loadJson(f);


            runRepTests(session);
            substructureSearchShouldWaitAndLaterPagesShouldReturn(session);
            
        }
    }
    
    @Test  
    public void substructureSearchOnRep90ShouldPreserveHighlightingAfterPaging() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File(TEST_TESTDUMPS_REP90_GINAS);

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

            File f = new File(TEST_TESTDUMPS_REP90_GINAS);

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

            File f = new File(TEST_TESTDUMPS_REP90_GINAS);

            expectedException.expectMessage("401 Unauthorized");
            loader.loadJson(f);
        }
    }


    
    @Test  
    public void noDataLoadedShouldReturnZeroResults() throws IOException {

        BrowserSubstanceSearcher searcher = new BrowserSubstanceSearcher(ts.notLoggedInBrowserSession());

        SearchResult results = searcher.substructure("C1=CC=CC=C1");

        assertEquals(0, results.numberOfResults());

        assertEquals(Collections.emptyMap(), results.getAllFacets());
    }
    
    
    
    @Test  
    public void deleteLuceneIndexesButNOTDatabaseShouldReturnZeroResults() throws IOException{
        try(BrowserSession session = ts.newBrowserSession(admin)) {

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File(TEST_TESTDUMPS_REP90_GINAS);

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

            BrowserSubstanceSearcher searcher = new BrowserSubstanceSearcher(session);

            SearchResult results = searcher.substructure("C1=CC=CC=C1");

            assertEquals(0, results.numberOfResults());
            assertTrue(results.getAllFacets().isEmpty());
        }
    }

}
