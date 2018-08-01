package ix.ginas.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ix.core.EntityProcessor;
import ix.ginas.models.v1.Name;

public class FDANameNormalizer implements EntityProcessor<Name>{
	

	
	private static final Map<String, String> toFdaMap, fromFdaMap;
	static{
		fromFdaMap = new HashMap<>();

		fromFdaMap.put(".ALPHA.", "\u03b1");
		fromFdaMap.put(".BETA.", "\u03b2");
		fromFdaMap.put(".GAMMA.", "\u03b3");
		fromFdaMap.put(".DELTA.", "\u03b4");
		fromFdaMap.put(".EPSILON.", "\u03b5");
		fromFdaMap.put(".ZETA.", "\u03b6");
		fromFdaMap.put(".ETA.", "\u03b7");
		fromFdaMap.put(".THETA.", "\u03b8");
		fromFdaMap.put(".IOTA.", "\u03b9");
		fromFdaMap.put(".KAPPA.", "\u03ba");
		fromFdaMap.put(".LAMBDA.", "\u03bb");
		fromFdaMap.put(".MU.", "\u03bc");
		fromFdaMap.put(".NU.", "\u03bd");
		fromFdaMap.put(".XI.", "\u03be");
		fromFdaMap.put(".OMICRON.", "\u03bf");

		fromFdaMap.put(".PI.", "\u03c0");
		fromFdaMap.put(".RHO.", "\u03c1");
		fromFdaMap.put(".SIGMA.", "\u03c3");
		//skip final sigma
		fromFdaMap.put(".TAU.", "\u03c4");
		fromFdaMap.put(".UPSILON.", "\u03c5");
		fromFdaMap.put(".PHI.", "\u03c6");
		fromFdaMap.put(".CHI.", "\u03c7");
		fromFdaMap.put(".PSI.", "\u03c8");
		fromFdaMap.put(".OMEGA.", "\u03c9");
		fromFdaMap.put("+/-", "\u00b1");


		toFdaMap = new HashMap<>();

		for(Map.Entry<String, String> e : fromFdaMap.entrySet()){
			toFdaMap.put(e.getValue(), e.getKey());
		}
	}

	/**
	 * Replaces all occurrences of keys of the given map in the given string
	 * with the associated value in that map.
	 *
	 * This method is semantically the same as calling
	 * {@link String#replace(CharSequence, CharSequence)} for each of the
	 * entries in the map, but may be significantly faster for many replacements
	 * performed on a short string, since
	 * {@link String#replace(CharSequence, CharSequence)} uses regular
	 * expressions internally and results in many String object allocations when
	 * applied iteratively.
	 *
	 * The order in which replacements are applied depends on the order of the
	 * map's entry set.
	 *
	 * This method was taken from a blog written by Fabian Streitel
	 * https://www.cqse.eu/en/blog/string-replace-performance/
	 *
	 * @param string the input string
	 * @param replacements the Map of literal string replacements to try to make.
	 */
	public static String replaceFromMap(String string,
										Map<String, String> replacements) {
		StringBuilder sb = new StringBuilder(string);
		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			int start = sb.indexOf(key, 0);
			while (start > -1) {
				int end = start + key.length();
				int nextSearchStart = start + value.length();
				sb.replace(start, end, value);
				start = sb.indexOf(key, nextSearchStart);
			}
		}
		return sb.toString();
	}
	
	public static String toFDA(String name){
		return replaceFromMap(name, toFdaMap).toUpperCase();

	}
	
	public static String fromFDA(String name){
		return replaceFromMap(name, fromFdaMap);

	}
	

	

	@Override
	public void prePersist(Name obj) {
		String name = obj.getName();
		obj.stdName=fromFDA(name);
	}
	
	@Override
	public void preUpdate(Name obj) {
		prePersist(obj);
	}
	
}