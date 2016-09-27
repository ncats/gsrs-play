package ix.core.plugins;

import ix.core.util.FilteredPrintStream;
import ix.core.util.Filters;
import play.Application;
import play.Plugin;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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

        
        //Should we close here?
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
    
    
    /**
     * Convenience method to run a task while swallowing all StdErr within that
     * thread. The output swallowed can be caught by a provided consumer. 
     * @param r The task to run.
     * @param consumeSwallowed A consumer for the output swallowed.
     */
    public static void runWithSwallowedStdErr(Runnable r, Consumer<Object> consumeSwallowed){
    	 FilteredPrintStream.Filter filterOutCurrentThread = Filters.filterOutCurrentThread();
         try (FilteredPrintStream.FilterSession ignoreThread = ConsoleFilterPlugin.getStdErrOutputFilter().newFilter(filterOutCurrentThread).withOnSwallowed(consumeSwallowed)){
        	r.run(); 
         }
    }
    public static void runWithSwallowedStdErr(Runnable r){
    	runWithSwallowedStdErr(r,null);
    }
    /**
     * Convenience method to run a task while swallowing StdErr from classes
     * matching a certain regex pattern. The output swalled can be caught by
     * a provided consumer. 
     * @param r The task to run.
     * @param pattern Regex pattern of the Class names to swallow output from.
     * @param consumeSwallowed A consumer for the output swallowed
     */
    public static void runWithSwallowedStdErrFor(Runnable r, String pattern, Consumer<Object> consumeSwallowed){
   	 FilteredPrintStream.Filter filterOutPattern = Filters.filterOutAllClasses(pattern);
        try (FilteredPrintStream.FilterSession ignoreClass = ConsoleFilterPlugin.getStdErrOutputFilter().newFilter(filterOutPattern).withOnSwallowed(consumeSwallowed)){
        	r.run();	 
        }
    }

	public static void runWithSwallowedStdErrFor(Runnable r, String pattern) {
		runWithSwallowedStdErrFor(r,pattern,null);
	}
	
	/**
	 * Get the original STDOUT for debugging the redirect, or to force a message through
	 * all filters
	 * @return
	 */
	public static PrintStream stdOut(){
    	return oldStdOut;
    }
    
}
