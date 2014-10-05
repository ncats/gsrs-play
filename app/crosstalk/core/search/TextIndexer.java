package crosstalk.core.search;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.*;
import java.lang.annotation.Annotation;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntDocValuesField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.DoubleDocValuesField;
import static org.apache.lucene.document.Field.Store.*;

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
import org.apache.lucene.util.BytesRef;

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

import org.apache.lucene.facet.*;
import org.apache.lucene.facet.range.*;
import org.apache.lucene.facet.taxonomy.*;
import org.apache.lucene.facet.taxonomy.directory.*;
import org.apache.lucene.facet.sortedset.*;

import org.apache.lucene.search.suggest.DocumentDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.persistence.Entity;
import javax.persistence.Id;
import play.Logger;
import play.db.ebean.Model;

import crosstalk.utils.Global;
import crosstalk.core.models.Indexable;

/**
 * Singleton class that responsible for all entity indexing
 */
public class TextIndexer {
    @Indexable
    static final class DefaultIndexable {}
    static final Indexable defaultIndexable = 
        (Indexable)DefaultIndexable.class.getAnnotation(Indexable.class);

    /**
     * Make sure to properly update the code when upgrading version
     */
    static final Version LUCENE_VERSION = Version.LATEST;
    static final String FACETS_CONFIG_FILE = "facet_conf.json";
    static final String SUGGEST_CONFIG_FILE = "suggest_conf.json";

    public static class SearchResult {
        
    }

    public static class LookupResult {
        CharSequence key, highlight;
        LookupResult (CharSequence key, CharSequence highlight) {
            this.key = key;
            this.highlight = highlight;
        }

        public CharSequence getKey () { return key; }
        public CharSequence getHighlight () { return highlight; }
    }

    class SuggestLookup {
        String name;
        File dir;
        AtomicInteger dirty = new AtomicInteger ();
        AnalyzingInfixSuggester lookup;
        long lastRefresh;

        SuggestLookup (File dir) throws IOException {
            boolean isNew = false;
            if (!dir.exists()) {
                dir.mkdirs();
                isNew = true;
            }
            else if (!dir.isDirectory()) 
                throw new IllegalArgumentException ("Not a directory: "+dir);

            lookup = new AnalyzingInfixSuggester 
                (LUCENE_VERSION, new NIOFSDirectory 
                 (dir, NoLockFactory.getNoLockFactory()), indexAnalyzer);

            if (isNew) {
                Logger.debug("Initializing lookup "+dir.getName());
                build ();
            }
            else {
                Logger.debug(lookup.getCount()
                             +" entries loaded for "+dir.getName());
            }

            this.dir = dir;
            this.name = dir.getName();
        }

        SuggestLookup (String name) throws IOException {
            this (new File (suggestDir, name));
        }

        void add (BytesRef text, Set<BytesRef> contexts, 
                  long weight, BytesRef payload) throws IOException { 
            lookup.update(text, contexts, weight, payload);
            incr ();
        }

        void add (String text) throws IOException {
            BytesRef ref = new BytesRef (text);
            lookup.update(ref, null, 0, ref);
            incr ();
        }

        void incr ()  {
            dirty.incrementAndGet();
        }

        synchronized void refresh () throws IOException {
            long start = System.currentTimeMillis();
            lookup.refresh();
            lastRefresh = System.currentTimeMillis();
            Logger.debug(lookup.getClass().getName()
                         +" refreshs "+lookup.getCount()+" entries in "
                         +String.format("%1$.2fs", 
                                        1e-3*(lastRefresh - start)));
            dirty.set(0);
        }

        void close () throws IOException {
            lookup.close();
        }

        long build () throws IOException {
            IndexReader reader = DirectoryReader.open(indexWriter, true);
            // now weight field
            long start = System.currentTimeMillis();
            lookup.build(new DocumentDictionary (reader, name, null));
            long count = lookup.getCount();
            Logger.debug(lookup.getClass().getName()
                         +" builds "+count+" entries in "
                         +String.format("%1$.2fs", 
                                        1e-3*(System.currentTimeMillis()
                                              - start)));
            return count;
        }

