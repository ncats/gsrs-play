package ix.core.plugins;

import com.sleepycat.je.*;
import ix.utils.Util;
import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.writebehind.operations.SingleOperationType;
import play.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by katzelda on 7/7/16.
 */
public class FileDbCache implements GinasFileBasedCacheAdapter {

    private Database db;

    private final File dir;
    private final String cacheName;

    private int serializableCount=0, notSerializableCount=0;
    public FileDbCache(File dir, String cacheName){
        Objects.requireNonNull(dir);
        Objects.requireNonNull(cacheName);

        this.cacheName = cacheName;
       this.dir = dir;
    }

    @Override
    public Object createEntry(Object key) throws Exception {

        System.out.println("in create entry");
        if (!(key instanceof Serializable)) {
            throw new IllegalArgumentException
                    ("Cache key "+key+" is not serliazable!");
        }
        System.out.println("is serializable");
        Element elm = null;
        try {
            DatabaseEntry dkey = getKeyEntry (key);
            DatabaseEntry data = new DatabaseEntry ();
            OperationStatus status = db.get(null, dkey, data, null);
            if (status == OperationStatus.SUCCESS) {
                try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data.getData(), data.getOffset(), data.getSize()))) {
                    elm = new Element(key, ois.readObject());
                }
            }
            else if (status == OperationStatus.NOTFOUND) {
                //
            }
            else {
                Logger.warn("Unknown status for key "+key+": "+status);
            }
        }
        catch (Exception ex) {
            Logger.error("Can't recreate entry for "+key, ex);
        }
        return elm;
    }

    @Override
    public CacheWriter clone(Ehcache cache) throws CloneNotSupportedException {
        System.out.println("trying to clone!!!");
        throw new CloneNotSupportedException();
    }

    @Override
    public void init() {
        //use this instead of dir.mkdirs()
        //because it will throw IOException with reason why dir couldn't be created
        //mkdirs just returns boolean
        try{
            Files.createDirectories(dir.toPath());
        }catch(IOException e){
            throw new RuntimeException("error creating dir", e);
        }

        EnvironmentConfig envconf = new EnvironmentConfig ();
        envconf.setAllowCreate(true);
        Environment env = new Environment (dir, envconf);
        DatabaseConfig dbconf = new DatabaseConfig ();
        dbconf.setAllowCreate(true);
        db = env.openDatabase(null, cacheName, dbconf);
    }

    @Override
    public void dispose() throws CacheException {

        System.out.println("# serializable = " + serializableCount);
        System.out.println("# not serializable = " + notSerializableCount);

        if (db != null) {
            try {
                Logger.debug("#### closing cache writer "+cacheName
                        +"; "+db.count()+" entries #####");
                db.close();
            }
            catch (Exception ex) {
                Logger.error("Can't close lucene index!", ex);
            }
        }
    }

    static DatabaseEntry getKeyEntry (Object value) {
        return new DatabaseEntry (value.toString().getBytes());
    }
    @Override
    public void write(Element elm) throws CacheException {
        System.out.println("# serializable = " + serializableCount);
        System.out.println("# not serializable = " + notSerializableCount);

        if(!elm.isSerializable()){
            notSerializableCount++;
            System.out.println(" not = " + elm.getObjectValue().getClass());
            return;
        }
        serializableCount++;

        System.out.println("calling write");
        //TODO is this a safe cast?


        Serializable key = (Serializable) elm.getObjectKey();

        Object value = elm.getObjectValue();

        if (key != null) {
            //Logger.debug("Persisting cache key="+key+" value="+elm.getObjectValue());
            try {
                DatabaseEntry dkey = getKeyEntry (key);
                DatabaseEntry data = new DatabaseEntry
                        (Util.serialize(elm.getObjectValue()));
                OperationStatus status = db.put(null, dkey, data);
                if (status != OperationStatus.SUCCESS) {
                    System.out.println(status.name());
                    Logger.warn
                            ("** PUT for key " + key + " returns status " + status);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("Can't write cache element: key="
                        +key+" value="+elm.getObjectValue(), ex);
            }
        }
        else {
            System.out.println("not serializable");
            Logger.warn("Key "+elm.getObjectKey()+" isn't serializable!");
        }
    }

    @Override
    public void writeAll(Collection<Element> elements) throws CacheException {

        //TODO is there a better way?
        for(Element e : elements){
            write(e);
        }
    }

    @Override
    public void delete(CacheEntry entry) throws CacheException {
            Object key = entry.getKey();
            if (!(key instanceof Serializable))
                return;

            try {
                DatabaseEntry dkey = getKeyEntry (key);
                OperationStatus status = db.delete(null, dkey);
                if (status != OperationStatus.SUCCESS)
                    Logger.warn("Delete cache key '"
                            +key+"' returns status "+status);
            }
            catch (Exception ex) {
                Logger.error("Deleting cache "+key+" from persistence!", ex);
            }
    }

    @Override
    public void deleteAll(Collection<CacheEntry> entries) throws CacheException {
        for(CacheEntry e : entries){
            delete(e);
        }
    }

    @Override
    public void throwAway(Element elm, SingleOperationType operationType, RuntimeException ex) {
        Logger.error("Throwing away cache element "+elm.getObjectKey(), ex);
    }
}
