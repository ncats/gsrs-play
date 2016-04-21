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

    private boolean printStart = true;
    private boolean printEnd = false;

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

    @Override
    protected void starting(Description description) {
        if(printStart) {
            out.println("Starting test: " + description.getMethodName());
        }
    }

    @Override
    protected void finished(Description description) {
        if(printEnd) {
            out.println("Ending test: " + getClass().getCanonicalName() + " . " + description.getMethodName());
        }
    }

    public boolean shouldPrintStart() {
        return printStart;
    }

    public TestNamePrinter setPrintStart(boolean printStart) {
        this.printStart = printStart;
        return this;
    }

    public boolean shouldPrintEnd() {
        return printEnd;
    }

    public TestNamePrinter setPrintEnd(boolean printEnd) {
        this.printEnd = printEnd;
        return this;
    }
}
