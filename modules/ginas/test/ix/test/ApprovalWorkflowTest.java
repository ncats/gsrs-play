package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

import ix.ginas.models.v1.Substance;
import play.Logger;
import play.libs.ws.WSResponse;

public class ApprovalWorkflowTest {
	

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    

    
	@Test
	public void testApprovalRoundTrip() {
		final File resource=new File("test/testJSON/toapprove.json");
		ts.run(new Runnable() {
            public void run() {
                try {
                	ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
                	
                	InputStream is=new FileInputStream(resource);
                    JsonNode js= new ObjectMapper().readTree(is);
                    
                    String uuid=js.get("uuid").asText();
                    
                    JsonNode jsonNode2 = ts.submitSubstanceJSON(js);
                    assertEquals(jsonNode2.get("status").asText().toLowerCase(),Substance.STATUS_PENDING);
                    
                    //approval, can't approve if same user
                    {
	                    WSResponse wsResponse3 = ts.approveSubstance(uuid);
	                    assertEquals(400, wsResponse3.getStatus());
	                    
                    }
                    String approvalID;
                    JsonNode before=null;
                    JsonNode after=null;
                    
                    //approval, CAN approve if different user
                    {
	                    ts.login(GinasTestServer.FAKE_USER_2, GinasTestServer.FAKE_PASSWORD_2);
	                    before = ts.approveSubstanceJSON(uuid);
	                    assertFalse(null == before.get("approvalID"));
	                    approvalID=before.get("approvalID").asText();
                    }
                    
                   
                    {
                    	after = ts.fetchSubstanceJSON(uuid);
	                    assertEquals(Substance.STATUS_APPROVED,after.get("status").asText().toLowerCase());
	                    assertFalse(null == after.get("approvalID"));
	                    assertEquals(approvalID,after.get("approvalID").asText());
	                    
	                    JsonNode jp = JsonDiff.asJson(before,after);
	                    int changes=0;
	                    for(JsonNode jschange: jp){
	                    	changes++;
	                    	System.out.println("CHANGED:" + jschange + " old: " + before.at(jschange.get("path").asText()));
	                    }
	                    assertTrue(changes==0);
                    }
                    
                    
                } catch (Exception e1) {
                	e1.printStackTrace();
                    throw new IllegalStateException(e1);
                }
            }
        });

	}
	
	
	
}
