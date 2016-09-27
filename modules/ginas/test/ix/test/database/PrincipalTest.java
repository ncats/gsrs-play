package ix.test.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;

import ix.core.controllers.PrincipalFactory;
import ix.core.models.Edit;
import ix.core.models.Principal;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.util.TestNamePrinter;

public class PrincipalTest {

    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
	public GinasTestServer ts = new GinasTestServer(9001);
    
    
    @Test
    public void ensureUsernamesAreCaseInsensitive() {
          
          Principal p1=PrincipalFactory.registerIfAbsent(new Principal("TEST",null));
          Principal p2=PrincipalFactory.registerIfAbsent(new Principal("test",null));
          
          
          assertEquals(p1.id,p2.id);
    }
}
