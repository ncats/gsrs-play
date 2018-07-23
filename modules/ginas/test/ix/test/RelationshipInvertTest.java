package ix.test;

import static ix.test.SubstanceJsonUtil.ensureFailure;
import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;

import ix.core.util.RunOnly;
import ix.ginas.modelBuilders.SubstanceBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasServerTest;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import util.json.Change;
import util.json.ChangeFilter;
import util.json.Changes;
import util.json.JsonUtil;
import util.json.JsonUtil.JsonNodeBuilder;

public class RelationshipInvertTest extends AbstractGinasServerTest {

    File invrelate1 =  new File("test/testJSON/invrelate1.json");
    File invrelate2 =  new File("test/testJSON/invrelate2.json");

    private SubstanceAPI api;
    private RestSession session;

    @Before
    public void login(){
        //TODO do we need to specify token type?
        session = ts.newRestSession(ts.getFakeUser1(), RestSession.AUTH_TYPE.TOKEN);

        api = new SubstanceAPI(session);
    }

    @After
    public void logout(){
        session.logout();
    }

    
    @Test   
    public void addSubstanceWithRelationshipThenAddRelatedSubstanceShouldResultInBirectionalRelationship()   throws Exception {
        //submit primary, with dangling relationship
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
        String uuid = js.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
        assertTrue(validationResult.isValid());



        ensurePass(api.submitSubstance(js));
        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(js);
        String[] parts=type1.split("->");
        
        //submit the dangled
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
        String uuidA = jsA.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
        assertTrue(validationResultA.isValid());
        ensurePass(api.submitSubstance(jsA));

        //confirm that the dangled has a relationship to the dangler 
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        assertTrue(refUuidA.equals(uuid));
        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
    }
    
    
    @Test   
    public void removeSourceRelationshipShouldRemoveInvertedRelationship()   throws Exception {
    	
	    	 //submit primary, with dangling relationship
	        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
	        String uuid = js.get("uuid").asText();
	        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
	        assertTrue(validationResult.isValid());
	        ensurePass(api.submitSubstance(js));
	        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(js);
	        String[] parts=type1.split("->");
	        //submit the dangled
	
	        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
	        String uuidA = jsA.get("uuid").asText();
	        SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
	        assertTrue(validationResultA.isValid());
	        ensurePass(api.submitSubstance(jsA));
	
	        //confirm that the dangled has a relationship to the dangler 
	        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
	        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
	        assertTrue(refUuidA.equals(uuid));
	        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
	        
	        JsonNode priSubstance = api.fetchSubstanceJsonByUuid(uuid);
	        
	        //Remove the primary relationship, ensure the inverse is gone
	        JsonNode update=new JsonUtil.JsonNodeBuilder(priSubstance)
									.remove("/relationships/0")
									.ignoreMissing()
									.build();
	        
	        ensurePass(api.updateSubstance(update));
	        
	        JsonNode removedRelationship = api.fetchSubstanceJsonByUuid(uuid);
	        assertEquals(update.at("/relationships").size(),removedRelationship.at("/relationships").size());
	        
	        JsonNode newerVersion = api.fetchSubstanceJsonByUuid(uuidA);
	        
	        assertEquals(0,newerVersion.at("/relationships").size());
	     
    }
    
    
    @Test  
    public void addRelationshipAfterAddingEachSubstanceShouldAddInvertedRelationship()   throws Exception {
    	
        //submit primary
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
        JsonNode newRelate = js.at("/relationships/0");
        js=new JsonUtil.JsonNodeBuilder(js)
			.remove("/relationships/1")
			.remove("/relationships/0")
			.ignoreMissing()
			.build();
        
        String uuid = js.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
        assertTrue(validationResult.isValid());
        ensurePass(api.submitSubstance(js));
        js =api.fetchSubstanceJsonByUuid(uuid);
        
        
        //submit alternative
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
        String uuidA = jsA.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
        assertTrue(validationResultA.isValid());
        ensurePass(api.submitSubstance(jsA));

        
        //add relationship
        JsonNode updated=new JsonUtil.JsonNodeBuilder(js)
		.add("/relationships/-", newRelate)
		.ignoreMissing().build();
        ensurePass(api.updateSubstance(updated));
        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(updated);
        String[] parts=type1.split("->");
        
        //check inverse relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        assertTrue(refUuidA.equals(uuid));
        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
    
    	
    }
    
