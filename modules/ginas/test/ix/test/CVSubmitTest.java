package ix.test;
import static org.junit.Assert.assertEquals;

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



    }
