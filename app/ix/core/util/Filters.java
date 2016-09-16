package ix.core.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 9/15/16.
 */
public class Filters {

    public static FilteredPrintStream.Filter filterOutClasses(String classPattern){
        return filterOutClasses(Pattern.compile(classPattern));
    }
    public static FilteredPrintStream.Filter filterOutClasses(Pattern classPattern){
        Objects.requireNonNull(classPattern);
        return new ClassFilter(classPattern);
    }

    public static FilteredPrintStream.Filter filterOutCurrentThread(){
        return filterOutThread(Thread.currentThread());
    }

    public static FilteredPrintStream.Filter filterOutThread(Thread t){
        Objects.requireNonNull(t);

        return new ThreadFilter(t);
    }

    private static class ThreadFilter implements FilteredPrintStream.Filter {
        private final Thread threadToFilter;

        public ThreadFilter(Thread threadToFilter) {
            this.threadToFilter = threadToFilter;
        }

        @Override
        public boolean test(StackTraceElement stackTraceElement) {
            return Thread.currentThread() == threadToFilter;
        }
    }

    private static class ClassFilter implements FilteredPrintStream.Filter {

        private final Pattern pattern;

        public ClassFilter(Pattern pattern) {
            this.pattern = pattern;
        }



        @Override
        public boolean test(StackTraceElement stackTraceElement) {
            String className = stackTraceElement.getClassName();

            Matcher matcher = pattern.matcher(className);

            return !matcher.matches();

        }
    }
}
