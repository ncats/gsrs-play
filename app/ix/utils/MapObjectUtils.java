package ix.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapObjectUtils {
	private static final String TAB_CHAR = "\t";
	private static final String ARRAY_INDEX_SPECIFIER_RIGHT = "]";
	private static final String ARRAY_INDEX_SPECIFIER_LEFT = "[";
	private static final String PATH_SEPARATOR = ".";
	private static final String ROOT = "root";

	private static Pattern ARRAY_REPLACER = Pattern.compile("\\[[0-9]*\\]");
	
	public static enum MatchType {
		EXACT_STRING, NUMERIC_EQUIV
	}

	public abstract static class EquivalenceAnalyzer {
		public abstract boolean equals(Object o1, Object o2);

		public static final EquivalenceAnalyzer EXACT_MATCHER = new EquivalenceAnalyzer() {

			@Override
			public boolean equals(Object o1, Object o2) {
				return (o1 + "").equals(o2 + "");
			}

		};
		public static final EquivalenceAnalyzer NUMERIC_EQUIV_MATCHER = new EquivalenceAnalyzer() {
			public static final double equivRatio = 0.01;

			@Override
			public boolean equals(Object o1, Object o2) {
				boolean exact = (o1 + "").equals(o2 + "");
				if (!exact) {
					try {
						double d1 = Double.parseDouble(o1 + "");
						double d2 = Double.parseDouble(o2 + "");
						double diff = Math.abs(d2 - d1);
						double max = Math.max(Math.abs(d1), Math.abs(d2));
						if (Math.max(Math.abs(d1), Math.abs(d2)) > 0) {
							if (diff / max < equivRatio) {
								return true;
							}
						} else {
							return true;
						}
					} catch (Exception e) {

					}
				}
				return exact;
			}

		};
	}

	/**
	 * "Canonicalizes" an object. This means only that any list in the map will
	 * be sorted lexicographically, with depth-first recursion.
	 * 
	 * This is done with toString() method for non-primitive objects.
	 * 
	 * This does NOT sort the keys of each map.
	 * 
	 * @param m
	 */
	public static void canonicalize(Map m) {
		traverseMap(m, new TreeTraverse() {
			@Override
			public void process(String path, Object o) {
				if (o instanceof List) {
					List l = ((List) o);
					Collections.sort(l, new Comparator() {

						@Override
						public int compare(Object o1, Object o2) {
							return o1.toString().compareTo(o2.toString());
						}

					});
				}

			}
		}, "doc");
	}

	/**
	 * Converts the tree structure of a map-of-maps-and-lists typical to complex
	 * JSON objects into a flat string-to-string map, more typical of struts
	 * HTML form syntax.
	 * 
	 * 
	 * That is, a map object like the following: {test={item=[2, 1]}} Will
	 * become: {root.test.item[0]=1, root.test.item[1]=2}
	 * 
	 * Here, "root" is used as the root node for the map.
	 * 
	 * @param m
	 *            the Map object to be flattened
	 * @param root
	 *            the string used for the root node
	 * @return
	 */
	public static LinkedHashMap<String, String> flatten(Map m, final String root) {
		final LinkedHashMap<String, String> mflat = new LinkedHashMap<String, String>();
		traverseMap(m, new TreeTraverse() {

			@Override
			public void process(String path, Object o) {
				if (o instanceof Map || o instanceof List)
					return;
				if (o != null)
					mflat.put(path, o.toString());

			}
		}, root);
		return mflat;
	}
	
	

	public static LinkedHashMap<String, String> flatten(Map m) {
		return flatten(m, MapObjectUtils.ROOT);
	}

	/**
	 * Generates a Map of differences between 2 map objects that are typical of
	 * JSON-serialized objects.
	 * 
	 * Resulting map is of the form: key = {OPERATION}{TAB}{PATH} value= {VALUE}
	 * 
	 * The {OPERATION} values are: ADD [addition from m1 to m2] REMOVE [removal
	 * from m1 to m2]
	 * 
	 * For example, comparing these maps, with canonicalization:
	 * {"test":{"item":["2","1"]}} {"test":{"item":["1","3"]},"ok":2}
	 * 
	 * Will produce: {REMOVE\troot.test.item[1]=2, ADD\troot.test.item[1]=3,
	 * ADD\troot.ok=2.0}
	 * 
	 * Where "\t" represents a tab character.
	 * 
	 * @param m1
	 *            first map to be diff'd
	 * @param m2
	 *            second map to be diff'd
	 * @param canonicalize
	 *            perform canonicalization step prior to diff mapping
	 * @param mt
	 *            specific match type for equivalence testing. null is
	 *            equivalent to EXACT_STRING
	 * @return
	 */
	public static Map<String, String> mapDiff(Map m1, Map m2,
			boolean canonicalize, MatchType mt) {
		Map<String, String> diffs = new LinkedHashMap<String, String>();
		if (canonicalize) {
			canonicalize(m1);
			canonicalize(m2);
		}
		EquivalenceAnalyzer evaluator = EquivalenceAnalyzer.EXACT_MATCHER;
		switch (mt) {
		case EXACT_STRING:
			break;
		case NUMERIC_EQUIV:
			evaluator = EquivalenceAnalyzer.NUMERIC_EQUIV_MATCHER;
			break;
		default:
			break;

		}
		LinkedHashMap<String, String> flat1 = flatten(m1);
		LinkedHashMap<String, String> flat2 = flatten(m2);
		LinkedHashSet<String> flat1_missing_flat2 = new LinkedHashSet<String>(
				flat1.keySet());
		LinkedHashSet<String> flat2_missing_flat1 = new LinkedHashSet<String>(
				flat2.keySet());
		flat1_missing_flat2.removeAll(flat2.keySet());
		flat2_missing_flat1.removeAll(flat1.keySet());
		LinkedHashSet<String> common = new LinkedHashSet<String>(flat2.keySet());
		common.retainAll(flat1.keySet());
		for (String s : common) {
			if (!evaluator.equals(flat1.get(s), flat2.get(s))) {
				diffs.put("REMOVE" + MapObjectUtils.TAB_CHAR + s, flat1.get(s));
				diffs.put("ADD" + MapObjectUtils.TAB_CHAR + s, flat2.get(s));
			}
		}
		for (String s1 : flat1_missing_flat2) {
			diffs.put("REMOVE" + MapObjectUtils.TAB_CHAR + s1, flat1.get(s1));
		}
		for (String s1 : flat2_missing_flat1) {
			diffs.put("ADD" + MapObjectUtils.TAB_CHAR + s1, flat2.get(s1));
		}
		return diffs;
	}

	public static interface TreeTraverse {
		public void process(String path, Object o);
	}

	public static void removeKeysLike(Map<String, ? extends Object> m,
			final String startswith) {
		traverseMap(m, new TreeTraverse() {

			@Override
			public void process(String path, Object o) {
				if (o instanceof Map) {
					Map<String, ? extends Object> m = (Map) o;
					List<String> keys = new ArrayList<String>();
					for (String k : m.keySet()) {
						if (k.startsWith(startswith)) {
							keys.add(k);
						}
					}
					for (String k : keys) {
						m.remove(k);
					}
				}
			}
		}, null);

	}

	public static void traverseMap(Map<String, ? extends Object> m,
			TreeTraverse tt, String psofar) {

		m.forEach((s, o) -> {
			String npath = psofar;
			if (npath.length() > 0) {
				npath += MapObjectUtils.PATH_SEPARATOR;
			}
			npath += s;
			if (o instanceof Map) {
				traverseMap((Map<String, ? extends Object>) o, tt, npath);
			} else if (o instanceof List) {
				traverseMap((List<? extends Object>) o, tt, npath);
			} else {
				tt.process(npath, o);
			}
		});
		tt.process(psofar, m);
	}
	

	public static void traverseMap(List<? extends Object> m, TreeTraverse tt,
			String psofar) {

		for (int i = 0; i < m.size(); i++) {
			String npath = psofar + MapObjectUtils.ARRAY_INDEX_SPECIFIER_LEFT
					+ i + MapObjectUtils.ARRAY_INDEX_SPECIFIER_RIGHT;
			Object o = m.get(i);
			if (o instanceof Map) {
				traverseMap((Map<String, ? extends Object>) o, tt, npath);
			} else if (o instanceof List) {
				traverseMap((List<? extends Object>) o, tt, npath);
			} else {
				tt.process(npath, o);
			}
		}
		tt.process(psofar, m);
	}
	
	public static Map ObjectToMap(Object o){
		ObjectMapper mapper = new ObjectMapper();
	    Map<String, Object> map = mapper.convertValue(o, Map.class);
	    return map;
	}

	public static String simplifyKeyPath(String pth) {
		return ARRAY_REPLACER.matcher(pth).replaceAll("");
	}
}
