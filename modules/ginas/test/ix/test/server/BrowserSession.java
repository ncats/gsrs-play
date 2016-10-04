package ix.test.server;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;


import com.gargoylesoftware.htmlunit.html.HtmlPage;

import ix.utils.Util;

import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static play.mvc.Http.Status.OK;

/**
 * Mimics a Session
 *
 * Created by katzelda on 3/17/16.
 */
public class BrowserSession extends AbstractSession<WSResponse>{


    private static final long TIMEOUT = 10_000L;

    private WebClient webClient;
    private  String sessionCookie;
    private AuthenticationStrategy authenticationStrategy = NullAuthenticationStrategy.INSTANCE;

    public BrowserSession(int port) {
        super(port);
        
        webClient = new WebClient();
      //  webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }
    public BrowserSession(GinasTestServer.User user, int port) {
        this(user, port, AuthenticationStrategyFactory.getDefault());
    }
    public BrowserSession(GinasTestServer.User user, int port, AuthenticationStrategyFactory authenticationStrategyFactory) {
        super(user, port);
//
//        try {
            webClient = new WebClient();
            authenticationStrategy= authenticationStrategyFactory.newInstance(this, webClient);

            authenticationStrategy.login(user);
//       //     webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
//            HtmlPage page = webClient.getPage(constructUrlFor("ginas/app/login"));
//            //there is only 1 form but it isn't named..
//            HtmlForm form = page.getForms().get(0);
//
//            form.getInputsByName("username").get(0).setValueAttribute(user.getUserName());
//            form.getInputsByName("password").get(0).setValueAttribute(user.getPassword());
//
//            HtmlPage returnPage = form.getButtonByName("submit").click();
//
//
//            Cookie cook=webClient.getCookieManager().getCookie("PLAY_SESSION");
//            if(cook==null)throw new IOException("no session established");
//            if(cook!=null){
//            	this.sessionCookie = String.format("PLAY_SESSION=%s", cook.getValue());
//            }
//        } catch (IOException e) {
//           throw new IllegalStateException("error logging in ", e);
//        }


    }
    @Override
    public WSResponse get(String path){
        return url(constructUrlFor(path)).get().get(timeout);
    }

    private WSRequestHolder  url(String url) {
        WSRequestHolder ws = WS.url(url);
        authenticationStrategy.modifyRequest(ws);
        return ws;
    }

    public WebRequest newGetRequest(String path) throws MalformedURLException {
        return new WebRequest(new URL(constructUrlFor(path)), HttpMethod.GET);
    }
    
    public String sha1ofResponse(WebRequest ws) throws IOException{
    	HtmlPage html=submit(ws);
    	return Util.sha1(html.asXml());
    }

    public WebRequest newPostRequest(String path) throws MalformedURLException {
        return new WebRequest(new URL(constructUrlFor(path)), HttpMethod.POST);
    }

    public HtmlPage submit(WebRequest request) throws IOException{
        Objects.requireNonNull(request);
        Objects.requireNonNull(webClient, "webclient is null!!!!!!!");
        
        HtmlPage page= webClient.getPage(request);
        webClient.closeAllWindows();
        return page;
    }

    public WSRequestHolder newRequestHolder(String relativePath){
        return url(constructUrlFor(relativePath));
    }

    @Override
    protected WSResponse doLogout() {
        WSResponse wsResponse1 =  get("ginas/app/logout");
        if(wsResponse1.getStatus() != OK){
            throw new IllegalStateException("error logging out : " + wsResponse1.getStatus() + " \n" +  wsResponse1.getBody());
        }


       authenticationStrategy.logout();
        return wsResponse1;
    }

    public WSResponse get(URL url) {
        return url(url.toString()).get().get(timeout);
    }


    private static enum NullAuthenticationStrategy implements AuthenticationStrategy{
        INSTANCE;

        @Override
        public void login(GinasTestServer.User user) {

        }

        @Override
        public void logout() {

        }

        @Override
        public void modifyRequest(WSRequestHolder request) {

        }
    }
}
