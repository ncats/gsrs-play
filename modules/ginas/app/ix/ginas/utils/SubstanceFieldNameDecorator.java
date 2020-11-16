package ix.ginas.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ix.core.FieldNameDecorator;

public class SubstanceFieldNameDecorator implements FieldNameDecorator{
	private static final Pattern DYNAMIC_CODE_SYSTEM=  Pattern.compile("root_codes_(.*)");
	private static final Map<String,String> displayNames;
	
	//TODO: This is super ugly, should be somewhere nice, outside
	//of the code-base altogether, and loaded in.
	static{
			Map<String, String> m = new HashMap<>();
			m.put("root_approvalID" , "Approval ID");
			m.put("root_approved" , "Record Approved"); 
			m.put("root_approvedBy" , "Record Approved By"); 
			m.put("root_codes_comments" , "Code Comments"); 
			m.put("root_codes_createdBy" , "Code Created By"); 
			m.put("root_codes_lastEditedBy" , "Code Last Edited By"); 
			m.put("root_codes_code" , "Code Literal"); 
			m.put("root_codes_codeSystem" , "Code System"); 
			m.put("root_codes_type" , "Code Type"); 
			m.put("root_codes_url" , "Code URL"); 
			m.put("root_names_name" , "Any Name"); 
			m.put("root_names_stdName" , "Standardized Name");
			m.put("root_Display Name","Display Name"); 
			m.put("root_names_languages_GInAS Language","Language Code"); 
			m.put("root_mixture_components_substance_approvalID" , "Mixture Component ApprovalID"); 
			m.put("root_mixture_components_substance_refPname" , "Mixture Component Name"); 
			m.put("root_mixture_components_type" , "Mixture Component Type"); 
			m.put("root_moieties_structure_stereoChemistry" , "Moeity Stereochemistry"); 
			m.put("root_moieties_structure_createdBy" , "Moiety Created By"); 
			m.put("root_moieties_structure_formula" , "Moiety Formula"); 
			m.put("root_moieties_structure_atropisomerism" , "Moiety Has Atropisomerism"); 
			m.put("root_moieties_structure_lastEditedBy" , "Moiety Last Edited By"); 
			m.put("root_moieties_structure_properties_LyChI_L1" , "Moiety LyChI Level 1"); 
			m.put("root_moieties_structure_properties_LyChI_L2" , "Moiety LyChI Level 2"); 
			m.put("root_moieties_structure_properties_LyChI_L3" , "Moiety LyChI Level 3"); 
			m.put("root_moieties_structure_properties_LyChI_L4" , "Moiety LyChI Level 4"); 
			m.put("root_moieties_structure_opticalActivity" , "Moiety Optical Activity"); 
			m.put("root_moieties_structure_stereoComments" , "Moiety Stereo Comments"); 
			m.put("root_structure_formula" , "Mol Formua"); 
			m.put("root_polymer_monomers_amount_nonNumericValue" , "Monomer Amount Non-Numeric Value"); 
			m.put("root_polymer_monomers_amount_type" , "Monomer Amount Type"); 
			m.put("root_polymer_monomers_amount_units" , "Monomer Amount Units"); 
			m.put("root_polymer_monomers_monomerSubstance_approvalID" , "Monomer Approval ID"); 
			m.put("root_polymer_monomers_defining" , "Monomer Defining"); 
			m.put("root_polymer_monomers_monomerSubstance_refPname" , "Monomer Substance Name"); 
			m.put("root_polymer_monomers_type" , "Monomer Type"); 
			m.put("root_names_createdBy" , "Name Created By"); 
			m.put("root_names_domains_GInAS Domain","Name Domain"); 
			m.put("root_names_lastEditedBy" , "Name Last Edited By"); 
			m.put("root_names_type" , "Name Type Code"); 
			m.put("root_names_nameOrgs_nameOrg" , "Naming Organization"); 
			m.put("root_notes_note" , "Note"); 
			m.put("root_notes_createdBy" , "Note Created By"); 
			m.put("root_notes_lastEditedBy" , "Note Last Edited By"); 
			m.put("root_structurallyDiverse_organismAuthor" , "Organism Author"); 
			m.put("root_structurallyDiverse_organismFamily" , "Organism Family"); 
			m.put("root_structurallyDiverse_organismGenus" , "Organism Genus"); 
			m.put("root_structurallyDiverse_organismSpecies" , "Organism Species"); 
			m.put("root_polymer_classification_polymerClass" , "Polymer Class"); 
			m.put("root_polymer_classification_polymerGeometry" , "Polymer Geometry"); 
			m.put("root_polymer_classification_polymerSubclass_GInAS Subclass","Polymer Subclass"); 
			m.put("root_properties_createdBy" , "Property Created By"); 
			m.put("root_properties_lastEditedBy" , "Property Last Edited By"); 
			m.put("root_protein_glycosylation_glycosylationType" , "Protein Glycosylation Type"); 
			m.put("root_protein_subunits_sequence" , "Protein Sequence"); 
			m.put("root_protein_sequenceType" , "Protein Sequence Type"); 
			m.put("root_protein_subunits_createdBy" , "Protein Subunit Created By"); 
			m.put("root_protein_subunits_lastEditedBy" , "Protein Subunit Last Edited By"); 
			m.put("root_createdBy" , "Record Created By"); 
			m.put("root_definitionLevel" , "Record Definition Level"); 
			m.put("root_definitionType" , "Record Definition Type"); 
			m.put("root_lastEditedBy" , "Record Last Edited By"); 
			m.put("root_status" , "Record Status"); 
			m.put("root_tags_GInAS Tag","Record Tag"); 
			m.put("root_version" , "Record Version"); 
			m.put("root_references_tags_GInAS Document Tag" , "Reference Collection"); 
			m.put("root_references_createdBy" , "Reference Created By"); 
			m.put("root_references_id" , "Reference ID"); 
			m.put("root_references_lastEditedBy" , "Reference Last Edited By"); 
			m.put("root_references_citation" , "Reference Text / Citation"); 
			m.put("root_references_docType" , "Reference Type"); 
			m.put("root_references_url" , "Reference URL"); 
			m.put("root_relationships_relatedSubstance_approvalID" , "Related Substance Approval ID"); 
			m.put("root_relationships_relatedSubstance_refPname" , "Related Substance Name"); 
			m.put("root_relationships_comments" , "Relationship Comments"); 
			m.put("root_relationships_createdBy" , "Relationship Created By"); 
			m.put("root_relationships_interactionType" , "Relationship Interaction Type"); 
			m.put("root_relationships_lastEditedBy" , "Relationship Last Edited By"); 
			m.put("root_relationships_qualification" , "Relationship Qualification"); 
			m.put("root_relationships_type" , "Relationship Type"); 
			m.put("root_structurallyDiverse_sourceMaterialClass" , "Source Material Class"); 
			m.put("root_structurallyDiverse_sourceMaterialType" , "Source Material Type"); 
			m.put("root_polymer_structuralUnits_amount_nonNumericValue" , "SRU Amount Non-Numeric Value"); 
			m.put("root_polymer_structuralUnits_amount_type" , "SRU Amount Type"); 
			m.put("root_polymer_structuralUnits_amount_units" , "SRU Amount Units"); 
			m.put("root_polymer_structuralUnits_label" , "SRU Label"); 
			m.put("root_polymer_structuralUnits_type" , "SRU Type"); 
			m.put("root_SubstanceStereochemistry" , "Stereochemistry Type"); 
			m.put("root_protein_modifications_structuralModifications_molecularFragment_approvalID" , "Structural Modification Approval ID"); 
			m.put("root_protein_modifications_structuralModifications_extent" , "Structural Modification Extent"); 
			m.put("root_protein_modifications_structuralModifications_extentAmount_nonNumericValue" , "Structural Modification Extent Amount Non-numeric Value"); 
			m.put("root_protein_modifications_structuralModifications_extentAmount_type" , "Structural Modification Extent Amount Type"); 
			m.put("root_protein_modifications_structuralModifications_extentAmount_units" , "Structural Modification Extent Amount Units"); 
			m.put("root_protein_modifications_structuralModifications_residueModified" , "Structural Modification Residue"); 
			m.put("root_protein_modifications_structuralModifications_molecularFragment_refPname" , "Structural Modification Substance Name"); 
			m.put("root_protein_modifications_structuralModifications_structuralModificationType" , "Structural Modification Type");
			
			
			m.put("root_structurallyDiverse_parentSubstance_approvalID" , "St. Div. Parent Approval ID"); 
			m.put("root_structurallyDiverse_parentSubstance_refPname" , "St. Div. Parent Substance Name"); 
			
			m.put("root_structurallyDiverse_hybridSpeciesPaternalOrganism_approvalID" , "St. Div. Hybrid Parent (p) Approval ID"); 
			m.put("root_structurallyDiverse_hybridSpeciesPaternalOrganism_refPname" , "St. Div. Hybrid Parent (p) Substance Name"); 
			
			m.put("root_structurallyDiverse_hybridSpeciesMaternalOrganism_approvalID" , "St. Div. Hybrid Parent (m) Approval ID"); 
			m.put("root_structurallyDiverse_hybridSpeciesMaternalOrganism_refPname" , "St. Div. Hybrid Parent (m) Substance Name"); 
			
			
			m.put("root_structurallyDiverse_part_Parts" , "Structurally Diverse Part"); 
			m.put("root_moieties_structure_properties_text" , "Structure Created By"); 
			m.put("root_structure_createdBy" , "Structure Created By"); 
			m.put("root_structure_atropisomerism" , "Structure Has Atropisomerism"); 
			m.put("root_structure_lastEditedBy" , "Structure Last Edited By"); 
			m.put("root_structure_properties_LyChI_L1" , "Structure LyChI Level 1"); 
			m.put("root_structure_properties_LyChI_L2" , "Structure LyChI Level 2"); 
			m.put("root_structure_properties_LyChI_L3" , "Structure LyChI Level 3"); 
			m.put("root_structure_properties_LyChI_L4" , "Structure LyChI Level 4"); 
			m.put("root_structure_opticalActivity" , "Structure Optical Activity"); 
			m.put("root_structure_stereoComments" , "Structure Stereo Comments"); 
			m.put("root_substanceClass","Substance Class");
			displayNames=m;
	}

	@Override
	public String getDisplayName(String field) {
		String fdisp=displayNames.get(field);
		if(fdisp==null){
			Matcher m=DYNAMIC_CODE_SYSTEM.matcher(field);
			if(m.find()){
				return m.group(1);
			}
		}
		return fdisp;
	}

}
