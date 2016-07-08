package ix.test.ix;

import java.io.File;

import ix.core.plugins.IxCache;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by katzelda on 5/24/16.
 */
public class IxCacheTest {

    @Test @Ignore
    public void emptyCache(){
        IxCache cache = new IxCache(new File("testcache"),2, 5,5,5);

       // cache.
    }
}
