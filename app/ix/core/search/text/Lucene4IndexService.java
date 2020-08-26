package ix.core.search.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jcvi.jillion.core.io.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Lucene4IndexService implements IndexerService {

    //following code block copied from TextIndexer
    /**
     * Make sure to properly update the code when upgrading version
     */
    static final Version LUCENE_VERSION = Version.LATEST;
    static final String FACETS_CONFIG_FILE = "facet_conf.json";
    static final String SUGGEST_CONFIG_FILE = "suggest_conf.json";
    static final String SORTER_CONFIG_FILE = "sorter_conf.json";
    public static final String DIM_CLASS = "ix.Class";

    private IndexWriter indexWriter;
    private  Directory indexDir;
    private Analyzer indexAnalyzer;

    public Lucene4IndexService() throws IOException{
        indexDir= new RAMDirectory();
        Analyzer indexAnalyzer = createIndexAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(LUCENE_VERSION, indexAnalyzer);

        indexWriter = new IndexWriter(indexDir, conf);
    }
    public Lucene4IndexService(File dir) throws IOException{
        // Path dirPath = baseDir.toPath();
        if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir);
        }
        File indexFileDir = new File(dir, "index");
        Files.createDirectories(indexFileDir.toPath());
        //
        // if (!indexFileDir.exists())
        // indexFileDir.mkdirs();
        indexDir = new NIOFSDirectory(indexFileDir, NoLockFactory.getNoLockFactory());
        indexAnalyzer = createIndexAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(LUCENE_VERSION, indexAnalyzer);
        indexWriter = new IndexWriter(indexDir, conf);
    }

    @SuppressWarnings("deprecation")
    static Analyzer createIndexAnalyzer() {
        Map<String, Analyzer> fields = new HashMap<>();
        fields.put(TextIndexer.FIELD_ID, new KeywordAnalyzer());
        fields.put(TextIndexer.FIELD_KIND, new KeywordAnalyzer());
        //dkatzel 2017-08 no stop words
        return new PerFieldAnalyzerWrapper(new StandardAnalyzer(LUCENE_VERSION, CharArraySet.EMPTY_SET), fields);
    }
    @Override
    public Analyzer getIndexAnalyzer(){
        return indexAnalyzer;
    }

    @Override
    public SearcherManager createSearchManager() throws IOException{
        return new SearcherManager(indexWriter, true, null);
    }
    @Override
    public IndexReader createIndexReader() throws IOException{
        return  DirectoryReader.open(indexWriter, true);
    }
    @Override
    public Document addDocument(Document doc) {
        try {
            indexWriter.addDocument(doc);
            return doc;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }



    @Override
    public void deleteDocuments(Query query) {
        try {
            indexWriter.deleteDocuments(query);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void removeAll() {
        try {
            indexWriter.deleteAll();
            indexWriter.commit();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        IOUtil.closeAndIgnoreErrors(indexWriter, indexDir);

    }

    @Override
    public boolean flushChangesIfNeeded() {
        if(indexWriter.hasUncommittedChanges()){
            try {
                indexWriter.commit();
            } catch (IOException e) {
                //doing what TextIndexer did
                e.printStackTrace();
                try {
                    indexWriter.rollback();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }
}
