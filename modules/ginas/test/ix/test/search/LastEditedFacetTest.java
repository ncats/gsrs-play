package ix.test.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.models.Role;
import ix.core.plugins.IxCache;
import ix.core.util.RunOnly;
import ix.core.util.TimeTraveller;
import ix.core.util.TimeUtil;
import ix.ginas.models.v1.Substance;
import ix.test.SubstanceJsonUtil;
import ix.test.builder.SubstanceBuilder;
import ix.test.load.AbstractLoadDataSetTest;
import ix.test.server.*;
import ix.test.util.TestUtil;
import org.junit.*;
import play.api.Configuration;
import play.libs.ws.WSResponse;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LastEditedFacetTest extends AbstractLoadDataSetTest {

    public static final String TEST_TESTDUMPS_REP90_PART1_GINAS = "test/testdumps/rep90_part1.ginas";

    SubstanceSearcher searcher;
    SubstanceLoader loader;
    BrowserSession session;

   // @Before
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
    public void directReindex() throws IOException {

      //  TimeUtil.setCurrentTime(TimeUtil.toMillis(LocalDateTime.of(1955, 11, 12, 10, 23, 0)));

        session = ts.newBrowserSession(admin);

        loader = new SubstanceLoader(session);
        File f = new File(TEST_TESTDUMPS_REP90_PART1_GINAS);
        loader.loadJson(f, 0, 20);

        searcher = new SubstanceSearcher(session);

        Map<String, Integer> lastEditMap;

        SubstanceSearcher.SearchResult results = searcher.all();

        assertTrue(results.numberOfResults() > 0);
        assertTrue(!results.getAllFacets().isEmpty());
        System.out.println("facets = " + results.getAllFacets().keySet());
        lastEditMap = results.getFacet("Last Edited");
        System.out.println("last Edited-before:" + lastEditMap);

        int beforeCount = lastEditMap.get("Today");
        // assertEquals(45,  beforeCount);


        String uuid = "8206586d-2a94-42a5-bc66-ebb1bd8dc618";

        timeTraveller.jumpAhead(8, ChronoUnit.MONTHS);

        GinasTestServer.User admin3 = ts.createUser(Arrays.asList(Role.values()));
        try(RestSession restSession = ts.newRestSession(admin3)) {
            SubstanceAPI api = restSession.newSubstanceAPI();


            JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
            Substance s = SubstanceBuilder.from(fetched)
                    .setLastEditedDate(TimeUtil.getCurrentDate())
                    .build();

            System.out.println("substance last edited " + s.getLastEdited() + " millis = " + s.getLastEdited().getTime());
            EntityPersistAdapter.getInstance().deepreindex(s);
        }

        try( BrowserSession session2 = ts.newBrowserSession(admin)) {
            SubstanceSearcher searcher2 = session2.newSubstanceSearcher();

            System.out.println(TimeUtil.getCurrentDate());
            results = searcher2.all();
            lastEditMap = results.getFacet("Last Edited");
            System.out.println("last Edited-after:" + lastEditMap);

            int afterCount = lastEditMap.get("Today");

            assertEquals(1, afterCount);
        }
    }

    @Test
    @RunOnly
    public void LastEditedFacetChangeAfterSubstanceEdit() throws IOException {

        TimeUtil.setCurrentTime(TimeUtil.toMillis(TimeUtil.getCurrentLocalDateTime().plusYears(5)));

        session = ts.newBrowserSession(admin);

        loader = new SubstanceLoader(session);
        File f = new File(TEST_TESTDUMPS_REP90_PART1_GINAS);
        loader.loadJson(f, 0, 20);

        searcher = new SubstanceSearcher(session);

        Map<String, Integer> lastEditMap;

        SubstanceSearcher.SearchResult results = searcher.all();

        assertTrue(results.numberOfResults() > 0);
        assertTrue(!results.getAllFacets().isEmpty());

        System.out.println(results.getUuids());

        System.out.println("facets = " + results.getAllFacets().keySet());
        lastEditMap = results.getFacet("Last Edited");
        System.out.println("last Edited-before:" + lastEditMap);

        int beforeCount = lastEditMap.get("Today");
       // assertEquals(45,  beforeCount);


        String uuid = "8206586d-2a94-42a5-bc66-ebb1bd8dc618";

        timeTraveller.jumpAhead(8, ChronoUnit.MONTHS);


        System.out.println("=======================================\n\n\n\n\n");

        GinasTestServer.User admin3 = ts.createUser(Arrays.asList(Role.values()));
        try(RestSession restSession = ts.newRestSession(admin3)) {
            SubstanceAPI api =  restSession.newSubstanceAPI();


            JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
            SubstanceBuilder.from(fetched)
                    .setLastEditedDate(TimeUtil.getCurrentDate())
                    .buildJsonAnd(json -> api.updateSubstanceJson(json));

             Substance substance = api.fetchSubstanceObjectByUuid(uuid, Substance.class);

            assertEquals(TimeUtil.getCurrentLocalDate(), TimeUtil.asLocalDate(substance.getLastEdited()));



        }
    //    IxCache.clearCache();

////        timeTraveller.jump(5, TimeUnit.MINUTES);
//        try(BrowserSession bs2 = ts.newBrowserSession(admin)){
//            SubstanceLoader loader = new SubstanceLoader(bs2);
//            loader.loadJson(f, 20, 25);
//        }

        timeTraveller.jump(5, TimeUnit.MINUTES);

    try( BrowserSession session2 = ts.notLoggedInBrowserSession()) {
            SubstanceSearcher searcher2 = session2.newSubstanceSearcher();

        System.out.println(TimeUtil.getCurrentDate());
            results = searcher2.all();
            lastEditMap = results.getFacet("Last Edited");
            System.out.println("last Edited-after:" + lastEditMap);

            int afterCount = lastEditMap.get("Today");

        assertEquals(1, afterCount);
    }

        //assertEquals(1, afterCount);



        //JsonNode json = session.extractJSON(response);
         /*   //JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
		    SubstanceBuilder.from(json)
					.addName("ASDASDSD")
					.build();*/

    }
}


