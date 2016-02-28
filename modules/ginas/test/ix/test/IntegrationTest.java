import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.test.TestServer;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {

        private static long timeout;

        @BeforeClass
        public static void setUp() {
            timeout = 10000L;
            // Dummy Objects
            /*
            Substance sub = new Substance();
            sub.uuid = UUID.fromString("8798e4b8-223c-4d24-aeeb-1f3ca2914328");
            sub.approvalID = "7X1DH96Q9D";
            sub.status = "approved";

            Name test = new Name();
            test.name = "SELENOASPIRINE";
            test.preferred = true;
            sub.names.add(test);

            subNode = Json.toJson(sub);
            */
          //  start(testServer);
        }
        @Rule
        public GinasTestServer ts = new GinasTestServer(9001);

       

    @Test
    public void testRestAPISubstance() {
    	ts.run(new Runnable() {
            public void run() {
            	try{
                WSResponse wsResponse1 = ts.url("http://localhost:9001/ginas/app/api/v1/substances").get().get(timeout);
                assertThat(wsResponse1.getStatus()).isEqualTo(OK);
                assertThat(wsResponse1.getStatus()).isEqualTo(200);
                JsonNode jsonNode1 = wsResponse1.asJson();
                assertThat(!jsonNode1.isNull()).isEqualTo(true);
            	}catch(Exception e){
            		throw e;
            	}
            }
        });

    }
    
    @Test 
    public void testFakeUserSetup(){
    	ts.run(new Runnable() {
            public void run() {
            	try{
            		ts.ensureSetupUsers();
            	}catch(Exception e){
            		throw e;
            	}
            }
        });
    }
    
    @Test
    public void testFakeUserLoginPassword(){
    	ts.run(new Runnable() {
            public void run() {
            	try{
            		ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
                	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.USERNAME_PASSWORD);
                	
            		WSResponse wsResponse1 = ts.whoami();
            		JsonNode jsn=wsResponse1.asJson();
                	assertThat(jsn.get("identifier").asText()).isEqualTo(ts.FAKE_USER_1);
                	
            	}catch(Exception e){
            		throw e;
            	}
            }
        });
    	//ts.logout();
    	
    }
    
    @Test
    public void testFakeUserLoginKey(){
    	ts.run(new Runnable() {
            public void run() {
            	try{
            		ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
                	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.USERNAME_KEY);
                	
            		WSResponse wsResponse1 = ts.whoami();
            		JsonNode jsn=wsResponse1.asJson();
                	assertThat(jsn.get("identifier").asText()).isEqualTo(ts.FAKE_USER_1);
                	
            	}catch(Exception e){
            		throw e;
            	}
            }
        });
    	//ts.logout();
    	
    }
    

    @Test
    public void testFakeUserLoginToken(){
    	ts.run(new Runnable() {
            public void run() {
            	try{
            		ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
                	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.TOKEN);
                	
            		WSResponse wsResponse1 = ts.whoami();
            		System.out.println(wsResponse1.getBody());
            		JsonNode jsn=wsResponse1.asJson();
                	assertThat(jsn.get("identifier").asText()).isEqualTo(ts.FAKE_USER_1);
                	
            	}catch(Exception e){
            		throw e;
            	}
            }
        });
    	//ts.logout();
    	
    }
    
    @Test
    public void testFakeUserLoginNone(){
    	ts.run(new Runnable() {
            public void run() {
            	try{
            		ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1);
                	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.NONE);
                	
            		WSResponse wsResponse1 = ts.whoami();
            		if(wsResponse1.getStatus()==OK){
                      JsonNode jsn=wsResponse1.asJson();
              		  assertThat(jsn.get("identifier").asText()).isNotEqualTo(ts.FAKE_USER_1);
            		}
                	
            	}catch(Exception e){
            		throw e;
            	}
            }
        });
    	//ts.logout();
    	
    }
    

    @Test
    public void testRestAPIVocabularies() {
    	ts.run(new Runnable() {
            public void run() {
            	try{
                WSResponse wsResponse = WS.url("http://localhost:9001/ginas/app/api/v1/vocabularies").get().get(timeout);
                JsonNode jsonNode = wsResponse.asJson();
                assertThat(wsResponse.getStatus()).isEqualTo(OK);
                assertThat(wsResponse.getStatus()).isEqualTo(200);
                assertThat(!jsonNode.isNull()).isEqualTo(true);
                assertThat(jsonNode.path("total").asInt()).isGreaterThan(1);
            	}catch(Exception e){
            		throw e;
            	}
            }
        });
    }

    @AfterClass
    public static void tearDown(){
        // Stop the server
       // stop(testServer);
    }
}
