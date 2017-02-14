package ix.test.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import ix.core.util.RunOnly;
import ix.test.server.*;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.adapters.EntityPersistAdapter;
import ix.core.models.Role;
import ix.core.util.TimeTraveller;
import ix.core.util.TimeUtil;
import ix.ginas.models.v1.Substance;
import ix.test.builder.SubstanceBuilder;
import ix.test.load.AbstractLoadDataSetTest;
import org.junit.rules.TemporaryFolder;

public class LastEditedFacetTest extends AbstractLoadDataSetTest {

    public static final String TEST_TESTDUMPS_REP90_PART1_GINAS = "test/testdumps/rep90_part1.ginas";

    BrowserSubstanceSearcher searcher;
    SubstanceLoader loader;
    BrowserSession session;


    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @After
    public void tearDown(){
        if(session !=null){
            session.close();
        }

    }
    @Rule
    public TimeTraveller timeTraveller = new TimeTraveller();


    @Test
    public void loadWithoutPreserveAuditFlagWillSetLastEditedToUserDoingTheLoading() throws IOException, InterruptedException {

        GinasTestServer.User otherUser = ts.getFakeUser2();

        File loadFile = tmpDir.newFile();
        try (JsonSubstanceWriter writer = new JsonSubstanceWriter(loadFile)) {
            writer.write(new SubstanceBuilder().addName("nameA").setLastEditedBy(otherUser).build());
            writer.write(new SubstanceBuilder().addName("nameB").setLastEditedBy(otherUser).build());
        }



        session = ts.newBrowserSession(admin);

        SubstanceLoader loader = new SubstanceLoader(session);
        loader.loadJson(loadFile, new SubstanceLoader.LoadOptions()
                //.preserveAuditInfo(true)
        );


        searcher = new BrowserSubstanceSearcher(session);
        SearchResult results = searcher.all();



        Map<String, Long> actual = results.getSubstances()
                                .map(s -> s.getLastEditedBy().username)
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<String, Long> expected = new HashMap<String, Long>(){{
            put(admin.getUserName(), 2L);
        }
        };

        assertEquals(expected, actual);
    }

    @Test
    public void loadWithPreserveAuditFlagWillSetLastEditedToWhatJsonSays() throws IOException, InterruptedException {

        GinasTestServer.User otherUser = ts.getFakeUser2();

        File loadFile = tmpDir.newFile();
        try (JsonSubstanceWriter writer = new JsonSubstanceWriter(loadFile)) {
            writer.write(new SubstanceBuilder().addName("nameA").setLastEditedBy(otherUser).build());
            writer.write(new SubstanceBuilder().addName("nameB").setLastEditedBy(otherUser).build());
        }



        session = ts.newBrowserSession(admin);

        SubstanceLoader loader = new SubstanceLoader(session);
        loader.loadJson(loadFile, new SubstanceLoader.LoadOptions()
                .preserveAuditInfo(true)
        );


        searcher = new BrowserSubstanceSearcher(session);
        SearchResult results = searcher.all();



        Map<String, Long> actual = results.getSubstances()
                .map(s -> s.getLastEditedBy().username)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<String, Long> expected = new HashMap<String, Long>(){{
            put(otherUser.getUserName(), 2L);
        }
        };

        assertEquals(expected, actual);
    }


    @Test
    public void lastEditedFacetChangeWithDates() throws IOException {

        session = ts.newBrowserSession(admin);

        loader = new SubstanceLoader(session);
        File f = new File(TEST_TESTDUMPS_REP90_PART1_GINAS);
        loader.loadJson(f);


        searcher = new BrowserSubstanceSearcher(session);
        Map<String, Integer> lastEditMap;
        SearchResult results = searcher.all();


        assertTrue(results.numberOfResults() > 0);
        assertTrue(!results.getAllFacets().isEmpty());


        lastEditMap = results.getFacet("Last Edited");

        int beforeCount = lastEditMap.get("Today");
        assertEquals(45,  beforeCount);

        timeTraveller.jump(2, TimeUnit.DAYS);

        try( BrowserSession session2 = ts.notLoggedInBrowserSession()) {
            BrowserSubstanceSearcher searcher2 = session2.newSubstanceSearcher();

            SearchResult results2 = searcher2.all();


            lastEditMap = results2.getFacet("Last Edited");

            int afterCount = lastEditMap.get("Today");
            assertEquals(0, afterCount);
            assertEquals(45, lastEditMap.get("This week").intValue());

        }
    }

