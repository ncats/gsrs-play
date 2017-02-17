package ix.test.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.AbstractGinasServerTest;
import ix.core.controllers.search.SearchFactory;
import ix.core.plugins.TextIndexerPlugin;
import ix.test.SubstanceJsonUtil;
import ix.test.builder.SubstanceBuilder;
import ix.test.load.AbstractLoadDataSetTest;
import ix.test.server.BrowserSession;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.test.server.SubstanceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.ws.WSResponse;

import java.util.*;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 2/17/17.
 */
public class SuggestFieldExactNameTest extends AbstractLoadDataSetTest{
    RestSession session;
    @Before
    public void setup(){
        session = ts.newRestSession(admin);
    }

    @After
    public void tearDown(){
        session.close();
    }
    @Test
    public void suggestNames() throws Exception{


        SubstanceAPI api = new SubstanceAPI(session);


        api.submitSubstance( new SubstanceBuilder().addName("fooBar"));
        api.submitSubstance( new SubstanceBuilder().addName("food"));
        api.submitSubstance( new SubstanceBuilder().addName("Luke"));
        api.submitSubstance( new SubstanceBuilder().addName("Vader"));

         WSResponse wsResponse = session.get("ginas/app/api/v1/suggest/Name?q=foo");
        SubstanceJsonUtil.ensurePass(wsResponse);

        JsonNode actualResults = wsResponse.asJson();

        assertTrue(actualResults.isArray());

        Set<String> actualList = new HashSet<>();
        for(JsonNode n :actualResults){
            actualList.add(n.get("key").asText());
        }

        Set<String> expected = new HashSet<>();
        expected.add("fooBar");
        expected.add("food");

        assertEquals(expected, actualList);

//        System.out.println(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(results));
    }

    @Test
    public void exactNameSearchShouldMakeExactMatchFirstHit() throws Exception{


        SubstanceAPI api = new SubstanceAPI(session);


        api.submitSubstance( new SubstanceBuilder().addName("fooBar"));
        api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz"));
        api.submitSubstance( new SubstanceBuilder().addName("food"));
        api.submitSubstance( new SubstanceBuilder().addName("Luke"));
        api.submitSubstance( new SubstanceBuilder().addName("Vader"));

        WSResponse wsResponse = session.get("ginas/app/api/v1/suggest/Name?q=fooBar");
        SubstanceJsonUtil.ensurePass(wsResponse);

        JsonNode actualResults = wsResponse.asJson();

        assertTrue(actualResults.isArray());

        List<String> actualList = new ArrayList<>();
        for(JsonNode n :actualResults){
            actualList.add(n.get("key").asText());
        }

        List<String> expected = Arrays.asList("fooBar", "fooBarBaz");

        assertEquals(expected, actualList);

//        System.out.println(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(results));
    }


    @Test
    public void exactNameSearchShouldMakeExactMatchFirstHitEvenWithTonsOfHits() throws Exception{


        SubstanceAPI api = new SubstanceAPI(session);


        for(int i=0; i< 1000; i++){
            api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz"+i));
        }
        api.submitSubstance( new SubstanceBuilder().addName("fooBar"));
        api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz"));
        api.submitSubstance( new SubstanceBuilder().addName("food"));
        api.submitSubstance( new SubstanceBuilder().addName("Luke"));
        api.submitSubstance( new SubstanceBuilder().addName("Vader"));

        WSResponse wsResponse = session.get("ginas/app/api/v1/suggest/Name?q=fooBar&max=100");
        SubstanceJsonUtil.ensurePass(wsResponse);

        JsonNode actualResults = wsResponse.asJson();

        assertTrue(actualResults.isArray());

        List<String> actualList = new ArrayList<>();
        for(JsonNode n :actualResults){
            actualList.add(n.get("key").asText());
        }
        assertEquals(100, actualList.size());
        assertEquals("fooBar", actualList.get(0));

        for(int i=1; i< actualList.size(); i++){
            assertTrue(actualList.get(i).startsWith("fooBarBaz"));
        }

//        System.out.println(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(results));
    }


}
