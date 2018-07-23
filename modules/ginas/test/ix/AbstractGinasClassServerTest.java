package ix;

import ix.test.server.GinasTestServerFactory;
import org.junit.ClassRule;

import ix.test.server.GinasTestServer;

public abstract class AbstractGinasClassServerTest extends AbstractGinasTest {

	@ClassRule
	public static GinasTestServer ts = GinasTestServerFactory.createNewTestServer();
	 
}
