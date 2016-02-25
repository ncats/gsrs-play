import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.test.TestServer;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {

        private static long timeout;
        // Test Server
       // private static TestServer testServer = testServer(3332, fakeApplication(inMemoryDatabase()));
        private static JsonNode subNode;

        static play.api.Application app;

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

    @Test
    public void testRestAPISubstance() {
    	TestServer ts=testServer(9001);
        running(ts, new Runnable() {
            public void run() {
                WSResponse wsResponse1 = WS.url("http://localhost:9001/ginas/app/api/v1/substances").get().get(timeout);
                JsonNode jsonNode1 = wsResponse1.asJson();
                assertThat(wsResponse1.getStatus()).isEqualTo(OK);
                assertThat(wsResponse1.getStatus()).isEqualTo(200);
                assertThat(!jsonNode1.isNull()).isEqualTo(true);
            }
        });

        stop(ts);
    }

    @Test
    public void testRestAPIVocabularies() {
    	TestServer ts=testServer(9001);
        running(ts, new Runnable() {
            public void run() {
                WSResponse wsResponse = WS.url("http://localhost:9001/ginas/app/api/v1/vocabularies").get().get(timeout);
                JsonNode jsonNode = wsResponse.asJson();
                assertThat(wsResponse.getStatus()).isEqualTo(OK);
                assertThat(wsResponse.getStatus()).isEqualTo(200);
                assertThat(!jsonNode.isNull()).isEqualTo(true);
                assertThat(jsonNode.path("total").asInt()).isGreaterThan(1);
            }
        });

        stop(ts);
    }

    @AfterClass
    public static void tearDown(){
        // Stop the server
       // stop(testServer);
    }
}
