package ix.test.login;

import ix.test.ix.test.server.AbstractSession;
import ix.test.ix.test.server.BrowserSession;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.util.MultiThreadInteracter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import play.libs.ws.WSResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 3/14/16.
 */
public class UserSessionTest {
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + getClass().getName() + " . " + description.getMethodName());
        }
    };
    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    private GinasTestServer.User luke;


    private static Pattern LOGGED_IN_AS_PATTERN = Pattern.compile("username:\\s*(\\S+)?");
            //Pattern.compile("var session = \{\\s+username:null\\s+\};", Pattern.MULTILINE);
    @Before
    public void createuser(){
        luke = ts.createNormalUser("Luke", "TK421");
    }
    @Test
    public void loggedInUserSeesLogoutButton() throws Exception{
        try(BrowserSession session = ts.newBrowserSession(luke)){
                WSResponse response = session.get("ginas/app/substances");
                String content = response.getBody();
                ensureLoggedInAs(response, luke);
                assertTrue(content.contains("/ginas/app/logout"));
        }
    }

    @Test
    public void logoutShouldRedirectBackToLoginScreen() throws Exception{
        try(BrowserSession session = ts.newBrowserSession(luke)){

            String content = session.logout().getBody();
            assertTrue(content.contains("<title>NCATS Login</title>"));
        }
    }



    @Test
    public void restrictedUrlRequestAfterLogoutShouldError401() throws Exception{
        try(BrowserSession session = ts.newBrowserSession(luke)){

            session.logout();
            WSResponse response = session.get("ginas/app/wizard?kind=chemical");
            System.out.println(response.getBody());
            assertEquals(401, response.getStatus());
        }
    }

    @Test
    public void restrictedUrlRequestNotLoggedInShouldError401() throws Exception{
        try(BrowserSession session = ts.notLoggedInBrowserSession()){
            WSResponse response = session.get("ginas/app/wizard?kind=chemical");

            assertEquals(401, response.getStatus());
        }
    }

    @Test
    public void restrictedUrl() throws Exception{
        try(BrowserSession session = ts.newBrowserSession(luke)){

            WSResponse response = session.get("ginas/app/wizard?kind=chemical");

            assertEquals(200, response.getStatus());
            ensureLoggedInAs(response, luke);
        }
    }


    @Test
    public void notLoggedInBrowserSessionViewSubstancesl() {
        try (BrowserSession session = ts.notLoggedInBrowserSession()) {
            WSResponse response = session.get("ginas/app/substances");

            ensureNotLoggedIn(response);
        }
    }

    @Test
    public void loggedInBrowserSessionViewSubstancesl() {
        GinasTestServer.User user1 = ts.getFakeUser1();

        try (BrowserSession session = ts.newBrowserSession(user1)) {
            WSResponse response = session.get("ginas/app/substances");

            ensureLoggedInAs(response, user1);
        }
    }
    private static void ensureNotLoggedIn(WSResponse response){
        ensureLoggedInAs(response, "null");
    }
    private static void ensureLoggedInAs(WSResponse response, GinasTestServer.User user){
        ensureLoggedInAs(response, user.getUserName());

    }

    private static void ensureLoggedInAs(WSResponse response, String username) {
        String body = response.getBody();

        Matcher matcher = LOGGED_IN_AS_PATTERN.matcher(body);
        if(!matcher.find()){
            throw new IllegalStateException("could not parse username from session:" + body);
        }
        String foundName = unquote(matcher.group(1));
        System.out.println("USER NAME FOUND = '" + foundName+"'");
        assertEquals(username, foundName);
    }

    private static String unquote(String s){
       return s.replaceAll("\"", "");
    }

    @Test
    public void notLoggedInBrowserSessionViewSubstancesWithOtherLoggedInUsersSingleThreaded() {
        GinasTestServer.User user1 = ts.getFakeUser1();

        try (AbstractSession session = ts.newBrowserSession(user1);
             AbstractSession session2 = ts.notLoggedInBrowserSession()) {

            ensureNotLoggedIn(session2.get("ginas/app/substances"));

            System.out.println("first logged in attempt");
            ensureLoggedInAs( session.get("ginas/app/substances"), user1);

            System.out.println("1================================");
            ensureNotLoggedIn(session2.get("ginas/app/substances"));

            System.out.println("2================================");
            System.out.println("2nd logged in attempt");
            ensureLoggedInAs( session.get("ginas/app/substances"), user1);

        }

    }

    @Test
    public void twoDifferentLoggedInUsersViewSubstancesWithOtherLoggedInUsersSingleThreaded() {
        GinasTestServer.User user1 = ts.getFakeUser1();
        GinasTestServer.User user3 = ts.getFakeUser3();

        try (RestSession session1 = ts.newRestSession(user1);
             RestSession session3 = ts.newRestSession(user3);) {



            ensureLoggedInAs( session3.get("ginas/app/substances"), user3);

            System.out.println("first logged in attempt");
            ensureLoggedInAs( session1.get("ginas/app/substances"), user1);

            System.out.println("================================");
            ensureLoggedInAs( session3.get("ginas/app/substances"), user3);

            System.out.println("2nd logged in attempt");
            ensureLoggedInAs( session1.get("ginas/app/substances"), user1);

        }

    }



    @Test
    public void twoDifferentLoggedInUsersViewSubstancesMultiThreadedInterleaved() {

        final GinasTestServer.User user1 = ts.getFakeUser1();
        final GinasTestServer.User user3 = ts.getFakeUser3();

        try (final RestSession session1 = ts.newRestSession(user1);
             final RestSession session3 = ts.newRestSession(user3);) {

            MultiThreadInteracter.Builder builder = new MultiThreadInteracter.Builder();

            builder.newThread()
                    .step(1, new MultiThreadInteracter.Step() {

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(session3.get("ginas/app/substances"), user3);
                        }
                    })
                    .step(3, new MultiThreadInteracter.Step() {

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(session3.get("ginas/app/substances"), user3);
                        }
                    });


            builder.newThread()
                    .step(2, new MultiThreadInteracter.Step() {

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(session1.get("ginas/app/substances"), user1);
                        }
                    })
                    .step(4, new MultiThreadInteracter.Step() {

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(session1.get("ginas/app/substances"), user1);
                        }
                    });


            MultiThreadInteracter multiThreadInteracter = builder.build();

            multiThreadInteracter.run();

        }
    }

        @Test
        public void twoDifferentLoggedInUsersViewSubstancesMultiThreadedConcurrently(){

            final GinasTestServer.User user1 = ts.getFakeUser1();
            final GinasTestServer.User user3 = ts.getFakeUser3();

            try (final RestSession session1 = ts.newRestSession(user1);
                 final RestSession session3 = ts.newRestSession(user3);) {

                MultiThreadInteracter.Builder builder = new MultiThreadInteracter.Builder();

                builder.newThread()
                        .step(1, new MultiThreadInteracter.Step(){

                            @Override
                            public void execute() throws Exception {
                                ensureLoggedInAs( session3.get("ginas/app/substances"), user3);
                            }
                        })
                        .step(3, new MultiThreadInteracter.Step(){

                            @Override
                            public void execute() throws Exception {
                                ensureLoggedInAs( session3.get("ginas/app/substances"), user3);
                            }
                        });


                builder.newThread()
                        .step(2, new MultiThreadInteracter.Step(){

                            @Override
                            public void execute() throws Exception {
                                ensureLoggedInAs( session1.get("ginas/app/substances"), user1);
                            }
                        })
                        .step(3, new MultiThreadInteracter.Step(){

                            @Override
                            public void execute() throws Exception {
                                ensureLoggedInAs( session1.get("ginas/app/substances"), user1);
                            }
                        });


                MultiThreadInteracter multiThreadInteracter = builder.build();

                multiThreadInteracter.run();

            }


    }


}
