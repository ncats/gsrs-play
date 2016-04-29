package ix.test.load;

import ix.ginas.controllers.GinasApp;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.SubstanceSearch;
import ix.test.util.TestNamePrinter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by katzelda on 4/13/16.
 */
public abstract class AbstractLoadDataSetTest {



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
