package ix.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by katzelda on 5/27/16.
 */
public class ClassFinder {

    /**
     * Get all Test Classes that
     * @param parentSuite
     * @param includeJars
     * @param filter
     * @return
     */
    public static Class<?>[] getTestClasses(Class<?> parentSuite, boolean includeJars, ClassFilter filter) {
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(System.getProperty("path.separator"));


        List<Class<?>> classes = new ArrayList<>();
        for(String path : paths){
            File f = new File(path);
            if(!f.exists()){
                continue;
            }
            if(f.isDirectory()){
                getClassesFrom(parentSuite, f,f,includeJars, filter, classes);
            }


        }
        return classes.toArray(new Class<?>[classes.size()]);
    }

    private static void getClassesFrom(Class<?> suiteClass, File root, File currentFile, boolean includeJars, ClassFilter filter, List<Class<?>> classes){
        if(currentFile.isDirectory()){
            File[] children = currentFile.listFiles();
            if(children ==null){
                return;
            }
            for(File child : children){
                getClassesFrom(suiteClass, root, child, includeJars, filter, classes);
            }
        }else {
            if (currentFile.getName().endsWith(".jar") && includeJars) {
                //TODO handle jars
            }else if(currentFile.getName().endsWith(".class")){
                String className = currentFile.getName().substring(0, currentFile.getName().lastIndexOf(".class"));
                filter.test(className, root, currentFile)
                        .ifPresent( c ->{
                            if(c != suiteClass) {
                                classes.add(c);

                            }
                        });

            }
        }
    }
}
