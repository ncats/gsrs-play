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
import java.lang.reflect.Modifier;
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
        super(builder, null, getTestClasses(klass));
    }


    private static Class<?>[] getTestClasses(Class<?> suiteClass) {
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(System.getProperty("path.separator"));

        FindAllTestConfig config = suiteClass.getAnnotation(FindAllTestConfig.class);
        if(config ==null){
            config = defaultConfigInstance.getClass().getAnnotation(FindAllTestConfig.class);
        }
    if(config ==null ){
        throw new IllegalStateException("how!");
    }
        List<Class<?>> classes = new ArrayList<>();
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
        return classes.toArray(new Class<?>[classes.size()]);
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
                        if(!Modifier.isAbstract(c.getModifiers())){
                            classes.add(c);
                        }
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


}
