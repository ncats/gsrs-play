package ix.test.login;

import ix.test.ix.test.server.BrowserSession;
import ix.test.ix.test.server.GinasTestServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.libs.ws.WSResponse;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 3/14/16.
 */
public class UserSessionTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    private GinasTestServer.User luke;

    @Before
    public void createuser(){
        luke = ts.createNormalUser("Luke", "TK421");
    }
    @Test
    public void loggedInUserSeesLogoutButton() throws Exception{
        try(BrowserSession session = ts.newBrowserSession(luke)){
                WSResponse response = session.get("ginas/app/substances");
                String content = response.getBody();
                assertTrue(content.contains("Logged in as: " + session.getUserName()));
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

            assertTrue(response.getBody().contains("Logged in as: " + session.getUserName()));
        }
    }


    @Test
    public void notLoggedInBrowserSessionViewSubstancesl() {
        try (BrowserSession session = ts.notLoggedInBrowserSession()) {
            WSResponse response = session.get("ginas/app/substances");

            ensureNoLoggedInAs(response);
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
    private static void ensureNoLoggedInAs(WSResponse response){
        assertTrue("User should not be logged in",response.getBody().contains("username:null"));
    }
    private static void ensureLoggedInAs(WSResponse response, GinasTestServer.User user){
        String username = user.getUserName();

        String body = response.getBody();

        assertTrue("User should be logged in as " + username,body.contains("username:\"" + username + "\""));
    }

   /* @Test
    public void notLoggedInBrowserSessionViewSubstancesWithOtherLoggedInUsers() {
        try (BrowserSession session = ts.notLoggedInBrowserSession()) {
            WSResponse response = session.get("ginas/app/substances");

            assertTrue("User should not be logged in",response.getBody().contains("username:null"));
        }

    }
    */


}
