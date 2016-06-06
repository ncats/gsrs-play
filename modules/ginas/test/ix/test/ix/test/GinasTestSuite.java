package ix.test.ix.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GinasTestSuite extends Suite{

    /**
     * Annotation to add to provide configuration details.
     */
    @Retention(value= RetentionPolicy.RUNTIME)
    @Target(value = ElementType.TYPE)
    public @interface Config {
        /**
         * Should jar files in the class path be included.
         * @return {@code true} if jars should be included; {@code false} otherwise.
         */
        boolean includeJars() default false;

        /**
         * String Patterns of class names that should be included
         * in this suite.
         * @return an array of Strings that can be converted into Pattern objects.
         */
        String[] includePattern() default {".*Test$"};
        /**
         * String Patterns of class names that should NOT be included
         * in this suite.
         * @return an array of Strings that can be converted into Pattern objects.
         */
        String[] excludePattern() default {};
        /**
         * String Patterns of fully qualified class names, including all packages separated by "\\." that should be included
         * in this suite.
         * @return an array of Strings that can be conveted into Pattern objects.
         */
        String[] fullExcludePattern() default {};
        /**
         * String Patterns of fully qualified class names, including all packages separated by "\\." that should NOT be included
         * in this suite.
         * @return an array of Strings that can be conveted into Pattern objects.
         */
        String[] fullIncludePattern() default {".*"};
    }
    @Config
    private static class DefaultConfig {}


    private static DefaultConfig defaultConfigInstance = new DefaultConfig();



    public GinasTestSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        this(builder, null, getTestClasses(klass));
    }

    public static Class<?>[] getTestClasses(Class<?> parentSuite) {

        Config config = parentSuite.getAnnotation(Config.class);
        if(config ==null){
            config = defaultConfigInstance.getClass().getAnnotation(Config.class);
        }
        ClassFilter filter = new ConfigClassFilter(config);

        return ClassFinder.getTestClasses(parentSuite, config.includeJars(), filter);
    }

    /**
     * Called by this class and subclasses once the classes making up the suite have been determined
     *
     * @param builder builds runners for classes in the suite
     * @param klass the root of the suite
     * @param suiteClasses the classes in the suite
     * @throws InitializationError
     */
    public GinasTestSuite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        super(builder, klass, handleClasses(suiteClasses));


    }

    private static Class<?>[] handleClasses(Class<?>[] suiteClasses) {
        MessageFormat format = new MessageFormat(System.getenv("command"));
        System.out.println("property value of command is '" + format.toPattern());

        System.out.println(System.getProperties().keySet());
        System.out.println("test suite now has the following classes");

        File root = new File(new File(".").getAbsolutePath()).getParentFile().getParentFile().getParentFile();
        System.out.println("root dir = " + root.getAbsolutePath());
        for(Class c : suiteClasses){
            String command = format.format(new Object[]{c.getCanonicalName()});

            List<String> args = ArgParser.parseArgs(command);
            System.out.println("executing " + args);
            System.out.println("cwd = " + new File(".").getAbsolutePath());
            try{
                Process process = new ProcessBuilder(args)
                                                .directory(root)
                                                .inheritIO()
                                                .start();
                process.waitFor();
                
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return new Class[0];
    }



    private static class ConfigClassFilter implements ClassFilter {
        private final  List<Pattern> includePatterns, excludePatterns, fullIncludePatterns,fullExcludePatterns;

        ConfigClassFilter(Config config) {
            includePatterns = compilePatterns(config.includePattern());
            excludePatterns = compilePatterns(config.excludePattern());

            fullIncludePatterns = compilePatterns(config.fullIncludePattern());
            fullExcludePatterns = compilePatterns(config.fullExcludePattern());
        }


        @Override
        public Optional<Class<?>> test(String className, File root, File classFile){
            //for now don't let subclasses through...
            if(className.contains("$")){
                return Optional.empty();
            }
            if(matches(className, includePatterns) && !matches(className, excludePatterns)) {
                String fullClassName = computeFullClassName(root, classFile);
                //System.out.println("matched class checking full patterns for " + fullClassName);
                if(matches(fullClassName, fullIncludePatterns) && !matches(fullClassName, fullExcludePatterns)){
                    try {
                        Class<?> c = Class.forName(fullClassName);
                        int modifiers = c.getModifiers();
                        if( !c.isEnum() && !Modifier.isAbstract(modifiers)){
                            return Optional.of(c);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return Optional.empty();
        }

        private boolean matches(String input, List<Pattern> patterns){
            for(Pattern p : patterns){
                if(p.matcher(input).matches()){
                    return true;
                }
            }
            return false;
        }
    }
    private static List<Pattern> compilePatterns( String[] array) {
        if(array !=null){
            List<Pattern> includePatterns = new ArrayList<>(array.length);

            for(String pattern : array){
                includePatterns.add(Pattern.compile(pattern));
            }
            return includePatterns;
        }
        return Collections.emptyList();
    }





    private static String computeFullClassName(File root, File currentFile) {
        String name = currentFile.getName();
        String className = name.substring(0, name.lastIndexOf(".class"));

        File p = currentFile.getParentFile();

        StringBuilder builder = new StringBuilder();
        builder.append(className);
        while( p!=null && !root.equals(p)){
            builder.insert(0, '.').insert(0, p.getName());
            p = p.getParentFile();
        }

        return builder.toString();
    }





}
