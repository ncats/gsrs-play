package ix.core.search.text;

import java.util.regex.Pattern;

import org.apache.lucene.index.IndexableField;

public interface IndexableValue {
	
	public String path();
	public String name();
	public Object value();
	
	default boolean isDynamicFacet(){
		return false;
	}
	
	default boolean isDirectIndexField(){
		return false;
	}
	
	default IndexableField getDirectIndexableField(){
		return null;
	}
	
	
	default String rawName(){
		return this.name();
	}
	
	default boolean indexed(){
		return true;
	}
	default boolean sortable (){
		return false;
	}
	default boolean taxonomy (){
		return false;
	}
	default boolean facet(){
		return false;
	}
	default boolean suggest () {
		return false;
	}
	default boolean sequence(){
		return false;
	}
	default boolean structure(){
		return false;
	}
	default boolean fullText(){
		return true;
	}

	// path separator for
 	default String pathsep(){
		return "/";
	}   
 	
 	default long[] ranges(){
 		return new long[]{};
 	}
 	
 	default double[] dranges(){
 		return new double[]{};
 	}
 	
 	default String format(){
 		return "%1$.2f";
 	}
 	
 	default boolean recurse(){
 		return true;
 	}
 	
 	default boolean indexEmpty(){
 		return false;
 	}
 	
 	default String emptyString(){
 		return "<EMTPY>";
 	}
 	
 	default Pattern getPathSepPattern(){
		return Pattern.compile(this.pathsep());
	}
	
	default String[] splitPath(String path){
		return getPathSepPattern().split(path);
	}
	
	public static IndexableValue simpleFacetStringValue(String name, String value){
		
		return new IndexableValueFromRaw(name,value);
	}
	public static IndexableValue simpleFacetLongValue(String name, long value, long[] ranges){
		
		return new IndexableValueFromRaw(name,value).withRange(ranges);
	}
	public static IndexableValue simpleFacetDoubleValue(String name, double val, double[] ranges){
		
		return new IndexableValueFromRaw(name,val).withRange(ranges);
	}
}