    @Test   
    public void addRelationshipAfterAddingEachSubstanceShouldAddInvertedRelationshipAndIncrementVersion()   throws Exception {
    	
        //submit primary
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
        JsonNode newRelate = js.at("/relationships/0");
        js=new JsonUtil.JsonNodeBuilder(js)
			.remove("/relationships/1")
			.remove("/relationships/0")
			.ignoreMissing()
			.build();
        
        String uuid = js.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
        assertTrue(validationResult.isValid());
        ensurePass(api.submitSubstance(js));
        js =api.fetchSubstanceJsonByUuid(uuid);
        
        
        //submit alternative
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
        String uuidA = jsA.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
        assertTrue(validationResultA.isValid());
        ensurePass(api.submitSubstance(jsA));

        
        //add relationship
        JsonNode updated=new JsonUtil.JsonNodeBuilder(js)
		.add("/relationships/-", newRelate)
		.ignoreMissing().build();
        ensurePass(api.updateSubstance(updated));
        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(updated);
        String[] parts=type1.split("->");
        
        //check inverse relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        assertTrue(refUuidA.equals(uuid));
        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
    	assertEquals("2",fetchedA.at("/version").asText());
    	
    }
    

    
    @Test 
    public void addRelationshipAfterAddingEachSubstanceShouldAddInvertedRelationshipAndShouldBeInHistoryOnlyOnce()   throws Exception {
    	
    		//This is very hard to read right now. A substanceBuilder would make this easy.
    		
        //submit primary
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
        JsonNode newRelate = js.at("/relationships/0");
        js=new JsonUtil.JsonNodeBuilder(js)
							.remove("/relationships/1")
							.remove("/relationships/0")
							.ignoreMissing()
							.build();
        
        String uuid = js.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
            assertTrue(validationResult.isValid());
        ensurePass(api.submitSubstance(js));
        js =api.fetchSubstanceJsonByUuid(uuid);
        
        
        //submit alternative
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
        String uuidA = jsA.get("uuid").asText();
            SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
            assertTrue(validationResultA.isValid());
        ensurePass(api.submitSubstance(jsA));
        JsonNode beforeA = api.fetchSubstanceJsonByUuid(uuidA);
        
        
       
        //add relationship To
        JsonNode updated=new JsonUtil.JsonNodeBuilder(js)
								.add("/relationships/-", newRelate)
								.ignoreMissing()
								.build();
        
        //update the substance for real
        ensurePass(api.updateSubstance(updated));
        
        
        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(updated);
        String[] parts=type1.split("->");
        
        //check inverse relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        
        assertTrue(refUuidA.equals(uuid));
        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
    	assertEquals("2",fetchedA.at("/version").asText());
    	
    	
    	//This part is broken?
    	//Doesn't even return? 
    	//Probably not actually being considered an edit!
    	JsonNode historyFetchedForFirst=api.fetchSubstanceJsonByUuid(uuid, 1).getOldValue();
    	
    	
    	Exception thrown = null;
    	try{
    		api.fetchSubstanceJsonByUuid(uuidA, 2);
    	}catch(Exception e){
    		thrown = e;
    	}
    	
    	assertNotNull("Version 2 of edit should not exist in the history table",thrown);
    	
    }
    
