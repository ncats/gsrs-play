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

    @Test
    public void loadMultipleFiles() throws IOException {

        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);


            loader.loadJson(new File("test/testdumps/rep90_part1.ginas"));

            loader.loadJson(new File("test/testdumps/rep90_part2.ginas"));

            SubstanceSearch searcher = new SubstanceSearch(session);

            Set<String> uuids = searcher.substructure("C1=CC=CC=C1");

            assertEquals(17, uuids.size());
        }
    }
    @Test
    public void loadAsAdmin() throws IOException {
        try(BrowserSession session = ts.newBrowserSession(admin)){

            SubstanceLoader loader = new SubstanceLoader(session);

            File f = new File("test/testdumps/rep90.ginas");

            loader.loadJson(f);


            SubstanceSearch searcher = new SubstanceSearch(session);

           Set<String> uuids = searcher.substructure("C1=CC=CC=C1");

            assertEquals(17, uuids.size());
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

            Set<String> uuids = searcher.substructure("C1=CC=CC=C1");

            assertEquals(17, uuids.size());
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

        Set<String> uuids = searcher.substructure("C1=CC=CC=C1");

        assertEquals(0, uuids.size());

    }
}
