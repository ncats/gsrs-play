package ix.core.util;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JUnit Rule that allows tests to be marked as Expected to Fail
 * which we can use to write tests for features that don't work
 * yet.  The test will pass JUnit if the method throws any uncaught Throwable.
 *   If the method is annotated as ExpectedToFail and it doesn't throw anything,
 *   then it is marked as failure.  This is to make sure once a bug fix or new
 *   feature is implemented, we remove the ExpectToFail annotation.
 *
 *   <p>
 *       Example usage:
 *
 *   </p>
 *   <pre>
 *
 *@Rule
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

 @ExpectedToFail
 @Test
 public void shouldFail(){
 assertTrue(true);
 }
 *   </pre>
 *
  * <p>
  *     The middle test "passes" in Junit because even though it throws an exception,
  *     our new {@link ExpectedToFail} annotation is on the method so it passes.
  *     The last test fails Junit because it is marked as Expected to fail but doesn't throw an exception.
  * </p>
 * Created by katzelda on 9/19/16.
 */
public class ExpectFailureChecker extends ExternalResource{
    @Override
    public Statement apply(Statement base, Description description) {

       ExpectedToFail shouldFail = description.getAnnotation(ExpectedToFail.class);

        if(shouldFail ==null) {
            return super.apply(base, description);
        }
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                boolean failed=true;
                try{
                    base.evaluate();
                    failed=false;
                }catch(Throwable t){
                    //expected to fail and threw an exception is OK

                }
                if(!failed){
                    throw new AssertionError("expected to fail but test passed");
                }
            }
        };

    }

    /**
     * Annotation to put on a test method
     * that is expected to fail.  The test will
     * fail if it is annotated with this
     * and doesn't throw an uncaught throwable;
     * and will pass otherwise.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExpectedToFail{
        /**
         * The reason this test is annotated to fail.
         * @return a String message.
         */
        String reason() default "";
    }
}
