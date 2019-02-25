package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.flipkart.zjsonpatch.JsonDiff;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.AbstractGinasServerTest;
import ix.core.models.Role;
import ix.ginas.models.v1.Substance;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import util.json.JsonUtil.JsonNodeBuilder;

public class ApprovalWorkflowTest  extends AbstractGinasServerTest {
	public final static String VALID_APPROVAL_ID="0000000000";
	public final static String INVALID_APPROVAL_ID="0000000001";
    
	@Test
	public void testApprovalRoundTrip() throws Exception {
        String uuid;
        final File resource=new File("test/testJSON/toapprove.json");
        try(RestSession session = ts.newRestSession(ts.getFakeUser1());
            InputStream is = new FileInputStream(resource)){


                JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(new ObjectMapper().readTree(is));

                uuid = js.get("uuid").asText();

                SubstanceAPI api = new SubstanceAPI(session);

                JsonNode jsonNode2 = api.submitSubstanceJson(js);
                assertEquals(Substance.STATUS_PENDING, SubstanceJsonUtil.getApprovalStatus(jsonNode2));

                SubstanceJsonUtil.ensureFailure(api.approveSubstance(uuid));
            }


        try(RestSession session2 = ts.newRestSession(ts.createUser(Role.Approver))){
            String approvalID;
            JsonNode before = null;
            JsonNode after = null;

            SubstanceAPI api2 = new SubstanceAPI(session2);
            //approval, CAN approve if different user

                before = api2.approveSubstanceJson(uuid);
                approvalID = SubstanceJsonUtil.getApprovalId(before);



                after = api2.fetchSubstanceJsonByUuid(uuid);
                assertEquals(Substance.STATUS_APPROVED, SubstanceJsonUtil.getApprovalStatus(after));
                assertEquals(approvalID, SubstanceJsonUtil.getApprovalId(after));

                JsonNode jp = JsonDiff.asJson(before, after);
                int changes = 0;
                for (JsonNode jschange : jp) {
                    changes++;
                    System.out.println("CHANGED:" + jschange + " old: " + before.at(jschange.get("path").asText()));
                }
                assertEquals(0,changes);

        }

	}
	@Test
	public void testNonAdminCantChangeApprovalID() throws Exception {
        String uuid;
        final File resource=new File("test/testJSON/toapprove.json");
        try(RestSession session = ts.newRestSession(ts.createUser(Role.DataEntry, Role.SuperDataEntry));
        		InputStream is = new FileInputStream(resource)){


                JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(new ObjectMapper().readTree(is));
                uuid = js.get("uuid").asText();
                SubstanceAPI api = new SubstanceAPI(session);
                JsonNode jsonNode2 = api.submitSubstanceJson(js);
                assertEquals(Substance.STATUS_PENDING, SubstanceJsonUtil.getApprovalStatus(jsonNode2));
            }


        try(RestSession session2 = ts.newRestSession(ts.createUser(Role.SuperDataEntry, Role.SuperUpdate, Role.Approver))){
            String approvalID;
            JsonNode before = null;
            JsonNode after = null;

            SubstanceAPI api2 = new SubstanceAPI(session2);
            //approval, CAN approve if different user

                before = api2.approveSubstanceJson(uuid);
                approvalID = SubstanceJsonUtil.getApprovalId(before);



                after = api2.fetchSubstanceJsonByUuid(uuid);
                assertEquals(Substance.STATUS_APPROVED, SubstanceJsonUtil.getApprovalStatus(after));
                assertEquals(approvalID, SubstanceJsonUtil.getApprovalId(after));

                JsonNode withChangedApprovalID= new JsonNodeBuilder(after).set("/approvalID", VALID_APPROVAL_ID).build();
                SubstanceJsonUtil.ensureFailure(api2.updateSubstance(withChangedApprovalID));
        }

	}
	@Test
	public void testAdminCanChangeApprovalID() throws Exception {
        String uuid;
        final File resource=new File("test/testJSON/toapprove.json");
        try(RestSession session = ts.newRestSession(ts.getFakeUser1());
        		InputStream is = new FileInputStream(resource)){


                JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(new ObjectMapper().readTree(is));
                uuid = js.get("uuid").asText();
                SubstanceAPI api = new SubstanceAPI(session);
                JsonNode jsonNode2 = api.submitSubstanceJson(js);
                assertEquals(Substance.STATUS_PENDING, SubstanceJsonUtil.getApprovalStatus(jsonNode2));
            }


        try(RestSession session2 = ts.newRestSession(ts.createUser(Role.Approver, Role.SuperUpdate))){
            String approvalID;
            JsonNode before = null;
            JsonNode after = null;

            SubstanceAPI api2 = new SubstanceAPI(session2);
            //approval, CAN approve if different user

                before = api2.approveSubstanceJson(uuid);
                approvalID = SubstanceJsonUtil.getApprovalId(before);



                after = api2.fetchSubstanceJsonByUuid(uuid);
                assertEquals(Substance.STATUS_APPROVED, SubstanceJsonUtil.getApprovalStatus(after));
                assertEquals(approvalID, SubstanceJsonUtil.getApprovalId(after));

                JsonNode withChangedApprovalID= new JsonNodeBuilder(after).set("/approvalID", VALID_APPROVAL_ID).build();
                SubstanceJsonUtil.ensureFailure(api2.updateSubstance(withChangedApprovalID));
        }
        try(RestSession session3 = ts.newRestSession(ts.createAdmin("adminguy", "nonsense"))){
            
            	SubstanceAPI api3 = new SubstanceAPI(session3);
            	//approval, CAN approve if different user
                JsonNode after = api3.fetchSubstanceJsonByUuid(uuid);
                JsonNode withChangedApprovalID= new JsonNodeBuilder(after).set("/approvalID", VALID_APPROVAL_ID).build();
                SubstanceJsonUtil.ensurePass(api3.updateSubstance(withChangedApprovalID));
                JsonNode changed = api3.fetchSubstanceJsonByUuid(uuid);
                assertEquals(changed.at("/approvalID").asText(),VALID_APPROVAL_ID);
        }
	}
	@Test
	public void testAdminCannotChangeApprovalIDToInvalid() throws Exception {
        String uuid;
        final File resource=new File("test/testJSON/toapprove.json");
        try(RestSession session = ts.newRestSession(ts.createUser(Role.SuperDataEntry));
        		InputStream is = new FileInputStream(resource)){


                JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(new ObjectMapper().readTree(is));
                uuid = js.get("uuid").asText();
                SubstanceAPI api = new SubstanceAPI(session);
                JsonNode jsonNode2 = api.submitSubstanceJson(js);
                assertEquals(Substance.STATUS_PENDING, SubstanceJsonUtil.getApprovalStatus(jsonNode2));
            }


        try(RestSession session2 = ts.newRestSession(ts.createUser(Role.Approver))){
            String approvalID;
            JsonNode before = null;
            JsonNode after = null;

            SubstanceAPI api2 = new SubstanceAPI(session2);

                before = api2.approveSubstanceJson(uuid);
                approvalID = SubstanceJsonUtil.getApprovalId(before);
                after = api2.fetchSubstanceJsonByUuid(uuid);
                assertEquals(Substance.STATUS_APPROVED, SubstanceJsonUtil.getApprovalStatus(after));
                assertEquals(approvalID, SubstanceJsonUtil.getApprovalId(after));
        }
        try(RestSession session3 = ts.newRestSession(ts.createAdmin("adminguy", "nonsense"))){
            
            	SubstanceAPI api3 = new SubstanceAPI(session3);
            	//approval, CAN approve if different user
                JsonNode after = api3.fetchSubstanceJsonByUuid(uuid);
                JsonNode withChangedApprovalID= new JsonNodeBuilder(after).set("/approvalID", INVALID_APPROVAL_ID).build();
                SubstanceJsonUtil.ensureFailure(api3.updateSubstance(withChangedApprovalID));
        }
	}
	
