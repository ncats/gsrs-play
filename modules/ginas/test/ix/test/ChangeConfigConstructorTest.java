package ix.test;

import ix.AbstractGinasServerTest;
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
    	return new GinasTestServer(new HashMap<String, Object>() {
            {
                put("new.field", "true");
                put("new.field2", "true");
            }
        });
    }

    @Test
    public void addManyNewItemsToConfigMap() {
        Configuration config = ts.getApplication().configuration();

        assertTrue((Boolean) config.getBoolean("new.field").get());
        assertTrue((Boolean) config.getBoolean("new.field2").get());
    }

    @Test
    public void removeItemFromConfig(){
        ts.stop();
        ts.removeConfigProperty("new.field");

        ts.start();
        Configuration config = ts.getApplication().configuration();

        assertFalse(config.getBoolean("new.field").isDefined());
        assertTrue((Boolean) config.getBoolean("new.field2").get());
    }

    @Test
    public void canRemoveOtherItemsInOtherTests(){
        ts.stop();
        ts.removeConfigProperty("new.field2");

        ts.start();
        Configuration config = ts.getApplication().configuration();

        assertFalse(config.getBoolean("new.field2").isDefined());
        assertTrue((Boolean) config.getBoolean("new.field").get());
    }
}
