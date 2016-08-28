package ix.core.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;

import ix.core.search.FieldFacet.MATCH_TYPE;

public abstract class FieldBasedSearchAnalyzer<K> implements SearchAnalyzer<K> {
	private static final String NULL_FIELD = "{NULL}";
	
	Map<String, FieldFacet> ffacet = new HashMap<String, FieldFacet>();

	public Set<Term> POISON=new HashSet<Term>();
	public Map<String, Set<Term>> translationCache = new HashMap<String,Set<Term>>();
	
	protected int recordsToAnalyze=100;
	protected boolean enabled=true;
	protected int recordsAnalyzed=0;
	
	/**
	 * Update statistics for FieldFacets, to be used for
	 * context of text search results. 
	 */
	public void addWithQuery(K o, String q) {
		if(!isEnabled())return;
		if(o==null)return;
		recordsAnalyzed++;
		
		Set<Term> qterms=parseQuery(q);
		if(qterms == null || qterms.size()<=0)return;
		
		try {
			Map<String,String> m2 = flattenObject(o);
			updateFieldQueryFacets(m2, qterms, ffacet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract Map<String,String> flattenObject(K o);
	
	private Set<Term> parseQuery(String q){
		Set<Term> qterms=null;
		try{
			qterms=translationCache.get(q);
			if(qterms==POISON)return null;
			if(qterms==null){
				qterms=extractUnqualifiedTerms(q);
				translationCache.put(q, qterms);
			}
		}catch(Exception e){
			translationCache.put(q, POISON);
			return null;
		}
		return qterms;
	}
	
	
	@Override
	public List<FieldFacet> getFieldFacets() {
		return new ArrayList<FieldFacet>(ffacet.values());
	}

	
	private static Set<Term> extractUnqualifiedTerms(String q) throws QueryNodeException{
		StandardQueryParser sqp = new StandardQueryParser();
		Query q2 = sqp.parse(q.toUpperCase(), FieldBasedSearchAnalyzer.NULL_FIELD);
		Set<Term> myterms = new LinkedHashSet<Term>();
		q2.extractTerms(myterms);
		return myterms
				.stream()
				.filter(t -> t.field().equals(NULL_FIELD))
				.collect(Collectors.toSet());
	}
	
	private static void updateFieldQueryFacets(Map<String,String> m2, 
			Set<Term> realterms,
			Map<String, FieldFacet> ffacet) throws Exception {
		
		if(realterms == null || realterms.size()<=0){
			throw new IllegalStateException("Need unspecified field queiries");
		}
		
		Set<String> matchedFields = new HashSet<String>();
		
		m2.forEach((key,value)->{
			String realkey = MapObjectUtils.simplifyKeyPath(key);
			
			if(ignoreField(realkey))return;
			
			for(Term t:realterms){
				String q = t.text();
				MATCH_TYPE match = getMatchType(value,q);
				
				if(match==MATCH_TYPE.NO_MATCH)continue;
				if(match==MATCH_TYPE.CONTAINS)continue;
				if(match==MATCH_TYPE.WORD_STARTS_WITH)continue;
				
				
				if (matchedFields.contains(realkey + match))continue;
				
				FieldFacet ff = ffacet.get(realkey + match);
				if (ff == null) {
					ff = new FieldFacet(realkey, q, match);
					ffacet.put(realkey + match, ff);
				}
				ff.count++;
				matchedFields.add(realkey + match);
			}
		});
	}
	
	public static MATCH_TYPE getMatchType(String tterm, String q) {
		if(tterm==null) return MATCH_TYPE.NO_MATCH;
		String term = tterm.toUpperCase().trim();
		
		//System.out.println(term + "?=" + q);
		if (term.equals(q)){
			return MATCH_TYPE.FULL;
		}
		
		int i = term.indexOf(q);

		if (i < 0){
			return MATCH_TYPE.NO_MATCH;
		}
		
		if (i == 0) {
			if (term.charAt(i + q.length()) == ' '){
				return MATCH_TYPE.WORD;
			}
			return MATCH_TYPE.WORD_STARTS_WITH;
		}
		
		
		//ends the value
		if (term.length() == i + q.length()) {
			if (term.charAt(i-1) == ' '){
				return MATCH_TYPE.WORD;
			}
		}
		
		
		if (term.charAt(i - 1) == ' '){
			if(term.charAt(i + q.length()) == ' '){
				return MATCH_TYPE.WORD;
			}
			return MATCH_TYPE.WORD_STARTS_WITH;
		}
		return MATCH_TYPE.CONTAINS;
	}

	
	public static boolean ignoreField(String field){
		if(field.contains("._"))return true;
		return false;
	}
	
	
	@Override
	public boolean isEnabled() {
		return enabled && (recordsAnalyzed<recordsToAnalyze);
	}

}
