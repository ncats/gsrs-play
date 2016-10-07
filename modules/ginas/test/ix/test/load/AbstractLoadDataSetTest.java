package ix.test.load;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import ix.test.AbstractGinasTest;
import ix.test.server.GinasTestServer;
import ix.test.util.TestNamePrinter;

/**
 * Created by katzelda on 4/13/16.
 */
public abstract class AbstractLoadDataSetTest extends AbstractGinasTest{


    public GinasTestServer ts = new GinasTestServer();


    protected GinasTestServer.User admin;

    @Rule
    public RuleChain chain = RuleChain.outerRule( new TestNamePrinter())
                                                    .around(ts);

    @Before
    public void createAdmin(){
        admin = ts.createAdmin("admin2", "adminPass");
    }
    
}