    @Test 
    public void addRelationshipAfterAddingEachSubstanceShouldAddInvertedRelationshipAndShouldBeInHistory()   throws Exception {
    	
    		//This is very hard to read right now. A substanceBuilder would make this easy.
    		
        //submit primary
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
        JsonNode newRelate = js.at("/relationships/0");
        js=new JsonUtil.JsonNodeBuilder(js)
							.remove("/relationships/1")
							.remove("/relationships/0")
							.ignoreMissing()
							.build();
        
        String uuid = js.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
            assertTrue(validationResult.isValid());
        ensurePass(api.submitSubstance(js));
        js =api.fetchSubstanceJsonByUuid(uuid);
        
        
        //submit alternative
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
        String uuidA = jsA.get("uuid").asText();
            SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
            assertTrue(validationResultA.isValid());
        ensurePass(api.submitSubstance(jsA));
        JsonNode beforeA = api.fetchSubstanceJsonByUuid(uuidA);
        
        
       
        //add relationship To
        JsonNode updated=new JsonUtil.JsonNodeBuilder(js)
								.add("/relationships/-", newRelate)
								.ignoreMissing()
								.build();
        
        //update the substance for real
        ensurePass(api.updateSubstance(updated));
        
        
        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(updated);
        String[] parts=type1.split("->");
        
        //check inverse relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        
        assertTrue(refUuidA.equals(uuid));
        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
    	assertEquals("2",fetchedA.at("/version").asText());
    	
    	
    	//This part is broken?
    	//Doesn't even return? 
    	//Probably not actually being considered an edit!
    	JsonNode historyFetchedForFirst=api.fetchSubstanceJsonByUuid(uuid, 1).getOldValue();
    	
    	JsonNode historyFetchedForSecond=api.fetchSubstanceJsonByUuid(uuidA, 1).getOldValue();
    	Changes changes= JsonUtil.computeChanges(beforeA, historyFetchedForSecond, new ChangeFilter[0]);
    	for(Change c:changes.getAllChanges()){
    		System.out.println("Change is:" + c);
    	}
    	assertEquals(beforeA,historyFetchedForSecond);
    	
    }
    
    
    @Test
    public void testAddRelationshipAfterAddingEachSubstanceThenRemovingInvertedRelationshipShouldFail() throws Exception {

	        //submit primary
	        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
	        JsonNode newRelate = js.at("/relationships/0");
	        js=new JsonUtil.JsonNodeBuilder(js)
				.remove("/relationships/1")
				.remove("/relationships/0")
				.ignoreMissing()
				.build();
	        
	        String uuid = js.get("uuid").asText();
	        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
	        assertTrue(validationResult.isValid());
	        ensurePass(api.submitSubstance(js));
	        js =api.fetchSubstanceJsonByUuid(uuid);
	        
	        
	        //submit alternative
	        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
	        String uuidA = jsA.get("uuid").asText();
	        SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
	        assertTrue(validationResultA.isValid());
	        ensurePass(api.submitSubstance(jsA));
	        
	        //add relationship
	        JsonNode updated=new JsonUtil.JsonNodeBuilder(js)
			.add("/relationships/-", newRelate)
			.ignoreMissing().build();
	        ensurePass(api.updateSubstance(updated));
	        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(updated);
	        String[] parts=type1.split("->");
	        
	        //check inverse relationship with primary
	        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
	        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
	        assertTrue(refUuidA.equals(uuid));
	        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
	        
	        JsonNode updatedA=new JsonUtil.JsonNodeBuilder(fetchedA)
			.remove("/relationships/0")
			.ignoreMissing().build();
	        
	        ensurePass(api.updateSubstance(updatedA));

        assertEquals(Collections.emptyList(),  SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(uuid)).build().relationships);
        assertEquals("substance with uuid " + uuidA, Collections.emptyList(),  SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(uuidA)).build().relationships);


    }

    @Test
    public void testAddRelationshipAfterAddingEachSubstanceThenRemovingFromPrimaryRelationshipShouldPass() throws Exception {

        //submit primary
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
        JsonNode newRelate = js.at("/relationships/0");
        js=new JsonUtil.JsonNodeBuilder(js)
                .remove("/relationships/1")
                .remove("/relationships/0")
                .ignoreMissing()
                .build();

        String uuid = js.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
        assertTrue(validationResult.isValid());
        ensurePass(api.submitSubstance(js));
        js =api.fetchSubstanceJsonByUuid(uuid);


        //submit alternative
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
        String uuidA = jsA.get("uuid").asText();
        SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
        assertTrue(validationResultA.isValid());
        ensurePass(api.submitSubstance(jsA));

        //add relationship
        JsonNode updated=new JsonUtil.JsonNodeBuilder(js)
                .add("/relationships/-", newRelate)
                .ignoreMissing().build();
        ensurePass(api.updateSubstance(updated));
        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(updated);
        String[] parts=type1.split("->");

        //check inverse relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuid);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        assertTrue(refUuidA.equals(uuidA));
        assertEquals(parts[0] + "->" + parts[1],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));

        JsonNode updatedA=new JsonUtil.JsonNodeBuilder(fetchedA)
                .remove("/relationships/0")
                .ignoreMissing().build();

        ensurePass(api.updateSubstance(updatedA));




        assertEquals(Collections.emptyList(),  SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(uuid)).build().relationships);
        assertEquals(Collections.emptyList(),  SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(uuidA)).build().relationships);
    }
    
    @Test   
    public void testDontAddRelationshipIfOneLikeItAlreadyExists()   throws Exception {
    	
        //submit primary
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
        JsonNode newRelate = js.at("/relationships/0");
        String uuid = js.get("uuid").asText();
        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(js);
        String[] parts=type1.split("->");
        
        newRelate=new JsonUtil.JsonNodeBuilder(newRelate)
			.set("/relatedSubstance/refuuid",uuid)
			.set("/relatedSubstance/refPname",js.at("/names/0/name").asText())
			.set("/type",parts[1] + "->" + parts[0])
			.ignoreMissing()
			.build();


        SubstanceAPI.ValidationResponse validationResult = api.validateSubstance(js);
        assertTrue(validationResult.isValid());
        ensurePass(api.submitSubstance(js));
        js =api.fetchSubstanceJsonByUuid(uuid);
        
        
        //submit alternative
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
        String uuidA = jsA.get("uuid").asText();
        jsA=new JsonUtil.JsonNodeBuilder(jsA)
		.add("/relationships/-",newRelate)
		.ignoreMissing()
		.build();
        SubstanceAPI.ValidationResponse validationResultA = api.validateSubstance(js);
        assertTrue(validationResultA.isValid());
        ensurePass(api.submitSubstance(jsA));
        
        
        //check inverse relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        assertTrue(refUuidA.equals(uuid));
        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
        assertEquals(1,fetchedA.at("/relationships").size());
        
    }
    
    @Test   
    public void testDontAddRelationshipIfOneLikeItAlreadyExistsAndDontMakeEdit()   throws Exception {
    	
        //submit primary
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate1));
        JsonNode newRelate = js.at("/relationships/0");
        String uuid = js.get("uuid").asText();
        String type1=SubstanceJsonUtil.getTypeOnFirstRelationship(js);
        String[] parts=type1.split("->");
        
        newRelate=new JsonUtil.JsonNodeBuilder(newRelate)
			.set("/relatedSubstance/refuuid",uuid)
			.set("/relatedSubstance/refPname",js.at("/names/0/name").asText())
			.set("/type",parts[1] + "->" + parts[0])
			.ignoreMissing()
			.build();

        SubstanceAPI.ValidationResponse validationResponse = api.validateSubstance(js);
        assertTrue(validationResponse.isValid());
        ensurePass(api.submitSubstance(js));

        api.fetchSubstanceJsonByUuid(uuid);
        
        
        //submit alternative
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(invrelate2));
        String uuidA = jsA.get("uuid").asText();
        jsA=new JsonUtil.JsonNodeBuilder(jsA)
			.add("/relationships/-",newRelate)
			.ignoreMissing()
			.build();

        SubstanceAPI.ValidationResponse validationResponseA = api.validateSubstance(jsA);

        assertTrue(validationResponseA.isValid());
        ensurePass(api.submitSubstance(jsA));
        
        
        //check inverse relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
        assertTrue(refUuidA.equals(uuid));
        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
        assertEquals(1,fetchedA.at("/relationships").size());
        
        //Shouldn't be any edit history either
        assertEquals(404,api.fetchAllSubstanceHistory(uuid).getStatus());
    }

}
