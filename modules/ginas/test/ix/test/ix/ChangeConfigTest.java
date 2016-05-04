package ix.test.ix;

import ix.test.ix.test.server.GinasTestServer;
import org.junit.Rule;
import org.junit.Test;
import play.api.Configuration;

import java.util.HashMap;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 5/2/16.
 */
public class ChangeConfigTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer();

    @Test
    public void unchanged(){
        Configuration config = ts.getApplication().configuration();

        assertFalse(config.getBoolean("new.field").isDefined());
        assertFalse(config.getBoolean("new.field2").isDefined());
    }

    @Test
    public void addNewItemToConfig(){
        ts.stop(true);
        ts.modifyConfig("new.field", "true");

        ts.start();
        Configuration config = ts.getApplication().configuration();

        assertTrue((Boolean)config.getBoolean("new.field").get());
        assertFalse(config.getBoolean("new.field2").isDefined());
    }

    @Test
    public void addManyNewItemsToConfig(){
        ts.stop(true);
        ts.modifyConfig("new.field", "true");
        ts.modifyConfig("new.field2", "true");

        ts.start();
        Configuration config = ts.getApplication().configuration();

        assertTrue((Boolean)config.getBoolean("new.field").get());
        assertTrue((Boolean)config.getBoolean("new.field2").get());
    }

    @Test
    public void addManyNewItemsToConfigMap(){
        ts.stop(true);

        ts.modifyConfig(new HashMap<String, Object>() {
                            {
                                put("new.field", "true");
                                put("new.field2", "true");
                            }
                        });

        ts.start();
        Configuration config = ts.getApplication().configuration();

        assertTrue((Boolean)config.getBoolean("new.field").get());
        assertTrue((Boolean)config.getBoolean("new.field2").get());
    }
}
