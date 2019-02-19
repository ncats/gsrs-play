package ix.test;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import ix.core.util.RunOnly;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasServerTest;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import util.json.JsonUtil;

public class SubstanceAlternativeTest extends AbstractGinasServerTest {


    File resource ;
    
    private SubstanceAPI api;
    private RestSession session;

    @Before
    public void login(){

        session = ts.newRestSession(ts.getAdmin(), RestSession.AUTH_TYPE.TOKEN);

        api = new SubstanceAPI(session);
    }

    @After
    public void logout(){
        session.logout();
    }

    @Test
    public void testAPIAlternativeSubstanceSubmitValidate()   throws Exception {
    	try{
	        //submit primary
	        resource = new File("test/testJSON/alternative/Prim1.json");
	        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
	        String uuid = js.get("uuid").asText();

            SubstanceAPI.ValidationResponse response = api.validateSubstance(js);
            
            response.assertValid();

	        ensurePass(api.submitSubstance(js));
	
	        //submit alternative
	        resource = new File("test/testJSON/alternative/PostAlt.json");
	        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
	        String uuidA = jsA.get("uuid").asText();

            SubstanceAPI.ValidationResponse response2 = api.validateSubstance(jsA);
            assertTrue(response2.isValid());

	        ensurePass(api.submitSubstance(jsA));
	
	        //check alternative relationship with primary
	        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
	        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
	        assertTrue(refUuidA.equals(uuid));
	
	        //check primary relationship with alternative
	        JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
	        String refUuid = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetched);
	        assertTrue(refUuid.equals(uuidA));
    	}catch(Exception e){
    		e.printStackTrace();
    		throw e;
    	}
    }

    @Test
    public void testAPIAlternativeSubstanceUpdate()   throws Exception {
        //submit primary
        resource = new File("test/testJSON/alternative/Prim1.json");
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        String uuid = js.get("uuid").asText();

        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
        
        
        validationResult.assertValid();
        ensurePass(api.submitSubstance(js));

        //submit alternative
        resource = new File("test/testJSON/alternative/PostAlt.json");
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
       
        String uuidA = jsA.get("uuid").asText();

        SubstanceAPI.ValidationResponse responseA = api.validateSubstance(jsA);
        
        responseA.assertValid();
        ensurePass(api.submitSubstance(jsA));

        //check alternative relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        assertTrue(refUuidA.equals(uuid));

        //check primary relationship with alternative
        JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
        String refUuid = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetched);
        assertTrue(refUuid.equals(uuidA));

        //submit new primary
        resource = new File("test/testJSON/alternative/Prim2.json");
        JsonNode jsNew = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        String uuidNew = jsNew.get("uuid").asText();
//        JsonNode validationResultNew = api.validateSubstanceJson(jsNew);
//        SubstanceJsonUtil.ensureIsValid(validationResultNew);
//
        SubstanceAPI.ValidationResponse validationResultNew = api.validateSubstance(jsNew);
        validationResultNew.assertValid();
        ensurePass(api.submitSubstance(jsNew));

        //update alternative
        resource = new File("test/testJSON/alternative/PutAlt.json");
        JsonNode jsAUpdate = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        JsonNode jsp = JsonDiff.asJson(jsA,jsAUpdate);
        JsonNode newAVersion = JsonPatch.apply(jsp, fetchedA);
        String uuidAUpdate = newAVersion.get("uuid").asText();
        System.out.println("These are the relationships:" + newAVersion.get("relationships").size());

        SubstanceAPI.ValidationResponse validationResultAUpdate = api.validateSubstance(newAVersion);
        assertTrue(validationResultAUpdate.getMessages().toString(), validationResultAUpdate.isValid());

        ensurePass(api.updateSubstance(newAVersion));

        //check primary has no relationships after alternative update
        JsonNode fetchedPrim = api.fetchSubstanceJsonByUuid(uuid);
        assertNull(fetchedPrim.get("relationships").get(0));

         //check alternative relationship with New primary
        JsonNode fetchedAUpdate = api.fetchSubstanceJsonByUuid(uuidAUpdate);
        String refUuidAUpdate = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedAUpdate);
        assertTrue(refUuidAUpdate.equals(uuidNew));

        //check New primary relationship with alternative
        JsonNode fetchedNew = api.fetchSubstanceJsonByUuid(uuidNew);
        String refUuidNew = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedNew);
        assertTrue(refUuidNew.equals(uuidAUpdate));
    }
}
