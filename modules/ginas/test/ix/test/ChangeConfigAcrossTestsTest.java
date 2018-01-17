package ix.test;

import ix.AbstractGinasServerTest;
import ix.test.server.GinasTestServer;
import org.junit.Test;
import play.Play;
import play.api.Configuration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by katzelda on 1/12/18.
 */
public class ChangeConfigAcrossTestsTest extends AbstractGinasServerTest{

    @Override
    public GinasTestServer createGinasTestServer() {
        GinasTestServer ts =  super.createGinasTestServer();

        ts.modifyConfig("myProperty", true, GinasTestServer.ConfigOptions.ALL_TESTS);
        return ts;
    }

    @Test
    public void test1(){

        Configuration config = ts.getApplication().configuration();

        assertTrue((Boolean)config.getBoolean("myProperty").get());
    }


    @Test
    public void test2(){
        test1();
    }
}
