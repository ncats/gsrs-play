package ix.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple utility class to parse basic
 * arguments from a command line string.
 *
 * Created by katzelda on 5/31/16.
 */
public final class ArgParser {
    //The regexes used are based on a stack overflow answer
    //http://stackoverflow.com/a/23227490
    //
    //This uses look ahead to match quotes
    //
    //changes to stackoverflow answer include:
    //1. renamed variables
    // 2. changing single arg pattern to use "\\S+" instead of "\\w+" since that wouldn't match periods and slashes in file paths
    //          or dashes and equal signs in jvm options
    private static Pattern VALID_PATTERN = Pattern.compile("^(?=(?:(?:[^\"]*\"){2})*[^\"]*$).*$");
    private static Pattern SINGLE_ARG_PATTERN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    private ArgParser(){
        //can not instantiate
    }

    /**
     * Parse a command line string into its constituent arguments.
     *
     * <p>
     *     The types of arguments parsed can be in the form:
     * </p>
     * <ul>
     *     <li>arguments separated by whitespace</li>
     *     <li>an argument with multiple words contained in quotes (")</li>
     *     <li>Dashes, equals and periods are retained in the arguments</li>
     *
     * </ul>
     *
     * Example:
     * <pre>
     *     -Dtestconfig=ginas.conf foo "bar baz"
     * </pre>
     * Will get parsed as a 3 element list:
     * <pre>
     *     [
     *          -Dtestconfig=ginas.conf
     *          foo
     *          bar baz
     *     ]
     * </pre>
     * @param argStr the argument String.
     * @return a List where each element is a single argument.
     */
    public static List<String> parseArgs(String argStr) {

        List<String> params = null;
        if (VALID_PATTERN.matcher(argStr).matches()) {
            params = new ArrayList<String>();
            Matcher matcher = SINGLE_ARG_PATTERN.matcher(argStr);
            while (matcher.find())
                params.add( matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        }
        return params;
    }

}
