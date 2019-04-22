package ix.test;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.AbstractGinasServerTest;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;

public class CVSubmitTest extends AbstractGinasServerTest {


        private SubstanceAPI api;
        private RestSession session;

        @Before
        public void login(){

            session = ts.newRestSession(ts.getFakeUser1());
            api = new SubstanceAPI(session);
        }

        @After
        public void logout(){
            session.logout();
        }

        @Test
        public void testAPIValidateCVSubmit() throws Exception {
        	ObjectMapper om = new ObjectMapper();
        	String domain ="ADASDAS";
        	String raw="{\"domain\":\""+domain+"\",\"terms\":[]}";
        	JsonNode newCD=om.readTree(raw);
            JsonNode jsonNode1 = api.submitCVDomainJson(newCD);
            assertEquals(domain,jsonNode1.at("/domain").asText());
        }

        @Test
        public void testAPIValidateCVUpdate() throws Exception {


        	ObjectMapper om = new ObjectMapper();
        	String domain ="ADASDAS";
        	String raw="{\"domain\":\""+domain+"\",\"terms\":[]}";
        	JsonNode newCD=om.readTree(raw);
            JsonNode jsonNode1 = api.submitCVDomainJson(newCD);
            assertEquals(domain,jsonNode1.at("/domain").asText());

            String cvID=jsonNode1.at("/id").asText();

            JsonNode cv= api.fetchCVJsonByUuid(cvID);

            assertEquals(domain,cv.at("/domain").asText());

            ObjectNode ob = (ObjectNode)cv;
            ArrayNode an = om.createArrayNode();
            an.add(om.readTree("{\"value\":\"TERM1\",\"display\":\"TERM1 DISPLAY\"}"));
            ob.set("terms", an);

            api.updateCVDomainJson(ob);

            JsonNode cvAfterUpdate= api.fetchCVJsonByUuid(cvID);

            assertEquals("TERM1",cvAfterUpdate.at("/terms/0/value").asText());
            assertEquals("TERM1 DISPLAY",cvAfterUpdate.at("/terms/0/display").asText());


        }

        @Test
        public void testAPIValidateCVUpdateTwice() throws Exception {


        	ObjectMapper om = new ObjectMapper();
        	String domain ="ADASDAS";
        	String raw="{\"domain\":\""+domain+"\",\"terms\":[]}";
        	JsonNode newCD=om.readTree(raw);
            JsonNode jsonNode1 = api.submitCVDomainJson(newCD);
            assertEquals(domain,jsonNode1.at("/domain").asText());

            String cvID=jsonNode1.at("/id").asText();

            JsonNode cv= api.fetchCVJsonByUuid(cvID);

            assertEquals(domain,cv.at("/domain").asText());

            ObjectNode ob = (ObjectNode)cv;
            ArrayNode an = om.createArrayNode();
            an.add(om.readTree("{\"value\":\"TERM1\",\"display\":\"TERM1 DISPLAY\"}"));
            ob.set("terms", an);

            api.updateCVDomainJson(ob);

            JsonNode cvAfterUpdate= api.fetchCVJsonByUuid(cvID);

            assertEquals("TERM1",cvAfterUpdate.at("/terms/0/value").asText());
            assertEquals("TERM1 DISPLAY",cvAfterUpdate.at("/terms/0/display").asText());

            //second time
            ArrayNode nan = (ArrayNode) cvAfterUpdate.at("/terms");
            nan.add(om.readTree("{\"value\":\"TERM2\",\"display\":\"TERM2 DISPLAY\"}"));
            ((ObjectNode)cvAfterUpdate).set("terms", nan);
            api.updateCVDomainJson(cvAfterUpdate);
            JsonNode cvAfterUpdate2= api.fetchCVJsonByUuid(cvID);

            assertEquals("TERM2",cvAfterUpdate2.at("/terms/1/value").asText());
            assertEquals("TERM2 DISPLAY",cvAfterUpdate2.at("/terms/1/display").asText());

        }

