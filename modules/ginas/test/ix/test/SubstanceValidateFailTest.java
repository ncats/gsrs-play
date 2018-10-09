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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasClassServerTest;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.test.util.TestNamePrinter;
import util.json.JsonUtil;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubstanceValidateFailTest extends AbstractGinasClassServerTest {

    File resource;

    @Rule
    public TestNamePrinter printer = new TestNamePrinter().setPrintEnd(true);


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


   public SubstanceValidateFailTest(File f, String dummy){
    	this.resource=f;
    }


    @Test
    public void notValid() throws Exception {

        try(RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));

            SubstanceAPI.ValidationResponse wsResponse1 = api.validateSubstance(js);

            assertEquals(OK, wsResponse1.getHttpStatus());

            assertFalse(wsResponse1.isNull());
            assertFalse(wsResponse1.getMessages().toString(), wsResponse1.isValid());


        }catch(Throwable e){
        	e.printStackTrace();
        	throw e;
        }

    }
    

}
