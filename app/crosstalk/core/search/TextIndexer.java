package crosstalk.core.search;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.FieldType;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.FieldInfo.IndexOptions;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.RegexpQuery;

import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import static org.apache.lucene.document.Field.Store.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import play.Logger;
import play.db.ebean.Model;


public class TextIndexer {
    private File dir;
    private Directory indexDir;
    private IndexWriter indexWriter;
    private Analyzer indexAnalyzer;

    static final int DEBUG = 0;

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
        indexDir = new NIOFSDirectory (dir, NoLockFactory.getNoLockFactory());
        indexAnalyzer = createIndexAnalyzer ();
        IndexWriterConfig conf = new IndexWriterConfig 
            (Version.LUCENE_4_9, indexAnalyzer);
        indexWriter = new IndexWriter (indexDir, conf);

        this.dir = dir;
    }

    Analyzer createIndexAnalyzer () {
        Map<String, Analyzer> fields = new HashMap<String, Analyzer>();
        fields.put("id", new KeywordAnalyzer ());
        fields.put("kind", new KeywordAnalyzer ());
	return 	new PerFieldAnalyzerWrapper 
            (new StandardAnalyzer (Version.LUCENE_4_9), fields);
    }

    public List search (String text, int top, int skip) throws IOException {
        IndexSearcher searcher = new IndexSearcher
            (DirectoryReader.open(indexWriter, true));

        List results = new ArrayList ();
        try {
            QueryParser parser = new QueryParser 
                (Version.LUCENE_4_9, "text", indexAnalyzer);
            Query query = parser.parse(text);
            Logger.debug("## Query: "+query);

            long start = System.currentTimeMillis();

            TopDocs hits = searcher.search(query, skip+top);
            int size = Math.max(0, Math.min(skip+top, hits.totalHits));
            for (int i = skip; i < size; ++i) {
                Document doc = searcher.doc(hits.scoreDocs[i].doc);
                IndexableField kind = doc.getField("kind");
                if (kind != null) {
                    String field = kind.stringValue()+"._id";
                    IndexableField id = doc.getField(field);
                    if (id != null) {
                        Number n = id.numericValue();
                        try {
                            Model.Finder finder = new Model.Finder
                                (n != null ? Long.class : String.class,
                                 Class.forName(kind.stringValue()));
                            results.add(finder.byId
                                        (n != null 
                                         ? n.longValue() : id.stringValue()));
                            
                            Logger.debug("++ matched doc "
                                         +field+"="+id.stringValue());
                        }
                        catch (ClassNotFoundException ex) {
                            Logger.trace("Can't locate class "
                                         +kind.stringValue()
                                         +" in classpath!", ex);
                        }
                    }
                    else {
                        Logger.error("Index corrupted; document "
                                     +"doesn't have field "+field);
                    }
                }
            }

            Logger.debug("## Query finishes in "
                         +String.format("%1$.3fs", 
                                        (System.currentTimeMillis()-start)*1e-3)
                         +"..."+hits.totalHits+" hit(s) found; returning "
                         +results.size()+"!");
        }
        catch (ParseException ex) {
            Logger.warn("Can't parse query expression: "+text, ex);
        }

        return results;
    }

    /**
     * recursively index any object annotated with Entity
     */
    public void add (Object entity) throws IOException {
        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return;
        }
        if (DEBUG > 0)
            Logger.debug(">>> Indexing "+entity+"...");
        
        List<IndexableField> fields = new ArrayList<IndexableField>();
        fields.add(new StringField
                   ("kind", entity.getClass().getName(), YES));
        instrument (entity, fields);
        
        List<IndexableField> text = new ArrayList<IndexableField>();
        for (IndexableField f : fields) {
            text.add(new TextField ("text", f.stringValue(), NO));

            if (DEBUG > 1)
                Logger.debug(".."+f.name()+":"
                             +f.stringValue()+" ["+f.getClass().getName()+"]");
        }
        fields.addAll(text);
        
        // now index
        indexWriter.addDocument(fields);

        if (DEBUG > 0)
            Logger.debug("<<< "+entity);
    }

    public void update (Object entity) throws IOException {
        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return;
        }

        if (DEBUG > 0)
            Logger.debug(">>> Updating "+entity+"...");

        try {
            for (Field f : entity.getClass().getDeclaredFields()) {
                if (f.getAnnotation(Id.class) != null) {
                    Object id = f.get(entity);
                    if (id != null) {
                        String field = entity.getClass().getName()+".id";
                        indexWriter.deleteDocuments
                            (new Term (field, id.toString()));

                        Logger.debug("++ Updating "+field+"="+id);

                        // now reindex .. there isn't an IndexWriter.update 
                        // that takes a Query
                        add (entity);
                    }
                }
            }
        }
        catch (Exception ex) {
            Logger.trace("Unable to update index for "+entity, ex);
        }

        if (DEBUG > 0)
            Logger.debug("<<< "+entity);
    }

    public void remove (Object id, Class kind) throws IOException {
        BooleanQuery query = new BooleanQuery ();
    }

    public void remove (Object entity) throws Exception {
        Class cls = entity.getClass();
        if (cls.isAnnotationPresent(Entity.class)) {
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                if (f.getAnnotation(Id.class) != null) {
                    Object id = f.get(entity);
                    if (id != null) {
                        String field = entity.getClass().getName()+".id";
                        
                        Logger.debug("Deleting document "+field+"...");
                        indexWriter.deleteDocuments
                            (new Term (field+".id", id.toString()));
                    }
                    else {
                        Logger.warn("Id field "+f.getName()+" is null");
                    }
                }
            }
        }
        else {
            throw new IllegalArgumentException
                ("Object is not of type Entity");
        }
    }

    public void remove (String text) throws Exception {
        try {
            QueryParser parser = new QueryParser 
                (Version.LUCENE_4_9, "text", indexAnalyzer);
            Query query = parser.parse(text);
            Logger.debug("## removing documents: "+query);
            indexWriter.deleteDocuments(query);
        }
        catch (ParseException ex) {
            Logger.warn("Can't parse query expression: "+text, ex);
            throw new IllegalArgumentException
                ("Can't parse query: "+text, ex);
        }
    }

    protected void instrument (Object entity, List<IndexableField> ixFields) {
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field f : fields) {
                try {
                    Class type = f.getType();
                    Object value = f.get(entity);
                    /*
                    Logger.debug("__ "+f.getName()+": type="+type+" entity="
                                 +value.getClass().getAnnotation(Entity.class));
                    */

                    if (f.getAnnotation(Id.class) != null) {
                        //Logger.debug("+ Id: "+value);
                        if (value != null) {
                            // the hidden _id field stores the field's value
                            // in its native type whereas the display field id
                            // is used for indexing purposes and as such is
                            // represented as a string
                            String kind = entity.getClass().getName();
                            ixFields.add(getField (kind+"._id", value, YES));
                            ixFields.add
                                (new StringField (kind+".id", 
                                                  value.toString(), NO));
                        }
                        else {
                            //Logger.warn("Id field "+f+" is null");
                        }
                    }
                    else if (value == null) {
                        // do nothing
                    }
                    else if (type.isPrimitive()) {
                        ixFields.add(getField (f.getName(), value));
                    }
                    else if (type.isArray()) {
                        int len = Array.getLength(value);
                        // recursively evaluate each element in the array
                        for (int i = 0; i < len; ++i)
                            instrument (Array.get(value, i), ixFields); 
                    }
                    else if (Collection.class.isAssignableFrom(type)) {
                        Iterator it = ((Collection)value).iterator();
                        while (it.hasNext())
                            instrument (it.next(), ixFields);
                    }
                    // why isn't this the same as using type?
                    else if (value.getClass()
                             .isAnnotationPresent(Entity.class)) {
                        // composite type; recurse
                        instrument (value, ixFields);
                    }
                    else { // treat as string
                        ixFields.add(getField (f.getName(), value));
                    }
                }
                catch (Exception ex) {
                    /*
                    Logger.warn(entity.getClass()
                                +": Field "+f+" is not indexable due to "
                                +ex.getMessage());
                    */
                }
            }
        }
        catch (Exception ex) {
            Logger.trace("Fetching entity fields", ex);
        }
    }

    IndexableField getField (String name, Object value) {
        return getField (name, value, NO);
    }

    IndexableField getField (String name, Object value, 
                             org.apache.lucene.document.Field.Store store) {
        org.apache.lucene.document.Field f = null;
        if (value instanceof Long) {
            f = new LongField (name, (Long)value, store);
        }
        else if (value instanceof Integer) {
            f = new IntField (name, (Integer)value, store);
        }
        else if (value instanceof Float) {
            f = new FloatField (name, (Float)value, store);
        }
        else if (value instanceof Double) {
            f = new DoubleField (name, (Double)value, store);
        }
        else {
            f = new TextField (name, value.toString(), store);
        }

        //setFieldType (f.fieldType());
        return f;
    }

    static void setFieldType (FieldType ftype) {
        ftype.setIndexed(true);
        ftype.setTokenized(true);
        ftype.setStoreTermVectors(true);
        ftype.setIndexOptions
            (IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    }

    public void shutdown () {
        try {
            if (indexWriter != null)
                indexWriter.close();
        }
        catch (IOException ex) {
            //ex.printStackTrace();
            Logger.error("Closing index", ex);
        }
        indexers.remove(dir);
    }
}