        @Test
        public void testAPIValidateCVUpdateTwiceAndReorder() throws Exception {


        	ObjectMapper om = new ObjectMapper();
        	String domain ="ADASDAS";
        	String raw="{\"domain\":\""+domain+"\",\"terms\":[]}";
        	JsonNode newCD=om.readTree(raw);
            JsonNode jsonNode1 = api.submitCVDomainJson(newCD);
            assertEquals(domain,jsonNode1.at("/domain").asText());

            String cvID=jsonNode1.at("/id").asText();

            JsonNode cv= api.fetchCVJsonByUuid(cvID);

            assertEquals(domain,cv.at("/domain").asText());

            ObjectNode ob = (ObjectNode)cv;
            ArrayNode an = om.createArrayNode();
            an.add(om.readTree("{\"value\":\"TERM1\",\"display\":\"TERM1 DISPLAY\"}"));
            ob.set("terms", an);

            api.updateCVDomainJson(ob);

            JsonNode cvAfterUpdate= api.fetchCVJsonByUuid(cvID);

            assertEquals("TERM1",cvAfterUpdate.at("/terms/0/value").asText());
            assertEquals("TERM1 DISPLAY",cvAfterUpdate.at("/terms/0/display").asText());

            //second time
            ArrayNode nan = (ArrayNode) cvAfterUpdate.at("/terms");
            nan.add(om.readTree("{\"value\":\"TERM2\",\"display\":\"TERM2 DISPLAY\"}"));
            ((ObjectNode)cvAfterUpdate).set("terms", nan);
            api.updateCVDomainJson(cvAfterUpdate);
            JsonNode cvAfterUpdate2= api.fetchCVJsonByUuid(cvID);

            assertEquals("TERM2",cvAfterUpdate2.at("/terms/1/value").asText());
            assertEquals("TERM2 DISPLAY",cvAfterUpdate2.at("/terms/1/display").asText());



			ArrayNode anNew = om.createArrayNode();
			anNew.add(cvAfterUpdate2.at("/terms/1"));
			anNew.add(cvAfterUpdate2.at("/terms/0"));
			anNew.add(om.readTree(
					"{\"value\":\"TERM3\",\"display\":\"TERM3 DISPLAY\"}"));
			((ObjectNode) cvAfterUpdate2).set("terms", anNew);
			api.updateCVDomainJson(cvAfterUpdate2);

			Optional.of(api.fetchCVJsonByUuid(cvID)).ifPresent(json -> {
				assertEquals("TERM1", json.at("/terms/0/value").asText());
				assertEquals("TERM1 DISPLAY", json.at("/terms/0/display").asText());
				assertEquals("TERM2", json.at("/terms/1/value").asText());
				assertEquals("TERM2 DISPLAY", json.at("/terms/1/display").asText());
				assertEquals("TERM3", json.at("/terms/2/value").asText());
				assertEquals("TERM3 DISPLAY", json.at("/terms/2/display").asText());
			});

        }

        @Test
        public void testAPIValidateCVAddThenRemove() throws Exception {


        	ObjectMapper om = new ObjectMapper();
        	String domain ="ADASDAS";
        	String raw="{\"domain\":\""+domain+"\",\"terms\":[]}";
        	JsonNode newCD=om.readTree(raw);
            JsonNode jsonNode1 = api.submitCVDomainJson(newCD);
            assertEquals(domain,jsonNode1.at("/domain").asText());

            String cvID=jsonNode1.at("/id").asText();

            JsonNode cv= api.fetchCVJsonByUuid(cvID);

            assertEquals(domain,cv.at("/domain").asText());

            ObjectNode ob = (ObjectNode)cv;
            ArrayNode an = om.createArrayNode();
            an.add(om.readTree("{\"value\":\"TERM1\",\"display\":\"TERM1 DISPLAY\"}"));
            ob.set("terms", an);

            api.updateCVDomainJson(ob);

            JsonNode cvAfterUpdate= api.fetchCVJsonByUuid(cvID);

            assertEquals("TERM1",cvAfterUpdate.at("/terms/0/value").asText());
            assertEquals("TERM1 DISPLAY",cvAfterUpdate.at("/terms/0/display").asText());

            //second time
            //delete the terms (set to empty)
            ((ObjectNode)cvAfterUpdate).set("terms", om.createArrayNode());
            api.updateCVDomainJson(cvAfterUpdate);
            JsonNode cvAfterUpdate2= api.fetchCVJsonByUuid(cvID);

            assertEquals(0,((ArrayNode)cvAfterUpdate2.at("/terms")).size());
//            System.out.println("Navigate in browser");
//            while(true){
//            	Thread.sleep(10000);
//            }
        }



    }
