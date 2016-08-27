package ix.core.search;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.sorter.EarlyTerminatingSortingCollector;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class InxightInfixSuggester extends AnalyzingInfixSuggester{
	private static final Sort SORT2 = new Sort(new SortField("weight", SortField.Type.LONG, true));
	public InxightInfixSuggester(Version matchVersion, Directory dir,
			Analyzer analyzer) throws IOException {
		super(matchVersion, dir, analyzer);
	}
	
	/**
	 * 
	 * This method will get the assigned weight for the exact match text provided
	 * 
	 * @param text
	 * @return
	 */
	public long getWeightFor(BytesRef text){
		IndexSearcher searcher=null;
		try{
			Term t=new Term(EXACT_TEXT_FIELD_NAME, text.utf8ToString());
			TermQuery tq=new TermQuery(t);
			
			// Sort by weight, descending:
		    TopFieldCollector c = TopFieldCollector.create(SORT2, 2, true, false, false, false);

		    // We sorted postings by weight during indexing, so we
		    // only retrieve the first num hits now:
		    Collector c2 = new EarlyTerminatingSortingCollector(c, SORT2, 2);
		    
			searcher = searcherMgr.acquire();
			searcher.search(tq, c2);
			
			TopFieldDocs hits = (TopFieldDocs) c.topDocs();
			if(hits.totalHits>=1){
				int i=0;
				FieldDoc fd = (FieldDoc) hits.scoreDocs[i];
			    long score = (Long) fd.fields[0];
			    return score;
			}
		}catch(Exception e){
			e.printStackTrace();
		} finally{
			if(searcher!=null){
				try{
					searcherMgr.release(searcher);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
    	return 0;
	}

}