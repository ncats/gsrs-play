package ix.core.search;

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

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;

import play.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class LazySearchAnalyzer implements SearchContextAnalyzer{
	private static final String NULL_FIELD = "{NULL}";
	Map<String, FieldFacet> ffacet = new HashMap<String, FieldFacet>();

	public Set<Term> POISON=new HashSet<Term>();
	public Map<String, Set<Term>> translationCache = new HashMap<String,Set<Term>>();
	
	public void updateFieldQueryFacets(Object o, String q) {
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
	public static List<FieldFacet> getFieldMathingList(Iterator it, String q)
			throws Exception {
		Map<String, FieldFacet> ffacet = new HashMap<String, FieldFacet>();

		Object o = null;

		while (it.hasNext()) {
			o = it.next();
			updateFieldQueryFacets(o, q, ffacet);
		}

		return new ArrayList<FieldFacet>(ffacet.values());
	}

	public static List<FieldFacet> getFieldMathingList(Collection c, String q)
			throws Exception {
		return getFieldMathingList(c.iterator(), q);
	}

	public static void updateFieldQueryFacets(Object o, String q,
			Map<String, FieldFacet> ffacet) throws Exception {
		updateFieldQueryFacets(o,extractUnqualifiedTerms(q),ffacet);
	}
	
	public static Set<Term> extractUnqualifiedTerms(String q) throws QueryNodeException{
		StandardQueryParser sqp = new StandardQueryParser();
		Query q2 = sqp.parse(q, LazySearchAnalyzer.NULL_FIELD);
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
	
	public static void updateFieldQueryFacets(Object o, Set<Term> realterms,
			Map<String, FieldFacet> ffacet) throws Exception {
		
		
		if(realterms == null || realterms.size()<=0)throw new IllegalStateException("Need unspecified field queiries");
		
		
		
		ObjectMapper om = new ObjectMapper();
		Logger.debug("About to serialize");
		
		PrintWriter pw = new PrintWriter(o.toString() + ".analyze.log");
		JsonNode jn = mappingAnalyzer(pw).valueToTree(o);
		pw.close();
		//Logger.debug("About to deserialize");
		//om.se
		Map m = om.treeToValue(jn, Map.class);
		//Logger.debug("About to flatten");
		Map m2 = MapObjectUtils.flatten(m,o.getClass().getSimpleName());
		String lastMatchKey = null;
		//Logger.debug("About to traverse : " + m2.size());
		
		
		for (Object key : m2.keySet()) {
			pw.println(key);
			pw.flush();
			//Logger.debug("About to simplify:" + key);
			String realkey = MapObjectUtils.simplifyKeyPath(key + "");
			if(ignoreField(realkey))continue;
			if (realkey.equals(lastMatchKey))continue;
			for(Term t:realterms){
				if ((m2.get(key) + "").contains(t.text())) {
					lastMatchKey = realkey;
					
					FieldFacet ff = ffacet.get(realkey);
					if (ff == null) {
						ff = new FieldFacet(realkey, t.text());
						ffacet.put(realkey, ff);
					}
					ff.count++;
				}
			}
		}
		pw.close();
	}

	public static boolean ignoreField(String field){
		if(field.contains("._"))return true;
		return false;
	}
	
}
