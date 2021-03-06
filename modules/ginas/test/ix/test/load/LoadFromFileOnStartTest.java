package ix.test.load;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.AbstractGinasTest;
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
import ix.test.server.GinasTestServer.User;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;

public class LoadFromFileOnStartTest extends AbstractGinasServerTest{

	 
	 @Override
	 public GinasTestServer createGinasTestServer(){
		 return new GinasTestServer("ix.ginas.load.file = "+ LoadDataSetTest.TEST_TESTDUMPS_REP90_GINAS);
	 }
	 
	 

	RestSession session;
	SubstanceAPI api;
	User u;

	@Before
	public void setup() {
		u=ts.createAdmin("madeUp", "SomePassword");
		session = ts.newRestSession(u);
		api = new SubstanceAPI(session);
	}

	@After
	public void breakdown() {
		session.close();
	}
		
	@Test
	public void ensureLoadingrep90FromConfFileLoadsSameData() throws Exception {
		try(BrowserSession session = ts.newBrowserSession(u)){
			LoadDataSetTest.runRepTests(session);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}

	}
}
