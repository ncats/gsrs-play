package ix.core.search.text;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

import ix.core.search.EntityFetcher;
import ix.core.search.SearchOptions;
import ix.core.search.SearchResult;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.Key;

import ix.utils.Util;
import play.Logger;

/**
 * Takes the TopDocs and IndexSearcher from a lucene index, 
 * and uses them to populate a given SearchResult with
 * the expected results. In the current instantiation,
 * the objects returned are deferred rather than direct.
 * 
 * @author peryeata
 *
 */
class LuceneSearchResultPopulator {
	SearchResult result;
	TopDocs hits;
	IndexSearcher searcher;
	SearchOptions options;
	int total, offset;
	
	LuceneSearchResultPopulator(SearchResult result, TopDocs hits, IndexSearcher searcher) {
		this.result = result;
		this.hits = hits;
		this.searcher = searcher;
		this.options = result.getOptions();
		result.setCount(hits.totalHits);
		total  = Math.max(0, Math.min(options.max(), result.getCount()));
		offset = Math.min(options.getSkip(), total);
	}
	
	
	public void setSearcher(IndexSearcher searcher){
		this.searcher=searcher;
	}

	void fetch() throws IOException, InterruptedException {
		try {
			fetch(total);
		} finally {
			result.done();
		}
	}

	void fetch(int size) throws IOException, InterruptedException {
		size = Math.min(options.getTop(), Math.min(total - offset, size));
		
		for (int i = result.size(); i < size; ++i) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Document doc = searcher.doc(hits.scoreDocs[i + offset].doc); //bad idea
			try {
				
				Key k = Key.of(doc);
				result.addNamedCallable(new EntityFetcher<>(k));
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error(e.getMessage());
			}
		}
	}
}