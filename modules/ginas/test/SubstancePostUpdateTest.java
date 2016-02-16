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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;


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

            running(testServer(9001), new Runnable() {
                public void run() {
                    try (InputStream is=new FileInputStream(resource);){
                        JsonNode js=null;
                        js = (new ObjectMapper()).readTree(is);
                        Logger.info("Running: " + resource);

                        WSResponse wsResponse1 = WS.url(SubstancePostUpdateTest.VALIDATE_URL).post(js).get(timeout);
                        assertThat(wsResponse1.getStatus()).isEqualTo(OK);
                        JsonNode jsonNode1 = wsResponse1.asJson();
                        assertThat(jsonNode1.get("valid").asBoolean()).isEqualTo(true);

                        //create
                        WSResponse wsResponse2 = WS.url(SubstancePostUpdateTest.API_URL).post(js).get(timeout);
                        assertThat(wsResponse2.getStatus()).isIn(200, 201);
                        JsonNode jsonNode2 = wsResponse2.asJson();
                      //  assertThat(!jsonNode2.isNull()).isEqualTo(true);

                       //search
                        WSResponse wsResponse3 = WS.url("http://localhost:9001/ginas/app/api/v1/substances(8798e4b8-223c-4d24-aeeb-1f3ca2914328)?view=full").get().get(timeout);
                        JsonNode jsonNode3 = wsResponse3.asJson();
                        //System.out.println("jsonNode:" + jsonNode3);
                        assertThat(wsResponse3.getStatus()).isEqualTo(OK);
                        assertThat(!jsonNode3.isNull()).isEqualTo(true);

                        //validate
                        WSResponse wsResponse4 = WS.url(SubstancePostUpdateTest.VALIDATE_URL).post(jsonNode3).get(timeout);
                        assertThat(wsResponse4.getStatus()).isEqualTo(OK);
                        JsonNode jsonNode4 = wsResponse4.asJson();
                        System.out.println("jsonNode:" + jsonNode4);
                        assertThat(jsonNode4.get("valid").asBoolean()).isEqualTo(true);

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