    @Test
    public void testAdminCanUnapproveSubstance() throws Exception {
        String uuid;
        final File resource=new File("test/testJSON/toapprove.json");
        try(RestSession session = ts.newRestSession(ts.getFakeUser1());
            InputStream is = new FileInputStream(resource)){


            JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(new ObjectMapper().readTree(is));
            uuid = js.get("uuid").asText();
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode jsonNode2 = api.submitSubstanceJson(js);
            assertEquals(Substance.STATUS_PENDING, SubstanceJsonUtil.getApprovalStatus(jsonNode2));
        }


        try(RestSession session2 = ts.newRestSession(ts.createUser(Role.Approver, Role.SuperUpdate))){
            String approvalID;
            JsonNode before = null;
            JsonNode after = null;

            SubstanceAPI api2 = new SubstanceAPI(session2);
            //approval, CAN approve if different user

            before = api2.approveSubstanceJson(uuid);
            approvalID = SubstanceJsonUtil.getApprovalId(before);



            after = api2.fetchSubstanceJsonByUuid(uuid);
            assertEquals(Substance.STATUS_APPROVED, SubstanceJsonUtil.getApprovalStatus(after));
            assertEquals(approvalID, SubstanceJsonUtil.getApprovalId(after));

            JsonNode withChangedApprovalID= new JsonNodeBuilder(after).set("/approvalID", VALID_APPROVAL_ID).build();
            SubstanceJsonUtil.ensureFailure(api2.updateSubstance(withChangedApprovalID));
        }
        try(RestSession session3 = ts.newRestSession(ts.createAdmin("adminguy", "nonsense"))){

            SubstanceAPI api3 = new SubstanceAPI(session3);
            //approval, CAN approve if different user
            JsonNode after = api3.fetchSubstanceJsonByUuid(uuid);
            JsonNode unapproved= SubstanceJsonUtil.prepareUnapprovedPublic(after);
            SubstanceJsonUtil.ensurePass(api3.updateSubstance(unapproved));
            JsonNode changed = api3.fetchSubstanceJsonByUuid(uuid);
            assertTrue(changed.at("/approvalID").isMissingNode());
        }
    }

