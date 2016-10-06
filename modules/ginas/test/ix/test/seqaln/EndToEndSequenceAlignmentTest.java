package ix.test.seqaln;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import ix.test.seqaln.SequenceSearchAPI;
import ix.test.util.TestNamePrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ix.test.SubstanceJsonUtil;
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import play.libs.ws.WSResponse;
import util.json.JsonUtil;

/**
 * Created by katzelda on 3/30/16.
 */
public class EndToEndSequenceAlignmentTest {

    File folder=new File("test/testJSON/pass");
    @Rule
    public TestNamePrinter printer = new TestNamePrinter();


    @Rule
    public GinasTestServer ts = new GinasTestServer();

    private BrowserSession session;

    @Before
    public void login(){
        session = ts.newBrowserSession(ts.getFakeUser1());
    }

    @After
    public void logout(){
        session.logout();
    }
    @Test
    public void findLoadedSequence100PercentIdentity(){
        File jsonFile = new File(folder, "peptide.json");
        submitSubstance(jsonFile);

        SequenceSearchAPI api = new SequenceSearchAPI(session);

        List<SequenceSearchAPI.SearchResult> actual = api.search("CYIQNCPLG", 1);
        //we can't guess the uuid at the moment because it's randomly generated

        assertEquals(1, actual.size());

        SequenceSearchAPI.SearchResult result = actual.get(0);
        assertEquals(1, result.getPercentIdentity(), 0.001);

        assertEquals("OXYTOCIN", result.getName());

    }

    @Test
    public void findLoadedSequence(){
        File jsonFile = new File(folder, "peptide.json");
        submitSubstance(jsonFile);

        SequenceSearchAPI api = new SequenceSearchAPI(session);
                                                               //"CYIQNCPLG"
        List<SequenceSearchAPI.SearchResult> actual = api.search("CYIQXCPLG", .5);
        //we can't guess the uuid at the moment because it's randomly generated

        assertEquals(1, actual.size());

        SequenceSearchAPI.SearchResult result = actual.get(0);
        assertEquals(8/9D, result.getPercentIdentity(), 0.001);
      //  System.out.println(result.getPercentIdentity());
        assertEquals("OXYTOCIN", result.getName());

    }

    private void submitSubstance(File jsonFile) {
        try(RestSession restSession = ts.newRestSession(ts.getFakeUser2())){
            SubstanceAPI api = new SubstanceAPI(restSession);

            WSResponse response = api.submitSubstance(SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(jsonFile)));
            SubstanceJsonUtil.ensurePass(response);
        }
    }
}