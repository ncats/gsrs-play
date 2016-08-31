package ix.core.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ix.core.search.text.TextIndexer;

public class FieldFacet implements Serializable{
	private int count;
	public String field;
	public String queryTerm;
	
	private String explicitLucenQuery=null;
	
	public String displayField = null;
	public MATCH_TYPE matchType=MATCH_TYPE.NO_MATCH;
	
	private boolean couldBeMore=true;
	
	static final Map<String,String> displayNameForField = new HashMap<String,String>();
	
	//TODO: Move to configuration area, not to be hard-coded here
	static{
		displayNameForField.put("root_root_references_citation", "Reference text or citation");
		displayNameForField.put("root_references_docType", "Reference type");
		displayNameForField.put("root_relationships_relatedSubstance_refPname", "Related substance name");
		displayNameForField.put("root_relationships_relatedSubstance_approvalID", "Related Substance Approval ID");
		displayNameForField.put("root_approvalID", "Approval ID");
		displayNameForField.put("root_codes_code", "Code");
		displayNameForField.put("root_codes_codeSystem", "Code system");
		displayNameForField.put("root_codes_comments", "Code text or Code comments");
		displayNameForField.put("root_notes_note", "Notes");
		displayNameForField.put("root_names_name", "Any name");
		displayNameForField.put("root_relationships_qualification", "Relationship qualification");
		displayNameForField.put("root_relationships_comments", "Relationship comments");
		displayNameForField.put("root_relationships_interactionType", "Relationship interaction type");
	}

	
	
	
	public FieldFacet(String field, String q) {
		queryTerm = q;
		this.field = field;
	}
	public FieldFacet(String field, String q, MATCH_TYPE mt) {
		queryTerm = q;
		this.field = field;
		this.matchType=mt;
	}
	
	public FieldFacet(String field, String q, int count, MATCH_TYPE mt) {
		queryTerm = q;
		this.field = field;
		this.matchType=mt;
		this.count=count;
		this.couldBeMore=false;
	}

	public String getDisplayField() {
		if(displayField!=null)return displayField;
		displayField= getDisplayField(field);
		return displayField;
	}
	
	public String getValue(){
		return queryTerm;
	}
	
	
	private static String getDisplayField(String field){
		String disp=displayNameForField.get(field);
		
		if(disp!=null){
			return disp;
		}
		String[] fs = field.replace(" ", "_").split("_");
		if(fs.length>=2){
			String ff=(fs[fs.length - 2] + " " + fs[fs.length - 1]).toLowerCase();
			ff = ff.substring(0, 1).toUpperCase() + ff.substring(1);
			return ff;
		}
		return field;
	}
	
	public String getIndexedField() {
		return field.replace(".", "_");
	}
	
	public String toLuceneQuery(){
		if(explicitLucenQuery!=null)return explicitLucenQuery;
		if(matchType==MATCH_TYPE.WORD)
			return getIndexedField() + ":\"" + queryTerm + "\"";
		else if(matchType==MATCH_TYPE.WORD_STARTS_WITH)
			return getIndexedField() + ":\"" + queryTerm + "*\"";
		else if(matchType==MATCH_TYPE.FULL)
			return getIndexedField() + ":\"" + TextIndexer.GIVEN_START_WORD + queryTerm + TextIndexer.GIVEN_STOP_WORD + "\"";
		return getIndexedField() + ":\"" + queryTerm + "\"";
	}
	
	public enum MATCH_TYPE{
		FULL,
		WORD,
		WORD_STARTS_WITH,
		CONTAINS,
		NO_MATCH
	};
	
	public void increment(){
		this.count++;
	}
	
	public int getCount(){
		return this.count;
	}
	
	public String getCountText(){
		if(this.couldBeMore){
			return this.getCount() + "+";
		}
		return this.getCount() +"";
	}
	
	public void markDone(){
		this.couldBeMore=false;
	}
	
	
	public FieldFacet explicitQuery(String q){
		this.explicitLucenQuery=q;
		return this;
	}
	
	
}