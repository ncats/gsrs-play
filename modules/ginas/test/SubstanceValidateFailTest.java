import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
    	
    	File folder=null;
    	try{
    		folder = new File("test/testJSON/fail");
    		System.out.println("found:" + folder.getAbsolutePath());
    		
    	}catch(Exception e){
    		
    		e.printStackTrace();
    		throw new IllegalStateException(e);
    	}
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
    
    @AfterClass
    public static void tearDown(){
        // Stop the server
       // stop(testServer);
    }
}
