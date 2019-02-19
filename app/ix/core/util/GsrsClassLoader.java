package ix.core.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Custom Class loader that will be used
 * to load additional jars/classes from external
 * code such as plugins.
 *
 * Created by katzelda on 12/17/18.
 */
public class GsrsClassLoader extends URLClassLoader {


    public GsrsClassLoader(List<URL> urls, ClassLoader parent) {
        super(urls.toArray(new URL[urls.size()]), parent);

    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }
}
