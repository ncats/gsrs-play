package ix.test.search;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.util.TimeTraveller;
import ix.core.util.TimeUtil;
import ix.ginas.models.v1.Substance;
import ix.test.builder.SubstanceBuilder;
import ix.test.load.AbstractLoadDataSetTest;
import ix.test.server.*;
import ix.test.util.TestUtil;
import org.junit.*;
import play.api.Configuration;
import play.libs.ws.WSResponse;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LastEditedFacetTest extends AbstractLoadDataSetTest {

    public static final String TEST_TESTDUMPS_REP90_PART1_GINAS = "test/testdumps/rep90_part1.ginas";

    SubstanceSearcher searcher;
    SubstanceLoader loader;
    BrowserSession session;

    @Before
    public void loadData() throws Exception{

       session = ts.newBrowserSession(admin);

            loader = new SubstanceLoader(session);
            File f = new File(TEST_TESTDUMPS_REP90_PART1_GINAS);
            loader.loadJson(f);

            searcher = new SubstanceSearcher(session);
    }

    @After
    public void closeSession() {
        session.close();
    }

    @Rule
    public TimeTraveller timeTraveller = new TimeTraveller();

    @Ignore
    @Test
    public void lastEditedFacetChangeWithDates() throws IOException {

        Map<String, Integer> lastEditMap = new LinkedHashMap<>();

        SubstanceSearcher.SearchResult results = searcher.all();

            assertTrue(results.numberOfResults() > 0);
            assertTrue(!results.getAllFacets().isEmpty());

            lastEditMap = results.getFacet("Last Edited");
            System.out.println("last Edited-before:" + lastEditMap);

            int beforeCount = lastEditMap.get("Today");
            assertEquals(45,  beforeCount);

            timeTraveller.jump(2, TimeUnit.DAYS);

            results = searcher.all();
            lastEditMap = results.getFacet("Last Edited");
            System.out.println("last Edited-after:" + lastEditMap);

            int afterCount = lastEditMap.get("Today");
            assertEquals(0, afterCount);


        }


    @Test
    public void LastEditedFacetChangeAfterSubstanceEdit() throws IOException {

        Map<String, Integer> lastEditMap = new LinkedHashMap<>();

        SubstanceSearcher.SearchResult results = searcher.all();

        assertTrue(results.numberOfResults() > 0);
        assertTrue(!results.getAllFacets().isEmpty());

        lastEditMap = results.getFacet("Last Edited");
        System.out.println("last Edited-before:" + lastEditMap);

        int beforeCount = lastEditMap.get("Today");
        assertEquals(45,  beforeCount);


       /* String uuid = results.getSubstances().findFirst().get().getUuid().toString();
        System.out.println("uuid: " + uuid);

        timeTraveller.jump(10, TimeUnit.DAYS);

        try(RestSession restSession = ts.newRestSession(admin)) {
            SubstanceAPI api = new SubstanceAPI(restSession);

            JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
            SubstanceBuilder.from(fetched)
                    .addName("ASDASDSD").buildJsonAnd(json -> api.submitSubstance(json));

        }*/


        results = searcher.all();
        lastEditMap = results.getFacet("Last Edited");
        System.out.println("last Edited-after:" + lastEditMap);

        int afterCount = lastEditMap.get("Today");
        //assertEquals(1, afterCount);



        //JsonNode json = session.extractJSON(response);
         /*   //JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
		    SubstanceBuilder.from(json)
					.addName("ASDASDSD")
					.build();*/

    }
}


