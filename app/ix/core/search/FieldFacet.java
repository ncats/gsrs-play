package ix.core.search;

public class FieldFacet {
	public int count;
	public String field;
	public String displayField;
	public String query;

	public FieldFacet(String field, String q) {
		query = q;
		this.field = field;
	}

	public String getDisplayField() {
		String[] fs = field.split("\\.");
		return fs[fs.length - 2] + "." + fs[fs.length - 1];
	}
	public String getIndexedField() {
		String[] fs = field.split("\\.");
		return fs[fs.length - 1];
	}
	public String getLuceneQuery(){
		return getIndexedField() + ":\"" + query + "\"";
	}
}