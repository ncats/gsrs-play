package ix.core.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ix.core.plugins.ConsoleFilterPlugin;

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
    
    /**
     * A more aggressive form of the class filter, which will filter out all
     * output which originate from anywhere with a parent StackTraceElement
     * which matches the pattern
     * @param classPattern the pattern to filter OUT.
     * @return
     */
    public static FilteredPrintStream.Filter filterOutAllClasses(String classPattern){
        return filterOutAllClasses(Pattern.compile(classPattern));
    }
    /**
     * A more aggressive form of the class filter, which will filter out all
     * output which originate from anywhere with a parent StackTraceElement
     * which matches the pattern
     * @param classPattern The pattern to filter OUT.
     * @return
     */
    public static FilteredPrintStream.Filter filterOutAllClasses(Pattern classPattern){
        Objects.requireNonNull(classPattern);
        return new ClassFilter(classPattern).asAnywhereFilter();
    }
    
    

    public static FilteredPrintStream.Filter filterOutCurrentThread(){
        return filterOutThread(Thread.currentThread());
    }

    public static FilteredPrintStream.Filter filterOutThread(Thread t){
        Objects.requireNonNull(t);

        return new ThreadFilter(t).not();
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
        public AnywhereClassFilter asAnywhereFilter(){
            return new AnywhereClassFilter(this);
        }
    }
    /**
     * This is a more aggressive (and expensive) form of the class filter,
     * which throws its own Throwable, and analyzes the StackTraceElements, rather
     * than rely on the (most likely) StackTraceElement which is provided by the
     * {@link ix.core.util.FilteredPrintStream}  
     * 
     * @author tyler
     *
     */
    private static class AnywhereClassFilter implements FilteredPrintStream.Filter {

        private final ClassFilter cf;

        public AnywhereClassFilter(ClassFilter cf) {
            this.cf = cf;
        }

        @Override
        public boolean test(StackTraceElement stackTraceElement) {
                if(Arrays.stream(new Throwable().getStackTrace()).anyMatch(s->{
                    return !cf.test(s);
                })){
                    return false;
                }
                return true;
        }
    }
}
