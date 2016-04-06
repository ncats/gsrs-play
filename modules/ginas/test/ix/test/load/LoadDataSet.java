package ix.test.load;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ix.test.ix.test.server.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    @Rule
    public GinasTestServer ts = new GinasTestServer();

    @Test
    public void loadAsAdmin() throws IOException {
        GinasTestServer.User admin = ts.createAdmin("admin2", "adminPass");
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
