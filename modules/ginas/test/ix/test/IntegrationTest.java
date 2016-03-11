package ix.test;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {

	@Rule
	public GinasTestServer ts = new GinasTestServer(9001);


    @Test
    public void testRestAPISubstance() throws Exception {
        JsonNode substances = ts.getNotLoggedInSession().urlJSON("http://localhost:9001/ginas/app/api/v1/substances");
        assertTrue(!substances.isNull());
    }
    
    
    @Test 
    public void testFakeUserSetup(){
    	ts.ensureSetupUsers();

    }
    
    @Test
    public void testFakeUserLoginPassword() throws Exception {
        try(GinasTestServer.UserSession session = ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1)) {
            session.withPasswordAuth();

            assertEquals(GinasTestServer.FAKE_USER_1, session.whoamiUsername());
        }
    }
    
    @Test
    public void testFakeUserLoginKey() throws Exception {
        try(GinasTestServer.UserSession session = ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1)) {
            session.withKeyAuth();

            assertEquals(GinasTestServer.FAKE_USER_1, session.whoamiUsername());
        }
    }
    

    @Test
    public void testFakeUserLoginToken() throws Exception {
        try(GinasTestServer.UserSession session = ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1)) {
            session.withTokenAuth();

            assertEquals(GinasTestServer.FAKE_USER_1, session.whoamiUsername());
        }
    	
    }
    
    @Test
    public void testFakeUserLoginNone() throws Exception {
        try(GinasTestServer.UserSession session = ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1,
                GinasTestServer.AUTH_TYPE.NONE)) {
            session.whoamiFail();
        }
    	
    }
    

    @Test
    public void testRestAPIVocabularies()  throws Exception {
        try(GinasTestServer.UserSession session = ts.getNotLoggedInSession()) {
            JsonNode vacabs = session.vocabulariesJSON();
            JsonNode content = vacabs.at("/content");
            assertTrue("There should be content found in CV response", !content.isNull());
            assertTrue("There should be more than 0 CVs loaded, found (" + content.size() + ")", content.size() >= 1);
            assertTrue("There should be more than 0 CVs listed in total, found (" + vacabs.at("/total").asText() + ")", vacabs.at("/total").asInt() >= 1);
        }
    }
    

}