    @Test
    public void directReindex() throws IOException {
        session = ts.newBrowserSession(admin);

        loader = new SubstanceLoader(session);
        File f = new File(TEST_TESTDUMPS_REP90_PART1_GINAS);
        loader.loadJson(f, new SubstanceLoader.LoadOptions()
                                                .numRecordsToLoad(20));

        searcher = new BrowserSubstanceSearcher(session);

        Map<String, Integer> lastEditMap;

        SearchResult results = searcher.all();

        assertTrue(results.numberOfResults() > 0);
        assertTrue(!results.getAllFacets().isEmpty());
        lastEditMap = results.getFacet("Last Edited");

        int beforeCount = lastEditMap.get("Today");
        assertEquals(20,  beforeCount);


        String uuid = "8206586d-2a94-42a5-bc66-ebb1bd8dc618";

        timeTraveller.jumpAhead(8, ChronoUnit.MONTHS);

        GinasTestServer.User admin3 = ts.createUser(Arrays.asList(Role.values()));
        try(RestSession restSession = ts.newRestSession(admin3)) {
            SubstanceAPI api = restSession.newSubstanceAPI();


            JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
            Substance s = SubstanceBuilder.from(fetched)
                    .setLastEditedDate(TimeUtil.getCurrentDate())
                    .build();

            EntityPersistAdapter.getInstance().deepreindex(s);
        }

        try( BrowserSession session2 = ts.newBrowserSession(admin)) {
            BrowserSubstanceSearcher searcher2 = session2.newSubstanceSearcher();

            System.out.println(TimeUtil.getCurrentDate());
            results = searcher2.all();
            lastEditMap = results.getFacet("Last Edited");

            int afterCount = lastEditMap.get("Today");

            assertEquals(1, afterCount);
        }
    }

    @Test
    public void LastEditedFacetChangeAfterSubstanceEdit() throws IOException {

        TimeUtil.setCurrentTime(TimeUtil.toMillis(TimeUtil.getCurrentLocalDateTime().plusYears(5)));

        session = ts.newBrowserSession(admin);

        loader = new SubstanceLoader(session);
        File f = new File(TEST_TESTDUMPS_REP90_PART1_GINAS);
        loader.loadJson(f, new SubstanceLoader.LoadOptions()
                                                .numRecordsToLoad(20));

        searcher = new BrowserSubstanceSearcher(session);

        Map<String, Integer> lastEditMap;

        SearchResult results = searcher.all();

        assertTrue(results.numberOfResults() > 0);
        assertTrue(!results.getAllFacets().isEmpty());

        lastEditMap = results.getFacet("Last Edited");

        int beforeCount = lastEditMap.get("Today");

        assertEquals(20,  beforeCount);


        String uuid = "8206586d-2a94-42a5-bc66-ebb1bd8dc618";

        timeTraveller.jumpAhead(8, ChronoUnit.MONTHS);


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

        try( BrowserSession session2 = ts.notLoggedInBrowserSession()) {
            BrowserSubstanceSearcher searcher2 = session2.newSubstanceSearcher();

            results = searcher2.all();
            lastEditMap = results.getFacet("Last Edited");

            int afterCount = lastEditMap.get("Today");

            assertEquals(1, afterCount);
        }

    }

    @Test
    public void editTopLevelFieldShouldUpdatedLastEdited() throws IOException {

        TimeUtil.setCurrentTime(TimeUtil.toMillis(TimeUtil.getCurrentLocalDateTime().plusYears(5)));

        session = ts.newBrowserSession(admin);

        loader = new SubstanceLoader(session);
        File f = new File(TEST_TESTDUMPS_REP90_PART1_GINAS);
        loader.loadJson(f, new SubstanceLoader.LoadOptions()
                                             .numRecordsToLoad(20));

        searcher = new BrowserSubstanceSearcher(session);

        Map<String, Integer> lastEditMap;

        SearchResult results = searcher.all();

        assertTrue(results.numberOfResults() > 0);
        assertTrue(!results.getAllFacets().isEmpty());

        lastEditMap = results.getFacet("Last Edited");

        int beforeCount = lastEditMap.get("Today");
        assertEquals(20,  beforeCount);


        String uuid = "8206586d-2a94-42a5-bc66-ebb1bd8dc618";

        timeTraveller.jumpAhead(8, ChronoUnit.MONTHS);



        GinasTestServer.User admin3 = ts.createUser(Arrays.asList(Role.values()));
        try(RestSession restSession = ts.newRestSession(admin3)) {
            SubstanceAPI api =  restSession.newSubstanceAPI();


            JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
            SubstanceBuilder.from(fetched)
            .andThen(s ->{
                s.changeReason = "TESTING";
            })
            .buildJsonAnd(json -> api.updateSubstanceJson(json));

            Substance substance = api.fetchSubstanceObjectByUuid(uuid, Substance.class);

            assertEquals(TimeUtil.getCurrentLocalDate(), TimeUtil.asLocalDate(substance.getLastEdited()));
            assertEquals("TESTING", substance.changeReason);


        }



        try( BrowserSession session2 = ts.notLoggedInBrowserSession()) {
            BrowserSubstanceSearcher searcher2 = session2.newSubstanceSearcher();

            results = searcher2.all();

            lastEditMap = results.getFacet("Last Edited");


            assertEquals(1, lastEditMap.get("Today").intValue());
            assertEquals(19, lastEditMap.get("Past 1 year").intValue());
        }

    }
}


