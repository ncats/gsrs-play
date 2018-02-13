package ix.core.util;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by katzelda on 10/21/16.
 */
public class RunOnlyTestRunner extends BlockJUnit4ClassRunner{

    private boolean isRunOnly=false;
    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public RunOnlyTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        if(isRunOnly){
            Description description= describeChild(method);
            if (method.getAnnotation(RunOnly.class) == null) {
                notifier.fireTestIgnored(description);
                return;
            }
        }
        super.runChild(method, notifier);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> runOnlyMethods = getTestClass().getAnnotatedMethods(RunOnly.class);
        if(runOnlyMethods==null || runOnlyMethods.isEmpty()){
            //nothing to runOnly
            return super.computeTestMethods();
        }
        //filter out any ignored tests
        runOnlyMethods =  runOnlyMethods.stream().filter(m ->   m.getAnnotation(Ignore.class) ==null)
                                .collect(Collectors.toList());
        if(runOnlyMethods.isEmpty()){
            return super.computeTestMethods();
        }
        isRunOnly=!runOnlyMethods.isEmpty();
        return super.computeTestMethods();
    }
}
