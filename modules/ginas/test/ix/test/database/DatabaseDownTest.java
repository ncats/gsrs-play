package ix.test.database;

import ix.AbstractGinasServerTest;
import org.junit.Test;
import play.api.Configuration;

import java.util.HashMap;

import static ix.test.util.TestUtil.assertContains;

public class DatabaseDownTest extends AbstractGinasServerTest {

    @Test
    public void runAppWithSecondDBDownAndEvolutionDisabledTest(){
        String content = ts.notLoggedInBrowserSession().get("ginas/app").getBody();
        assertContains(content,"substances");

        ts.stop(true);
        ts.modifyConfig(new HashMap<String, Object>() {
            {
                put("db.default2.driver", "com.mysql.jdbc.Driver");
                put("db.default2.url", "jdbc:mysql://localhost:3306/second");
                put("db.default2.user", "root");
                put("db.default2.password", "password");
            }
        });

        ts.start();
        content = ts.notLoggedInBrowserSession().get("ginas/app").getBody();
        assertContains(content,"substances");
    }


    
    //In the ideal world, this would be ok, and wouldn't fail.
    //But now it does, because it can't use the ebean pieces if the
    //database can't be connected
    @Test
    public void runAppWithSecondDBDownAndEvolutionNotDisabledTest(){
        String content = ts.notLoggedInBrowserSession().get("ginas/app").getBody();
        assertContains(content,"substances");

        ts.stop(true);

        ts.modifyConfig(new HashMap<String, Object>() {
            {

                put("db.default2.driver", "com.mysql.jdbc.Driver");
                put("db.default2.url", "jdbc:mysql://localhost:3306/second");
                put("db.default2.user", "root");
                put("db.default2.password", "password");
                put("ebean.default2","ix.ginas.secondarymodels.*");
            }
        });

        expectedException.expectMessage("dataSource or dataSourceClassName or jdbcUrl is required");
        ts.start();
        content = ts.notLoggedInBrowserSession().get("ginas/app").getBody();

        //System.out.println("content:" + content);
        assertContains(content,"substances");
    }
}
