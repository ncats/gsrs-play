package ix.core.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.MultiDocValues;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.sorter.EarlyTerminatingSortingCollector;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.*;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.*;

public class InxightInfixSuggester extends AnalyzingInfixSuggester{
	private static final Sort SORT2 = new Sort(new SortField("weight", SortField.Type.LONG, true));
	public InxightInfixSuggester(Version matchVersion, Directory dir,
			Analyzer analyzer) throws IOException {
		super(matchVersion, dir, analyzer);
	}

	@Override
	public List<LookupResult> lookup(CharSequence key, Set<BytesRef> contexts, int num, boolean allTermsRequired, boolean doHighlight) throws IOException {
		//katzelda 2/2017
		// this is the real lookup method
		//the problem is our weights are bad and we can get tons of hits with the same weight
		//so we want to re-order it so exact matches are first.
		//
		//My first attempt was to just reorder the returned list but that doesn't work
		//because we might miss an exact match in the limited number of results returned
		//
		//so the solution is to do an exact search query first and then
		//append the normal suggest results after dealing with duplicate hits.

		List<LookupResult> exactMatches = getExactHitsFor(key, num);

			List<LookupResult>  superMatches =  super.lookup(key, contexts, num + exactMatches.size(), allTermsRequired, doHighlight);
		if(exactMatches.isEmpty()){
			return superMatches;
		}

		//remove any duplicate exact matches
		exactMatches.forEach( r -> {
			Iterator<LookupResult> iter = superMatches.iterator();
			String rKey = r.key.toString();

			while(iter.hasNext()){
				LookupResult r2 = iter.next();
				String r2Key = r2.key.toString();

				if(doHighlight){
					//for some reason the super lookup's key is the highlight!!
					//which adds bold tag around the match
					r2Key = r2Key.replaceAll("<b>(.+?)</b>", "$1");
				}
				if(rKey.equalsIgnoreCase(r2Key)){
					//duplicate
					iter.remove();

				}
			}

		});


		List<LookupResult> combinedList = new ArrayList<>(exactMatches.size() + superMatches.size());
		combinedList.addAll(exactMatches);
		combinedList.addAll(superMatches);

		//in case we get an exact match that isn't included in the super call limit the size of the return to num
		if(combinedList.size() > num) {
			return combinedList.subList(0, num);
		}
		return combinedList;
	}



	private List<LookupResult> getExactHitsFor(CharSequence query, int num){
		Term t=new Term(EXACT_TEXT_FIELD_NAME, new BytesRef(query).utf8ToString());
		TermQuery tq=new TermQuery(t);

		IndexSearcher searcher=null;
		try{
			// Sort by weight, descending:
			TopFieldCollector c = TopFieldCollector.create(SORT2, num, true, false, false, false);

			// We sorted postings by weight during indexing, so we
			// only retrieve the first num hits now:
			Collector c2 = new EarlyTerminatingSortingCollector(c, SORT2, num);

			searcher = searcherMgr.acquire();
			searcher.search(tq, c2);

			TopFieldDocs hits = (TopFieldDocs) c.topDocs();

			if(hits.totalHits ==0){
				return Collections.emptyList();
			}
			BinaryDocValues textDV = MultiDocValues.getBinaryValues(searcher.getIndexReader(), TEXT_FIELD_NAME);
			String queryStr = query.toString();
			List<LookupResult> exactMatches = new ArrayList<>();
			for (int i=0;i<hits.scoreDocs.length;i++) {
				FieldDoc fd = (FieldDoc) hits.scoreDocs[i];
				BytesRef term = textDV.get(fd.doc);
				String text = term.utf8ToString();
				if(text.equalsIgnoreCase(queryStr)){
					//used by TextIndexer like this
					//.map(r -> new SuggestResult(r.payload.utf8ToString(), r.key, r.value))
					//key, highlight, weight
					exactMatches.add(new LookupResult(query, "<b>"+queryStr + "</b>", Integer.MAX_VALUE, new BytesRef(query)));
				}
			}
			return exactMatches;
		}catch(Exception e){
			throw new RuntimeException(e);
		} finally{
			if(searcher!=null){
				try{
					searcherMgr.release(searcher);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
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