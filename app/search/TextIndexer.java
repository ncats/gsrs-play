package search;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import javax.persistence.Entity;
import javax.persistence.Id;
import play.Logger;

public class TextIndexer {
    private File dir;
    private Directory idxDir;
    private IndexWriter idxWriter;
    private Analyzer idxAnalyzer;

    static ConcurrentMap<File, TextIndexer> indexers = 
        new ConcurrentHashMap<File, TextIndexer>();

    public static TextIndexer getInstance (File dir) throws IOException {
        if (indexers.containsKey(dir)) 
            return indexers.get(dir);

        try {
            TextIndexer indexer = new TextIndexer (dir);
            TextIndexer old = indexers.putIfAbsent(dir, indexer);
            return old == null ? indexer : old;
        }
        catch (IOException ex) {
            return indexers.get(dir);
        }
    }

    protected TextIndexer (File dir) throws IOException {
        idxDir = new NIOFSDirectory (dir, NoLockFactory.getNoLockFactory());
        idxAnalyzer = createIndexAnalyzer ();
        IndexWriterConfig conf = new IndexWriterConfig 
            (Version.LUCENE_4_9, idxAnalyzer);
        idxWriter = new IndexWriter (idxDir, conf);
        this.dir = dir;
    }

    Analyzer createIndexAnalyzer () {
        Map<String, Analyzer> fields = new HashMap<String, Analyzer>();
        fields.put("id", new KeywordAnalyzer ());
	return 	new PerFieldAnalyzerWrapper 
            (new StandardAnalyzer (Version.LUCENE_4_9), fields);
    }

    /**
     * recursively index any object annotated with Entity
     */
    public void add (Object entity) throws IOException {
        if (entity.getClass().isAnnotationPresent(Entity.class)) {
            Logger.debug("Indexing "+entity+"...");
            List<IndexableField> fields = new ArrayList<IndexableField>();
            fields.add(new StringField
                       ("kind", entity.getClass().getName(), 
                        org.apache.lucene.document.Field.Store.YES));
            add (entity, fields);
            idxWriter.addDocument(fields);
        }
    }

    public void search (String query) {

    }

    protected void add (Object entity, List<IndexableField> ixFields) {
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field f : fields) {
                try {
                    Class type = f.getType();
                    Object value = f.get(entity);
                    if (f.getAnnotation(Id.class) != null) {
                        //Logger.debug("+ Id: "+value);
                        if (value != null) {
                            if (value instanceof Number) {
                                ixFields.add
                                    (new LongField 
                                     ("id", ((Number)value).longValue(),
                                      org.apache.lucene.document.Field.Store.NO));
                            }
                            else {
                                ixFields.add
                                    (new StringField
                                     ("id", value.toString(), 
                                      org.apache.lucene.document.Field.Store.NO));
                            }
                        }
                        else {
                            Logger.warn("Id field "+f+" is null");
                        }
                    }
                    else if (value == null) {
                        // do nothing
                    }
                    else if (type.isPrimitive()) {
                        
                    }
                    else if (type.isArray()) {
                        
                    }
                    else if (type.isAnnotationPresent(Entity.class)) {
                        // composite type; recurse
                        this.add(value);
                    }
                    else { // treat as string
                        ixFields.add
                            (new StringField
                             (f.getName(), value.toString(), 
                              org.apache.lucene.document.Field.Store.NO));
                    }
                }
                catch (Exception ex) {
                    Logger.warn(entity.getClass()
                                +": Field "+f+" is not indexable due to "
                                +ex.getMessage());
                }
            }
        }
        catch (Exception ex) {
            Logger.trace("Fetching entity fields", ex);
        }
    }

    public void shutdown () {
        try {
            if (idxWriter != null)
                idxWriter.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        indexers.remove(dir);
    }
}
