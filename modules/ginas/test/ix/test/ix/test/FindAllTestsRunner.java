package ix.test.ix.test;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.internal.matchers.Find;
import org.openqa.selenium.support.FindAll;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by katzelda on 5/20/16.
 */
public class FindAllTestsRunner extends Suite{
    /*
    /**
	 * Called by this class and subclasses once the classes making up the suite have been determined
	 *
	 * @param builder builds runners for classes in the suite
	 * @param klass the root of the suite
	 * @param suiteClasses the classes in the suite
	 * @throws InitializationError
	 *
    protected Suite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
     */
    @Retention(value= RetentionPolicy.RUNTIME)
    @Target(value = ElementType.TYPE)
    public @interface FindAllTestConfig{
        boolean includeJars() default false;
        String includePattern() default ".*Test$";
    }
    @FindAllTestConfig
    private static class DefaultConfig {}


    private static DefaultConfig defaultConfigInstance = new DefaultConfig();


    public FindAllTestsRunner(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(null, getRunnersFor(getTestClasses(klass), klass));
    }

    private static List<Runner> getRunnersFor(List<Class<?>> testClasses, Class<?> parent) throws InitializationError{
        List<Runner> runners = new LinkedList<>();

        Object parentInstance = null;
        try {
            parentInstance = parent.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        List<TestRule> rules = new ArrayList<>();
        for(Field f : parent.getFields()){
            if(f.getDeclaredAnnotation(Rule.class) !=null){
                try {

                    if( TestRule.class.isAssignableFrom(f.getType())){
                        rules.add((TestRule) f.get(parentInstance));
                        System.out.println("adding " + f.get(parentInstance));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }


        for (Class<?> klazz : testClasses) {
            System.out.println("adding test class " + klazz);
            try {
                runners.add(new MyRunner(klazz, rules));
            }catch(Exception ex){
                //ignore ?
            }
        }

        return runners;
    }
    private static List<Class<?>> getTestClasses(Class<?> suiteClass) {
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(System.getProperty("path.separator"));

        FindAllTestConfig config = suiteClass.getAnnotation(FindAllTestConfig.class);
        if(config ==null){
            config = defaultConfigInstance.getClass().getAnnotation(FindAllTestConfig.class);
        }
    if(config ==null ){
        throw new IllegalStateException("how!");
    }
        List<Class<?>> classes = new LinkedList<>();
        Pattern includePattern = Pattern.compile(config.includePattern());
        for(String path : paths){
            File f = new File(path);
            if(!f.exists()){
                continue;
            }
            if(f.isDirectory()){
                getClassesFrom(suiteClass, f,f,config, includePattern, classes);
            }


        }
        return classes;
    }

    private static void getClassesFrom(Class<?> suiteClass, File root, File currentFile, FindAllTestConfig config, Pattern includePattern,  List<Class<?>> classes){
        if(currentFile.isDirectory()){
            File[] children = currentFile.listFiles();
            if(children ==null){
                return;
            }
            for(File child : children){
                getClassesFrom(suiteClass, root, child, config, includePattern, classes);
            }
        }else {
            if (currentFile.getName().endsWith(".jar") && config.includeJars()) {
                //TODO handle jars
            }else if(currentFile.getName().endsWith(".class")){
                String className = currentFile.getName().substring(0, currentFile.getName().lastIndexOf(".class"));
                if(includePattern.matcher(className).matches()) {
                    String fullClassName = computeFullClassName(root, currentFile);
                    System.out.println("full qualified class name for " + currentFile.getAbsolutePath() + " is ");
                    System.out.println(fullClassName);

                    try {
                        Class<?> c = Class.forName(fullClassName);
                       // if(! suiteClass.equals(c)) {
                            classes.add(c);
                       // }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String computeFullClassName(File root, File currentFile) {
        String name = currentFile.getName();
        String className = name.substring(0, name.lastIndexOf(".class"));

        File p = currentFile.getParentFile();

        System.out.println("getting class name for " + currentFile.getAbsolutePath());
        StringBuilder builder = new StringBuilder();
        builder.append(className);
        while( p!=null && !root.equals(p)){
            builder.insert(0, '.').insert(0, p.getName());
            p = p.getParentFile();
        }

        return builder.toString();
    }

    private static class MyRunner extends BlockJUnit4ClassRunner {
        private final List<TestRule> rules;

        public MyRunner(Class<?> klass, List<TestRule> rules) throws InitializationError {
            super(klass);
            this.rules = rules;
        }

        @Override
        protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
            Description description= describeChild(method);
            if (method.getAnnotation(Ignore.class) != null) {
                notifier.fireTestIgnored(description);
            } else {
                if (description.getAnnotation(Deprecated.class) != null) {
                    System.out.println("name=" + description.getMethodName() + " annotations=" + description.getAnnotations());
                }
                RunRules runRules = new RunRules(methodBlock(method), rules, description);
                runLeaf(runRules, description, notifier);
            }
        }
    }
}
