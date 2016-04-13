package ix.test.load;

import ix.test.ix.test.server.*;
import ix.test.util.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by katzelda on 4/12/16.
 */
public class ReIndexTest extends AbstractLoadDataSetTest{
    //http://localhost:9000/dev/ginas/app/_updateIndex/_monitor


    @Test
    public void reindexAfterDeleteShouldRestoreSearch()throws IOException {
            try(BrowserSession session = ts.newBrowserSession(admin)) {

                SubstanceLoader loader = new SubstanceLoader(session);

                File f = new File("test/testdumps/rep90.ginas");

                loader.loadJson(f);

            }

            ts.stop(true);

            File home = ConfigUtil.getValueAsFile("ix.home");
            //Delete the ginas.ix/text, ginas.ix/sequence and ginas.ix/structure folders
            TestUtil.tryToDeleteRecursively(new File(home, "text"));
            TestUtil.tryToDeleteRecursively(new File(home, "sequence"));
            TestUtil.tryToDeleteRecursively(new File(home, "structure"));

            ts.start();
            try(BrowserSession session = ts.newBrowserSession(admin)){

                SubstanceReIndexer reIndexer = new SubstanceReIndexer(session);

                reIndexer.reindex();

                SubstanceSearch searcher = new SubstanceSearch(session);

                SubstanceSearch.SearchResult results = searcher.substructure("C1=CC=CC=C1");

                assertEquals(17, results.numberOfResults());
                assertFacetsMatch(createExpectedRep90Facets(), results);
            }

    }

}
