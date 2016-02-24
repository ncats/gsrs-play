import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import play.test.TestServer;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

import com.github.fge.jsonpatch.diff.JsonDiff;
import com.github.fge.jsonpatch.JsonPatch;

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

                        JsonNode jp =JsonDiff.asJson(js,jsonNode3);

                        for(JsonNode jn:jp){
                        	System.out.println(jn.get("op") + "\t" + jn.get("path") + "\t" + jn.get("value") );	
                        }
                        
                        
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

        @AfterClass
        public static void tearDown(){
            // Stop the server
            // stop(testServer);
        }
    }
