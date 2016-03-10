package ix.test;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import com.typesafe.config.ConfigFactory;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.controllers.v1.RouteFactory;
import ix.ginas.utils.validation.Validation;
import ix.ncats.controllers.auth.Authentication;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import net.sf.ehcache.CacheManager;
import org.junit.rules.ExternalResource;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.test.TestServer;

/**
 * JUnit Rule to handle starting and stopping
 * a Ginas Server around each @Test method.
 *
 * <p>
 *     Example usage:
 *
 *     <pre>
 *         @Rule
 *         GinasTestServer ts = new GinasTestServer(9001);
 *
 *         ...
 *
 *         @Test
 *         public void myTest(){
 *             ts.run(new Callable<Void>() {
 *
 *                public Void call(){
 *                 //do stuff
 *                 return null;
 *             });
 *         }
 *
 *         @Test
 *         public void withJava8Lambda(){
 *             ts.run( () -> {
 *                //do stuff
 *                });
 *         }
 *     </pre>
 *
 *
 * </p>
 *
 *
 *
 *
 * Created by katzelda on 2/25/16.
 */
public class GinasTestServer extends ExternalResource{
	 private static final String API_URL_USERFETCH = "http://localhost:9001/ginas/app/api/v1/whoami";
	 private static final String API_URL_VALIDATE = "http://localhost:9001/ginas/app/api/v1/substances/@validate";
	 private static final String API_URL_SUBMIT = "http://localhost:9001/ginas/app/api/v1/substances";
	 private static final String API_URL_FETCH = "http://localhost:9001/ginas/app/api/v1/substances($UUID$)?view=full";
	 private static final String API_URL_HISTORY = "http://localhost:9001/ginas/app/api/v1/substances($UUID$)/@edits";
	 
	 private static final String API_URL_APPROVE = "http://localhost:9001/ginas/app/api/v1/substances($UUID$)/@approve";
	 private static final String API_URL_UPDATE = "http://localhost:9001/ginas/app/api/v1/substances";
	 
	 private static final String API_URL_MAKE_FAKE_USERS="http://localhost:9001/ginas/app/api/v1/@deleteme";
     private static final String API_URL_WHOAMI="http://localhost:9001/ginas/app/api/v1/whoami";
     
     
     private static final String UI_URL_SUBSTANCE="http://localhost:9001/ginas/app/substance/$ID$";
     private static final String UI_URL_SUBSTANCE_VERSION="http://localhost:9001/ginas/app/substance/$ID$/v/$VERSION$";
     private static final String API_CV_LIST="http://localhost:9001/ginas/app/api/v1/vocabularies";
     
     
	 public static final String FAKE_USER_1="fakeuser1";
	 public static final String FAKE_USER_2="fakeuser2";
	 public static final String FAKE_PASSWORD_1="madeup1";
	 public static final String FAKE_PASSWORD_2="madeup2";
	 
	 
    private static long timeout= 10000L;

    private UserSession defaultSession = new NotLoggedInSession();


    private List<UserSession> sessions = new ArrayList<>();

    private TestServer ts;
    private int port;

    
    public enum AUTH_TYPE{
    	USERNAME_PASSWORD,
    	USERNAME_KEY,
    	TOKEN,
    	NONE
    }

    public GinasTestServer(int port){
       this.port = port;
       
    }


    public UserSession loginFakeUser1(){
    	return login(FAKE_USER_1,FAKE_PASSWORD_1);
    }
    public UserSession loginFakeUser2(){
    	return login(FAKE_USER_2,FAKE_PASSWORD_2);
    }

    //logs in user, also sets default authentication type
    //if previously set to NONE
    public UserSession login(String username, String password){
        
    	return login(username, password, AUTH_TYPE.USERNAME_PASSWORD);
    }

    public UserSession login(String username, String password, AUTH_TYPE type){
        ensureSetupUsers();
        UserSession session= new UserSession(username, password, type);

        sessions.add(session);
        return session;
    }



    
    private WSRequestHolder  url(String url){
    	return defaultSession.url(url);
    }


    
    public void ensureSetupUsers(){
			    	WSResponse wsResponse1 = url(API_URL_MAKE_FAKE_USERS).get().get(timeout);
			    	assertThat(wsResponse1.getStatus()).isEqualTo(OK);
			        assertThat(wsResponse1.getStatus()).isEqualTo(200);
			    	JsonNode jsonNode1 = wsResponse1.asJson();
			    	assertThat(jsonNode1.get(0).get("identifier").asText()).isEqualTo(GinasTestServer.FAKE_USER_1);
			    	assertThat(jsonNode1.get(1).get("identifier").asText()).isEqualTo(GinasTestServer.FAKE_USER_2);
            
    }



