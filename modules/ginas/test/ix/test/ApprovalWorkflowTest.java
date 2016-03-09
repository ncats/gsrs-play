package ix.test;
import static org.junit.Assert.*;
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
		ts.run(new GinasTestServer.ServerWorker() {
            @Override
            public void doWork() throws Exception {
                try(GinasTestServer.UserSession session1 = ts.loginFakeUser1();
                    GinasTestServer.UserSession session2 = ts.loginFakeUser2();
                    InputStream is = new FileInputStream(resource)) {


                    JsonNode js = new ObjectMapper().readTree(is);

                    String uuid = js.get("uuid").asText();

                    JsonNode jsonNode2 = session1.submitSubstanceJSON(js);
                    assertEquals(jsonNode2.get("status").asText().toLowerCase(), Substance.STATUS_PENDING);

                    session1.approveSubstanceFail(uuid);


                    String approvalID;
                    JsonNode before = null;
                    JsonNode after = null;


                    //approval, CAN approve if different user
                    {
                        before = session2.approveSubstanceJSON(uuid);
                        assertTrue("Approval ID should not be null", before.get("approvalID") != null);
                        approvalID = before.get("approvalID").asText();
                    }


                    {
                        after = session2.fetchSubstanceJSON(uuid);
                        assertEquals(Substance.STATUS_APPROVED, after.get("status").asText().toLowerCase());
                        assertNotNull(after.get("approvalID"));
                        assertEquals(approvalID, after.get("approvalID").asText());

                        JsonNode jp = JsonDiff.asJson(before, after);
                        int changes = 0;
                        for (JsonNode jschange : jp) {
                            changes++;
                            System.out.println("CHANGED:" + jschange + " old: " + before.at(jschange.get("path").asText()));
                        }
                        assertTrue(changes == 0);
                    }
                }
                    

            }
        });

	}
	
	@Test
	public void testFailNonLoggedApprover() {
		final File resource=new File("test/testJSON/toapprove.json");
		ts.run(new GinasTestServer.ServerWorker() {
            @Override
            public void doWork() throws Exception {

                try(GinasTestServer.UserSession session = ts.loginFakeUser1()) {

                    InputStream is = new FileInputStream(resource);
                    JsonNode js = new ObjectMapper().readTree(is);
                    is.close();

                    String uuid = js.get("uuid").asText();
                    JsonNode jsonNode2 = session.submitSubstanceJSON(js);
                    assertEquals(jsonNode2.get("status").asText().toLowerCase(), Substance.STATUS_PENDING);
                    session.approveSubstanceFail(uuid);

                    session.logout();
                    session.approveSubstanceFail(uuid);
                }

            }
        });

	}
	
	@Test
	public void testFailDoubeApproved() {
		final File resource=new File("test/testJSON/toapprove.json");
		ts.run(new GinasTestServer.ServerWorker() {
            @Override
            public void doWork() throws Exception {

                try(GinasTestServer.UserSession session1 = ts.loginFakeUser1();
                    GinasTestServer.UserSession session2 = ts.loginFakeUser2();
                    InputStream is = new FileInputStream(resource)) {

                    JsonNode js = new ObjectMapper().readTree(is);


                    String uuid = js.get("uuid").asText();
                    JsonNode jsonNode2 = session1.submitSubstanceJSON(js);
                    assertEquals(jsonNode2.get("status").asText().toLowerCase(), Substance.STATUS_PENDING);

                    JsonNode before = session2.approveSubstanceJSON(uuid);
                    String approvalID1 = before.get("approvalID").asText();
                    assertNotNull("Approval ID should not be null", approvalID1);

                    session2.approveSubstanceFail(uuid);

                    session1.approveSubstanceFail(uuid);
                    JsonNode sub = session1.fetchSubstanceJSON(uuid);
                    String approvalID2 = before.get("approvalID").asText();

                    assertEquals(approvalID1, approvalID2);
                }

            }
        });

	}
	
	
	
}
