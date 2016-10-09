package ix.test;

import org.junit.ClassRule;

import ix.test.server.GinasTestServer;

public class AbstractGinasClassServerTest extends AbstractGinasTest {

	@ClassRule
	public static GinasTestServer ts = new GinasTestServer(9001);
	 
}
