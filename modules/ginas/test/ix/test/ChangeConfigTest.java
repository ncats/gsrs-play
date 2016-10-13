package ix.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import ix.AbstractGinasServerTest;
import play.api.Configuration;
/**
 * Created by katzelda on 5/2/16.
 */
public class ChangeConfigTest extends AbstractGinasServerTest {

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
