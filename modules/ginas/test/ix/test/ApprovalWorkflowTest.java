import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.test.TestServer;

public class ApprovalWorkflowTest {
	private static final String API_URL = "http://localhost:9001/ginas/app/api/v1/substances";
    
    private static long timeout;
   
    static play.api.Application app;

    @BeforeClass
    public static void setUp() {
        timeout = 10000L;
       
    }

    //Need logging in test
    //TODO: work into DK's framework
//	
//	@Test
//	public void testApprovalRoundTrip() {
//		final File resource=new File("test/testJSON/toapprove.json");
//		TestServer ts=testServer(9001);
//        running(ts, new Runnable() {
//            public void run() {
//                try (InputStream is=new FileInputStream(resource)){
//                    JsonNode js= new ObjectMapper().readTree(is);
//                    
//                    String uuid=js.get("uuid").asText();
//                    Logger.info("Running: " + resource);
//
//                    WSResponse wsResponse2 = WS.url(ApprovalWorkflowTest.API_URL).post(js).get(timeout);
//                    int status2 = wsResponse2.getStatus();
//                    assertTrue(status2 == 200 || status2 == 201);
//                    JsonNode jsonNode2 = wsResponse2.asJson();
//
//                    //search
//                    WSResponse wsResponse3 = WS.url("http://localhost:9001/ginas/app/api/v1/substances(" + uuid + ")/@approve").get().get(timeout);
//                    System.out.println(wsResponse3.toString());
//                    JsonNode jsonNode3 = wsResponse3.asJson();
//                    assertEquals(OK, wsResponse3.getStatus());
//                    assertFalse(jsonNode3.isNull());
//
//
//                                         
//                } catch (Exception e1) {
//                    throw new IllegalStateException(e1);
//                }
//            }
//        });
//
//        stop(ts);
//	}
	
	
	@AfterClass
	public static void tearDown(){
	    // Stop the server
	   // stop(testServer);
	}
}
