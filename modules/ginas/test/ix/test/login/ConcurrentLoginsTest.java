package ix.test.login;

import chemaxon.nfunk.jep.function.Abs;
import ix.test.ix.test.server.AbstractSession;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.util.MultiThreadInteracter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static ix.test.login.LoginUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by katzelda on 3/23/16.
 */
@RunWith(Parameterized.class)
public class ConcurrentLoginsTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);


    @Parameterized.Parameters
    public static List<Object[]> params(){

        List<Object[]> list = new ArrayList<>();
        for(LoginStrategy s : LoginStrategy.values()){

            for(LoginStrategy s2 : LoginStrategy.values()){
                    list.add(new Object[]{s, s2});
                }
            }

        return list;
    }

    private LoginStrategy strategy, strategy2;

    public ConcurrentLoginsTest(LoginStrategy strategy, LoginStrategy strategy2){
        this.strategy = strategy;
        this.strategy2 = strategy2;
    }

    @Test
    public void twoDifferentLoggedInUsersViewSubstancesMultiThreadedInterleaved() {

        final GinasTestServer.User user1 = ts.getFakeUser1();
        final GinasTestServer.User user3 = ts.getFakeUser3();

        try (final AbstractSession session1 = strategy.loginAs(ts, user1);
             final AbstractSession session3 = strategy2.loginAs(ts, user3);) {

            MultiThreadInteracter.Builder builder = new MultiThreadInteracter.Builder();

            builder.newThread()
                    .step(1, new MultiThreadInteracter.Step() {

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(session3.get("ginas/app/substances"), user3);
                        }
                    })
                    .step(3, new MultiThreadInteracter.Step() {

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(session3.get("ginas/app/substances"), user3);
                        }
                    });


            builder.newThread()
                    .step(2, new MultiThreadInteracter.Step() {

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(session1.get("ginas/app/substances"), user1);
                        }
                    })
                    .step(4, new MultiThreadInteracter.Step() {

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(session1.get("ginas/app/substances"), user1);
                        }
                    });


            MultiThreadInteracter multiThreadInteracter = builder.build();

            multiThreadInteracter.run();

        }
    }

    @Test
    public void twoDifferentLoggedInUsersViewSubstancesMultiThreadedConcurrently(){

        final GinasTestServer.User user1 = ts.getFakeUser1();
        final GinasTestServer.User user3 = ts.getFakeUser3();

        try (final AbstractSession session1 = strategy.loginAs(ts, user1);
             final AbstractSession session3 = strategy2.loginAs(ts, user3);) {

            MultiThreadInteracter.Builder builder = new MultiThreadInteracter.Builder();

            builder.newThread()
                    .step(1, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs( session3.get("ginas/app/substances"), user3);
                        }
                    })
                    .step(3, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs( session3.get("ginas/app/substances"), user3);
                        }
                    });


            builder.newThread()
                    .step(2, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs( session1.get("ginas/app/substances"), user1);
                        }
                    })
                    .step(3, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs( session1.get("ginas/app/substances"), user1);
                        }
                    });


            MultiThreadInteracter multiThreadInteracter = builder.build();

            multiThreadInteracter.run();

        }


    }

    @Test
    public void twoDifferentLoggedInAndNotLoggedInUsersViewSubstancesMultiThreadedConcurrently(){

        final GinasTestServer.User user1 = ts.getFakeUser1();

        try (final AbstractSession session1 = strategy.loginAs(ts, user1);
             final AbstractSession session3 = strategy2.getNotLoggedIn(ts)) {

            MultiThreadInteracter.Builder builder = new MultiThreadInteracter.Builder();

            builder.newThread()
                    .step(1, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureNotLoggedIn( session3.get("ginas/app/substances"));
                        }
                    })
                    .step(3, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureNotLoggedIn( session3.get("ginas/app/substances"));
                        }
                    });


            builder.newThread()
                    .step(2, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs( session1.get("ginas/app/substances"), user1);
                        }
                    })
                    .step(3, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs( session1.get("ginas/app/substances"), user1);
                        }
                    });


            MultiThreadInteracter multiThreadInteracter = builder.build();

            multiThreadInteracter.run();

        }


    }

    @Test
    public void manyLoggedInAndNotLoggedInUsersViewSubstancesMultiThreadedConcurrently(){



        MultiThreadInteracter.Builder builder = new MultiThreadInteracter.Builder();
        int numLoggedInUsers = 10;

        int numNotLoggedInUsers=20;

        final Random rand = new Random();
        for(int i=0; i< numLoggedInUsers; i++){
            final GinasTestServer.User user = ts.createNormalUser("testUser"+i, "password" +i);
            final AbstractSession[] session = new AbstractSession[1];
            builder.newThread()
                    .step(0, new MultiThreadInteracter.Step() {
                        @Override
                        public void execute() throws Exception {
                            session[0] = strategy.loginAs(ts, user);
                        }
                    })
                    .step(rand.nextInt(2) + 1, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(  session[0].get("ginas/app/substances"), user);
                        }
                    })
                    .step(3, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureLoggedInAs(  session[0].get("ginas/app/substances"), user);
                        }
                    })
                    .step(5, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            session[0].logout();
                        }
                    });

        }

        for(int i=0; i< numNotLoggedInUsers; i++){

            final AbstractSession[] session = new AbstractSession[1];
            builder.newThread()
                    .step(0, new MultiThreadInteracter.Step() {
                        @Override
                        public void execute() throws Exception {
                            session[0] = strategy2.getNotLoggedIn(ts);
                        }
                    })
                    .step(rand.nextInt(2) + 1, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureNotLoggedIn(session[0].get("ginas/app/substances"));
                        }
                    })
                    .step(3, new MultiThreadInteracter.Step(){

                        @Override
                        public void execute() throws Exception {
                            ensureNotLoggedIn(session[0].get("ginas/app/substances"));
                        }
                    })
            ;

        }

        MultiThreadInteracter multiThreadInteracter = builder.build();

        multiThreadInteracter.run();

    }


    private enum LoginStrategy{
        BROWSER{
            @Override
            public AbstractSession<?> loginAs(GinasTestServer ts, GinasTestServer.User user) {
                return ts.newBrowserSession(user);
            }

            @Override
            public AbstractSession<?> getNotLoggedIn(GinasTestServer ts) {
                return ts.notLoggedInBrowserSession();
            }
        },
        API_TOKEN{
            @Override
            public AbstractSession<?> loginAs(GinasTestServer ts, GinasTestServer.User user) {
                return ts.newRestSession(user, RestSession.AUTH_TYPE.TOKEN);
            }

            @Override
            public AbstractSession<?> getNotLoggedIn(GinasTestServer ts) {
                return ts.notLoggedInRestSession();
            }
        },
        API_USER_PASS{
            @Override
            public AbstractSession<?> loginAs(GinasTestServer ts, GinasTestServer.User user) {
                return ts.newRestSession(user, RestSession.AUTH_TYPE.USERNAME_PASSWORD);
            }

            @Override
            public AbstractSession<?> getNotLoggedIn(GinasTestServer ts) {
                return ts.notLoggedInRestSession();
            }
        },
        API_KEY{
            @Override
            public AbstractSession<?> loginAs(GinasTestServer ts, GinasTestServer.User user) {
                return ts.newRestSession(user, RestSession.AUTH_TYPE.USERNAME_KEY);
            }

            @Override
            public AbstractSession<?> getNotLoggedIn(GinasTestServer ts) {
                return ts.notLoggedInRestSession();
            }
        };

        public abstract AbstractSession<?> loginAs(GinasTestServer ts, GinasTestServer.User user);

        public abstract AbstractSession<?> getNotLoggedIn(GinasTestServer ts);
    }
}
