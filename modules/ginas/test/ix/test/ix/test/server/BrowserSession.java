package ix.test.ix.test.server;

import play.libs.ws.WS;
import play.libs.ws.WSCookie;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import static play.mvc.Http.Status.OK;

/**
 * Mimics a Session
 *
 * Created by katzelda on 3/17/16.
 */
public class BrowserSession extends AbstractSession<WSResponse>{


    private  String sessionCookie;
    public BrowserSession(int port) {
        super(port);
    }

    public BrowserSession(GinasTestServer.User user, int port) {
        super(user, port);

        WSRequestHolder ws = WS.url(constructUrlFor("ginas/app/login"));

            //This whole mess below is because the Play test framework
            //doesn't respect cookies (?) and the login response
            //returns the cookie we need to use to remain logged in.
            //
            //The login response is also a redirect and Play's redirect
            //doesn't keep the Cookie so we lose the session and get logged out
            //before our login() method returns!
            //
            //So we have to manually do the login POST with out following
            //redirects, parse the cookie and the redirect location
            //from the response, and then use it to create out UserSession object.

            WSResponse response = ws.setQueryParameter("username", user.getUserName())
                    .setQueryParameter("password", user.getPassword())
                    .setFollowRedirects(false)
                    .post("")
                    .get(1000);


            WSCookie sessionCookie = response.getCookie("PLAY_SESSION");
            this.sessionCookie = String.format("%s=%s", sessionCookie.getName(), sessionCookie.getValue());


        //    UserSession newSession = new UserSession(new User(username, password), type, sessionCookie, port);
    }
    @Override
    public WSResponse get(String path){
        return url(constructUrlFor(path)).get().get(timeout);
    }

    private WSRequestHolder  url(String url) {
        WSRequestHolder ws = WS.url(url);

        if (sessionCookie != null) {
            ws.setHeader("Cookie", sessionCookie);
        }
        return ws;
    }

    @Override
    protected WSResponse doLogout() {
        WSResponse wsResponse1 =  get("ginas/app/logout");
        if(wsResponse1.getStatus() != OK){
            throw new IllegalStateException("error logging out : " + wsResponse1.getStatus() + " \n" +  wsResponse1.getBody());
        }


        sessionCookie =null;

        return wsResponse1;
    }
}