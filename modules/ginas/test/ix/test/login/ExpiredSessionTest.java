package ix.test.login;

import ix.core.util.TimeTraveller;
import ix.core.util.TimeUtil;
import ix.ncats.controllers.auth.Authentication;
import ix.test.ix.test.server.BrowserSession;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import ix.test.util.TestNamePrinter;
import org.junit.Rule;
import org.junit.Test;
import play.libs.ws.WSResponse;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import static ix.test.login.LoginUtil.*;
/**
 * Created by katzelda on 3/24/16.
 */
public class ExpiredSessionTest {

    public static final String URL = "ginas/app/wizard?kind=chemical";

    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
    public GinasTestServer ts = new GinasTestServer();

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

            timeTraveller.jump(Authentication.TIMEOUT +1, TimeUnit.MILLISECONDS);

            ensureNotLoggedIn(session.get(URL));
        }
    }
    @Test
    public void notYetExpiredSessionShouldStillBeLogginIn(){
        try(BrowserSession session = ts.newBrowserSession(user)){

            timeTraveller.jump(Authentication.TIMEOUT -1, TimeUnit.MILLISECONDS);

            ensureLoggedInAs(session.get(URL), user);
        }
    }
}
