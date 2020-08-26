package ix.core.search.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.SearcherManager;

import java.io.IOException;

public interface IndexerService extends IndexListener {
    boolean flushChangesIfNeeded();
    SearcherManager createSearchManager() throws IOException;
    IndexReader createIndexReader() throws IOException;

    Analyzer getIndexAnalyzer();
}
