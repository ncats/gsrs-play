package ix.test.authentication;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.UserProfileFactory;
import ix.core.factories.EntityProcessorFactory;
import ix.core.models.UserProfile;
import ix.core.util.EntityUtils;
import ix.core.util.StreamUtil;
import ix.test.GinasTestSuite;
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
import org.junit.Test;
import play.Configuration;
import play.Play;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Created by katzelda on 3/6/17.
 */
public class AddUserMultiThreadTest extends AbstractAddUserTest {



    @Test
    public void multipleRequestsShouldNotMakeDuplicateUserProfiles() throws Exception{

//        EntityProcessorFactory.getInstance(Play.application()).register(EntityUtils.getEntityInfoFor(UserProfile.class),
//                new BlockingUserProfileEntityProcessor()
//                );

        createPrincipalWithoutUserProfile("fakeUser");

//        EntityProcessorFactory epf=EntityProcessorFactory
//                .getInstance(null);
//
//        epf.register(UserProfile.class, new BlockingUserProfileEntityProcessor(), true);
        final CountDownLatch latch = new CountDownLatch(2);
        Runnable r = () ->{try{
            //BrowserSession's webClient is not threadsafe
            //I've gotten intermittent javascript errors
            //so just in case each thread uses a new BrowserSession
            try(BrowserSession session2 = ts.newBrowserSession(ts.getAdmin())) {


                latch.countDown();
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                queryAllCurrentUsers(session2);
            }
        }catch(IOException e){throw new UncheckedIOException(e); }};


       new Thread(r).start();
        new Thread(r).start();
        List<UserResult> actual = queryAllCurrentUsers();
        System.out.println("done");

        List<UserResult> expected = new ArrayList(DEFAULT_USERS);
        expected.add(new UserResult("fakeUser", false,null));
        assertEquals(expected, actual);

        try(Stream<UserProfile> users=UserProfileFactory.userStream()){
        	assertEquals(1, users.filter(up -> up.user.username.equals("fakeUser")).count());
        }
    }


}


