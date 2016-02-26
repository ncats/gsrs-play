import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import java.util.concurrent.Callable;

import org.junit.rules.ExternalResource;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.test.TestServer;

/**
 * JUnit Rule to handle starting and stopping
 * a Ginas Server for testing.
 *
 * <p>
 *     Example usage:
 *
 *     <pre>
 *         @Rule
 *         GinasTestServer ts = new GinasTestServer(9001);
 *
 *         ...
 *
 *         @Test
 *         public void myTest(){
 *             ts.run(new Callable<Void>() {
 *
 *                public Void call(){
 *                 //do stuff
 *                 return null;
 *             });
 *         }
 *
 *         @Test
 *         public void withJava8Lambda(){
 *             ts.run( () -> {
 *                //do stuff
 *                });
 *         }
 *     </pre>
 *
 *
 * </p>
 *
 *
 *
 *
 * Created by katzelda on 2/25/16.
 */
public class GinasTestServer extends ExternalResource{
	 private static final String API_URL_USERFETCH = "http://localhost:9001/ginas/app/api/v1/whoami";
	 private static final String VALIDATE_URL = "http://localhost:9001/ginas/app/api/v1/substances/@validate";
	 private static final String API_URL_SUBMIT = "http://localhost:9001/ginas/app/api/v1/substances";
	 private static final String API_URL_FETCH = "http://localhost:9001/ginas/app/api/v1/substances($UUID$)?view=full";
     
	 
    private static long timeout= 10000L;
    private TestServer ts;
    private int port;

    private boolean loggedIn;
    private String username;
    private String password;
    private String key;
    private String token;
    private long deadtime=0;
    
    public enum AUTH_TYPE{
    	USERNAME_PASSWORD,
    	USERNAME_KEY,
    	TOKEN,
    	NONE
    }
    
    private AUTH_TYPE authType=AUTH_TYPE.NONE; 

    public GinasTestServer(int port){
       this.port = port;
       
    }
    
    public GinasTestServer(int port, AUTH_TYPE atype){
    	this(port);
    	this.setAuthenticationType(atype);
    }


    public void login(String username, String password){
        //TODO actually login
        loggedIn=true;
        this.username=username;
        this.password=password;
    }

    public void logout(){
        //TODO actually log out
        loggedIn = false;
        
        this.username=null;
        this.password=null;
        this.key=null;
        this.token=null;
        this.deadtime=0;
    }

    public void run(final Callable<Void> callable){
        running(ts, new Runnable(){
            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }
    
    public void setAuthenticationType(AUTH_TYPE atype){
    	this.authType=atype;
    	
    			
    }
    
    public WSRequestHolder  url(String url){
    	WSRequestHolder ws = WS.url(url);
	    	switch(authType){
			case TOKEN:
				ws.setHeader("auth-token", this.token);
				break;
			case USERNAME_KEY:
				ws.setHeader("auth-username", this.username);
		    	ws.setHeader("auth-key", this.key);
				break;
			case USERNAME_PASSWORD:
				ws.setHeader("auth-username", this.username);
		    	ws.setHeader("auth-password", this.password);
				break;
			default:
				break;
	    	}
        return ws;
    }
    
    public WSResponse validateSubstance(JsonNode js){
    	WSResponse wsResponse1 = this.url(VALIDATE_URL).post(js).get(timeout);
    	return wsResponse1;
        
    }
    
    public WSResponse submitSubstance(JsonNode js){
    	WSResponse wsResponse1 = this.url(API_URL_SUBMIT).post(js).get(timeout);
    	return wsResponse1;
        
    }
    
    public WSResponse fetchSubstance(String uuid){
    	WSResponse wsResponse1 = this.url(API_URL_FETCH.replace("$UUID$", uuid)).get().get(timeout);
    	return wsResponse1;
    }
    
    
    
    private void refreshAuthInfoByUserNamePassword(){
    	WSRequestHolder  ws = WS.url(API_URL_USERFETCH);
    	ws.setHeader("auth-username", this.username);
    	ws.setHeader("auth-password", this.password);
    	WSResponse wsr=ws.get().get(timeout);
    	JsonNode userinfo=wsr.asJson();
    	token=userinfo.get("computedToken").asText();
    	key=userinfo.get("key").asText();
    	deadtime=System.currentTimeMillis()+userinfo.get("tokenTimeToExpireMS").asLong();
    	
    	
    }
    
    private void refreshTokenIfNeccesarry(){
    	if(System.currentTimeMillis()>this.deadtime){
    		refreshAuthInfoByUserNamePassword();	
    	}
    	
    }

    public void run(final Runnable r){
        running(ts,r);
    }

    @Override
    protected void before() throws Throwable {
        ts = testServer(port);
    }

    @Override
    protected void after() {
        if(loggedIn){
            logout();
        }
        stop(ts);
    }
}
