package ix.test;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.test.util.TestNamePrinter;
import util.json.JsonUtil;

public class ChangeSubstanceClassTest {


    final File resource=new File("test/testJSON/toedit.json");

    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);


    private GinasTestServer.User fakeUser1, fakeUser2;

    @Before
    public void getUsers(){
        fakeUser1 = ts.getFakeUser1();
        fakeUser2 = ts.getFakeUser2();
    }

    @Test
    public void changeProteinToConceptTest(){
		JsonNode entered = SubstanceJsonUtil
				.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        try( RestSession session = ts.newRestSession(fakeUser1)) {
        	String uuid = entered.at("/uuid").asText();
            SubstanceAPI api = new SubstanceAPI(session);
            ensurePass(api.submitSubstance(entered));
            JsonNode retrievedProtein=api.fetchSubstanceJsonByUuid(uuid);
            JsonNode toSubmitConcept=
            		new JsonUtil.JsonNodeBuilder(retrievedProtein)
            		.remove("/protein")
            		.set("/substanceClass", "concept")
            		.build();
            ensurePass(api.updateSubstance(toSubmitConcept));
            JsonNode retrievedConcept=api.fetchSubstanceJsonByUuid(uuid);
            assertEquals("concept",retrievedConcept.at("/substanceClass").asText());
            
        }catch(Throwable t){
        	t.printStackTrace();
        	throw t;
        }
    }
    @Test
    public void changeProteinToChemicalTest(){
		JsonNode entered = SubstanceJsonUtil
				.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        try( RestSession session = ts.newRestSession(fakeUser1)) {
        	String uuid = entered.at("/uuid").asText();
            SubstanceAPI api = new SubstanceAPI(session);
            ensurePass(api.submitSubstance(entered));
            JsonNode achemical=ChemicalApiTest.makeChemicalSubstanceJSON("C1CCCCCCCC1");
            JsonNode retrievedProtein=api.fetchSubstanceJsonByUuid(uuid);
           
            
            JsonNode toSubmitConcept=
            		new JsonUtil.JsonNodeBuilder(retrievedProtein)
            		.remove("/protein")
            		.set("/substanceClass", "chemical")
            		.set("/structure", achemical.at("/structure"))
            		.set("/moieties", achemical.at("/moieties"))
            		.add("/references/-", achemical.at("/references/0"))
            		.build();
            ensurePass(api.updateSubstance(toSubmitConcept));
            JsonNode retrievedChemical=api.fetchSubstanceJsonByUuid(uuid);
            assertEquals("chemical",retrievedChemical.at("/substanceClass").asText());
            assertTrue("New chemical should have structure",retrievedChemical.at("/structure/molfile").asText().length()>0);
            
        }catch(Throwable t){
        	t.printStackTrace();
        	throw t;
        }
    }
    
    @Test
   	public void testPromoteConceptToProtein() throws Exception {
        try( RestSession session = ts.newRestSession(fakeUser1)) {
            SubstanceAPI api = new SubstanceAPI(session);
    		
           	JsonNode entered= SubstanceJsonUtil
    				.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
           	JsonNode concept=new JsonUtil
            .JsonNodeBuilder(entered)
            .remove("/protein")
            .set("/substanceClass", "concept")
            .build();
           	String uuid=entered.get("uuid").asText();
            ensurePass(api.submitSubstance(concept));
            JsonNode fetched=api.fetchSubstanceJsonByUuid(uuid);
            assertEquals(fetched.at("/substanceClass").asText(), "concept");
            JsonNode updated = new JsonUtil
                    .JsonNodeBuilder(fetched)
                    .add("/protein",entered.at("/protein"))
                    .set("/substanceClass", "protein")
                    .build();
            ensurePass(api.updateSubstance(updated));
            JsonNode fetchedagain=api.fetchSubstanceJsonByUuid(uuid);
            assertEquals(fetchedagain.at("/substanceClass").asText(), "protein");
            
            
        }
   	}
}