        List<LookupResult> suggest (CharSequence key, int max)
            throws IOException {
            if (dirty.get() > 0)
                refresh ();

            List<Lookup.LookupResult> results = lookup.lookup
                (key, null, false, max);

            List<LookupResult> m = new ArrayList<LookupResult>();
            for (Lookup.LookupResult r : results) {
                m.add(new LookupResult (r.payload.utf8ToString(), r.key));
            }

            return m;
        }
    }

    private File baseDir;
    private File suggestDir;
    private Directory indexDir;
    private Directory taxonDir;
    private IndexWriter indexWriter;
    private Analyzer indexAnalyzer;
    private DirectoryTaxonomyWriter taxonWriter;
    private FacetsConfig facetsConfig;
    private ConcurrentMap<String, SuggestLookup> lookups;

    static ConcurrentMap<File, TextIndexer> indexers = 
        new ConcurrentHashMap<File, TextIndexer>();

    public static TextIndexer getInstance (File baseDir) throws IOException {
        if (indexers.containsKey(baseDir)) 
            return indexers.get(baseDir);

        try {
            TextIndexer indexer = new TextIndexer (baseDir);
            TextIndexer old = indexers.putIfAbsent(baseDir, indexer);
            return old == null ? indexer : old;
        }
        catch (IOException ex) {
            return indexers.get(baseDir);
        }
    }

    protected TextIndexer (File dir) throws IOException {
        if (!dir.isDirectory())
            throw new IllegalArgumentException ("Not a directory: "+dir);

        File index = new File (dir, "index");
        if (!index.exists())
            index.mkdirs();
        indexDir = new NIOFSDirectory 
            (index, NoLockFactory.getNoLockFactory());

        File taxon = new File (dir, "facet");
        if (!taxon.exists())
            taxon.mkdirs();
        taxonDir = new NIOFSDirectory
            (taxon, NoLockFactory.getNoLockFactory());

        indexAnalyzer = createIndexAnalyzer ();
        IndexWriterConfig conf = new IndexWriterConfig 
            (LUCENE_VERSION, indexAnalyzer);
        indexWriter = new IndexWriter (indexDir, conf);
        taxonWriter = new DirectoryTaxonomyWriter (taxonDir);

        facetsConfig = loadFacetsConfig (new File (dir, FACETS_CONFIG_FILE));
        if (facetsConfig == null) {
            int size = taxonWriter.getSize();
            if (size > 0) {
                Logger.warn("There are "+size+" dimensions in "
                            +"taxonomy but no facet\nconfiguration found; "
                            +"facet searching might not work properly!");
            }
            facetsConfig = new FacetsConfig ();
        }

        suggestDir = new File (dir, "suggest");
        if (!suggestDir.exists())
            suggestDir.mkdirs();

        // load saved lookups
        lookups = new ConcurrentHashMap<String, SuggestLookup>();
        for (File f : suggestDir.listFiles()) {
            if (f.isDirectory()) {
                try {
                    lookups.put(f.getName(), new SuggestLookup (f));
                }
                catch (IOException ex) {
                    Logger.error("Unable to load lookup from "+f, ex);
                }
            }
        }
        Logger.info("## "+suggestDir+": "
                    +lookups.size()+" lookups loaded!");

        this.baseDir = dir;
    }

    static boolean DEBUG (int level) {
        Global g = Global.getInstance();
        if (g != null)
            return g.debug(level);
        return false;
    }

    Analyzer createIndexAnalyzer () {
        Map<String, Analyzer> fields = new HashMap<String, Analyzer>();
        fields.put("id", new KeywordAnalyzer ());
        fields.put("kind", new KeywordAnalyzer ());
	return 	new PerFieldAnalyzerWrapper 
            (new StandardAnalyzer (LUCENE_VERSION), fields);
    }

