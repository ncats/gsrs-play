package ix.test.util;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Test Rule that will listen for any tests that fail
 * due to htmlunit's FailingHttpStatusCodeException
 * which happens if a browswer sessions has a 4xx or 5xx status code
 * and if a test fails with this kind of error, print out
 * the raw html to the given PrintStream (defaults to STDOUT).
 *
 * This is very useful for tracking down the problems of
 * why a test failed since the exception message in this case is useless.
 *
 * Created by katzelda on 3/6/17.
 */
public class BrowserErrorPrinter extends TestWatcher{

    private PrintStream out;

    private boolean printStart = true;
    private boolean printEnd = true;

    /**
     * Write the test names to {@code System.out}.
     */
    public BrowserErrorPrinter(){
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
    public BrowserErrorPrinter(PrintStream out){
        Objects.requireNonNull(out);
        this.out = out;
    }

    @Override
    protected void failed(Throwable e, Description description) {
       if(e instanceof FailingHttpStatusCodeException){
           FailingHttpStatusCodeException ex = (FailingHttpStatusCodeException) e;
           out.println(ex.getResponse().getContentAsString());
           ex.getCause().printStackTrace();
       }
    }
}
