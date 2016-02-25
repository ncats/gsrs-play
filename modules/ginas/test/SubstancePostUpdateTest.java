import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.test.TestServer;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubstancePostUpdateTest {

	
        private static final String VALIDATE_URL = "http://localhost:9001/ginas/app/api/v1/substances/@validate";
        private static final String API_URL = "http://localhost:9001/ginas/app/api/v1/substances";
        private static long timeout= 10000L;;

        @Parameterized.Parameters(name="{1}")
        static public Collection<Object[]> findFiles(){
        	
            List<Object[]> myFilelist  =  new ArrayList<Object[]>();

            File folder=null;
            try{
                folder = new File("test/testJSON/pass");
            }catch(Exception e){

                e.printStackTrace();
                throw new IllegalStateException(e);
            }
            assertTrue(folder.exists());
            for(File s:folder.listFiles()){
                if(s.getName().endsWith(".json")){
                    myFilelist.add(new Object[]{s, s.getName().replace(".", "")});
                }
            }
            return myFilelist;
        }

        File resource;
        public SubstancePostUpdateTest(File f, String dummy){
            this.resource=f;
        }

        @Test
        public void testAPICreateSubstance() {
        	TestServer ts=testServer(9001);
            running(ts, new Runnable() {
                public void run() {
                    try (InputStream is=new FileInputStream(resource);){
                        JsonNode js=null;
                        js = (new ObjectMapper()).readTree(is);
                        
                        String uuid=js.get("uuid").asText();
                        Logger.info("Running: " + resource);

                        WSResponse wsResponse1 = WS.url(SubstancePostUpdateTest.VALIDATE_URL).post(js).get(timeout);
                        assertThat(wsResponse1.getStatus()).isEqualTo(OK);
                        JsonNode jsonNode1 = wsResponse1.asJson();
                        System.out.println("VALID:" + jsonNode1.get("valid"));
                        System.out.println(jsonNode1);
                        
                        assertThat("initalialValid:" +jsonNode1.get("valid").asBoolean()).isEqualTo("initalialValid:true");

                        //create
                        WSResponse wsResponse2 = WS.url(SubstancePostUpdateTest.API_URL).post(js).get(timeout);
                        assertThat(wsResponse2.getStatus()).isIn(200, 201);
                        JsonNode jsonNode2 = wsResponse2.asJson();
                        //assertThat(!jsonNode2.isNull()).isEqualTo(true);

                        //search
                        WSResponse wsResponse3 = WS.url("http://localhost:9001/ginas/app/api/v1/substances(" + uuid + ")?view=full").get().get(timeout);
                        JsonNode jsonNode3 = wsResponse3.asJson();
                        //System.out.println("jsonNode:" + jsonNode3);
                        assertThat(wsResponse3.getStatus()).isEqualTo(OK);
                        assertThat(!jsonNode3.isNull()).isEqualTo(true);

                        
                        assertThatNonDestructive(js,jsonNode3);                        
                        
                        //validate
                        WSResponse wsResponse4 = WS.url(SubstancePostUpdateTest.VALIDATE_URL).post(jsonNode3).get(timeout);
                        assertThat(wsResponse4.getStatus()).isEqualTo(OK);
                        JsonNode jsonNode4 = wsResponse4.asJson();
                        System.out.println("jsonNode:" + jsonNode4);
                        //jp.
                        assertThat("roundTripValid:" + jsonNode4.get("valid").asBoolean()).isEqualTo("roundTripValid:true");

                    } catch (Exception e1) {
                        throw new IllegalStateException(e1);
                    }
                }
            });

            stop(ts);
        }
        
        /**
         * Ideally this method would actually fail when there is a destructive change between the two
         * JSON objects. However, the current implementation uses JSONPatch, which is specifically attempting
         * just to give instructions on how to turn JSON a into JSON b using a few operations.
         * 
         * The problem is, we don't consider reordering of list items to be destructive. We consider
         * those to be "move" operations. It turns out that the server will sometimes reorder
         * names, for example, which is allowed and expected.The implementation of JSONPatch
         * we use though doesn't do the heavy-lifting of finding if a "move" is more appropriate.
         * 
         * 
         * So, for example:
         * 
         * JSON a:
         * [
         * {
         * 		"name":"myname1"
         * },
         * {
         * 		"name":"myname2",
         * 		"type":"type2"
         * }
         * ]
         * 
         * JSON b:
         * [
         * {
         * 		"name":"myname2",
         * 		"type":"type2"
         * },
         * {
         * 		"name":"myname1"
         * }
         * ]
         *  
         *  
         * This implementation is likely to call a->b destructive, because may say 4 operations
         * have happened:
         * 
         * 1. "/0/name", "change", "myname2"
         * 2. "/0/type", "add", "type2"
         * 3. "/1/name", "change", "myname1"
         * 4. "/1/type", "remove", null 
         * 
         * TODO: We should fix this to allow any list/array to change the ordering
         * 
         * @param before
         * @param after
         * @throws AssertionError
         */
        public static void assertThatNonDestructive(JsonNode before, JsonNode after) throws AssertionError{
        	JsonNode jp =JsonDiff.asJson(before,after);
            for(JsonNode jn:jp){
            	
            	if(jn.get("op").asText().equals("remove")){
            		if(jn.get("path").asText().equals("/protein/modifications") ||
            		   jn.get("path").asText().equals("/nucleicAcid/modifications") ||
            		   jn.get("path").asText().contains("nameOrgs") ||
            		   jn.get("path").asText().contains("domains") 
            				){
            			//acceptable removals, do nothing
            			
            		}else{
            			throw new AssertionError("removed property at '" + jn.get("path") + "' , was '" + before.at(jn.get("path").textValue())+ "'");
            		}
            		//System.out.println("Error:" + jn + " was:" + before.at(jn.get("path").textValue()));
            	}
            }
        }

        @AfterClass
        public static void tearDown(){
            // Stop the server
            // stop(testServer);
        }
    }
