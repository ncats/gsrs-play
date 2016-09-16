package ix.core.plugins;

import ix.core.util.FilteredPrintStream;
import play.Application;
import play.Plugin;

import java.io.PrintStream;

/**
 * Created by katzelda on 9/15/16.
 */
public class ConsoleFilterPlugin  extends Plugin {

    private static PrintStream oldStdOut, oldStdErr;

    private static FilteredPrintStream stdOutFilter, stdErrFilter;
    public ConsoleFilterPlugin(Application app) {

    }

    @Override
    public void onStart() {
       oldStdErr = System.err;
        oldStdOut = System.out;

        stdOutFilter = new FilteredPrintStream(oldStdOut);
        stdErrFilter = new FilteredPrintStream(oldStdErr);

        System.setErr(stdErrFilter);
        System.setOut(stdOutFilter);
    }

    @Override
    public void onStop() {
        System.setErr(oldStdOut);
        System.setOut(oldStdErr);

        stdErrFilter.close();
        stdOutFilter.close();

    }

    @Override
    public boolean enabled() {
        return true;
    }

    public static FilteredPrintStream getStdOutOutputFilter(){
        return stdOutFilter;
    }
    public static FilteredPrintStream getStdErrOutputFilter(){
        return stdErrFilter;
    }
}
