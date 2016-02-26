import static play.test.Helpers.running;
import static play.test.Helpers.stop;
import static play.test.Helpers.testServer;

import org.junit.rules.ExternalResource;
import play.test.TestServer;

import java.util.concurrent.Callable;

/**
 * JUnit Rule to handle starting and stopping
 * a Ginas Server around each @Test method.
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
    private TestServer ts;
    private int port;

    private boolean loggedIn;

    public GinasTestServer(int port){
       this.port = port;
    }


    public void login(String username, String password){
        //TODO actually login
        loggedIn=true;
    }

    public void logout(){
        //TODO actually log out
        loggedIn = false;
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
