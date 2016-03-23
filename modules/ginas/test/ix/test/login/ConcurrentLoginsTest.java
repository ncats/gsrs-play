package ix.test.login;

import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.util.MultiThreadInteracter;
import org.junit.Rule;
import org.junit.Test;

import static ix.test.login.LoginUtil.*;
import java.util.Random;

/**
 * Created by katzelda on 3/23/16.
 */
public class ConcurrentLoginsTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);

    @Test
    public void twoDifferentLoggedInUsersViewSubstancesMultiThreadedInterleaved() {

        final GinasTestServer.User user1 = ts.getFakeUser1();
        final GinasTestServer.User user3 = ts.getFakeUser3();

        try (final RestSession session1 = ts.newRestSession(user1);
             final RestSession session3 = ts.newRestSession(user3);) {

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

        try (final RestSession session1 = ts.newRestSession(user1);
             final RestSession session3 = ts.newRestSession(user3);) {

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

        try (final RestSession session1 = ts.newRestSession(user1);
             final RestSession session3 = ts.notLoggedInRestSession();) {

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
            final RestSession[] session = new RestSession[1];
            builder.newThread()
                    .step(0, new MultiThreadInteracter.Step() {
                        @Override
                        public void execute() throws Exception {
                            session[0] = ts.newRestSession(user);
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

            final RestSession[] session = new RestSession[1];
            builder.newThread()
                    .step(0, new MultiThreadInteracter.Step() {
                        @Override
                        public void execute() throws Exception {
                            session[0] = ts.notLoggedInRestSession();
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
}
