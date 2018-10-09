package ix.test.search;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.util.RunOnly;
import ix.test.SubstanceJsonUtil;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.test.load.AbstractLoadDataSetTest;
import ix.test.server.RestSession;
import ix.test.server.RestSubstanceSubstanceSearcher;
import ix.test.server.SubstanceAPI;
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


         WSResponse wsResponse = session.get(session.getHttpResolver().apiV1("suggest/Name?q=foo"));

        JsonNode actualResults = SubstanceJsonUtil.ensurePass(wsResponse);

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

        WSResponse wsResponse = session.get(session.getHttpResolver().apiV1("suggest/Name?q=fooBar"));
//        SubstanceJsonUtil.ensurePass(wsResponse);

        JsonNode actualResults = SubstanceJsonUtil.ensurePass(wsResponse);

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
    public void exactNameSearchShorterPrefixShouldMakeExactMatchFirstHitEvenWithTonsOfHits() throws Exception{


        SubstanceAPI api = new SubstanceAPI(session);


        for(int i=0; i< 1000; i++){
            api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz"+i));
        }
        api.submitSubstance( new SubstanceBuilder().addName("fooBar"));
        api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz"));
        api.submitSubstance( new SubstanceBuilder().addName("food"));
        api.submitSubstance( new SubstanceBuilder().addName("Luke"));
        api.submitSubstance( new SubstanceBuilder().addName("Vader"));

        WSResponse wsResponse = session.get(session.getHttpResolver().apiV1("suggest/Name?q=fooBar&max=100"));
//

        JsonNode actualResults = SubstanceJsonUtil.ensurePass(wsResponse);

        assertTrue(actualResults.isArray());

        List<String> actualList = new ArrayList<>();
        for(JsonNode n :actualResults){
            actualList.add(n.get("key").asText());
        }
        assertEquals(100, actualList.size());
        assertEquals("fooBar", actualList.get(0));
        //sometimes we had problems with duplicates
       // Assert.assertNotEquals("fooBar", actualList.get(1));

        for(int i=1; i< actualList.size(); i++){
            assertTrue(actualList.get(i).startsWith("fooBarBaz"));
        }

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

        WSResponse wsResponse = session.get(session.getHttpResolver().apiV1("suggest/Name?q=fooBarBaz5&max=100"));
//        SubstanceJsonUtil.ensurePass(wsResponse);

        JsonNode actualResults = SubstanceJsonUtil.ensurePass(wsResponse);

        assertTrue(actualResults.isArray());

        List<String> actualList = new ArrayList<>();
        for(JsonNode n :actualResults){
            actualList.add(n.get("key").asText());
        }
        //list should be
        //FooBarBaz5
        //FooBarBaz50
        //FooBarBaz51
        //  ..
        ////FooBarBaz500
        //etc

        assertEquals(100, actualList.size());
        assertEquals("fooBarBaz5", actualList.get(0));

        assertFalse("fooBarBaz5".equals(actualList.get(1)));
        for(int i=1; i< actualList.size(); i++){
            assertTrue(actualList.get(i).startsWith("fooBarBaz5"));
        }

    }

    @Test
    public void exactNameSearchWithWhitespaceInHitsShouldMakeExactMatchFirstHitEvenWithTonsOfHits() throws Exception{


        SubstanceAPI api = new SubstanceAPI(session);


        for(int i=0; i< 1000; i++){
            api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz "+i));
        }
        api.submitSubstance( new SubstanceBuilder().addName("fooBar"));
        api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz"));
        api.submitSubstance( new SubstanceBuilder().addName("food"));
        api.submitSubstance( new SubstanceBuilder().addName("Luke"));
        api.submitSubstance( new SubstanceBuilder().addName("Vader"));

        WSResponse wsResponse = session.get(session.getHttpResolver().apiV1("suggest/Name?q=fooBarBaz&max=100"));
//        SubstanceJsonUtil.ensurePass(wsResponse);

        JsonNode actualResults = SubstanceJsonUtil.ensurePass(wsResponse);

        assertTrue(actualResults.isArray());

        List<String> actualList = new ArrayList<>();
        for(JsonNode n :actualResults){
            actualList.add(n.get("key").asText());
        }

        assertEquals(100, actualList.size());
        assertEquals("fooBarBaz", actualList.get(0));

        assertFalse("fooBarBaz".equals(actualList.get(1)));
        for(int i=1; i< actualList.size(); i++){
            assertTrue(actualList.get(i).startsWith("fooBarBaz "));
        }

    }

    @Test
    public void exactNameSearchWithWhitespaceInQueryShouldMakeExactMatchFirstHitEvenWithTonsOfHits() throws Exception{


        SubstanceAPI api = new SubstanceAPI(session);


        for(int i=0; i< 1000; i++){
            api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz "+i));
        }
        api.submitSubstance( new SubstanceBuilder().addName("fooBar"));
        api.submitSubstance( new SubstanceBuilder().addName("fooBarBaz"));
        api.submitSubstance( new SubstanceBuilder().addName("food"));
        api.submitSubstance( new SubstanceBuilder().addName("Luke"));
        api.submitSubstance( new SubstanceBuilder().addName("Vader"));

        WSResponse wsResponse = session.get(session.getHttpResolver().apiV1("suggest/Name?q=fooBarBaz%205&max=100"));
//        SubstanceJsonUtil.ensurePass(wsResponse);

        JsonNode actualResults = SubstanceJsonUtil.ensurePass(wsResponse);

        assertTrue(actualResults.isArray());

        List<String> actualList = new ArrayList<>();
        for(JsonNode n :actualResults){
            actualList.add(n.get("key").asText());
        }

        assertEquals(100, actualList.size());
        assertEquals("fooBarBaz 5", actualList.get(0));

        assertFalse("fooBarBaz 5".equals(actualList.get(1)));
        for(int i=1; i< actualList.size(); i++){
            assertTrue(actualList.get(i).startsWith("fooBarBaz 5"));
        }

    }
}
