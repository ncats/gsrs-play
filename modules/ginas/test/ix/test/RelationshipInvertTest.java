package ix.test;

import static ix.test.SubstanceJsonUtil.ensureFailure;
import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.EntityFactory;
import ix.core.util.EntityUtils;
import ix.core.util.RunOnly;
import ix.ginas.exporters.JsonExporter;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.*;
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
        session = ts.newRestSession(ts.getAdmin(), RestSession.AUTH_TYPE.TOKEN);

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
	        System.out.println("fetchedA = " + fetchedA);
	        String refUuidA = SubstanceJsonUtil.getRefUuidOnFirstRelationship(fetchedA);
	        assertTrue(refUuidA.equals(uuid));
	        assertEquals(parts[1] + "->" + parts[0],SubstanceJsonUtil.getTypeOnFirstRelationship(fetchedA));
	        
	        JsonNode priSubstance = api.fetchSubstanceJsonByUuid(uuid);
	        System.out.println("==============\n\n\n");
	        System.out.println("before remove sub = " + priSubstance);
	        //Remove the primary relationship, ensure the inverse is gone
	        JsonNode update=new JsonUtil.JsonNodeBuilder(priSubstance)
									.remove("/relationships/0")
									.ignoreMissing()
									.build();
	        
	        ensurePass(api.updateSubstance(update));
	        System.out.println("expected update = \n\n" + update);
	        JsonNode removedRelationship = api.fetchSubstanceJsonByUuid(uuid);

        System.out.println("\n\n\nAFTER remove sub = " + removedRelationship);
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

    /**
     * On trying to delete a INHIBITOR -> TRANSPORTER relationship,
     * the validation rules portion of the form returned:
     *
     * ERROR
     Error updating entity [Error ID:e86fed46]:java.lang.IllegalStateException: java.lang.IllegalStateException: java.lang.IllegalStateException: javax.persistence.OptimisticLockException: Data has changed. updated [0] rows sql[update ix_ginas_substance set current_version=?, last_edited=?, version=?, internal_version=? where uuid=? and internal_version=?] bind[null]. See applicaiton log for more details.


     root cause is

     Caused by: javax.persistence.OptimisticLockException: Data has changed. updated [0] rows sql[update ix_ginas_substance set current_version=?, last_edited=?, version=?, internal_version=? where uuid=? and internal_version=?] bind[null]
     at com.avaje.ebeaninternal.server.persist.dml.DmlHandler.checkRowCount(DmlHandler.java:95) ~[org.avaje.ebeanorm.avaje-ebeanorm-3.3.4.jar:na]
     at com.avaje.ebeaninternal.server.persist.dml.UpdateHandler.execute(UpdateHandler.java:81) ~[org.avaje.ebeanorm.avaje-ebeanorm-3.3.4.jar:na]

     */
    @Test
    public void removeRelationshipWithActiveMoeityGsrs587FromOriginal(){
        Substance parent = new SubstanceBuilder()
                .addName("parent")
                .generateNewUUID()
                .build();

        api.submitSubstance(parent);
        Substance inhibitor = new SubstanceBuilder()
                                        .addName("inhibitor")
                                        .addActiveMoiety()
                .generateNewUUID()
                                        .build();

        List<Relationship> activeMoeity = inhibitor.getActiveMoieties();

        assertEquals(1, activeMoeity.size());

        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .build();

        JsonNode json = api.submitSubstance(inhibitor);


        SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(parent.getUuid()))
                .addRelationshipTo(inhibitor, "INFRASPECIFIC->PARENT ORGANISM")
                .buildJsonAnd( js -> api.updateSubstanceJson(js));

        Substance storedInhibitor = SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(inhibitor.getUuid())).build();

        Substance storedTransporter =SubstanceBuilder.from(api.submitSubstance(transporter)).build();


        JsonNode withRel = new SubstanceBuilder(storedInhibitor)
                .addRelationshipTo(storedTransporter, "INHIBITOR -> TRANSPORTER")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();

        Substance actualTransporter = api.fetchSubstanceObjectByUuid(storedTransporter.getUuid().toString(), Substance.class);

        assertEquals(1, actualTransporter.relationships.size());

        storedWithRel.relationships.remove(2);

        api.updateSubstanceJson(new SubstanceBuilder(storedWithRel).buildJson());

        Substance actualInhibitor = api.fetchSubstanceObjectByUuid(storedInhibitor.getUuid().toString(), Substance.class);

        assertEquals(2, actualInhibitor.relationships.size());
        assertFalse(actualInhibitor.relationships.stream()
                .filter(r -> r.type.equals("INHIBITOR -> TRANSPORTER"))
                .findAny()
                .isPresent());

        Substance modifiedTransporter = api.fetchSubstanceObjectByUuid(storedTransporter.getUuid().toString(), Substance.class);
        assertEquals(Collections.emptyList(), modifiedTransporter.relationships);


    }

    @Test
    public void removeRelationshipWithActiveMoeityGsrs587FromOtherSide(){

        Substance parent = new SubstanceBuilder()
                                .addName("parent")
                                .generateNewUUID()
                                .build();

        api.submitSubstance(parent);

        Substance inhibitor = new SubstanceBuilder()
                .addName("inhibitor")
                .addActiveMoiety()

                .build();

        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .build();

        JsonNode json = api.submitSubstance(inhibitor);

        Substance storedInhibitor = SubstanceBuilder.from(json).build();

        Substance storedTransporter =SubstanceBuilder.from(api.submitSubstance(transporter)).build();



        JsonNode withRel = new SubstanceBuilder(storedInhibitor)
                .addRelationshipTo(storedTransporter, "INHIBITOR -> TRANSPORTER")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();

        SubstanceBuilder.from(api.fetchSubstanceJsonByUuid(parent.getUuid()))
                .addRelationshipTo(inhibitor, "INFRASPECIFIC->PARENT ORGANISM")
                .buildJsonAnd( js -> api.updateSubstanceJson(js));


        Substance actualTransporter = api.fetchSubstanceObjectByUuid(storedTransporter.getUuid().toString(), Substance.class);

        assertEquals(1, actualTransporter.relationships.size());

        actualTransporter.relationships.remove(0);

        api.updateSubstanceJson(new SubstanceBuilder(actualTransporter).buildJson());

        Substance actualInhibitor = api.fetchSubstanceObjectByUuid(storedInhibitor.getUuid().toString(), Substance.class);

        assertFalse(actualInhibitor.relationships.stream()
                                        .filter(r -> r.type.equals("INHIBITOR -> TRANSPORTER"))
                                        .findAny()
                                        .isPresent());
//        assertEquals(actualInhibitor.getActiveMoieties(), actualInhibitor.relationships);

        Substance modifiedTransporter = api.fetchSubstanceObjectByUuid(storedTransporter.getUuid().toString(), Substance.class);
        assertEquals(Collections.emptyList(), modifiedTransporter.relationships);


    }

    @Test
    public void changeCommentOrRelationshipOnGeneratorSideShouldAlsoBeReflectedOnOtherSide(){
        Substance inhibitor = new SubstanceBuilder()
                .addName("inhibitor")
                .generateNewUUID()
                .build();


        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .generateNewUUID()
                .build();

        Substance storedTransporter = SubstanceBuilder.from(api.submitSubstance(transporter)).build();

        JsonNode withRel = SubstanceBuilder.from(api.submitSubstance(inhibitor))
                .addRelationshipTo(storedTransporter, "INHIBITOR->TRANSPORTER")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();


        assertEquals(Arrays.asList("INHIBITOR->TRANSPORTER"), storedWithRel.relationships.stream()
                                                                                .map(r-> r.type)
                                                                                 .collect(Collectors.toList()));

        Substance actualTransporter = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("TRANSPORTER->INHIBITOR"), actualTransporter.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));

        storedWithRel.relationships.get(0).comments= "BEAN ME UP SCOTTY";
        Substance storedWithRel2 = SubstanceBuilder.from(api.updateSubstanceJson(storedWithRel.toFullJsonNode())).build();


        assertEquals(Arrays.asList("BEAN ME UP SCOTTY"), storedWithRel2.relationships.stream()
                .map(r-> r.comments)
                .collect(Collectors.toList()));


        Substance actualTransporter2 = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("BEAN ME UP SCOTTY"), actualTransporter2.relationships.stream()
                .map(r-> r.comments)
                .collect(Collectors.toList()));


    }

    @Test
    public void changeCommentOnRelationshipOnNonGeneratorSideShouldAlsoBeReflectedOnOtherSide(){
        Substance inhibitor = new SubstanceBuilder()
                .addName("inhibitor")
                .generateNewUUID()
                .build();


        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .generateNewUUID()
                .build();

        Substance storedTransporter = SubstanceBuilder.from(api.submitSubstance(transporter)).build();

        JsonNode withRel = SubstanceBuilder.from(api.submitSubstance(inhibitor))
                .addRelationshipTo(storedTransporter, "INHIBITOR->TRANSPORTER")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();


        assertEquals(Arrays.asList("INHIBITOR->TRANSPORTER"), storedWithRel.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));

        Substance actualTransporter = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("TRANSPORTER->INHIBITOR"), actualTransporter.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));

        actualTransporter.relationships.get(0).comments= "BEAN ME UP SCOTTY";
        Substance storedWithRel2 = SubstanceBuilder.from(api.updateSubstanceJson(actualTransporter.toFullJsonNode())).build();


        assertEquals(Arrays.asList("BEAN ME UP SCOTTY"), storedWithRel2.relationships.stream()
                .map(r-> r.comments)
                .collect(Collectors.toList()));


        Substance actualInhibitor2 = api.fetchSubstanceObjectByUuid(inhibitor.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("BEAN ME UP SCOTTY"), actualInhibitor2.relationships.stream()
                .map(r-> r.comments)
                .collect(Collectors.toList()));


    }

    @Test
    public void changeTypeOnRelationshipOnGeneratorSideShouldAlsoBeReflectedOnOtherSide(){
        Substance inhibitor = new SubstanceBuilder()
                .addName("inhibitor")
                .generateNewUUID()
                .build();


        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .generateNewUUID()
                .build();

        Substance storedTransporter = SubstanceBuilder.from(api.submitSubstance(transporter)).build();

        JsonNode withRel = SubstanceBuilder.from(api.submitSubstance(inhibitor))
                .addRelationshipTo(storedTransporter, "INHIBITOR->TRANSPORTER")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();


        assertEquals(Arrays.asList("INHIBITOR->TRANSPORTER"), storedWithRel.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));

        Substance actualTransporter = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("TRANSPORTER->INHIBITOR"), actualTransporter.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));

        storedWithRel.relationships.get(0).type= "KIRK->TRANSPORTER";
        Substance storedWithRel2 = SubstanceBuilder.from(api.updateSubstanceJson(storedWithRel.toFullJsonNode())).build();


        assertEquals(Arrays.asList("KIRK->TRANSPORTER"), storedWithRel2.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));


        Substance actualTransporter2 = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("TRANSPORTER->KIRK"), actualTransporter2.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));


    }

    @Test
    public void changeTypeOnRelationshipOnNonGeneratorSideShouldAlsoBeReflectedOnOtherSide(){
        Substance inhibitor = new SubstanceBuilder()
                .addName("inhibitor")
                .generateNewUUID()
                .build();


        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .generateNewUUID()
                .build();

        Substance storedTransporter = SubstanceBuilder.from(api.submitSubstance(transporter)).build();

        JsonNode withRel = SubstanceBuilder.from(api.submitSubstance(inhibitor))
                .addRelationshipTo(storedTransporter, "INHIBITOR->TRANSPORTER")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();


        assertEquals(Arrays.asList("INHIBITOR->TRANSPORTER"), storedWithRel.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));

        Substance actualTransporter = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("TRANSPORTER->INHIBITOR"), actualTransporter.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));

        actualTransporter.relationships.get(0).type= "TRANSPORTER->KIRK";
        Substance storedWithRel2 = SubstanceBuilder.from(api.updateSubstanceJson(actualTransporter.toFullJsonNode())).build();


        assertEquals(Arrays.asList("TRANSPORTER->KIRK"), storedWithRel2.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));


        Substance actualInhibitor2 = api.fetchSubstanceObjectByUuid(inhibitor.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("KIRK->TRANSPORTER"), actualInhibitor2.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));


    }

    @Test
    public void multipleChangesAllOnOneSide(){
        Substance inhibitor = new SubstanceBuilder()
                .addName("inhibitor")
                .generateNewUUID()
                .build();


        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .generateNewUUID()
                .build();

        Substance storedTransporter = SubstanceBuilder.from(api.submitSubstance(transporter)).build();

        JsonNode withRel = SubstanceBuilder.from(api.submitSubstance(inhibitor))
                .addRelationshipTo(storedTransporter, "INHIBITOR->TRANSPORTER")
                .addRelationshipTo(storedTransporter, "SOMETHING->DIFFERENT")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();


        //using TreeSets so they are sorted because
        // the relationships can be stored or come back in any order...
        assertEquals(setOf("INHIBITOR->TRANSPORTER","SOMETHING->DIFFERENT"), storedWithRel.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toCollection(TreeSet::new)));

        Substance actualTransporter = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(setOf("DIFFERENT->SOMETHING", "TRANSPORTER->INHIBITOR" ), actualTransporter.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toCollection(TreeSet::new)));

        storedWithRel.relationships.get(0).comments= "BEAN ME UP SCOTTY";
        storedWithRel.relationships.remove(1);
        Substance storedWithRel2 = SubstanceBuilder.from(api.updateSubstanceJson(storedWithRel.toFullJsonNode())).build();


        assertEquals(Arrays.asList("BEAN ME UP SCOTTY"), storedWithRel2.relationships.stream()
                .map(r-> r.comments)
                .collect(Collectors.toList()));


        Substance actualTransporter2 = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("BEAN ME UP SCOTTY"), actualTransporter2.relationships.stream()
                .map(r-> r.comments)
                .collect(Collectors.toList()));


    }

    @Test
    public void relatedSubstanceAddedLaterShouldGetWaitingRelationshipAddedOnLoad() {
        Substance inhibitor = new SubstanceBuilder()
                .addName("inhibitor")
                .generateNewUUID()
                .build();


        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .generateNewUUID()
                .build();


        JsonNode withRel = SubstanceBuilder.from(api.submitSubstance(inhibitor))
                .addRelationshipTo(transporter, "INHIBITOR->TRANSPORTER")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();

        List<Relationship> relationships = storedWithRel.relationships;
        assertEquals(Arrays.asList(transporter.getOrGenerateUUID().toString()), relationships.stream()
                                                                            .map(r-> r.relatedSubstance.refuuid)
                                                                            .collect(Collectors.toList()));

        Substance storedTransporter = SubstanceBuilder.from(api.submitSubstance(transporter)).build();

        List<Relationship> otherRelationships = storedTransporter.relationships;
        assertEquals(Arrays.asList(inhibitor.getOrGenerateUUID().toString()), otherRelationships.stream()
                .map(r-> r.relatedSubstance.refuuid)
                .collect(Collectors.toList()));

    }

        private static <T> Set<T> setOf(T... ts){
        Set<T> s = new HashSet<>();
        for(T t : ts){
            s.add(t);
        }
        return s;
    }

    /**
     * This tests to make sure if for some reason
     * the first commit fails,
     * follow up attempts still work - our procesor's set of ids in progress handle rollbacks
     *
     */
    @Test
    public void rollbackFirstTryAllow2ndFixed() {
        Substance inhibitor = new SubstanceBuilder()
                .addName("inhibitor")
                .generateNewUUID()
                .build();


        Substance transporter = new SubstanceBuilder()
                .addName("transporter")
                .generateNewUUID()
                .build();

        Substance storedTransporter = SubstanceBuilder.from(api.submitSubstance(transporter)).build();

        JsonNode withRel = SubstanceBuilder.from(api.submitSubstance(inhibitor))
                .addRelationshipTo(storedTransporter, "INHIBITOR->TRANSPORTER")
                .buildJson();

        Substance storedWithRel = SubstanceBuilder.from(api.updateSubstanceJson(withRel)).build();


        assertEquals(Arrays.asList("INHIBITOR->TRANSPORTER"), storedWithRel.relationships.stream()
                .map(r -> r.type)
                .collect(Collectors.toList()));

        Substance actualTransporter = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("TRANSPORTER->INHIBITOR"), actualTransporter.relationships.stream()
                .map(r -> r.type)
                .collect(Collectors.toList()));


        ts.addEntityProcessor(Relationship.class, ProcessorThatFailsFirstTime.class );

        logout();

        ts.restart();

        login();

        storedWithRel.relationships.get(0).type= "KIRK->TRANSPORTER";
        JsonNode updatedJson = storedWithRel.toFullJsonNode();

        SubstanceJsonUtil.ensureFailure(api.updateSubstance(updatedJson));

        Substance storedWithRel2 = SubstanceBuilder.from(api.updateSubstanceJson(updatedJson)).build();


        assertEquals(Arrays.asList("KIRK->TRANSPORTER"), storedWithRel2.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));


        Substance actualTransporter2 = api.fetchSubstanceObjectByUuid(transporter.getUuid().toString(), Substance.class);

        assertEquals(Arrays.asList("TRANSPORTER->KIRK"), actualTransporter2.relationships.stream()
                .map(r-> r.type)
                .collect(Collectors.toList()));


    }

    /**
     * processor that is used in a test that will throw an exception
     * and therefore rollback the transaction the first time it's update method is called.
     */
    public static class ProcessorThatFailsFirstTime implements EntityProcessor<Relationship>{

        private boolean shouldFail=true;

        @Override
        public void preUpdate(Relationship obj) throws FailProcessingException {
            try {
                if(shouldFail) {
                    throw new FailProcessingException("supposed to fail");
                }
            }finally {
                //only fail the 1st time
                shouldFail = false;
            }
        }
    }

}
