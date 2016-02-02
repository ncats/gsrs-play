import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ix.core.models.Payload;
import ix.core.plugins.PayloadPlugin;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasSDFUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import play.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import play.test.FakeRequest;
import play.test.TestBrowser;
import play.test.WithBrowser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

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
            Substance sub = new Substance();
            sub.uuid = UUID.fromString("8798e4b8-223c-4d24-aeeb-1f3ca2914328");
            sub.approvalID = "7X1DH96Q9D";
            sub.status = "approved";

            Name test = new Name();
            test.name = "SELENOASPIRINE";
            test.preferred = true;
            sub.names.add(test);

            subNode = Json.toJson(sub);
          //  start(testServer);
        }

    @Test
    public void testRestAPISubstance() {
        running(testServer(9001), new Runnable() {
            public void run() {
                WSResponse wsResponse1 = WS.url("http://localhost:9001/ginas/app/api/v1/substances").get().get(timeout);
                JsonNode jsonNode1 = wsResponse1.asJson();
                assertThat(wsResponse1.getStatus()).isEqualTo(OK);
                assertThat(wsResponse1.getStatus()).isEqualTo(200);
                assertThat(!jsonNode1.isNull()).isEqualTo(true);
            }
        });

        stop(testServer(9001));
    }

     /*
    @Test
    public void runInBrowser() {
        running(testServer(3331), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3331");
                assertThat(browser.$("#title").getText()).isEqualTo("Welcome to Play!");
                browser.$("a").click();
                assertThat(browser.url()).isEqualTo("http://localhost:3333/login");
            }
        });
    }
  */

 /*   @Test
    public void testBrowserConcept() throws Exception {
        running(testServer(9001), HTMLUNIT , new F.Callback<TestBrowser>() {

            @Override
            public void invoke(TestBrowser browser) throws Throwable{
                browser.goTo("http://localhost:9001/ginas/app");
                assertThat(!browser.$("title").isEmpty());
*//*
                WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
                final HtmlPage page =
                        (HtmlPage)webClient.getPage("http://localhost:9001/ginas/app/wizard?kind=concept");
                webClient.getOptions().setThrowExceptionOnScriptError(false);
*//*

               //assertThat(page.getBody()..contains("geetha"));
            }
        });
    }*/

  /*  @Test
    public void testJsonLoad() throws Exception {

    running(fakeApplication(), new Runnable() {
        public void run() {

            PayloadPlugin payloadPlugin = Play.application().plugin(
                    PayloadPlugin.class);

            FileInputStream fs = null;
            try {
                fs = new FileInputStream("test/testdumps/aspirinsetsmall.txt.gz");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Payload  sdpayload = null;
            try {
                sdpayload = payloadPlugin.createPayload("aspirinsetsmall.txt.gz",
                            "application/gzip", fs.toString() );
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (sdpayload != null) {
                sdpayload.save();
                Map<String, GinasSDFUtils.GinasSDFExtractor.FieldStatistics> m = GinasSDFUtils.GinasSDFExtractor
                        .getFieldStatistics(sdpayload, 100);
                assertThat(m.values() != null);
            }
        }
    });
    }*/

    @AfterClass
    public static void tearDown(){
        // Stop the server
       // stop(testServer);
    }
}
