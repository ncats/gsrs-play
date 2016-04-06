package ix.test.ix.test.server;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlForm;


import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import org.apache.http.impl.client.HttpClientBuilder;
import play.libs.ws.WS;
import play.libs.ws.WSCookie;
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
    public BrowserSession(int port) {
        super(port);
        webClient = new WebClient();
    }

    public BrowserSession(GinasTestServer.User user, int port) {
        super(user, port);

        try {
            webClient = new WebClient();
            HtmlPage page = webClient.getPage(constructUrlFor("ginas/app/login"));
            //there is only 1 form but it isn't named..
            HtmlForm form = page.getForms().get(0);

            form.getInputsByName("username").get(0).setValueAttribute(user.getUserName());
            form.getInputsByName("password").get(0).setValueAttribute(user.getPassword());

            HtmlPage returnPage = form.getButtonByName("submit").click();

            Cookie cook=webClient.getCookieManager().getCookie("PLAY_SESSION");
            if(cook==null)throw new IOException("no session established");
            if(cook!=null){
            	this.sessionCookie = String.format("PLAY_SESSION=%s", cook.getValue());
            }
        } catch (IOException e) {
           throw new IllegalStateException("error logging in ", e);
        }


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

    public WebRequest newGetRequest(String path) throws MalformedURLException {
        return new WebRequest(new URL(constructUrlFor(path)), HttpMethod.GET);
    }

    public WebRequest newPostRequest(String path) throws MalformedURLException {
        return new WebRequest(new URL(constructUrlFor(path)), HttpMethod.POST);
    }

    public HtmlPage submit(WebRequest request) throws IOException{
        Objects.requireNonNull(request);
        Objects.requireNonNull(webClient, "webclient is null!!!!!!!");
        return webClient.getPage(request);
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


        sessionCookie =null;

        webClient.getCookieManager().clearCookies();
        return wsResponse1;
    }
}
