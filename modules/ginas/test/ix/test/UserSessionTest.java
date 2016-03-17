package ix.test;

import ix.core.controllers.UserProfileFactory;
import ix.core.models.UserProfile;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import play.libs.ws.WSResponse;

import ix.test.GinasTestServer.UserSession;
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
        try(UserSession session = ts.loginFakeUser1()){
            System.out.println("getting substances");
                WSResponse response = session.get("ginas/app/substances");
                String content = response.getBody();
                System.out.println("ginas/app/substances CONTENTS is");
                System.out.println(response.getAllHeaders());
                System.out.println(content);
                assertTrue(content.contains("Logged in as: " + session.getUserName()));
                assertTrue(content.contains("/ginas/app/logout"));
        }
    }

    @Test
    public void logoutShouldRedirectBackToLoginScreen() throws Exception{
        try(UserSession session = ts.login(luke)){

            String content = session.logout();
            assertTrue(content.contains("<title>NCATS Login</title>"));
        }
    }



    @Test  @Ignore
    public void restrictedUrlRequestAfterLogoutShouldError401() throws Exception{
        try(UserSession session = ts.login(luke)){

            session.logout();
            WSResponse response = session.get("ginas/app/wizard?kind=chemical");

            assertEquals(401, response.getStatus());
        }
    }

    @Test  @Ignore
    public void restrictedUrlRequestNotLoggedInShouldError401() throws Exception{
        try(UserSession session = ts.getNotLoggedInSession()){
            WSResponse response = session.get("ginas/app/wizard?kind=chemical");

            assertEquals(401, response.getStatus());
        }
    }

    @Test
    public void restrictedUrl() throws Exception{
        try(UserSession session = ts.login(luke)){

            WSResponse response = session.get("ginas/app/wizard?kind=chemical");

            System.out.println(response.getStatus());
            System.out.println(response.getBody());
        }
    }

    @Test
    public void restrictedUrl2() throws Exception{
        try(UserSession session = ts.login(luke)){
            UserProfile profile = session.getUser().getProfile();
            System.out.println("profile = " + profile.getIdentifier());
            System.out.println("roles = " + profile.getRoles());

            WSResponse response = session.get("ginas/app/wizard?kind=chemical");

            System.out.println(response.getStatus());
            System.out.println(response.getAllHeaders());
            System.out.println(response.getBody());
        }
    }
}
