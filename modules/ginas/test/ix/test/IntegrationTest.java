package ix.test;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {

	@Rule
	public GinasTestServer ts = new GinasTestServer(9001);


    @Test
    public void testRestAPISubstance() {
    	ts.run(new GinasTestServer.ServerWorker(){
			public void doWork() throws Exception {
				JsonNode substances = ts.urlJSON("http://localhost:9001/ginas/app/api/v1/substances");
                assertThat(!substances.isNull()).isEqualTo(true);
			}
    	});
    }
    
    
    @Test 
    public void testFakeUserSetup(){
    	ts.run(new GinasTestServer.ServerWorker(){
			public void doWork() throws Exception {
				ts.ensureSetupUsers();
			}
    	});
    }
    
    @Test
    public void testFakeUserLoginPassword(){
    	ts.run(new GinasTestServer.ServerWorker(){
			public void doWork() throws Exception {
				ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
            	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.USERNAME_PASSWORD);
            	
        		JsonNode profile=ts.whoamiJSON();
            	assertThat(profile.get("identifier").asText()).isEqualTo(ts.FAKE_USER_1);
			}
    	});
    }
    
    @Test
    public void testFakeUserLoginKey(){
    	ts.run(new GinasTestServer.ServerWorker(){
			public void doWork() throws Exception {
				ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
            	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.USERNAME_KEY);
            	
            	JsonNode profile = ts.whoamiJSON();
            	assertThat(profile.get("identifier").asText()).isEqualTo(ts.FAKE_USER_1);
            	
			}
    	});
    }
    

    @Test
    public void testFakeUserLoginToken(){
    	ts.run(new GinasTestServer.ServerWorker(){
			public void doWork() throws Exception {
				ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
            	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.TOKEN);
            	
            	JsonNode profile = ts.whoamiJSON();
            	assertThat(profile.get("identifier").asText()).isEqualTo(ts.FAKE_USER_1);
			}
    	});
    	
    }
    
    @Test
    public void testFakeUserLoginNone(){
    	ts.run(new GinasTestServer.ServerWorker(){
			public void doWork() throws Exception {
				ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
            	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.NONE);
            	ts.whoamiFail();
			}
    	});
    	
    }
    

    @Test
    public void testRestAPIVocabularies() {
    	ts.run(new GinasTestServer.ServerWorker(){
			public void doWork() throws Exception {
				JsonNode vacabs=ts.vocabulariesJSON();
        		JsonNode content=vacabs.at("/content");
        		assertTrue("There should be content found in CV response", !content.isNull());
        		assertTrue("There should be more than 0 CVs loaded, found (" +content.size() + ")",content.size()>=1);
        		assertTrue("There should be more than 0 CVs listed in total, found (" +vacabs.at("/total").asText() + ")",vacabs.at("/total").asInt()>=1);
			}
    	});
    }

}
