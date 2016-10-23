package ix.test.server;

import static play.mvc.Http.Status.OK;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import ix.utils.Util;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

/**
 * Mimics a Session
 *
 * Created by katzelda on 3/17/16.
 */
public class BrowserSession extends AbstractSession<WSResponse>{


    private static final long TIMEOUT = 10_000L;

    private WebClient webClient;
    private String sessionCookie;
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
		webClient = new WebClient();
		authenticationStrategy = authenticationStrategyFactory.newInstance(this, webClient);

		authenticationStrategy.login(user);

    }
    @Override
    public WSResponse get(String path){
        return get(path, timeout);
    }
    
    public WSResponse get(String path, long timeout){
        return url(constructUrlFor(path)).get().get(timeout);
    }

    private WSRequestHolder  url(String url) {
        WSRequestHolder ws = WS.url(url);
        authenticationStrategy.modifyRequest(ws);
        return ws;
    }

    public static class WrappedWebRequest{
    	private WebRequest wq;
    	public WrappedWebRequest(WebRequest wq){
    		this.wq=wq;
    	}
    	/**
    	 * Add the name / value pair encoded into to the URL, <b>without</b> clobbering
    	 * any previous value that was present under that name. For example, adding
    	 * the pair <code>"test":"value1"</code>, and then adding the pair <code>"test":"value2"</code>
    	 * would result in a url like the following:
    	 * 
    	 * <p>
    	 * <code>
    	 * path/to/resource?test=value1&test=value2
    	 * </code>
    	 * </p>
    	 * 
    	 * Use {@link #setQueryParameter(String, String)} to clobber the values
    	 * 
    	 * 
    	 */
    	public WrappedWebRequest addQueryParameter(String name, String value){
    		List<NameValuePair> mylist=wq.getRequestParameters()
    									.stream()
    									.collect(Collectors.toList());
    		mylist.add(new NameValuePair(name, value));
    		wq.setRequestParameters(mylist);
    		return this;
    	}
    	
    	/**
    	 * Add the name / value pair encoded into to the URL, <b>with</b> clobbering
    	 * any previous value that was present under that name. For example, adding
    	 * the pair <code>"test":"value1"</code>, and then adding the pair <code>"test":"value2"</code>
    	 * would result in a url like the following:
    	 * 
    	 * <p>
    	 * <code>
    	 * path/to/resource?test=value2
    	 * </code>
    	 * </p>
    	 * Use {@link #addQueryParameter(String, String)} to append the values instead
    	 * 
    	 * 
    	 */
    	public WrappedWebRequest setQueryParameter(String name, String value){
    		List<NameValuePair> mylist=wq.getRequestParameters()
    						.stream()
    						.filter(nv->!nv.getName().equals(name))
    						.collect(Collectors.toList());
    		mylist.add(new NameValuePair(name, value));
    		wq.setRequestParameters(mylist);
    		return this;
    	}
    	public WebRequest get(){
    		wq.setCharset("UTF-8");
    		return wq;
    	}
    }
    public WrappedWebRequest newGetRequest(String path) throws MalformedURLException {
        return new WrappedWebRequest(new WebRequest(new URL(constructUrlFor(path)), HttpMethod.GET));
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
