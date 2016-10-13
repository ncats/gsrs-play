package ix.test.load;

import org.junit.Before;

import ix.AbstractGinasServerTest;
import ix.test.server.GinasTestServer;

/**
 * Created by katzelda on 4/13/16.
 */
public abstract class AbstractLoadDataSetTest extends AbstractGinasServerTest{


    protected GinasTestServer.User admin;


    @Before
    public void createAdmin(){
        admin = ts.createAdmin("admin2", "adminPass");
    }
    
}