    public List<LookupResult> suggest (String dim, CharSequence key, int max) 
        throws IOException {
        SuggestLookup lookup = lookups.get(dim);
        if (lookup == null) {
            Logger.debug("Unknown suggest dimension \""+dim+"\"");
            return new ArrayList ();
        }
        
        return lookup.suggest(key, max);
    }

    public List search (String text, int top, int skip) throws IOException {
        IndexSearcher searcher = new IndexSearcher
            (DirectoryReader.open(indexWriter, true));

        List results = new ArrayList ();
        try {
            QueryParser parser = new QueryParser 
                (LUCENE_VERSION, "text", indexAnalyzer);
            Query query = parser.parse(text);
            Logger.debug("## Query: "+query);

            long start = System.currentTimeMillis();
            Map<String, Model.Finder> finders = 
                new HashMap<String, Model.Finder>();

            FacetsCollector fc = new FacetsCollector ();
            TopDocs hits = FacetsCollector.search
                (searcher, query, skip+top, fc);

            TaxonomyReader taxon = new DirectoryTaxonomyReader (taxonWriter);
            Facets facets = new FastTaxonomyFacetCounts
                (taxon, facetsConfig, fc);

            List<FacetResult> facetResults = facets.getAllDims(10);
            Logger.info("## "+facetResults.size()+" facet dimension(s)");
            for (FacetResult result : facetResults) {
                Logger.info(" + ["+result.dim+"]");
                for (int i = 0; i < result.labelValues.length; ++i) {
                    LabelAndValue lv = result.labelValues[i];
                    Logger.info("     \""+lv.label+"\": "+lv.value);
                }
            }
            Logger.debug("++ Drilling down on \"MeSH\"...");
            DrillDownQuery ddq = new DrillDownQuery (facetsConfig, query);
            ddq.add("MeSH", "Mice");
            FacetsCollector fc2 = new FacetsCollector ();
            TopDocs docs = FacetsCollector.search
                (searcher, ddq, skip+top, fc2);
            Logger.debug("Drilled down results in "+docs.totalHits+" hit(s)...");
            Facets facets2 = new FastTaxonomyFacetCounts
                (taxon, facetsConfig, fc2);

            List<FacetResult> facetResults2 = facets2.getAllDims(10);
            Logger.info("## "+facetResults2.size()+" facet dimension(s)");
            for (FacetResult result : facetResults2) {
                Logger.info(" + ["+result.dim+"]");
                for (int i = 0; i < result.labelValues.length; ++i) {
                    LabelAndValue lv = result.labelValues[i];
                    Logger.info("     \""+lv.label+"\": "+lv.value);
                }
            }

            DrillSideways sideway = new DrillSideways 
                (searcher, facetsConfig, taxon);
            DrillSideways.DrillSidewaysResult swResult = 
                sideway.search(ddq, skip+top);
            Logger.info("## Drilled sideway "+swResult.facets.getAllDims(10).size()
                        +" facets and "+swResult.hits.totalHits+" hits");
            for (FacetResult result : swResult.facets.getAllDims(10)) {
                Logger.info(" + ["+result.dim+"]");
                for (int i = 0; i < result.labelValues.length; ++i) {
                    LabelAndValue lv = result.labelValues[i];
                    Logger.info("     \""+lv.label+"\": "+lv.value);
                }
            }

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
                            Model.Finder finder = 
                                finders.get(kind.stringValue());
                            if (finder == null) {
                                Class c = n != null 
                                    ? Long.class : String.class;
                                finder = new Model.Finder
                                    (c, Class.forName(kind.stringValue()));
                                finders.put(kind.stringValue(), finder);
                            }
                            results.add(finder.byId
                                        (n != null 
                                         ? n.longValue() : id.stringValue()));

                            if (DEBUG (1)) {
                                Logger.debug("++ matched doc "
                                             +field+"="+id.stringValue());
                            }
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
                         +String.format
                         ("%1$.3fs", 
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
        if (entity == null 
            || !entity.getClass().isAnnotationPresent(Entity.class)) {
            return;
        }

        Indexable indexable = 
            (Indexable)entity.getClass().getAnnotation(Indexable.class);

        if (indexable != null && !indexable.indexed()) {
            if (DEBUG (2)) {
                Logger.debug(">>> Not indexable "+entity);
            }

            return;
        }

        if (DEBUG (2))
            Logger.debug(">>> Indexing "+entity+"...");
        
        List<IndexableField> fields = new ArrayList<IndexableField>();
        fields.add(new StringField
                   ("kind", entity.getClass().getName(), YES));

        instrument (new LinkedList<String>(), entity, fields);

        Document doc = new Document ();
        for (IndexableField f : fields) {
            String text = f.stringValue();
            if (text != null) {
                if (DEBUG (2))
                    Logger.debug(".."+f.name()+":"
                                 +text+" ["+f.getClass().getName()+"]");
                
                doc.add(new TextField ("text", text, NO));
            }
            doc.add(f);
        }
        
        // now index
        //indexWriter.addDocument(fields);
        doc = facetsConfig.build(taxonWriter, doc);
        if (DEBUG (2))
            Logger.debug("++ adding document "+doc);

        indexWriter.addDocument(doc);

        if (DEBUG (2))
            Logger.debug("<<< "+entity);
    }

    public void update (Object entity) throws IOException {
        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return;
        }

        if (DEBUG (2))
            Logger.debug(">>> Updating "+entity+"...");

        try {
            for (Field f : entity.getClass().getDeclaredFields()) {
                if (f.getAnnotation(Id.class) != null) {
                    Object id = f.get(entity);
                    if (id != null) {
                        String field = entity.getClass().getName()+".id";
                        indexWriter.deleteDocuments
                            (new Term (field, id.toString()));

                        if (DEBUG (2))
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

        if (DEBUG (2))
            Logger.debug("<<< "+entity);
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
                (LUCENE_VERSION, "text", indexAnalyzer);
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

    protected void instrument (LinkedList<String> path,
                               Object entity, 
                               List<IndexableField> ixFields) {
        try {
            Field[] fields = entity.getClass().getFields();
            for (Field f : fields) {
                path.push(f.getName());

                try {
                    Class type = f.getType();
                    Object value = f.get(entity);

                    Indexable indexable = 
                        (Indexable)f.getAnnotation(Indexable.class);
                    if (indexable == null) {
                        indexable = defaultIndexable;
                    }

                    if (DEBUG (2)) {
                        Logger.debug
                            ("++ "+toPath (path)+": type="+type
                             +" value="+value);
                    }

                    if (f.getAnnotation(Id.class) != null) {
                        //Logger.debug("+ Id: "+value);
                        if (value != null) {
                            // the hidden _id field stores the field's value
                            // in its native type whereas the display field id
                            // is used for indexing purposes and as such is
                            // represented as a string
                            String kind = entity.getClass().getName();
                            if (value instanceof Long) {
                                ixFields.add(new LongField 
                                             (kind+"._id", 
                                              (Long)value, YES));
                            }
                            else {
                                ixFields.add(new StringField 
                                             (kind+"._id", 
                                              value.toString(), YES));
                            }
                            ixFields.add
                                (new StringField (kind+".id", 
                                                  value.toString(), NO));
                        }
                        else {
                            if (DEBUG (2))
                                Logger.warn("Id field "+f+" is null");
                        }
                    }
                    else if (value == null || !indexable.indexed()) {
                        // do nothing
                    }
                    else if (type.isPrimitive()) {
                        indexField (ixFields, indexable, path, value);
                    }
                    else if (type.isArray()) {
                        int len = Array.getLength(value);
                        // recursively evaluate each element in the array
                        for (int i = 0; i < len; ++i) {
                            path.push(String.valueOf(i));
                            instrument (path, Array.get(value, i), ixFields); 
                            path.pop();
                        }
                    }
                    else if (Collection.class.isAssignableFrom(type)) {
                        Iterator it = ((Collection)value).iterator();
                        for (int i = 0; it.hasNext(); ++i) {
                            path.push(String.valueOf(i));
                            instrument (path, it.next(), ixFields);
                            path.pop();
                        }
                    }
                    // why isn't this the same as using type?
                    else if (value.getClass()
                             .isAnnotationPresent(Entity.class)) {
                        // composite type; recurse
                        instrument (path, value, ixFields);
                    }
                    else { // treat as string
                        indexField (ixFields, indexable, path, value);
                    }
                }
                catch (Exception ex) {
                    if (DEBUG (3)) {
                        Logger.warn(entity.getClass()
                                    +": Field "+f+" is not indexable due to "
                                    +ex.getMessage());
                    }
                }
                path.pop();
            }

            Method[] methods = entity.getClass().getMethods();
            for (Method m: methods) {
                Indexable indexable = 
                    (Indexable)m.getAnnotation(Indexable.class);
                if (indexable != null && indexable.indexed()) {
                    // we only index no arguments methods
                    Class[] args = m.getParameterTypes();
                    if (args.length == 0) {
                        Object value = m.invoke(entity);
                        if (value != null) {
                            String name = m.getName();
                            if (name.startsWith("get"))
                                name = name.substring(3);
                            indexField (ixFields, indexable, 
                                        Arrays.asList(name), value);
                        }
                    }
                    else {
                        Logger.warn("Indexable is annotated for non-zero "
                                    +"arguments method \""+m.getName()+"\""); 
                    }
                }
            }
        }
        catch (Exception ex) {
            Logger.trace("Fetching entity fields", ex);
        }
    }

    void indexField (List<IndexableField> fields, 
                     Collection<String> path, Object value) {
        indexField (fields, null, path, value, NO);
    }

    void indexField (List<IndexableField> fields, Indexable indexable, 
                     Collection<String> path, Object value) {
        indexField (fields, indexable, path, value, NO);
    }

    void indexField (List<IndexableField> fields, Indexable indexable, 
                     Collection<String> path, Object value, 
                     org.apache.lucene.document.Field.Store store) {
        String name = path.iterator().next();
        String full = toPath (path);

        if (value instanceof Long) {
            fields.add(new NumericDocValuesField (full, (Long)value));
            fields.add(new LongField (name, (Long)value, store));
        }
        else if (value instanceof Integer) {
            fields.add(new IntDocValuesField (full, (Integer)value));
            fields.add(new IntField (name, (Integer)value, store));
        }
        else if (value instanceof Float) {
            fields.add(new FloatDocValuesField (full, (Float)value));
            fields.add(new FloatField (name, (Float)value, store));
        }
        else if (value instanceof Double) {
            fields.add(new DoubleDocValuesField (full, (Double)value));
            fields.add(new DoubleField (name, (Double)value, store));
        }
        else {
            String text = value.toString();
            String dim = indexable.name();
            if (dim.equals(""))
                dim = toPath (path, true);

            if (indexable.facet() || indexable.taxonomy()) {
                facetsConfig.setMultiValued(dim, true);
                facetsConfig.setRequireDimCount(dim, true);
                
                if (indexable.taxonomy()) {
                    facetsConfig.setHierarchical(dim, true);
                    fields.add
                        (new FacetField
                         (dim, text.split(indexable.pathsep())));
                }
                else {
                    fields.add(new FacetField (dim, text));
                }
            }

            if (indexable.suggest()) {
                try {
                    SuggestLookup lookup = lookups.get(dim);
                    if (lookup == null) {
                        lookups.put(dim, lookup = new SuggestLookup (dim));
                    }
                    lookup.add(text);
                }
                catch (Exception ex) { // 
                    Logger.debug("Can't create Lookup!", ex);
                }
            }

            fields.add(new TextField (name, text, store));
        }
    }

    static void setFieldType (FieldType ftype) {
        ftype.setIndexed(true);
        ftype.setTokenized(true);
        ftype.setStoreTermVectors(true);
        ftype.setIndexOptions
            (IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    }

    static String toPath (Collection<String> path) {
        return toPath (path, false);
    }

    static String toPath (Collection<String> path, boolean noindex) {
        StringBuilder sb = new StringBuilder ();
        for (Iterator<String> it = path.iterator(); it.hasNext(); ) {
            String p = it.next();

            boolean append = true;
            if (noindex) {
                try {
                    Integer.parseInt(p);
                    append = false;
                }
                catch (NumberFormatException ex) {
                }
            }

            if (append) {
                sb.append(p);
                if (it.hasNext())
                    sb.append('.');
            }
        }
        return sb.toString();
    }

    static FacetsConfig getFacetsConfig (JsonNode node) {
        if (!node.isContainerNode())
            throw new IllegalArgumentException
                ("Not a valid json node for FacetsConfig!");

        String text = node.get("version").asText();
        Version ver = Version.parseLeniently(text);
        if (!ver.equals(LUCENE_VERSION)) {
            Logger.warn("Facets configuration version ("+ver+") doesn't "
                        +"match index version ("+LUCENE_VERSION+")");
        }

        FacetsConfig config = null;
        ArrayNode array = (ArrayNode)node.get("dims");
        if (array != null) {
            config = new FacetsConfig ();
            for (int i = 0; i < array.size(); ++i) {
                ObjectNode n = (ObjectNode)array.get(i);
                String dim = n.get("dim").asText();
                config.setHierarchical
                    (dim, n.get("hierarchical").asBoolean());
                config.setIndexFieldName
                    (dim, n.get("indexFieldName").asText());
                config.setMultiValued(dim, n.get("multiValued").asBoolean());
                config.setRequireDimCount
                    (dim, n.get("requireDimCount").asBoolean());
            }
        }

        return config;
    }

    static JsonNode setFacetsConfig (FacetsConfig config) {
        ObjectMapper mapper = new ObjectMapper ();
        ObjectNode node = mapper.createObjectNode();
        node.put("created", new java.util.Date().getTime());
        node.put("version", LUCENE_VERSION.toString());
        node.put("warning", "AUTOMATICALLY GENERATED FILE; DO NOT EDIT");
        Map<String, FacetsConfig.DimConfig> dims = config.getDimConfigs();
        node.put("size", dims.size());
        ArrayNode array = node.putArray("dims");
        for (Map.Entry<String, FacetsConfig.DimConfig> me : dims.entrySet()) {
            FacetsConfig.DimConfig c = me.getValue();
            ObjectNode n = mapper.createObjectNode();
            n.put("dim", me.getKey());
            n.put("hierarchical", c.hierarchical);
            n.put("indexFieldName", c.indexFieldName);
            n.put("multiValued", c.multiValued);
            n.put("requireDimCount", c.requireDimCount);
            array.add(n);
        }
        return node;
    }

    static void saveFacetsConfig (File file, FacetsConfig facetsConfig) {
        JsonNode node = setFacetsConfig (facetsConfig);
        ObjectMapper mapper = new ObjectMapper ();
        try {
            FileOutputStream out = new FileOutputStream (file);
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, node);
            out.close();
        }
        catch (IOException ex) {
            Logger.trace("Can't persist facets config!", ex);
        }
    }

    static FacetsConfig loadFacetsConfig (File file) {
        FacetsConfig config = null;
        if (file.exists()) {
            ObjectMapper mapper = new ObjectMapper ();
            try {
                JsonNode conf = mapper.readTree(new FileInputStream (file));
                config = getFacetsConfig (conf);
                Logger.info("## FacetsConfig loaded with "
                            +config.getDimConfigs().size()
                            +" dimensions!");
            }
            catch (Exception ex) {
                Logger.trace("Can't read file "+file, ex);
            }
        }
        return config;
    }

    public void shutdown () {
        try {
            for (SuggestLookup look : lookups.values()) {
                look.close();
            }

            if (indexWriter != null)
                indexWriter.close();
            if (taxonWriter != null)
                taxonWriter.close();
            indexDir.close();
            taxonDir.close();

            saveFacetsConfig (new File (baseDir, FACETS_CONFIG_FILE), 
                              facetsConfig);
        }
        catch (IOException ex) {
            //ex.printStackTrace();
            Logger.trace("Closing index", ex);
        }
        indexers.remove(baseDir);
    }
}
