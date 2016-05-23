package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.diff.JsonDiff;

import util.json.JsonUtil;


import static ix.test.SubstanceJsonUtil.*;

@RunWith(Parameterized.class)
public class SubstanceValidSubmitTest {

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + getClass().getName() + " . " + description.getMethodName());
        }
    };
        
        

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
        public GinasTestServer ts = new GinasTestServer();

        public SubstanceValidSubmitTest(File f, String onlyUsedForParameterName){
            this.resource=f;
        }

        private SubstanceAPI api;
        private RestSession session;

        @Before
        public void login(){
            session = ts.newRestSession(ts.getFakeUser1());

            api = new SubstanceAPI(session);
        }

        @After
        public void logout(){
            session.logout();
        }

        @Test
        public void testAPIValidateSubstance() throws Exception {

            JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
            JsonNode jsonNode1 = api.validateSubstanceJson(js);
            assertTrue(jsonNode1.get("valid").asBoolean());
        }
        @Test
        public void testAPIValidateSubmitSubstance()  throws Exception {

            JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
            JsonNode jsonNode1 = api.validateSubstanceJson(js);

            SubstanceJsonUtil.ensureIsValid(jsonNode1);

            ensurePass(api.submitSubstance(js));
        }
        @Test
        public void testAPIValidateSubmitFetchSubstance()   throws Exception {

            JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
            String uuid = js.get("uuid").asText();
            JsonNode validationResult = api.validateSubstanceJson(js);

            SubstanceJsonUtil.ensureIsValid(validationResult);

            ensurePass(api.submitSubstance(js));

            JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);

            assertFalse(SubstanceJsonUtil.isLiteralNull(fetched));

            assertThatNonDestructive(js, fetched);

        }
        @Test
        public void validateFetchedSubmittedSubstance()  throws Exception {


            JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));

            String uuid = js.get("uuid").asText();

            JsonNode validationResult = api.validateSubstanceJson(js);

            SubstanceJsonUtil.ensureIsValid(validationResult);


            ensurePass(api.submitSubstance(js));

            JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);

            assertFalse(SubstanceJsonUtil.isLiteralNull(fetched));

            //System.out.println("about to test if it's destructive");
            assertThatNonDestructive(js, fetched);

            //validate
            JsonNode fetchedValidationResult = api.validateSubstanceJson(fetched);
            SubstanceJsonUtil.ensureIsValid(fetchedValidationResult);



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