package ix.test.login;

import static ix.test.login.LoginUtil.ensureLoggedInAs;
import static ix.test.login.LoginUtil.ensureNotLoggedIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.models.Role;
import ix.test.server.AbstractSession;
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import play.libs.ws.WSResponse;
/**
 * Created by katzelda on 3/14/16.
 */
public class UserSessionTest extends AbstractGinasServerTest{
    
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
                ensureLoggedInAs(response, luke);
                assertTrue(content.contains("/ginas/app/logout"));
        }
    }

    @Test
    public void logoutShouldRedirectBackToLoginScreen() throws Exception{
        try(BrowserSession session = ts.newBrowserSession(luke)){

            String content = session.logout().getBody();
            assertTrue(content, content.contains("Login</title>"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantCreateUserWithDuplicateUsername(){
        ts.createAdmin(luke.getUserName(), "a new pass");
    }


    @Test(expected = IllegalStateException.class)
    public void invalildPasswordBrowser(){
        ts.newBrowserSession( new GinasTestServer.User(luke.getUserName(), "wrong_pass"));
    }



    @Test(expected = IllegalStateException.class)
    public void invalildUsernameBrowser(){
        ts.newBrowserSession( new GinasTestServer.User("not_a_user", "wrong_pass"));
    }

    @Test
    public void invalildUsernameRest(){
        RestSession session = ts.newRestSession( new GinasTestServer.User("not_a_user", "wrong_pass"));

        WSResponse response = session.get("ginas/app/wizard?kind=chemical");
        assertEquals(401, response.getStatus());
    }

    @Test
    public void invalildPassordRest(){
        RestSession session = ts.newRestSession( new GinasTestServer.User(luke.getUserName(), "wrong_pass"));

        WSResponse response = session.get("ginas/app/wizard?kind=chemical");
        assertEquals(401, response.getStatus());
    }


    @Test
    public void restrictedUrlRequestAfterLogoutShouldError401() throws Exception{
        try(BrowserSession session = ts.newBrowserSession(luke)){

            session.logout();
            WSResponse response = session.get("ginas/app/wizard?kind=chemical");
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


    @Test
    public void notLoggedInBrowserSessionViewSubstancesWithOtherLoggedInUsersSingleThreaded() {
        GinasTestServer.User user1 = ts.getFakeUser1();

        try (AbstractSession session = ts.newBrowserSession(user1);
             AbstractSession session2 = ts.notLoggedInBrowserSession()) {

            ensureNotLoggedIn(session2.get("ginas/app/substances"));

            //System.out.println("first logged in attempt");
            ensureLoggedInAs( session.get("ginas/app/substances"), user1);

            //System.out.println("1================================");
            ensureNotLoggedIn(session2.get("ginas/app/substances"));

            //System.out.println("2================================");
            //System.out.println("2nd logged in attempt");
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
    public void adminCanSeeAdminPage(){
        GinasTestServer.User admin = ts.createAdmin("vader", "anakin");

        try(BrowserSession session = ts.newBrowserSession(admin)){
            WSResponse response = session.get("ginas/app/admin");
            ensureLoggedInAs(response, admin);
        }
    }
    
    @Test
    public void normalUserCantSeeAdminPage(){
    	

        try(BrowserSession session = ts.newBrowserSession(ts.getFakeUser1())){
            WSResponse response = session.get("ginas/app/admin");
            assertEquals(401, response.getStatus());


        }
    }
    
    @Test
    public void updaterUserCanRouteToEditPage(){
    	GinasTestServer.User updater = ts.createUser(Role.Updater);	

        try(BrowserSession session = ts.newBrowserSession(updater)){
            WSResponse response = session.get("ginas/app/substance/fakeid/edit");
            assertEquals(500, response.getStatus());

        }
    }

    @Test
    public void queryUserCantRouteToEditPage(){
    	GinasTestServer.User updater = ts.createUser(Role.Query);	

        try(BrowserSession session = ts.newBrowserSession(updater)){
            WSResponse response = session.get("ginas/app/substance/fakeid/edit");
            assertEquals(401, response.getStatus());

        }
    }
    
    @Test
    public void nullUserCantRouteToEditPage(){
        try(BrowserSession session = ts.notLoggedInBrowserSession()){
            WSResponse response = session.get("ginas/app/substance/fakeid/edit");
            assertEquals(401, response.getStatus());
        }
    }
    


}
