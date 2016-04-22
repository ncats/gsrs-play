package ix.core.search;

public class FieldFacet {
	public int count;
	public String field;
	public String displayField;
	public String query;
	public MATCH_TYPE matchType=MATCH_TYPE.NO_MATCH;

	public FieldFacet(String field, String q) {
		query = q;
		this.field = field;
	}
	public FieldFacet(String field, String q, MATCH_TYPE mt) {
		query = q;
		this.field = field;
		this.matchType=mt;
	}

	public String getDisplayField() {
		return getDisplayField(field);
	}
	public static String getDisplayField(String field){
		String[] fs = field.split("\\.");
		if(fs.length>=2)
		return fs[fs.length - 2] + "." + fs[fs.length - 1];
		return field;
	}
	
	public String getIndexedField() {
		return field.replace(".", "_");
//		String[] fs = field.split("\\.");
//		return fs[fs.length - 1];
	}
	public String getLuceneQuery(){
		if(matchType==MATCH_TYPE.WORD)
			return getIndexedField() + ":\"" + query + "\"";
		else if(matchType==MATCH_TYPE.WORD_STARTS_WITH)
			return getIndexedField() + ":\"" + query + "*\"";
		else if(matchType==MATCH_TYPE.FULL)
			return getIndexedField() + ":\"" + TextIndexer.GIVEN_START_WORD + query + TextIndexer.GIVEN_STOP_WORD + "\"";
		return getIndexedField() + ":\"" + query + "\"";
	}
	
	public static enum MATCH_TYPE{
		FULL,
		WORD,
		WORD_STARTS_WITH,
		CONTAINS,
		NO_MATCH
	};
}