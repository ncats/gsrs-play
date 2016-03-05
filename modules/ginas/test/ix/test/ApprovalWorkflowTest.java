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
                	ts.loginFakeUser1();
                	
                	InputStream is=new FileInputStream(resource);
                    JsonNode js= new ObjectMapper().readTree(is);
                    
                    String uuid=js.get("uuid").asText();
                    
                    JsonNode jsonNode2 = ts.submitSubstanceJSON(js);
                    assertEquals(jsonNode2.get("status").asText().toLowerCase(),Substance.STATUS_PENDING);
                    
                    ts.approveSubstanceFail(uuid);
                    
                    
                    String approvalID;
                    JsonNode before=null;
                    JsonNode after=null;
                    
                    
                    //approval, CAN approve if different user
                    {
                    	ts.loginFakeUser2();
	                    before = ts.approveSubstanceJSON(uuid);
	                    assertTrue("Approval ID should not be null",before.get("approvalID")!=null);
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
	
	@Test
	public void testFailNonLoggedApprover() {
		final File resource=new File("test/testJSON/toapprove.json");
		ts.run(new Runnable() {
            public void run() {
                try {
                	ts.loginFakeUser1();
                	
                	InputStream is=new FileInputStream(resource);
                	JsonNode js= new ObjectMapper().readTree(is);
                    is.close();
                    
                    String uuid=js.get("uuid").asText();
                    JsonNode jsonNode2 = ts.submitSubstanceJSON(js);
                    assertEquals(jsonNode2.get("status").asText().toLowerCase(),Substance.STATUS_PENDING);
                    ts.approveSubstanceFail(uuid);                    
                    ts.logout();
                    ts.approveSubstanceFail(uuid);
                    
                    
                } catch (Exception e1) {
                	e1.printStackTrace();
                    throw new IllegalStateException(e1);
                }
            }
        });

	}
	
	@Test
	public void testFailDoubeApproved() {
		final File resource=new File("test/testJSON/toapprove.json");
		ts.run(new Runnable() {
            public void run() {
                try {
                	ts.loginFakeUser1();
                	
                	InputStream is=new FileInputStream(resource);
                	JsonNode js= new ObjectMapper().readTree(is);
                    is.close();
                    
                    String uuid=js.get("uuid").asText();
                    JsonNode jsonNode2 = ts.submitSubstanceJSON(js);
                    assertEquals(jsonNode2.get("status").asText().toLowerCase(),Substance.STATUS_PENDING);
                    ts.loginFakeUser2();
                    JsonNode before = ts.approveSubstanceJSON(uuid);
                    String approvalID1=before.get("approvalID").asText();
                    assertTrue("Approval ID should not be null",approvalID1!=null);
                    
                    ts.approveSubstanceFail(uuid); 
                    //System.out.println("Same dude couldn't do it twice");
                    ts.loginFakeUser1();
                    System.out.println("");
                    ts.approveSubstanceFail(uuid);   
                    JsonNode sub=ts.fetchSubstanceJSON(uuid);
                    String approvalID2=before.get("approvalID").asText();
                    
                    assertEquals(approvalID1,approvalID2);
                    
                    
                } catch (Exception e1) {
                	e1.printStackTrace();
                    throw new IllegalStateException(e1);
                }
            }
        });

	}
	
	
	
}
