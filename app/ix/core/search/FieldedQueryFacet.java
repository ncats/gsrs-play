package ix.core.search;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	private static final Pattern DYNAMIC_CODE_SYSTEM=  Pattern.compile("root_codes_(.*)");
	private static final Map<String,String> displayNames;
	
	//TODO: This is super ugly, should be somewhere nice, outside
	//of the code-base altogether, and loaded in.
	static{
		String[] SPECIAL_LOOKUP=("root_approvalID	Approval ID\n" +
			"root_approved	Record Approved\n" + 
			"root_approvedBy	Record Approved By\n" + 
			"root_codes_comments	Code Comments\n" + 
			"root_codes_createdBy	Code Created By\n" + 
			"root_codes_lastEditedBy	Code Last Edited By\n" + 
			"root_codes_code	Code Literal\n" + 
			"root_codes_codeSystem	Code System\n" + 
			"root_codes_type	Code Type\n" + 
			"root_codes_url	Code URL\n" + 
			"root_names_name	Any Name\n" + 
			"root_Display Name	Display Name\n" + 
			"root_names_languages_GInAS Language	Language Code\n" + 
			"root_mixture_components_substance_approvalID	Mixture Component ApprovalID\n" + 
			"root_mixture_components_substance_refPname	Mixture Component Name\n" + 
			"root_mixture_components_type	Mixture Component Type\n" + 
			"root_moieties_structure_stereoChemistry	Moeity Stereochemistry\n" + 
			"root_moieties_structure_createdBy	Moiety Created By\n" + 
			"root_moieties_structure_formula	Moiety Formula\n" + 
			"root_moieties_structure_atropisomerism	Moiety Has Atropisomerism\n" + 
			"root_moieties_structure_lastEditedBy	Moiety Last Edited By\n" + 
			"root_moieties_structure_properties_LyChI_L1	Moiety LyChI Level 1\n" + 
			"root_moieties_structure_properties_LyChI_L2	Moiety LyChI Level 2\n" + 
			"root_moieties_structure_properties_LyChI_L3	Moiety LyChI Level 3\n" + 
			"root_moieties_structure_properties_LyChI_L4	Moiety LyChI Level 4\n" + 
			"root_moieties_structure_opticalActivity	Moiety Optical Activity\n" + 
			"root_moieties_structure_stereoComments	Moiety Stereo Comments\n" + 
			"root_structure_formula	Mol Formua\n" + 
			"root_polymer_monomers_amount_nonNumericValue	Monomer Amount Non-Numeric Value\n" + 
			"root_polymer_monomers_amount_type	Monomer Amount Type\n" + 
			"root_polymer_monomers_amount_units	Monomer Amount Units\n" + 
			"root_polymer_monomers_monomerSubstance_approvalID	Monomer Approval ID\n" + 
			"root_polymer_monomers_defining	Monomer Defining\n" + 
			"root_polymer_monomers_monomerSubstance_refPname	Monomer Substance Name\n" + 
			"root_polymer_monomers_type	Monomer Type\n" + 
			"root_names_createdBy	Name Created By\n" + 
			"root_names_domains_GInAS Domain	Name Domain\n" + 
			"root_names_lastEditedBy	Name Last Edited By\n" + 
			"root_names_type	Name Type Code\n" + 
			"root_names_nameOrgs_nameOrg	Naming Organization\n" + 
			"root_notes_note	Note\n" + 
			"root_notes_createdBy	Note Created By\n" + 
			"root_notes_lastEditedBy	Note Last Edited By\n" + 
			"root_structurallyDiverse_organismAuthor	Organism Author\n" + 
			"root_structurallyDiverse_organismFamily	Organism Family\n" + 
			"root_structurallyDiverse_organismGenus	Organism Genus\n" + 
			"root_structurallyDiverse_organismSpecies	Organism Species\n" + 
			"root_polymer_classification_polymerClass	Polymer Class\n" + 
			"root_polymer_classification_polymerGeometry	Polymer Geometry\n" + 
			"root_polymer_classification_polymerSubclass_GInAS Subclass	Polymer Subclass\n" + 
			"root_properties_createdBy	Property Created By\n" + 
			"root_properties_lastEditedBy	Property Last Edited By\n" + 
			"root_protein_glycosylation_glycosylationType	Protein Glycosylation Type\n" + 
			"root_protein_subunits_sequence	Protein Sequence\n" + 
			"root_protein_sequenceType	Protein Sequence Type\n" + 
			"root_protein_subunits_createdBy	Protein Subunit Created By\n" + 
			"root_protein_subunits_lastEditedBy	Protein Subunit Last Edited By\n" + 
			"root_createdBy	Record Created By\n" + 
			"root_definitionLevel	Record Definition Level\n" + 
			"root_definitionType	Record Definition Type\n" + 
			"root_lastEditedBy	Record Last Edited By\n" + 
			"root_status	Record Status\n" + 
			"root_tags_GInAS Tag	Record Tag\n" + 
			"root_version	Record Version\n" + 
			"root_references_tags_GInAS Document Tag	Reference Collection\n" + 
			"root_references_createdBy	Reference Created By\n" + 
			"root_references_id	Reference ID\n" + 
			"root_references_lastEditedBy	Reference Last Edited By\n" + 
			"root_references_citation	Reference Text / Citation\n" + 
			"root_references_docType	Reference Type\n" + 
			"root_references_url	Reference URL\n" + 
			"root_relationships_relatedSubstance_approvalID	Related Substance Approval ID\n" + 
			"root_relationships_relatedSubstance_refPname	Related Substance Name\n" + 
			"root_relationships_comments	Relationship Comments\n" + 
			"root_relationships_createdBy	Relationship Created By\n" + 
			"root_relationships_interactionType	Relationship Interaction Type\n" + 
			"root_relationships_lastEditedBy	Relationship Last Edited By\n" + 
			"root_relationships_qualification	Relationship Qualification\n" + 
			"root_relationships_type	Relationship Type\n" + 
			"root_structurallyDiverse_sourceMaterialClass	Source Material Class\n" + 
			"root_structurallyDiverse_sourceMaterialType	Source Material Type\n" + 
			"root_polymer_structuralUnits_amount_nonNumericValue	SRU Amount Non-Numeric Value\n" + 
			"root_polymer_structuralUnits_amount_type	SRU Amount Type\n" + 
			"root_polymer_structuralUnits_amount_units	SRU Amount Units\n" + 
			"root_polymer_structuralUnits_label	SRU Label\n" + 
			"root_polymer_structuralUnits_type	SRU Type\n" + 
			"root_SubstanceStereochemistry	Stereochemistry Type\n" + 
			"root_protein_modifications_structuralModifications_molecularFragment_approvalID	Structural Modification Approval ID\n" + 
			"root_protein_modifications_structuralModifications_extent	Structural Modification Extent\n" + 
			"root_protein_modifications_structuralModifications_extentAmount_nonNumericValue	Structural Modification Extent Amount Non-numeric Value\n" + 
			"root_protein_modifications_structuralModifications_extentAmount_type	Structural Modification Extent Amount Type\n" + 
			"root_protein_modifications_structuralModifications_extentAmount_units	Structural Modification Extent Amount Units\n" + 
			"root_protein_modifications_structuralModifications_residueModified	Structural Modification Residue\n" + 
			"root_protein_modifications_structuralModifications_molecularFragment_refPname	Structural Modification Substance Name\n" + 
			"root_protein_modifications_structuralModifications_structuralModificationType	Structural Modification Type\n" + 
			"root_structurallyDiverse_parentSubstance_approvalID	Structurally Diverse Parent Approval ID\n" + 
			"root_structurallyDiverse_parentSubstance_refPname	Structurally Diverse Parent Substance Name\n" + 
			"root_structurallyDiverse_part_Parts	Structurally Diverse Part\n" + 
			"root_moieties_structure_properties_text	Structure Created By\n" + 
			"root_structure_createdBy	Structure Created By\n" + 
			"root_structure_atropisomerism	Structure Has Atropisomerism\n" + 
			"root_structure_lastEditedBy	Structure Last Edited By\n" + 
			"root_structure_properties_LyChI_L1	Structure LyChI Level 1\n" + 
			"root_structure_properties_LyChI_L2	Structure LyChI Level 2\n" + 
			"root_structure_properties_LyChI_L3	Structure LyChI Level 3\n" + 
			"root_structure_properties_LyChI_L4	Structure LyChI Level 4\n" + 
			"root_structure_opticalActivity	Structure Optical Activity\n" + 
			"root_structure_stereoComments	Structure Stereo Comments\n" + 
			"root_substanceClass	Substance Class").split("\n");
		displayNames=Arrays.stream(SPECIAL_LOOKUP)
					.map(a->a.split("\t"))
					.collect(Collectors.toMap(a->a[0], a->a[1]));

				
	}

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
		String fdisp=displayNames.get(field);
		if(fdisp==null){
			Matcher m=DYNAMIC_CODE_SYSTEM.matcher(field);
			if(m.find()){
				return m.group(1);
			}
		}else{
			return fdisp;
		}
		
		
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
	
	
	public MATCH_TYPE getMatchType(){
		return this.matchType;
	}




	public static String[] displayQuery(String q){


		String dispField;
		String q2;
		String[] fieldAndQuery = q.split(":");
		if(fieldAndQuery.length>1){
			String field = fieldAndQuery[0];
			field=field.replace("\\ ", " ");
			q2 = fieldAndQuery[1];
			dispField=displayNames.get(field);
			if(dispField==null){
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