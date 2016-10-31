package ix.test.login;

import static ix.test.login.LoginUtil.ensureLoggedInAs;
import static ix.test.login.LoginUtil.ensureNotLoggedIn;

import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.util.TimeTraveller;
import ix.core.util.TimeUtil;
import ix.ncats.controllers.auth.Authentication;
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
/**
 * Created by katzelda on 3/24/16.
 */
public class ExpiredSessionTest extends AbstractGinasServerTest{

    public static final String URL = "ginas/app"; //changed to home page, so that no 401 or other errros occur

    @Rule
    public TimeTraveller timeTraveller = new TimeTraveller( TimeUtil.toDate(1955, 11, 5));

    private GinasTestServer.User user = ts.getFakeUser1();


    @Test
    public void notExpiredSession(){
        try(BrowserSession session = ts.newBrowserSession(user)){
            ensureLoggedInAs(session.get(URL), user);
        }
    }

    @Test
    public void veryExpiredSessionShouldMakeYouLoggedOut(){
        try(BrowserSession session = ts.newBrowserSession(user)){

            //I think session is currently 2 hrs?
            //but just to be on safe side advance by 1 day
            timeTraveller.jump(1, TimeUnit.DAYS);

            ensureNotLoggedIn(session.get(URL));
        }
    }
    @Test
    public void slightlyExpiredSessionShouldMakeYouLoggedOut(){
        try(BrowserSession session = ts.newBrowserSession(user)){

            timeTraveller.jump(Authentication.TIMEOUT.get() +1, TimeUnit.SECONDS);

            ensureNotLoggedIn(session.get(URL));
        }
    }
    @Test
    public void notYetExpiredSessionShouldStillBeLogginIn(){
        try(BrowserSession session = ts.newBrowserSession(user)){

            timeTraveller.jump(Authentication.TIMEOUT.get() -1, TimeUnit.SECONDS);

            ensureLoggedInAs(session.get(URL), user);
        }
    }
}