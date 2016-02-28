package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

import play.Logger;
import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubstancePostUpdateTest {

	
        
        

        @Parameterized.Parameters(name="{1}")
        static public Collection<Object[]> findFiles(){
        	
            List<Object[]> myFilelist  =  new ArrayList<>();

            File folder=new File("test/testJSON/pass");

            assertTrue(folder.exists());
            for(File s:folder.listFiles()){
                if(s.getName().endsWith(".json")){
                    myFilelist.add(new Object[]{s, s.getName().replace(".", "")});
                }
            }
            return myFilelist;
        }

        File resource;
        @Rule
        public GinasTestServer ts = new GinasTestServer(9001);


        public SubstancePostUpdateTest(File f, String dummy){
            this.resource=f;
        }

        @Test
        public void testAPICreateSubstance() {
            ts.run(new Runnable() {
                public void run() {
                	ts.login(ts.FAKE_USER_1, ts.FAKE_PASSWORD_1);
                	ts.setAuthenticationType(GinasTestServer.AUTH_TYPE.TOKEN);
                    try (InputStream is=new FileInputStream(resource)){
                        JsonNode js= new ObjectMapper().readTree(is);
                        
                        String uuid=js.get("uuid").asText();
                        Logger.info("Running: " + resource);

                        WSResponse wsResponse1 = ts.validateSubstance(js);

                        assertEquals(OK, wsResponse1.getStatus());

                        JsonNode jsonNode1 = wsResponse1.asJson();
                        //System.out.println("VALID:" + jsonNode1.get("valid"));
                        //System.out.println(jsonNode1);
                        
                        //assertThat("initalialValid:" +jsonNode1.get("valid").asBoolean()).isEqualTo("initalialValid:true");
                        assertTrue(jsonNode1.get("valid").asBoolean());
                        //create
                        WSResponse wsResponse2 = ts.submitSubstance(js);
                        int status2 = wsResponse2.getStatus();
                        assertTrue(status2 == 200 || status2 == 201);
                        JsonNode jsonNode2 = wsResponse2.asJson();

                        //search
                        WSResponse wsResponse3 = ts.fetchSubstance(uuid);
                        JsonNode jsonNode3 = wsResponse3.asJson();
                        //System.out.println("jsonNode:" + jsonNode3);
                        assertEquals(OK, wsResponse3.getStatus());
                        assertFalse(jsonNode3.isNull());


                        
                        assertThatNonDestructive(js,jsonNode3);                        
                        
                        //validate
                        WSResponse wsResponse4 = ts.validateSubstance(jsonNode3);
                        //assertThat(wsResponse4.getStatus()).isEqualTo(OK);
                        assertEquals(OK, wsResponse4.getStatus());
                        JsonNode jsonNode4 = wsResponse4.asJson();
                       // System.out.println("jsonNode:" + jsonNode4);
                        //jp.
                       // assertThat("roundTripValid:" + jsonNode4.get("valid").asBoolean()).isEqualTo("roundTripValid:true");
                        assertTrue(jsonNode4.get("valid").asBoolean());
                    } catch (Exception e1) {
                        throw new IllegalStateException(e1);
                    }
                }
            });


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
            		   jn.get("path").asText().equals("/nucleicAcid/modifications") 
            		   ||
            		   jn.get("path").asText().contains("nameOrgs") ||		//silly hacks to allow workaround for above
            		   jn.get("path").asText().contains("domains")  ||
            		   (jn.get("path").asText().startsWith("/names/") &&
            	        jn.get("path").asText().contains("references") 
            		   		)
            			){
            			//acceptable removals, do nothing
            			
            		}else{
            			JsonNode jsbefore=before.at(jn.get("path").textValue());
            			//TODO check if jsbefore is equivalent to null in some way: 
            			// [], {}, "", [""]
            			if(jsbefore.toString().equals("[\"\"]")){
            				
            			}else{
//            				System.out.println("OLD:");
//            				System.out.println(before);
//            				System.out.println("NEW:");
//            				System.out.println(after);
            				throw new AssertionError("removed property at '" + jn.get("path") + "' , was '" + jsbefore+ "'");
            			}
            		}
            		//System.out.println("Error:" + jn + " was:" + before.at(jn.get("path").textValue()));
            	}
            }
        }

    }
