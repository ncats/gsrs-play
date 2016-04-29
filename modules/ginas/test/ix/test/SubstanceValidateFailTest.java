package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.JsonNode;

import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import play.libs.ws.WSResponse;
import play.test.WithApplication;
import util.json.JsonUtil;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubstanceValidateFailTest extends WithApplication {



    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + getClass().getCanonicalName() + " . " + description.getMethodName());
        }
        
        @Override
        protected void finished(Description description) {
            System.out.println("Ending test: " + getClass().getCanonicalName() + " . " + description.getMethodName());
        }
    };

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
    public GinasTestServer ts = new GinasTestServer();

   public SubstanceValidateFailTest(File f, String dummy){
    	this.resource=f;
    }


    @Test
    public void notValid() throws Exception {

        try(RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode js = SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));

            WSResponse wsResponse1 = api.validateSubstance(js);
            JsonNode jsonNode1 = wsResponse1.asJson();
            assertEquals(OK, wsResponse1.getStatus());

            assertFalse(SubstanceJsonUtil.isLiteralNull(jsonNode1));
            assertFalse(SubstanceJsonUtil.isValid(jsonNode1));


        }

    }
    

}
