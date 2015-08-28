package ix.core.search;

import ix.core.models.Keyword;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;

import play.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class GinasSearchAnalyzer implements SearchContextAnalyzer<Substance>{
	private static final String NULL_FIELD = "{NULL}";
	Map<String, FieldFacet> ffacet = new HashMap<String, FieldFacet>();

	public Set<Term> POISON=new HashSet<Term>();
	public Map<String, Set<Term>> translationCache = new HashMap<String,Set<Term>>();
	
	public void updateFieldQueryFacets(Substance o, String q) {
		Set<Term> qterms=null;
		try{
			qterms=translationCache.get(q);
			if(qterms==POISON)return;
			if(qterms==null){
				qterms=extractUnqualifiedTerms(q);
				translationCache.put(q, qterms);
			}
		}catch(Exception e){
			translationCache.put(q, POISON);
			return;
		}
		
		try {
			Logger.debug("About to analyze");
			updateFieldQueryFacets(o, qterms, ffacet);
		} catch (Exception e) {

		}
	}
	
	@Override
	public List<FieldFacet> getFieldFacets() {
		return new ArrayList<FieldFacet>(ffacet.values());
	}

	/**
	 * This is an exceedingly lazy method for analyzing search result.
	 * 
	 * All it does is look through the objects for the query, and returns what
	 * path the query matched.
	 * 
	 * It does this very stupidly, by serializing the objects to a json object,
	 * flattening them, and listing the paths.
	 * 
	 * @param obj
	 * @return
	 * @throws JsonProcessingException
	 */
	public static List<FieldFacet> getFieldMathingList(Iterator<? extends Substance> it, String q)
			throws Exception {
		Map<String, FieldFacet> ffacet = new HashMap<String, FieldFacet>();

		Substance o = null;

		while (it.hasNext()) {
			o = it.next();
			updateFieldQueryFacets(o, q, ffacet);
		}

		return new ArrayList<FieldFacet>(ffacet.values());
	}

	public static List<FieldFacet> getFieldMathingList(Collection<? extends Substance> c, String q)
			throws Exception {
		return getFieldMathingList(c.iterator(), q);
	}

	public static void updateFieldQueryFacets(Substance o, String q,
			Map<String, FieldFacet> ffacet) throws Exception {
		updateFieldQueryFacets(o,extractUnqualifiedTerms(q),ffacet);
	}
	
	public static Set<Term> extractUnqualifiedTerms(String q) throws QueryNodeException{
		StandardQueryParser sqp = new StandardQueryParser();
		Query q2 = sqp.parse(q.toUpperCase(), GinasSearchAnalyzer.NULL_FIELD);
		Set<Term> myterms = new HashSet<Term>();
		Set<Term> realterms = new HashSet<Term>();
		q2.extractTerms(myterms);
		
		for(Term t:myterms){
			if(t.field().equals(NULL_FIELD)){
				realterms.add(t);
			}
		}
		return realterms;
	}
	public static ObjectMapper mappingAnalyzer(final PrintWriter pw){
		ObjectMapper mapper = new ObjectMapper();
		

		com.fasterxml.jackson.databind.BeanDescription bd;
		
		SimpleModule module = new SimpleModule();
		module.setSerializerModifier(new BeanSerializerModifier(){
			@Override public JsonSerializer modifySerializer(SerializationConfig config, com.fasterxml.jackson.databind.BeanDescription beanDesc, final JsonSerializer serializer)
		      {
				
		        return new JsonSerializer(){
					@Override
					public void serialize(Object arg0, JsonGenerator arg1,
							SerializerProvider arg2) throws IOException,
							JsonProcessingException {
						System.out.println("Serializing:" + arg0.getClass().toString());
						long l=System.currentTimeMillis();
						serializer.serialize(arg0, arg1, arg2);
						pw.println("Serializing:" + arg0.getClass().toString() + "\t" + (System.currentTimeMillis()-l));
						
					}
		        };
		      }
		});
		mapper.registerModule(module);
		return mapper;
	}
	
	public static void updateFieldQueryFacets(Substance o, Set<Term> realterms,
			Map<String, FieldFacet> ffacet) throws Exception {
		
		
		if(realterms == null || realterms.size()<=0)throw new IllegalStateException("Need unspecified field queiries");
		

		
		Map<String,String> m2 = new TreeMap<String,String>();		
		{
			int i=0;
			for(Name n: o.getAllNames()){
				m2.put("names[" + i++ + "].name", n.name);
				//m2.put( ".names[" + i + "].name", n.name);			
			}
		}
		
		{
			int i=0;
			for(Code n: o.codes){
				m2.put( "codes[" + i + "].code", n.code);
				m2.put( "codes[" + i + "].codeSystem", n.codeSystem);
				m2.put( "codes[" + i + "].comments", n.comments);
				i++;			
			}
		}
		
//		{
//			int i=0;
//			for(Reference n: o.references){
//				m2.put( "references[" + i + "].citation", n.citation);
//				m2.put( "references[" + i + "].docType", n.docType);
//				i++;			
//			}
//		}
		
//		{
//			int i=0;
//			for(Relationship n: o.relationships){
//				m2.put( "relationships[" + i + "].qualification", n.qualification);
//				m2.put( "relationships[" + i + "].comments", n.comments);
//				m2.put( "relationships[" + i + "].interactionType", n.interactionType);
//				m2.put( "relationships[" + i + "].relatedSubstance.refPname", n.relatedSubstance.refPname);
//				m2.put( "relationships[" + i + "].relatedSubstance.approvalID", n.relatedSubstance.approvalID);
//				i++;			
//			}
//		}
		
//		{
//			int i=0;
//			for(Note n: o.notes){
//				m2.put( "notes[" + i + "].note", n.note);
//				i++;			
//			}
//		}
		
//		{
//			int i=0;
//			for(Keyword n: o.tags){
//				m2.put( "tags[" + i + "]", n.getValue());
//				i++;			
//			}
//		}
		
		
		Set<String> matchedFields = new HashSet<String>();
		//Logger.debug("About to traverse : " + m2.size());
		
		
		for (Object key : m2.keySet()) {
			//Logger.debug("About to simplify:" + key);
			String realkey = MapObjectUtils.simplifyKeyPath(key + "");
			
			if(ignoreField(realkey))continue;
			if (matchedFields.contains(realkey))continue;
			for(Term t:realterms){
				MATCH_TYPE match = getMatchType(m2.get(key),t.text());
				if(match==MATCH_TYPE.NO_MATCH)continue;
				//if(match==MATCH_TYPE.CONTAINS)continue;
				
				matchedFields.add(realkey);
				String q = t.text();
				if(match==MATCH_TYPE.WORD_STARTS_WITH)
					q= q + "*";
				FieldFacet ff = ffacet.get(realkey);
				if (ff == null) {
					ff = new FieldFacet(realkey, q);
					ffacet.put(realkey, ff);
				}
				ff.count++;
			}
		}
	}
	
	public static MATCH_TYPE getMatchType(String tterm, String q) {
		String term = tterm.toUpperCase();
		
		if (term.equals(q))
			return MATCH_TYPE.FULL;
		
		int i = term.indexOf(q);

		if (i < 0)
			return MATCH_TYPE.NO_MATCH;
		
		if (i == 0) {
			if (term.charAt(i + 1) == ' ')
				return MATCH_TYPE.WORD;
			return MATCH_TYPE.WORD_STARTS_WITH;
		}
		
		if (term.length() == i + 1) {
			if (term.charAt(i + q.length() + 1) == ' ')
				return MATCH_TYPE.WORD;
		}
		
		if (term.charAt(i - 1) == ' '){
			if(term.charAt(i + q.length() + 1) == ' ')
				return MATCH_TYPE.WORD;
			return MATCH_TYPE.WORD_STARTS_WITH;
		}
		System.out.println("\"" + term + "\" contains \"" + q + "\"");
		return MATCH_TYPE.CONTAINS;
	}

	public static enum MATCH_TYPE{
		FULL,
		WORD,
		WORD_STARTS_WITH,
		CONTAINS,
		NO_MATCH
	};
	
	public static boolean ignoreField(String field){
		if(field.contains("._"))return true;
		return false;
	}
	
}
