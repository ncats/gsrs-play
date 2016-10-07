package ix.test.history;

import static org.junit.Assert.*;

import static ix.test.SubstanceJsonUtil.ensureFailure;
import static ix.test.SubstanceJsonUtil.ensurePass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.models.Role;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.test.util.TestNamePrinter;
import util.json.JsonUtilTest;

public class EditHistoryTest {
	public final static String INVALID_APPROVAL_ID="0000000001";

    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + getClass().getCanonicalName() + " . " + description.getMethodName());
        }
    };
    
    
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
