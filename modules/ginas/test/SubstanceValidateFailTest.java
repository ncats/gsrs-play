import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.test.WithApplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubstanceValidateFailTest extends WithApplication {

    private static final String VALIDATE_URL = "http://localhost:9001/ginas/app/api/v1/substances/@validate";
	private static long timeout= 10000L;;
    
    @Parameters(name="{1}")
    static public Collection<Object[]> findstuff(){
    	List<Object[]> mylist  =  new ArrayList<Object[]>();
    	
    	File folder = new File(SubstanceValidateFailTest.class.getResource("testJSON/fail").getFile());
    	assertTrue(folder.exists());
    	for(File s:folder.listFiles()){
    		if(s.getName().endsWith(".json")){
    			mylist.add(new Object[]{s, s.getName()});
    		}
    	}
    	return mylist;
    }

    File resource;
    public SubstanceValidateFailTest(File f, String dummy){
    	this.resource=f;
    }
        
    @Test
    public void testAPIValidateSubstance() {
    	    	
        running(testServer(9001), new Runnable() {
            public void run() {
				try (InputStream is=new FileInputStream(resource);){
					JsonNode js=null;
					js = (new ObjectMapper()).readTree(is);
	            	System.out.println("Running: " + resource);
	                WSResponse wsResponse1 = WS.url(SubstanceValidateFailTest.VALIDATE_URL).post(js).get(timeout);
	                JsonNode jsonNode1 = wsResponse1.asJson();
	                assertThat(wsResponse1.getStatus()).isEqualTo(OK);
	                assertThat(!jsonNode1.isNull()).isEqualTo(true);
	                assertThat(jsonNode1.get("valid").asBoolean()).isEqualTo(false);

				} catch (Exception e1) {
					throw new IllegalStateException(e1);
				}             	
            }
        });
        
        stop(testServer(9001));
    }
    


    

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
