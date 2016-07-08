package ix.core.plugins;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.writer.CacheWriter;

/**
 * Created by katzelda on 7/7/16.
 */
public interface GinasFileBasedCacheAdapter extends CacheWriter, CacheEntryFactory {
}
