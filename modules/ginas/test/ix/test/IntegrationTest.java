package ix.test;
import static org.junit.Assert.*;

import ix.core.models.Role;
import ix.test.ix.test.server.ControlledVocab;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class IntegrationTest {

	@Rule
	public GinasTestServer ts = new GinasTestServer(9001);


    @Test
    public void testRestAPISubstance() throws Exception {
        JsonNode substances = ts.notLoggedInRestSession().urlJSON("http://localhost:9001/ginas/app/api/v1/substances");
        assertFalse( SubstanceJsonUtil.isLiteralNull(substances));
    }


    @Test
    public void ensureSetupUsers() throws Exception{
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){

            assertEquals(ts.getFakeUser1().getUserName(), session.whoAmIJson().get("identifier").asText());
        }

    }
    
    @Test
    public void testFakeUserLoginPassword() throws Exception {
        GinasTestServer.User user = ts.createUser(Role.DataEntry);
        try(RestSession session = ts.newRestSession(user, RestSession.AUTH_TYPE.USERNAME_PASSWORD)){

            assertEquals(user.getUserName(), session.whoAmIJson().get("identifier").asText());
        }
    }
    
    @Test
    public void testFakeUserLoginKey() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1(), RestSession.AUTH_TYPE.USERNAME_KEY)){

            assertEquals(ts.getFakeUser1().getUserName(), session.whoAmIJson().get("identifier").asText());
        }
    }
    

    @Test
    public void loginToken() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1(), RestSession.AUTH_TYPE.TOKEN)){

            assertEquals(ts.getFakeUser1().getUserName(), session.whoAmIJson().get("identifier").asText());
        }
    	
    }
    
    @Test
    public void notPassingCredentialsShouldFailRestCalls() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1(), RestSession.AUTH_TYPE.NONE)){
            SubstanceJsonUtil.ensureFailure(session.whoAmI());
        }

    }
    

    @Test
    public void testRestAPIVocabularies()  throws Exception {
        ControlledVocab cv = ts.notLoggedInRestSession().getControlledVocabulary();
        int total = cv.getTotalCount();
        int loaded = cv.getLoadedCount();

        assertTrue("There should be more than 0 CVs loaded, found (" + loaded + ")", loaded >= 1);
        assertTrue("There should be more than 0 CVs listed in total, found " + total, total >=1);


    }
    

}
