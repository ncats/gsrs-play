package ix.test.load;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ix.test.server.BrowserSession;
import ix.test.server.ConfigUtil;
import ix.test.server.SearchResult;
import ix.test.server.SubstanceLoader;
import ix.test.server.SubstanceReIndexer;
import ix.test.server.BrowserSubstanceSearcher;
import ix.test.util.TestUtil;

/**
 * Created by katzelda on 4/12/16.
 */
public class ReIndexTest extends AbstractLoadDataSetTest{

    @Test
    public void reindexAfterDeleteShouldRestoreSearch()throws IOException {
            try(BrowserSession session = ts.newBrowserSession(admin)) {

                SubstanceLoader loader = new SubstanceLoader(session);

                File f = new File(LoadDataSetTest.TEST_TESTDUMPS_REP90_GINAS);

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

                SubstanceReIndexer reIndexer = new SubstanceReIndexer(session);

                reIndexer.reindex();

                BrowserSubstanceSearcher searcher = new BrowserSubstanceSearcher(session);

                SearchResult results = searcher.substructure("C1=CC=CC=C1");

                assertEquals(17, results.numberOfResults());
                TestFacetUtil.assertFacetsMatch(TestFacetUtil.createExpectedRep90Facets(), results);
            }

    }

}
