package ix.core.plugins;

import java.util.concurrent.Callable;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import org.w3c.dom.Document;

import play.Logger;
import play.Plugin;
import play.Application;
import play.cache.Cache;

import ix.utils.Eutils;
import ix.core.models.Publication;

public class EutilsPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private File eutilsDir;
    private int cacheTime;
    
    public EutilsPlugin (Application app) {
        this.app = app;
    }

    public void onStart () {
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        // initialize cache
        eutilsDir = new File (ctx.cache(), "eutils");
        if (!eutilsDir.exists()) {
            eutilsDir.mkdirs();
        }
        cacheTime = app.configuration().getInt("ix.cache.time", 60);
        Logger.info("Plugin "+getClass().getName()+" initialized; base="
                    +eutilsDir+" cacheTime="+cacheTime+"s");
    }

    public void onStop () {
    }

    File getCacheFile (Long pmid) {
        String name = pmid.toString().substring(0, 2);
        return new File (new File (eutilsDir, name), pmid+".xml");
    }
    
    Publication fetchAndCache (Long pmid) throws Exception {
        File file = getCacheFile (pmid);
        if (file.exists()) {
            Logger.debug("Cached file: "+file);
            return Eutils.parsePublication(file);
        }
        else {
            file.getParentFile().mkdirs();
            byte[] xml = Eutils.getByteArray(pmid);
            if (xml != null) {
                // cache this dom
                FileOutputStream fos = new FileOutputStream (file);
                fos.write(xml, 0, xml.length);          
                fos.close();
                
                return Eutils.parsePublication(xml);
            }
            else {
                Logger.error("Can't retrieve byte array for "+pmid);
            }
        }
        return null;
    }
    
    public boolean enabled () { return true; }
    public Publication getPublication (final Long pmid) {
        try {
            String key = getClass().getName()+":"+pmid;
            return Cache.getOrElse(key, new Callable<Publication> () {
                    public Publication call () throws Exception {
                        return fetchAndCache (pmid);
                    }
                }, cacheTime);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
