package ix.core.plugins;

import ix.core.util.ConfigHelper;
import ix.core.util.GsrsClassLoader;
import ix.core.util.IOUtil;
import play.Application;
import play.Plugin;

import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates custom classloader on start up and reads
 * the location of external plugin ins
 * from the config using the key "gsrs.plugins.dir"
 * any jars found under the directories listed (or jars explicitly listed)
 * as well as the root directories given (for .class file support)
 * are added to a new classloader and will be used
 * by the ScheduledTasks and CardViews, exporters, entityprocessors etc.
 *
 * Created by katzelda on 12/18/18.
 */
public class GsrsClassLoaderPlugin extends Plugin{

    public GsrsClassLoaderPlugin(Application app){}

    private static List<URL> walkDirs(File dir) throws IOException{
        List<URL> urls = new ArrayList<>();
        if(!dir.exists()){
            return urls;
        }
        if(dir.isDirectory()){
            urls.add(dir.toURI().toURL());
        }
        Files.walk(dir.toPath())
                .forEach(p->{
                    if(Files.isRegularFile(p)){
                        if(p.toFile().getName().endsWith(".jar")){
                            try {
                                urls.add(p.toFile().toURI().toURL());
                            }catch(MalformedURLException e){
                                e.printStackTrace();
                            }
                        }
                    }
                });

        return urls;
    }

    private void setGsrsClassLoader() throws IOException {


        List<URL> urls = new ArrayList<>();


        for(String f : ConfigHelper.getStringList("gsrs.plugins.dir", Collections.emptyList())){
            urls.addAll(walkDirs(new File(f)));
        }

//        System.out.println("all urls = " + urls);
        ClassLoader parent = getClass().getClassLoader();
        ClassLoader classLoader;
        if(urls ==null || urls.isEmpty()){
            classLoader = parent;
        }else {
            classLoader = new GsrsClassLoader(urls,parent );
        }
        IOUtil.setGinasClassLoader(classLoader);
    }

    @Override
    public void onStart() {
        try{
            setGsrsClassLoader();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
