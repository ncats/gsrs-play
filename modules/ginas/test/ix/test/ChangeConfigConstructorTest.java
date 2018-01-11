package ix.test;

import ix.AbstractGinasServerTest;
import ix.core.util.RunOnly;
import ix.test.server.GinasTestServer;
import ix.test.util.TestNamePrinter;
import org.junit.Rule;
import org.junit.Test;
import play.api.Configuration;
import static org.junit.Assert.*;
import java.util.HashMap;

/**
 * Created by katzelda on 5/2/16.
 */
public class ChangeConfigConstructorTest extends AbstractGinasServerTest {
    
    @Override
    public GinasTestServer createGinasTestServer(){
    	/*return new GinasTestServer(new HashMap<String, Object>() {
            {
                put("fake.field", "true");
                put("fake.field2", "true");
            }
        });*/

    	return new GinasTestServer("fake.field = true\n" +
                                    "fake.field2 = true");
    }

    @Test
    public void addManyNewItemsToConfigMap() {
        Configuration config = ts.getApplication().configuration();

        assertTrue((Boolean) config.getBoolean("fake.field").get());
        assertTrue((Boolean) config.getBoolean("fake.field2").get());
    }

    @Test

    public void removeItemFromConfig(){
        ts.stop();
        ts.removeConfigProperty("fake.field");

        ts.start();
        Configuration config = ts.getApplication().configuration();

        assertFalse(config.getBoolean("fake.field").isDefined());
        assertTrue((Boolean) config.getBoolean("fake.field2").get());
    }

    @Test
    public void canRemoveOtherItemsInOtherTests(){
        ts.stop();
        ts.removeConfigProperty("fake.field2");

        ts.start();
        Configuration config = ts.getApplication().configuration();
        assertFalse(config.getBoolean("fake.field2").isDefined());
        assertTrue((Boolean) config.getBoolean("fake.field").get());
    }

    @Test
    public void changesPersistedAcrossRestarts(){
        ts.stop();
        ts.removeConfigProperty("fake.field2");

        ts.start();
        Configuration config = ts.getApplication().configuration();
        assertFalse(config.getBoolean("fake.field2").isDefined());
        assertTrue((Boolean) config.getBoolean("fake.field").get());

        ts.restart();
        Configuration config2 = ts.getApplication().configuration();
        assertFalse(config2.getBoolean("fake.field2").isDefined());
        assertTrue((Boolean) config2.getBoolean("fake.field").get());
    }
}
