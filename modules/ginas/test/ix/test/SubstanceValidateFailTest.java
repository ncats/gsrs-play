package ix.test;
import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.test.TestServer;
import play.test.WithApplication;
import play.test.TestServer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubstanceValidateFailTest extends WithApplication {

    private static final String VALIDATE_URL = "http://localhost:9001/ginas/app/api/v1/substances/@validate";
	private static long timeout= 10000L;
    
    @Parameters(name="{1}")
    static public Collection<Object[]> findstuff(){
    	List<Object[]> mylist  =  new ArrayList<Object[]>();
    	
    	File folder= new File("test/testJSON/fail");

    	assertTrue(folder.exists());
    	for(File s:folder.listFiles()){
    		if(s.getName().endsWith(".json")){
    			mylist.add(new Object[]{s, s.getName().replace(".", "")});
    		}
    	}
    	return mylist;
    }

    File resource;

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);


    public SubstanceValidateFailTest(File f, String dummy){
    	this.resource=f;
    }


    @Test
    public void testAPIValidateSubstance() {
    	ts.run(new GinasTestServer.ServerWorker() {
            @Override
            public void doWork() throws Exception {

                try (InputStream is = new FileInputStream(resource)) {
                    ts.loginFakeUser1();
                    JsonNode js = new ObjectMapper().readTree(is);
                    Logger.info("Running: " + resource);
                    WSResponse wsResponse1 = ts.validateSubstance(js);
                    JsonNode jsonNode1 = wsResponse1.asJson();
                    assertEquals(OK, wsResponse1.getStatus());
                    assertFalse(jsonNode1.isNull());
                    assertFalse(jsonNode1.get("valid").asBoolean());
                    ts.logout();
                }
            }

        });

    }
    

}
