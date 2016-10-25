package ix.test.database;

import ix.AbstractGinasServerTest;
import org.junit.Test;
import play.api.Configuration;

import java.util.HashMap;

import static ix.test.util.TestUtil.assertContains;

public class DatabaseDownTest extends AbstractGinasServerTest {

    @Test
    public void runAppWithSecondDBDownAndEvolutionDisabledTest(){
        Configuration config = ts.getApplication().configuration();
        String content = ts.notLoggedInBrowserSession().get("ginas/app").getBody();
        assertContains(content,"substances");

        ts.stop(true);
        ts.modifyConfig(new HashMap<String, Object>() {
            {
                put("db.default2.driver", "com.mysql.jdbc.Driver");
                put("db.default2.url", "jdbc:mysql://localhost:3306/second");
                put("db.default2.user", "root");
                put("db.default2.password", "password");
                put("evolutionplugin", "disabled");
            }
        });

        ts.start();
        content = ts.notLoggedInBrowserSession().get("ginas/app").getBody();
        assertContains(content,"substances");
    }


    @Test
    public void runAppWithSecondDBDownAndEvolutionNotDisabledTest(){
        Configuration config = ts.getApplication().configuration();
        String content = ts.notLoggedInBrowserSession().get("ginas/app").getBody();
        assertContains(content,"substances");

        ts.stop(true);

        ts.modifyConfig(new HashMap<String, Object>() {
            {

                put("db.default2.driver", "com.mysql.jdbc.Driver");
                put("db.default2.url", "jdbc:mysql://localhost:3306/second");
                put("db.default2.user", "root");
                put("db.default2.password", "password");
                // put("evolutionplugin", "disabled");
            }
        });

        expectedException.expectMessage("dataSource or dataSourceClassName or jdbcUrl is required");
        ts.start();
        content = ts.notLoggedInBrowserSession().get("ginas/app").getBody();

        //System.out.println("content:" + content);
        //assertContains(content,"substances");
    }
}
