package ix.test.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.libs.ws.WSResponse;

/**
 * Parent class that handles generic inxight session
 * handling and logging out.  This class is {@link Closeable}
 * so it can be put in the Java 7 try-with-resource block
 * and auto-logout when the scope of the try block ends.
 *
 *
 * @param <T> the return value of {@link #logout()}
 *
 * Created by katzelda on 3/17/16.
 */
public abstract class AbstractSession<T> implements Closeable{

    public static long getDefaultTimeout(){
        return timeout;
    }

    protected static long timeout= 10000L;
    private boolean loggedIn = true;

    private final boolean neverLogout;

    private int port;

    private final GinasTestServer.User user;

    private final GinasTestServer ts;

    public AbstractSession(GinasTestServer ts, int port){
        this.ts = Objects.requireNonNull(ts);
        //null values for defaults
        this.port = port;
        this.user = null;

        this.neverLogout = true;
    }



    public AbstractSession(GinasTestServer ts, GinasTestServer.User user, int port){
        this.ts = Objects.requireNonNull(ts);

        if(port <1){
            throw new IllegalArgumentException("port can not be < 1");
        }
        this.port = port;
        this.user = Objects.requireNonNull(user);;
        this.neverLogout = false;
    }

    protected GinasTestServer getTestSever() {
        return ts;
    }

    public abstract RestSession getRestSession();
    public abstract BrowserSession getBrowserSession();
    public int getPort() {
        return port;
    }
    public JsonNode extractJSON(WSResponse wsResponse1){
        Objects.requireNonNull(wsResponse1);

        int status2 = wsResponse1.getStatus();
        if(status2>300){
            System.out.println("That's an error!");
            System.out.println(wsResponse1.getBody());
        }
        if(status2 != 200 && status2 != 201){
            throw new IllegalStateException("response status Not OK : " + status2 + " in " + wsResponse1.getBody());
        }
        JsonNode returned=null;
        String body = wsResponse1.getBody();
        try{
        	returned = (new ObjectMapper()).readTree(body);
        }catch(Exception e){
        	System.out.println("That's an error!");
            System.out.println(body);
            throw new IllegalStateException(e);
        }
        Objects.requireNonNull(returned);
        return returned;
    }

    public String getUserName(){
        String path = getTestSever().getHttpResolver().apiV1("whoami", true);
        System.out.println("path = " + path);
        WSResponse wsResponse1 = get(path);

        if(wsResponse1.getStatus() != 200){
            return null;
        }
        JsonNode node = extractJSON(wsResponse1);
        return node.get("identifier").asText();
    }

    public GinasTestServer.User getUser(){
        return user;
    }

    public String constructUrlFor(String path) {
        if(path.startsWith("http"))return path;
    	StringBuilder sb = new StringBuilder("http://localhost:")
                .append(port);
    	
    	if(!path.startsWith("/")){
                sb.append('/');
    	}
        return sb.append(path)
                 .toString();
                
    }




    /**
     * Is this user logged in.
     * @return {@code true} if  logged in; {@code false} otherwise.
     */
    public boolean isLoggedIn(){
        return loggedIn;
    }

    /**
     * Log out.
     * @throws IOException
     */
    @Override
    public void close() {
        logout();
    }

    /**
     * Logout of this session.
     * If this method is called more than once,
     * then the return value will be null after the
     * first call.
     * @return the return type.
     */
    public T logout(){
        if(neverLogout || !isLoggedIn()){
            //do nothing
            return null;
        }


        T result= doLogout();
        loggedIn = false;
        return result;

    }

    /**
     * template method for the child class to acutally perform the log out
     * function (clearing session or calling logout url etc).
     * @return the return response; may be null if there is nothing to return.
     */
    protected abstract T doLogout();

    /**
     * Convert the given path into a Play {@link WSResponse}.
     * @param path the <strong>relative</strong> path to get; can not be null.
     * @return a new {@link WSResponse} for getting the response for that path.
     */
    public abstract WSResponse get(String path);
}
