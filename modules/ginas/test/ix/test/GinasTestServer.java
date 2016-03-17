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
import java.net.URLEncoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

import com.hazelcast.client.AuthenticationException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.PrincipalFactory;
import ix.core.controllers.UserProfileFactory;
import ix.core.controllers.search.SearchFactory;
import ix.core.controllers.v1.RouteFactory;
import ix.core.models.Group;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.ginas.utils.validation.Validation;
import ix.ncats.controllers.App;
import ix.ncats.controllers.auth.Authentication;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import net.sf.ehcache.CacheManager;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.rules.ExternalResource;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.ws.WS;
import play.libs.ws.WSCookie;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.test.TestServer;
import sun.misc.BASE64Encoder;

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
	 
	 private static final String API_URL_SUBSTANCES_SEARCH="http://localhost:9001/ginas/app/api/v1/substances/search";

	 private static final String API_URL_MAKE_FAKE_USERS="http://localhost:9001/ginas/app/api/v1/@deleteme";
     private static final String API_URL_WHOAMI="http://localhost:9001/ginas/app/api/v1/whoami";
     
     
     private static final String UI_URL_SUBSTANCES="http://localhost:9001/ginas/app/substance";
     private static final String UI_URL_SUBSTANCE="http://localhost:9001/ginas/app/substance/$ID$";
     private static final String UI_URL_SUBSTANCE_VERSION="http://localhost:9001/ginas/app/substance/$ID$/v/$VERSION$";
     private static final String API_CV_LIST="http://localhost:9001/ginas/app/api/v1/vocabularies";
     
     private static final String FAKE_USERNAME_PREFIX="FAKE";
     private static final String FAKE_PASSWORD_PREFIX="pa$$word";
     
	 public static final String FAKE_USER_1="fakeuser1";
	 public static final String FAKE_USER_2="fakeuser2";
	 public static final String FAKE_USER_3="fakeuser3";

	 public static final String FAKE_PASSWORD_1="madeup1";
	 public static final String FAKE_PASSWORD_2="madeup2";
	 public static final String FAKE_PASSWORD_3="madeup3";



    private static final Pattern COOKIE_PATTERN = Pattern.compile("Set-Cookie=(.+?);");
    private static long timeout= 10000L;

    private final UserSession defaultSession;


    private List<UserSession> sessions = new ArrayList<>();

    private TestServer ts;
    private int port;
    private Group fakeUserGroup;

    private static final List<Role> superUserRoles = Role.roles(Role.SuperUpdate,Role.SuperDataEntry );
    private static final List<Role> normalUserRoles = Role.roles(Role.DataEntry,Role.Updater );

    private int userCount=0;
    
    public enum AUTH_TYPE{
    	USERNAME_PASSWORD,
    	USERNAME_KEY,
    	TOKEN,
    	NONE
    }
    
    public static class User{
    	private final String username;
    	private String password;

        public User(String username, String password) {
            Objects.requireNonNull(username);
            Objects.requireNonNull(password);

            this.username = username;
            this.password = password;
        }


        public String getUserName() {
            return username;
        }

        public UserProfile getProfile(){
            return Authentication.getUserProfileFromPassword(username, password);
        }
    }

    public GinasTestServer(int port){
       this.port = port;
        defaultSession = new NotLoggedInSession(port);


    }

    private void createInitialFakeUsers() {
        fakeUserGroup= AdminFactory.groupfinder.where().eq("name", "fake").findUnique();
        if(fakeUserGroup ==null){
            fakeUserGroup=new Group("fake");
        }
        List<Group> groups = Collections.singletonList(fakeUserGroup);

        UserProfileFactory.addActiveUser(FAKE_USER_1,FAKE_PASSWORD_1, superUserRoles,groups);
        UserProfileFactory.addActiveUser(FAKE_USER_2,FAKE_PASSWORD_2, superUserRoles,groups);

        UserProfileFactory.addActiveUser(FAKE_USER_3,FAKE_PASSWORD_3,normalUserRoles,groups);
    }



    public UserSession loginFakeUser1(){
    	return login(FAKE_USER_1,FAKE_PASSWORD_1);
    }
    public UserSession loginFakeUser2(){
    	return login(FAKE_USER_2,FAKE_PASSWORD_2);
    }

	public UserSession loginFakeUser3() {
		return login(FAKE_USER_3,FAKE_PASSWORD_3);
	}
    //logs in user, also sets default authentication type
    //if previously set to NONE
    public UserSession login(String username, String password){
        
    	return login(username, password, AUTH_TYPE.USERNAME_PASSWORD);
    }

    public UserSession login(String username, String password, AUTH_TYPE type){

        WSRequestHolder ws = url(defaultSession.constructUrlFor("ginas/app/login"));
        try {
            WSResponse response = ws.setQueryParameter("username", username)
                                    .setQueryParameter("password", password)
                                    .setFollowRedirects(false)
                                    .post("")
                                    .get(1000);


            System.out.println("login response");
            System.out.println("status code = " + response.getStatus());
            System.out.println(response.getAllHeaders());

            WSCookie sessionCookie = response.getCookie("PLAY_SESSION");

            System.out.println("session cookie name is " + sessionCookie.getName());
            System.out.println("session cookie value is " + sessionCookie.getValue());

            System.out.println("location to redirect is " +  response.getHeader("Location"));
           // System.out.println(response.getBody());
            //there is probably a leading slash remove it
            String newURl = removeLeadingSlash(response.getHeader("Location"));


            WSResponse redirectResponse = WS.url(defaultSession.constructUrlFor(newURl))

                                            .setHeader("Cookie", String.format("%s=%s", sessionCookie.getName(), sessionCookie.getValue()))
                                            .get()
                                            .get(1000);

            System.out.println("redirect response...");
            System.out.println("status code = " + redirectResponse.getStatus());
            System.out.println(redirectResponse.getAllHeaders());
            System.out.println(redirectResponse.getBody());

            UserSession newSession = new UserSession(new User(username, password), type, sessionCookie, port);

            sessions.add(newSession);

            return newSession;
        }catch(Exception e){
            throw new IllegalStateException(e);
        }
    }

    private String removeLeadingSlash(String path) {
        if(path.charAt(0) == '/'){
            return path.substring(1);
        }
        return path;
    }

    public UserSession login(User u){
        return login(u, AUTH_TYPE.USERNAME_PASSWORD);
    }
    public UserSession login(User u, AUTH_TYPE type){
       return login(u.getUserName(), u.password,type);
    }
    public User createSuperUser(String username, String password){
        return createUser(username, password, superUserRoles);
    }

    public User createNormalUser(String username, String password){
        return createUser(username, password, normalUserRoles);
    }

    public User createUser(String username, String password, Role role, Role ... roles){
        List<Role> list = new ArrayList<>();
        list.add(role);
        for(Role r: roles){
            list.add(r);
        }
        return createUser(username, password,list);
    }
    public User createUser(String username, String password, List<Role> roles){
    	if(roles.isEmpty()){
            throw new IllegalArgumentException("roles can not be empty");
        }
    	UserProfile up=UserProfileFactory.addActiveUser(username, password, roles, Collections.singletonList(fakeUserGroup));
    	return new User(up.getIdentifier(), password);
    }
    
    public UserSession createNewUserAndLogin(Role role, Role ...roles){

    	userCount++;
    	return  login(createUser(
    			FAKE_USERNAME_PREFIX + userCount,
    			FAKE_PASSWORD_PREFIX + userCount, role, roles),
    			AUTH_TYPE.USERNAME_PASSWORD
    			);
    	
    }
    


    private WSRequestHolder  url(String url){
    	return defaultSession.url(url);
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
        //we have to wait to create the users until after Play has started.
        createInitialFakeUsers();
    }

    private void initializeControllers() {

        App.init();

        Validation.init();
        AdminFactory.init();
        RouteFactory.init();
        Authentication.init();

        IxDynamicResourceHandler.init();

        UserProfileFactory.init();
        PrincipalFactory.init();

        EntityFactory.init();
        SearchFactory.init();

        EntityPersistAdapter.init();
    }

    private void deleteH2Db() throws IOException {
        Config load = ConfigFactory.load();
        //System.out.println(load.entrySet());
        Path path = new File(load.getString("ix.home")).toPath();
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
                }else{
                    System.out.println("found nfs file " + file.toString());
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
        //explicitly shutdown indexer to clear file locks
        App._textIndexer.shutdown();
        ts.stop();
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

        private  String sessionCookie;

        private int port;

        private final User user;

        private AUTH_TYPE authType=AUTH_TYPE.NONE;

        public UserSession(int port){
            //null values for defaults
            this.port = port;
            this.user = null;
        }
        public UserSession(User user, AUTH_TYPE type,  WSCookie sessionCookie, int port){
            Objects.requireNonNull(user);
            Objects.requireNonNull(type);

            if(port <1){
                throw new IllegalArgumentException("port can not be < 1");
            }
            loggedIn=true;
            this.user = user;
            this.authType=type;
            this.port = port;
            this.sessionCookie = String.format("%s=%s", sessionCookie.getName(), sessionCookie.getValue());

        }
        public String getUserName(){
        	return this.user.getUserName();
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

        public User getUser(){
            return user;
        }


        public String logout(){
            if(!loggedIn){
                //do nothing
                return "";
            }

            loggedIn = false;
            System.out.println("trying to log out");

           WSResponse wsResponse1 =  get("ginas/app/logout");
            if(wsResponse1.getStatus() != OK){
                throw new IllegalStateException("error logging out : " + wsResponse1.getStatus() + " \n" +  wsResponse1.getBody());
            }



            this.username=null;
            this.password=null;
            this.key=null;
            this.token=null;
            this.deadtime=0;

            sessionCookie =null;

            return wsResponse1.getBody();
        }
        public WSResponse get(String path){
            return url(constructUrlFor(path)).get().get(timeout);
        }

        private String constructUrlFor(String path) {
            return new StringBuilder("http://localhost:")
                                    .append(port)
                                    .append('/')
                                    .append(path)
                                    .toString();
        }

        public String getAsString(String path){
            return urlString(constructUrlFor(path));
        }

        public WSRequestHolder  url(String url){
            WSRequestHolder ws = WS.url(url);

            if(sessionCookie !=null){
                ws.setHeader("Cookie", sessionCookie);
            }
            if(loggedIn) {
                switch (authType) {
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

        public boolean isLoggedIn(){
            return loggedIn;
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

        public JsonNode fetchSubstancesSearchJSON() {
            return ensureExctractJSON(fetchSubstancesSearch());
        }
        public WSResponse fetchSubstancesSearch() {
            return url(API_URL_SUBSTANCES_SEARCH).get().get(timeout);
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

            if(wsResponse1.getStatus() != OK){
                throw new IllegalStateException("error getting url content : " + wsResponse1.getStatus() + " \n" +  wsResponse1.getBody());
            }

            return wsResponse1.getBody();
        }
    }

    /**
     * Special session that doesn't have any login information
     * and overrides #logout() so you can't "log out" since
     * there's nothing to log out from.
     */
    private static class NotLoggedInSession extends UserSession{

        public NotLoggedInSession(int port){
            super(port);
        }
        @Override
        public String logout() {
            //do nothing
            return "";
        }

        @Override
        public String getUserName(){
            return null;
        }
    }

}
