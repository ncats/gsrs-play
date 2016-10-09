package ix.core.search;

import java.io.Serializable;

import ix.core.FieldNameDecorator;
import ix.core.factories.FieldNameDecoratorFactory;
import ix.core.util.EntityUtils;
import play.Play;


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
	private boolean couldBeMore=false;
	
	
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
		return null;
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
	public FieldedQueryFacet withExplicitDisplayField(String df) {
		this.displayField=df;
		return this;
	}
	
	
	public MATCH_TYPE getMatchType(){
		return this.matchType;
	}

	// TODO: better parsing, and move this to its own 
	// resource
	public static String[] displayQuery(String kind, String q){
		if(q==null)return null;

		String dispField;
		String q2;
		String[] fieldAndQuery = q.split(":");
		if(fieldAndQuery.length>1){
			String field = fieldAndQuery[0];
			field=field.replace("\\ ", " ");
			q2 = fieldAndQuery[1];
			FieldNameDecorator fnd;
			try {
				fnd = FieldNameDecoratorFactory.getInstance(Play.application()).getSingleResourceFor(EntityUtils.getEntityInfoFor(kind));
				dispField=fnd.getDisplayName(field);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				dispField=field;
			}
		}else{
			dispField="Any Text";
			q2=q;
		}
		boolean exact = false;
		if(q2.startsWith("\"^") && q2.endsWith("$\"")){
			exact=true;
			q2=q2.replace("^","");
			q2=q2.replace("$","");

		}
		if(exact){
			return new String[]{dispField, q2, "(exact)"};
		}else {
			return new String[]{dispField, q2, "(contains)"};
		}


	}
}