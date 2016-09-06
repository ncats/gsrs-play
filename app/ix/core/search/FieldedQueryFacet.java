package ix.core.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ix.core.search.text.TextIndexer;


/**
 * FieldedQueryFacets are for storing information about what a query would
 * return if it were restricted to a particular field.
 * 
 * 
 * @author peryeata
 *
 */
public class FieldedQueryFacet implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//These match types are relics of the past
	//but are still useful here. Many of these
	//aren't exactly well-defined. It would
	//be good to define them, so we can use them 
	//elsewhere
	public enum MATCH_TYPE{
		FULL,				
		WORD,				
		WORD_STARTS_WITH,  
		CONTAINS,
		NO_MATCH
	};
	
	private MATCH_TYPE matchType=MATCH_TYPE.NO_MATCH;
	
	
	private String field;	   //field this restricts to (actual?)
	
	private int count;         //count of the query
	
	private String value;      //the value that was searched on

	private String displayField = null;
	
	private String explicitLucenQuery=null;
	private boolean couldBeMore=true;
	
	public FieldedQueryFacet(String field){
		this.field=field;
	}
	
	public String getDisplayField() {
		if(displayField!=null)return displayField;
		displayField= getDisplayField(field);
		return displayField;
	}
	
	public String getValue(){
		return value;
	}
	
	
	private static String getDisplayField(String field){
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
		return explicitLucenQuery;
	}
	
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
	
	
	/**
	 * Set the provided String to be the explicit query to
	 * use. This mutates the current object, it doesn't
	 * create a new FieldedQueryFacet
	 * @param q
	 * @return
	 */
	public FieldedQueryFacet withExplicitQuery(String q){
		this.explicitLucenQuery=q;
		return this;
	}

	public FieldedQueryFacet withExplicitCount(int intValue) {
		this.count=intValue;
		return this;
	}
	
	public FieldedQueryFacet withExplicitMatchType(MATCH_TYPE mt) {
		this.matchType=mt;
		return this;
	}
	
	
	public MATCH_TYPE getMatchType(){
		return this.matchType;
	}
}