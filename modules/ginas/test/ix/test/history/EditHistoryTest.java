package ix.test.history;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.models.Role;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;
import ix.test.AbstractGinasServerTest;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;

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
    				
    				
    				assertEquals(
    					Arrays.asList("/lastEdited","/version"),
	    				StreamSupport.stream(jsn.spliterator(), false)
	    	             	.filter(n->n.at("/op").asText().equals("replace"))
	    	             	.map(n->n.at("/path").asText())
	    	             	.collect(Collectors.toList())
    	             	);
    				
    				assertEquals(
        					Arrays.asList("/names/-"),
    	    				StreamSupport.stream(jsn.spliterator(), false)
    	    	             	.filter(n->n.at("/op").asText().equals("add"))
    	    	             	.map(n->n.at("/path").asText())
    	    	             	.collect(Collectors.toList())
        	             	);
    			});
        }
    	
	}
    
    
    
}
