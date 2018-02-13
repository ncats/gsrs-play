package ix.core.util;

import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
/**
 * Rule that will run a test as many times as stated in the RepeatTest annotation.
 */
public class RepeatTestRule implements TestRule {


    private static class RepeatedStatement extends Statement{

        private final Statement delegate;
        private final int times;

        public RepeatedStatement(Statement delegate, int times) {
            this.delegate = delegate;
            this.times = times;
        }

        @Override
        public void evaluate() throws Throwable {
            for(int i=0; i< times; i++){
                delegate.evaluate();
            }
        }
    }
    @Override
    public Statement apply(Statement base, Description description) {
        RepeatTest repeat = description.getAnnotation(RepeatTest.class);
        if(repeat !=null){
            return new RepeatedStatement(base, repeat.times());
        }
        return base;
    }
}
