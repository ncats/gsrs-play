package ix.test.util;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.PrintStream;
import java.util.Objects;

/**
 * JUnit Rule that will print
 * the name of each test when it gets executed.
 *
 * Usage:
 *  The following will print the name of each test
 *  as it is run to STDOUT:
 *
 * <pre>
 *     public class MyTest{
 *
 *          @Rule
            public TestNamePrinter testNamePrinter = new TestNamePrinter();
 *
 *         @Test
 *         public void test1(){
 *             ...
 *         }
 *
 *         @Test
 *         public void test2(){
 *             ...
 *         }
 *     }
 *
 *
 * </pre>
 *
 * Created by katzelda on 4/7/16.
 */
public class TestNamePrinter extends TestWatcher{

    private PrintStream out;

    /**
     * Write the test names to {@code System.out}.
     */
    public TestNamePrinter(){
        this(System.out);
    }

    /**
     * Write the test names to the given PrintStream.
     * The stream will not be closed by this class.
     *
     * @param out the PrintStream to print to; can not be null.
     *
     * @throws  NullPointerException if out is null.
     */
    public TestNamePrinter(PrintStream out){
        Objects.requireNonNull(out);
        this.out = out;
    }

    protected void starting(Description description) {
        out.println("Starting test: " + description.getMethodName());
    }
}
