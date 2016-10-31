package ix.test;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.AbstractGinasServerTest;
import ix.test.server.GinasTestServer;
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
    }
