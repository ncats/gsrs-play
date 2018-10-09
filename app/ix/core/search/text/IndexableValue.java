package ix.core.search.text;

import java.util.regex.Pattern;

import org.apache.lucene.index.IndexableField;

public interface IndexableValue {
	
	String path();
	String name();
	Object value();


	default boolean useFullPath(){ return false; }

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

	default int suggestWeight() {
		return 1;
	}

	
	default IndexableValue suggestable(){
		IndexableValue me = this;
		return new IndexableValue(){

			public String path() {
				return me.path();
			}

			public String name() {
				return me.name();
			}

			public Object value() {
				return me.value();
			}

			public  boolean useFullPath() {
				return me.useFullPath();
			}

			public  boolean isDynamicFacet() {
				return me.isDynamicFacet();
			}

			public  boolean isDirectIndexField() {
				return me.isDirectIndexField();
			}

			public  IndexableField getDirectIndexableField() {
				return me.getDirectIndexableField();
			}

			public  String rawName() {
				return me.rawName();
			}

			public  boolean indexed() {
				return me.indexed();
			}

			public  boolean sortable() {
				return me.sortable();
			}

			public  boolean taxonomy() {
				return me.taxonomy();
			}

			public  boolean facet() {
				return me.facet();
			}

			public  boolean suggest() {
				return true;
			}

			public  boolean sequence() {
				return me.sequence();
			}

			public  boolean structure() {
				return me.structure();
			}

			public  boolean fullText() {
				return me.fullText();
			}

			public  String pathsep() {
				return me.pathsep();
			}

			public  long[] ranges() {
				return me.ranges();
			}

			public  double[] dranges() {
				return me.dranges();
			}

			public  String format() {
				return me.format();
			}

			public  boolean recurse() {
				return me.recurse();
			}

			public  boolean indexEmpty() {
				return me.indexEmpty();
			}

			public  String emptyString() {
				return me.emptyString();
			}

			public  Pattern getPathSepPattern() {
				return me.getPathSepPattern();
			}

			public  String[] splitPath(String path) {
				return me.splitPath(path);
			}

		};
	}
	
	public static IndexableValue simpleFacetStringValue(String name, String value){
		
		return new IndexableValueFromRaw(name,value).dynamic();
	}
	
	public static IndexableValue simpleStringValue(String name, String value){
		return new IndexableValueFromRaw(name,value);
	}
	public static IndexableValue simpleFacetLongValue(String name, long value, long[] ranges){
		
		return new IndexableValueFromRaw(name,value).setFacet(true).withRange(ranges);
	}
	public static IndexableValue simpleFacetDoubleValue(String name, double val, double[] ranges){
		
		return new IndexableValueFromRaw(name,val).setFacet(true).withRange(ranges);
	}
}
