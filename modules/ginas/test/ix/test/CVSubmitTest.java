package ix.test;
import static org.junit.Assert.assertTrue;

import ix.test.util.TestNamePrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;

public class CVSubmitTest {

    @Rule
    public TestNamePrinter printer = new TestNamePrinter();
    ;
        
        

        @Rule
        public GinasTestServer ts = new GinasTestServer();


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
        	String raw="{\"domain\":\"ADASDAS\",\"terms\":[]}";
        	JsonNode newCD=om.readTree(raw);
            JsonNode jsonNode1 = api.submitCVDomainJson(newCD);
            System.out.println(jsonNode1);
        }
    }
