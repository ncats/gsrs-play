package ix.test.server;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import play.Play;
import play.libs.ws.WSRequestHolder;

import java.io.IOException;

/**
 * Created by katzelda on 9/8/16.
 */
public enum AuthenticationStrategyFactory {

    SINGLE_SIGN_ON{
        @Override
        public AuthenticationStrategy newInstance(BrowserSession session, WebClient client) {
            return null;
        }
    },
    COOKIE{
        @Override
        public AuthenticationStrategy newInstance(BrowserSession session, WebClient client) {
            return new CookieStrategy(session, client);
        }
    }
    ;

    public static AuthenticationStrategyFactory getDefault(){
        return COOKIE;
    }

    public abstract AuthenticationStrategy newInstance(BrowserSession session, WebClient client);



    private static class CookieStrategy implements AuthenticationStrategy{

        private final BrowserSession session;
        private final WebClient webClient;

        private String sessionCookie;

        public CookieStrategy(BrowserSession session, WebClient client) {
            this.session = session;
            this.webClient = client;
        }

        @Override
        public void login(GinasTestServer.User user) {
            try {

                //     webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                HtmlPage page = webClient.getPage(session.constructUrlFor("ginas/app/login"));
                //there is only 1 form but it isn't named..
                HtmlForm form = page.getForms().get(0);

                form.getInputsByName("username").get(0).setValueAttribute(user.getUserName());
                form.getInputsByName("password").get(0).setValueAttribute(user.getPassword());

                HtmlPage returnPage = form.getButtonByName("submit").click();


                Cookie cook=webClient.getCookieManager().getCookie("PLAY_SESSION");
                if(cook==null){
                    throw new IOException("no session established");
                }
                this.sessionCookie = String.format("PLAY_SESSION=%s", cook.getValue());

            } catch (IOException e) {
                throw new IllegalStateException("error logging in ", e);
            }
        }

        @Override
        public void logout() {
            sessionCookie =null;

            webClient.getCookieManager().clearCookies();
        }

        @Override
        public void modifyRequest(WSRequestHolder request) {
            if (sessionCookie != null) {
                request.setHeader("Cookie", sessionCookie);
            }
        }
    }

    private static class SSOAuthenticationStrategy implements AuthenticationStrategy {

        /*
        if (Play.application().configuration()
					    .getBoolean("ix.authentication.trustheader")) {
				String usernameheader = Play.application().configuration()
						.getString("ix.authentication.usernameheader");
				String usernameEmailheader = Play.application().configuration()
						.getString("ix.authentication.useremailheader");
				String username = r.getHeader(usernameheader);
				String userEmail = r.getHeader(usernameEmailheader);

				if (username != null) {
					if (validateUserHeader(username,r)) {
						setSessionUser(username,userEmail);
						return true;
					}
				}
			}
         */

        private String usernameField;
        private String emailField;

        private String username, email;

        @Override
        public void login(GinasTestServer.User user) {
            usernameField = Play.application().configuration()
                    .getString("ix.authentication.usernameheader");
            emailField = Play.application().configuration()
                    .getString("ix.authentication.useremailheader");

            username = user.getUserName();
            email = user.getEmail();
        }

        @Override
        public void logout() {
            username = null;
            email = null;
        }

        @Override
        public void modifyRequest(WSRequestHolder request) {
            if (username != null) {
                request.setHeader(usernameField, username);
                request.setHeader(emailField, email);
            }
        }
    }
}