    @Override
    protected void before() throws Throwable {
        deleteH2Db();
        //This cleans out the old eh-cache
        //and forces us to use a new one with each test
        CacheManager.getInstance().shutdown();

        ts = testServer(port);
        ts.start();
        initializeControllers();
    }

    private void initializeControllers() {


        Validation.init();
        AdminFactory.init();
        RouteFactory.init();
        Authentication.init();

        IxDynamicResourceHandler.init();

        UserProfileFactory.init();
        PrincipalFactory.init();

    }

    private void deleteH2Db() throws IOException {
        Path path = new File(ConfigFactory.load().getString("ix.home")).toPath();
        if(!path.toFile().exists()){
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {


            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                //we've have NFS problems where there are lock
                //objects that we can't delete
                //should be safe to keep them and delete every other file.
                if(!file.toFile().getName().startsWith(".nfs")){
                    //use new delete method which throws IOException
                    //if it can't delete instead of returning flag
                    //so we will know the reason why it failed.
                    Files.delete(file);
                }


                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    protected void after() {
        for(UserSession session : sessions){
            session.logout();
        }
        sessions.clear();

        stop(ts);
    }


    public UserSession getNotLoggedInSession(){
        return defaultSession;
    }
	

    public static class JsonHistoryResult{
        private final JsonNode historyNode;

        private final JsonNode oldValue, newValue;

        public JsonHistoryResult(JsonNode historyNode, JsonNode oldValue, JsonNode newValue) {
            this.historyNode = historyNode;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public JsonNode getHistoryNode() {
            return historyNode;
        }

        public JsonNode getOldValue() {
            return oldValue;
        }

        public JsonNode getNewValue() {
            return newValue;
        }
    }


    public static class UserSession implements Closeable {
        private boolean loggedIn;
        private String username;
        private String password;
        private String key;
        private String token;
        private long deadtime=0;

        private AUTH_TYPE authType=AUTH_TYPE.NONE;

        public UserSession(){
            //null values for defaults
        }
        public UserSession(String username, String password, AUTH_TYPE type){
            Objects.requireNonNull(username);
            Objects.requireNonNull(password);
            Objects.requireNonNull(type);

            loggedIn=true;
            this.username=username;
            this.password=password;
            this.authType=type;

        }
        private void setAuthenticationType(AUTH_TYPE atype){
            this.authType=atype;
        }

        public UserSession withTokenAuth(){
            this.setAuthenticationType(AUTH_TYPE.TOKEN);
            return this;
        }
        public UserSession withKeyAuth(){
            this.setAuthenticationType(AUTH_TYPE.USERNAME_KEY);
            return this;
        }
        public UserSession withPasswordAuth(){
            this.setAuthenticationType(AUTH_TYPE.USERNAME_PASSWORD);
            return this;
        }


        public void logout(){
            //TODO actually log out
            loggedIn = false;

            this.username=null;
            this.password=null;
            this.key=null;
            this.token=null;
            this.deadtime=0;
        }

        public WSRequestHolder  url(String url){
            WSRequestHolder ws = WS.url(url);
            switch(authType){
                case TOKEN:
                    refreshTokenIfNeccesarry();
                    ws.setHeader("auth-token", this.token);
                    break;
                case USERNAME_KEY:
                    refreshTokenIfNeccesarry();
                    ws.setHeader("auth-username", this.username);
                    ws.setHeader("auth-key", this.key);
                    break;
                case USERNAME_PASSWORD:
                    ws.setHeader("auth-username", this.username);
                    ws.setHeader("auth-password", this.password);
                    break;
                default:
                    break;
            }
            return ws;
        }

        private void refreshTokenIfNeccesarry(){
            if(System.currentTimeMillis()>this.deadtime){
                refreshAuthInfoByUserNamePassword();
            }
        }


        private void refreshAuthInfoByUserNamePassword(){
            WSRequestHolder  ws = WS.url(API_URL_USERFETCH);
            ws.setHeader("auth-username", this.username);
            ws.setHeader("auth-password", this.password);
            WSResponse wsr=ws.get().get(timeout);
            JsonNode userinfo=wsr.asJson();
            token=userinfo.get("computedToken").asText();
            key=userinfo.get("key").asText();
            deadtime=System.currentTimeMillis()+userinfo.get("tokenTimeToExpireMS").asLong();


        }

        @Override
        public void close() throws IOException {
           logout();
        }


        public WSResponse validateSubstance(JsonNode js){
            return this.url(API_URL_VALIDATE).post(js).get(timeout);


        }

        public WSResponse submitSubstance(JsonNode js){
            return this.url(API_URL_SUBMIT).post(js).get(timeout);

        }



        public WSResponse fetchSubstance(String uuid){
            return this.url(API_URL_FETCH.replace("$UUID$", uuid)).get().get(timeout);
        }
        public WSResponse fetchSubstanceHistory(String uuid, int version){
            return this.url(API_URL_HISTORY.replace("$UUID$", uuid))
                    .setQueryParameter("filter","path=null AND version=\'" + version + "\'")
                    .get().get(timeout);
        }

        public WSResponse updateSubstance(JsonNode js){
            return this.url(API_URL_UPDATE).put(js).get(timeout);
        }


        /**
         * Get the summary JSON which contains the oldValue and newValue URLs
         * for this version change.
         * @param uuid the UUID of the substance to fetch.
         *
         * @param version the version to of the substance to fetch.
         * @return the JsonNode , should not be null.
         */
        public JsonNode fetchSubstanceHistoryJSON(String uuid, int version){
            return ensureExctractJSON(fetchSubstanceHistory(uuid, version));
        }

        public JsonHistoryResult fetchSubstanceJSON(String uuid, int version){
            JsonNode edits = fetchSubstanceHistoryJSON(uuid,version);
            //should only have 1 edit...so this should be safe
            JsonNode edit = edits.iterator().next();
            JsonNode oldv= urlJSON(edit.get("oldValue").asText());
            JsonNode newv= urlJSON(edit.get("newValue").asText());



            return new JsonHistoryResult(edit, oldv, newv);
        }



        public JsonNode fetchSubstanceJSON(String uuid){
            return ensureExctractJSON(fetchSubstance(uuid));
        }
        public JsonNode submitSubstanceJSON(JsonNode js){
            return ensureExctractJSON(submitSubstance(js));
        }
        public JsonNode validateSubstanceJSON(JsonNode js) {
            return ensureExctractJSON(validateSubstance(js));
        }
        public JsonNode approveSubstanceJSON(String uuid){
            return ensureExctractJSON(approveSubstance(uuid));
        }
        public JsonNode updateSubstanceJSON(JsonNode updated) {
            return ensureExctractJSON(updateSubstance(updated));
        }
        public JsonNode urlJSON(String url){
            return ensureExctractJSON(url(url).get().get(timeout));
        }
        public JsonNode whoamiJSON(){
            return ensureExctractJSON(whoami());
        }
        public JsonNode vocabulariesJSON(){
            return ensureExctractJSON(vocabularies());
        }


        private WSResponse vocabularies(){
            return this.url(API_CV_LIST).get().get(timeout);
        }

        private JsonNode ensureExctractJSON(WSResponse wsResponse1){
            assertTrue(wsResponse1!=null);
            int status2 = wsResponse1.getStatus();
            if(status2>300){
                System.out.println("That's an error!");
                System.out.println(wsResponse1.getBody());
            }
            assertTrue(status2 == 200 || status2 == 201);
            JsonNode returned = wsResponse1.asJson();
            assertTrue(returned!=null);
            return returned;
        }

        private WSResponse ensureFailure(WSResponse wsResponse1){
            assertTrue(wsResponse1!=null);
            int status2 = wsResponse1.getStatus();
            assertTrue("Expected failure code, got:" + status2, status2 != 200 && status2 != 201);
            return wsResponse1;
        }

        private WSResponse approveSubstance(String uuid){
            return this.url(API_URL_APPROVE.replace("$UUID$", uuid)).get().get(timeout);
        }

        private WSResponse whoami(){
            return this.url(API_URL_WHOAMI).get().get(timeout);
        }

        public String whoamiUsername(){
            return whoamiJSON().get("identifier").asText();
        }

        public WSResponse fetchSubstanceFail(String uuid){
            return ensureFailure(fetchSubstance(uuid));
        }
        public WSResponse updateSubstanceFail(JsonNode updated) {
            return ensureFailure(updateSubstance(updated));
        }
        public WSResponse submitSubstanceFail(JsonNode js) {
            return ensureFailure(submitSubstance(js));
        }
        public WSResponse approveSubstanceFail(String uuid) {
            return ensureFailure(approveSubstance(uuid));
        }

        public WSResponse whoamiFail() {
            return ensureFailure(whoami());
        }

        public String fetchSubstanceUI(String id){
            return urlString(UI_URL_SUBSTANCE.replace("$ID$", id));
        }

        public String fetchSubstanceVersionUI(String id, int version){
            return urlString(UI_URL_SUBSTANCE_VERSION.replace("$ID$", id).replace("$VERSION$", Integer.toString(version)));
        }

        public String urlString(String url){
            WSResponse wsResponse1 = this.url(url).get().get(timeout);
            assertThat(wsResponse1.getStatus()).isEqualTo(OK);
            return wsResponse1.getBody();
        }
    }

    /**
     * Special session that doesn't have any login information
     * and overrides #logout() so you can't "log out" since
     * there's nothing to log out from.
     */
    private static class NotLoggedInSession extends UserSession{
        @Override
        public void logout() {
            //do nothing
        }
    }

}
