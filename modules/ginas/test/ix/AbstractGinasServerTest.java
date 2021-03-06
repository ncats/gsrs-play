package ix;

import ix.test.server.GinasTestServerFactory;
import org.junit.Rule;

import ix.test.server.GinasTestServer;

/**
 * Abstract test class for building up / tearing down a 
 * test server
 * @author peryeata
 *
 */
public abstract class AbstractGinasServerTest extends AbstractGinasTest{
	 @Rule
	 public GinasTestServer ts = createGinasTestServer();
	 
	 public GinasTestServer createGinasTestServer(){
		 return GinasTestServerFactory.createNewTestServer();
	 }
	 
}
