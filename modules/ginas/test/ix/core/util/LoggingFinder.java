package ix.core.util;

import ix.core.plugins.ConsoleFilterPlugin;
import ix.core.util.FilteredPrintStream.FilterSession;
import ix.utils.Util;

/**
 * Utility class to help identify source of STDOut and STDErr output.
 * @author peryeata
 *
 */
public class LoggingFinder {

	/**
	 * Report and print the StackTrace for any process that outputs to
	 * stdOut while supplied runnable runs
	 * 
	 * @param r
	 */
	public static void printWhoPrintsToSTDOut(Runnable r){
		 try(FilterSession fs =  ConsoleFilterPlugin.getStdOutOutputFilter().newFilter(Filters.filterOutAllClasses(".*")).withOnSwallowed(c->{
         	ConsoleFilterPlugin.stdOut().println("Logging STDOut:" + c);
         	ConsoleFilterPlugin.stdOut().println(Util.getExecutionPath());
         })){
         	r.run();
         }
	}
	
	/**
	 * Report and print the StackTrace for any process that outputs to
	 * stdErr while supplied runnable runs
	 * 
	 * @param r
	 */
	public static void printWhoPrintsToSTDErr(Runnable r){
		 try(FilterSession fs =  ConsoleFilterPlugin.getStdErrOutputFilter().newFilter(Filters.filterOutAllClasses(".*")).withOnSwallowed(c->{
        	ConsoleFilterPlugin.stdOut().println("Logging STDErr:" + c);
        	ConsoleFilterPlugin.stdOut().println(Util.getExecutionPath());
        })){
        	r.run();
        }
	}
}