	@Test
	public void testFailNonLoggedApprover() throws Exception {
        final File resource=new File("test/testJSON/toapprove.json");
        try(RestSession session = ts.newRestSession(ts.getFakeUser1());
            InputStream is = new FileInputStream(resource)) {

            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(new ObjectMapper().readTree(is));

            String uuid = js.get("uuid").asText();

            JsonNode jsonNode2 = api.submitSubstanceJson(js);
            assertEquals(Substance.STATUS_PENDING, SubstanceJsonUtil.getApprovalStatus(jsonNode2));

            JsonNode failResponse = SubstanceJsonUtil.ensureFailure(api.approveSubstance(uuid));


            session.logout();
            SubstanceJsonUtil.ensureFailure(api.approveSubstance(uuid));
        }

	}
	
	@Test
	public void testFailDoubeApproved()  throws Exception {
        final File resource=new File("test/testJSON/toapprove.json");
        try(RestSession session = ts.newRestSession(ts.createUser(Role.SuperDataEntry, Role.SuperUpdate, Role.Approver));
            RestSession session2 = ts.newRestSession(ts.createUser(Role.SuperDataEntry, Role.SuperUpdate, Role.Approver));
            InputStream is = new FileInputStream(resource)) {

            JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(new ObjectMapper().readTree(is));
            SubstanceAPI api = new SubstanceAPI(session);
            SubstanceAPI api2 = new SubstanceAPI(session2);

            String uuid = js.get("uuid").asText();
            JsonNode jsonNode2 = api.submitSubstanceJson(js);
            assertEquals(Substance.STATUS_PENDING, SubstanceJsonUtil.getApprovalStatus(jsonNode2));

            JsonNode before = api2.approveSubstanceJson(uuid);
            String approvalID1 = SubstanceJsonUtil.getApprovalId(before);
            assertNotNull("Approval ID should not be null", approvalID1);

            JsonNode failResponse = SubstanceJsonUtil.ensureFailure(api2.approveSubstance(uuid));

            System.out.println(failResponse.toString());
            assertTrue("Expected to find JSON with a message for not approving", failResponse.at("/message").asText().contains("approve an approved substance"));


            SubstanceJsonUtil.ensureFailure(api.approveSubstance(uuid));
            JsonNode sub = api.fetchSubstanceJsonByUuid(uuid);
            String approvalID2 = SubstanceJsonUtil.getApprovalId(sub);

            assertEquals(approvalID1, approvalID2);
        }

	}
	
	
	
}
