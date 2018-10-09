package ix.test.history;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import ix.test.server.JsonHistoryResult;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasServerTest;
import ix.core.models.Role;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import util.json.Changes;
import util.json.ChangesBuilder;
import util.json.JsonUtil;
import util.json.JsonUtilTest;

public class EditHistoryTest  extends AbstractGinasServerTest {
	public final static String INVALID_APPROVAL_ID="0000000001";
    
    
	@Test
	public void testRecordHistoryEditCanProduceDiff() throws Exception {
    	try(RestSession session = ts.newRestSession(ts.createUser(Role.SuperDataEntry,
    															  Role.SuperUpdate))){
    			SubstanceAPI api2 = new SubstanceAPI(session);
    			UUID uuid = UUID.randomUUID();
    			
    			new SubstanceBuilder()
    				.addName("Concept Name")
    				.setUUID(uuid)
    				.buildJsonAnd(c->{
    					ensurePass(api2.submitSubstance(c));
    				});
    			
    			JsonNode asEntered=api2.fetchSubstanceJsonByUuid(uuid.toString());
    			Substance old=SubstanceBuilder.from(asEntered).build();
    			
    			SubstanceBuilder.from(asEntered)
    				.addName("another name")
    				.buildJsonAnd(c->{
    					ensurePass(api2.updateSubstance(c));
    				});
    			
    			SubstanceFactory.getSubstance(uuid).getEdits().stream().forEach(p->{
    				JsonNode jsn=p.getDiff();
    				assertNotNull(jsn);
					Changes actualChanges;
					try {
						actualChanges = JsonUtil.computeChangesFromEdit(p);
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}

    				
					Changes expectedChanges = new ChangesBuilder(p.getOldValue().rawJson(), p.getNewValue().rawJson())
													.replace("/lastEdited")
													.replace("/version")
							                        .added("/names/1/displayName")
													.build();
    				
//					System.out.println("actual changes = " + actualChanges);

					Changes missingFrom = expectedChanges.missingFrom(actualChanges);
					assertTrue(missingFrom.toString() +"\n expected = " + expectedChanges + "\nactual = " + actualChanges, missingFrom.isEmpty());

//					assertEquals(
//    					Arrays.asList("/lastEdited","/version"),
//	    				StreamSupport.stream(jsn.spliterator(), false)
//	    	             	.filter(n->n.at("/op").asText().equals("replace"))
//	    	             	.map(n->n.at("/path").asText())
//	    	             	.collect(Collectors.toList())
//    	             	);
//
//    				assertEquals(
//        					Arrays.asList("/names/-"),
//    	    				StreamSupport.stream(jsn.spliterator(), false)
//    	    	             	.filter(n->n.at("/op").asText().equals("add"))
//    	    	             	.map(n->n.at("/path").asText())
//    	    	             	.collect(Collectors.toList())
//        	             	);
    			});
        }
    	
	}

	@Test
	public void approvingASubstanceMakesAEditForThePreviousVersion() throws Exception {
		UUID uuid = UUID.randomUUID();
		try(RestSession session = ts.newRestSession(ts.createUser(Role.SuperDataEntry,
				Role.SuperUpdate))){
			SubstanceAPI api2 = new SubstanceAPI(session);

			new SubstanceBuilder()
					.asChemical()
					.setStructure("C1CCCCC1CCCCO")
					.addName("Chemical Name")
					.setUUID(uuid)
					.buildJsonAnd(c->{
						ensurePass(api2.submitSubstance(c));
					});
		}

		try(RestSession session = ts.newRestSession(ts.createUser(Role.SuperDataEntry,
				Role.SuperUpdate, Role.Approver))){
			SubstanceAPI api2 = new SubstanceAPI(session);

			api2.approveSubstance(uuid.toString());
			JsonNode afterApprove=api2.fetchSubstanceJsonByUuid(uuid.toString());
			assertEquals("2", afterApprove.get("version").asText());

			JsonHistoryResult oldVersion = api2.fetchSubstanceJsonByUuid(uuid.toString(),1);

			assertEquals("1", oldVersion.getHistoryNode().get("version").asText());

		}


	}
    
    
    
}
