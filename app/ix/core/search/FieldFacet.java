package ix.core.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ix.core.search.text.TextIndexer;

public class FieldFacet implements Serializable{
	private int count;
	public String field;
	public String queryTerm;
	
	public String displayField = null;
	public MATCH_TYPE matchType=MATCH_TYPE.NO_MATCH;
	
	private boolean couldBeMore=true;
	
	static final Map<String,String> displayNameForField = new HashMap<String,String>();
	
	//TODO: Move to configuration area, not to be hard-coded here
	static{
		displayNameForField.put("references.citation", "Reference text or citation");
		displayNameForField.put("references.docType", "Reference type");
		displayNameForField.put("relationships.relatedSubstance.refPname", "Related substance name");
		displayNameForField.put("relationships.relatedSubstance.approvalID", "Related Substance Approval ID");
		displayNameForField.put("approvalID", "Approval ID");
		displayNameForField.put("codes.code", "Code");
		displayNameForField.put("codes.codeSystem", "Code system");
		displayNameForField.put("codes.comments", "Code text or Code comments");
		displayNameForField.put("notes.note", "Notes");
		displayNameForField.put("names.name", "Any name");
		displayNameForField.put("relationships.qualification", "Relationship qualification");
		displayNameForField.put("relationships.comments", "Relationship comments");
		displayNameForField.put("relationships.interactionType", "Relationship interaction type");
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

	public String getDisplayField() {
		if(displayField!=null)return displayField;
		displayField= getDisplayField(field);
		return displayField;
	}
	
	
	private static String getDisplayField(String field){
		String disp=displayNameForField.get(field);
		if(disp!=null){
			return disp;
		}
		String[] fs = field.split("\\.");
		System.out.println(field);
		if(fs.length>=2){
			return fs[fs.length - 2] + "." + fs[fs.length - 1];
		}
		return field;
	}
	
	public String getIndexedField() {
		return field.replace(".", "_");
	}
	
	public String toLuceneQuery(){
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
	
	
	
}