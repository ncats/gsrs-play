package ix.core.util;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;
import ix.core.util.ExpectFailureChecker.ExpectedToFail;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by katzelda on 9/19/16.
 */
public class TestExpectToFail {

    @Rule
    public ExpectFailureChecker failureChecker = new ExpectFailureChecker();

    @Test
    public void normalPass(){
        assertTrue(true);
    }

    @ExpectedToFail(reason = "this explains why we should fail. ex: not yet implemented")
    @Test
    public void expectedToFailAndThereforePass(){
        assertTrue(false);
    }


   @Test
   public void checkThatMethodsThatExpectToFailShouldFailJunitIfTheyPass(){
       JUnitCore core = new JUnitCore();

       AtomicInteger testStarted=new AtomicInteger(0);
       AtomicInteger testsFailed=new AtomicInteger(0);

       core.addListener(new RunListener(){
           @Override
           public void testFailure(Failure failure) throws Exception {
               testsFailed.incrementAndGet();
           }

           @Override
           public void testRunStarted(Description description) throws Exception {
               testStarted.incrementAndGet();
           }
       });
       core.run(ThisShouldFail.class);

       assertEquals(1, testStarted.get());
       assertEquals(1, testsFailed.get());

   }

    public static class ThisShouldFail {

        @Rule
        public ExpectFailureChecker failureChecker = new ExpectFailureChecker();

        @ExpectFailureChecker.ExpectedToFail
        @Test
        public void shouldFail() {
            assertTrue(true);
        }

    }

}
