package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Principal;
import ix.core.models.ProcessingJob;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.ginas.models.GinasCommonData;
import ix.ginas.models.KeywordListSerializer;
import ix.ginas.models.PrincipalDeserializer;
import ix.ginas.models.PrincipalSerializer;
import ix.ginas.models.TagListDeserializer;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Global;
import play.Logger;

@JSONEntity(name = "substance", title = "Substance")
@Entity
@Table(name = "ix_ginas_substance")
@Inheritance
@DiscriminatorValue("SUB")
public class Substance extends GinasCommonData {
	private static final String ALTERNATE_SUBSTANCE_REL = "SUB_ALTERNATE->SUBSTANCE";
	private static final String PRIMARY_SUBSTANCE_REL = "SUBSTANCE->SUB_ALTERNATE";

	private static final String DEFAULT_NO_NAME = "NO_NAME";

	private static final String DOC_TYPE_BATCH_IMPORT = "BATCH IMPORT";

	public static final boolean REMOVE_INVALID_RELATIONSHIPS = false;

	/**
	 * sigh.. can we be at least case-consistent?
	 */
	public enum SubstanceClass {
		chemical, 
		protein, 
		nucleicAcid, 
		polymer, 
		structurallyDiverse, 
		mixture, 
		specifiedSubstanceG1, 
		specifiedSubstanceG2, 
		specifiedSubstanceG3, 
		specifiedSubstanceG4, 
		unspecifiedSubstance, 
		concept, 
		reference
	}
	public enum SubstanceDefinitionType{
		PRIMARY,
		ALTERNATIVE
	}
	
	public SubstanceDefinitionType definitionType = SubstanceDefinitionType.PRIMARY;
	
	
	@JSONEntity(title = "Substance Type", values = "JSONConstants.ENUM_SUBSTANCETYPES", isRequired = true)
	@Indexable(suggest = true, facet = true, name = "Substance Class")
	@Column(name = "class")
	public SubstanceClass substanceClass;

	@Indexable(suggest = true, facet = true, name = "Record Status")
	public String status = "PENDING";
	public String version = "1";
	

	@OneToOne(cascade = CascadeType.ALL)
	@JsonSerialize(using = PrincipalSerializer.class)
	@JsonDeserialize(using = PrincipalDeserializer.class)
	@Indexable(facet = true, name = "Approved By")
	public Principal approvedBy;

	@Indexable(facet = true, name = "Approved Date")
	@JsonDeserialize(using = DateDeserializer.class)
	public Date approved;

	// @ManyToMany(cascade=CascadeType.ALL)
	// @JoinTable(name="ix_ginas_substance_access")
	// @JsonSerialize(using = PrincipalListSerializer.class)
	// @JsonDeserialize(using = PrincipalListDeserializer.class)
	// public List<Principal> access = new ArrayList<Principal>();

	@JSONEntity(title = "Names", minItems = 1, isRequired = true)
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_ginas_substance_name")
	@JsonView(BeanViews.Full.class)
	public List<Name> names = new ArrayList<Name>();

	// TOOD original schema has superfluous name = codes in the schema here and
	// in all of Code's properties
	@JSONEntity(title = "Codes")
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_ginas_substance_code")
	@JsonView(BeanViews.Full.class)
	public List<Code> codes = new ArrayList<Code>();

	@OneToOne(cascade = CascadeType.ALL)
	public Modifications modifications;

	@JSONEntity(title = "Notes")
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_ginas_substance_note")
	@JsonView(BeanViews.Full.class)
	public List<Note> notes = new ArrayList<Note>();

	@JSONEntity(title = "Properties")
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_ginas_substance_prop")
	@JsonView(BeanViews.Full.class)
	public List<Property> properties = new ArrayList<Property>();

	@JSONEntity(title = "Relationships")
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_ginas_substance_rel")
	@JsonView(BeanViews.Full.class)
	public List<Relationship> relationships = new ArrayList<Relationship>();

	@JSONEntity(title = "References", minItems = 1, isRequired = true)
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_ginas_substance_ref")
	@JsonView(BeanViews.Full.class)
	public List<Reference> references = new ArrayList<Reference>();

	@JSONEntity(title = "Approval ID", isReadOnly = true)
	@Column(length = 10)
	@Indexable(facet = true, suggest = true, name = "Approval ID")
	public String approvalID;

	// TODO in original schema, this field is missing its items: String
	@JSONEntity(title = "Tags", format = "table", isUniqueItems = true)
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ix_ginas_substance_tag")
	@JsonSerialize(using = KeywordListSerializer.class)
	@JsonDeserialize(using = TagListDeserializer.class)
	public List<Keyword> tags = new ArrayList<Keyword>();

	@Transient
	protected transient ObjectMapper mapper = new ObjectMapper();

	public Substance() {
		this(SubstanceClass.concept);
	}

	public Substance(SubstanceClass subcls) {
		substanceClass = subcls;
	}

	@JsonView(BeanViews.Compact.class)
	@JsonProperty("_names")
	public JsonNode getJsonNames() {
		JsonNode node = null;
		if (!names.isEmpty()) {
			try {
				ObjectNode n = mapper.createObjectNode();
				n.put("count", names.size());
				n.put("href", Global.getRef(getClass(), uuid) + "/names");
				node = n;
			} catch (Exception ex) {
				ex.printStackTrace();
				// this means that the class doesn't have the NamedResource
				// annotation, so we can't resolve the context
				node = mapper.valueToTree(names);
			}
		}
		return node;
	}

	@JsonView(BeanViews.Compact.class)
	@JsonProperty("_references")
	public JsonNode getJsonReferences() {
		JsonNode node = null;
		if (!references.isEmpty()) {
			try {
				ObjectNode n = mapper.createObjectNode();
				n.put("count", references.size());
				n.put("href", Global.getRef(getClass(), uuid) + "/references");
				node = n;
			} catch (Exception ex) {
				// this means that the class doesn't have the NamedResource
				// annotation, so we can't resolve the context
				node = mapper.valueToTree(references);
			}
		}
		return node;
	}

	@JsonView(BeanViews.Compact.class)
	@JsonProperty("_codes")
	public JsonNode getJsonCodes() {
		JsonNode node = null;
		if (!codes.isEmpty()) {
			try {
				ObjectNode n = mapper.createObjectNode();
				n.put("count", codes.size());
				n.put("href", Global.getRef(getClass(), uuid) + "/codes");
				node = n;
			} catch (Exception ex) {
				// this means that the class doesn't have the NamedResource
				// annotation, so we can't resolve the context
				node = mapper.valueToTree(codes);
			}
		}
		return node;
	}

	@JsonView(BeanViews.Compact.class)
	@JsonProperty("_relationships")
	public JsonNode getJsonRelationships() {
		JsonNode node = null;
		if (!relationships.isEmpty()) {
			try {
				ObjectNode n = mapper.createObjectNode();
				n.put("count", relationships.size());
				n.put("href", Global.getRef(getClass(), uuid)
						+ "/relationships");
				node = n;
			} catch (Exception ex) {
				// this means that the class doesn't have the NamedResource
				// annotation, so we can't resolve the context
				node = mapper.valueToTree(relationships);
			}
		}
		return node;
	}

	@JsonView(BeanViews.Compact.class)
	@JsonProperty("_properties")
	public JsonNode getJsonProperties() {
		JsonNode node = null;
		if (!properties.isEmpty()) {
			try {
				ObjectNode n = mapper.createObjectNode();
				n.put("count", properties.size());
				n.put("href", Global.getRef(getClass(), uuid) + "/properties");
				node = n;
			} catch (Exception ex) {
				// this means that the class doesn't have the NamedResource
				// annotation, so we can't resolve the context
				node = mapper.valueToTree(properties);
			}
		}
		return node;
	}

	@Column(length = 1024)
	@Indexable(suggest = true, facet = true, name = "Name")
	@JsonProperty("_name")
	public String getName() {
		for (Name n : names) {
			if (n.preferred) {
				return n.name;
			}
		}
		if(names!=null && names.size()>0){
			return names.get(0).name;	
		}
		return Substance.DEFAULT_NO_NAME;
	}

	@JsonIgnore
	public List<Name> getOfficialNames() {
		List<Name> officialNames = new ArrayList<Name>();
		for (Name n : this.names) {
			if (n.type.equals("of")) {
				officialNames.add(n);
			}
		}
		return Name.sortNames(officialNames);
	}

	@JsonIgnore
	public List<Name> getNonOfficialNames() {
		List<Name> nonOfficialNames = new ArrayList<Name>();
		for (Name n : this.names) {
			if (!n.type.equals("of")) {
				nonOfficialNames.add(n);
			}
		}
		return Name.sortNames(nonOfficialNames);
	}

	@JsonIgnore
	public List<Name> getAllNames() {
		return names;
	}

	@PrePersist
	@PreUpdate
	public void tidy() {
		// preform any validation prior to persistence
		List<Relationship> remove = new ArrayList<Relationship>();
		for (Relationship rel : relationships) {
			SubstanceReference ref = rel.relatedSubstance;
			if (ref != null && ref.refuuid == null) {
				// remove this relationship
				remove.add(rel);
			}
		}

		if (!remove.isEmpty()) {
			if (REMOVE_INVALID_RELATIONSHIPS)
				for (Relationship rel : remove)
					relationships.remove(rel);
			Logger.warn("Substance " + approvalID + " has " + remove.size()
					+ " invalid relationship(s)!");
		}
	}

	@JsonIgnore
	public boolean isNonSubstanceConcept() {
		if (this.substanceClass.toString().equals("concept")) {
			return !isSubstanceVariant();
		}
		return false;
	}

	@JsonIgnore
	public boolean isSubstanceVariant() {
		if (this.substanceClass.toString().equals("concept")) {
			for (Relationship r : relationships) {
				if (r.type.equals("SUBSTANCE->SUB_CONCEPT")) {
					return true;
				}
			}
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isPrimaryDefinition() {
		return this.definitionType==SubstanceDefinitionType.PRIMARY;
	}
	@JsonIgnore
	public boolean isAlternativeDefinition() {
		return this.definitionType==SubstanceDefinitionType.ALTERNATIVE;
	}

	/**
	 * Returns parent substance concept record for substance variant concepts.
	 * 
	 * 
	 * @return
	 */
	@JsonIgnore
	public SubstanceReference getParentSubstanceReference() {
		for (Relationship r : relationships) {
			if (r.type.equals("SUBSTANCE->SUB_CONCEPT")) {
				return r.relatedSubstance;
			}
		}
		return null;
	}

	
	public String getApprovalID() {
		if (approvalID != null)
			return approvalID;
		SubstanceReference subRef = getParentSubstanceReference();
		if (subRef != null) {
			return subRef.approvalID;
		}
		return null;
	}
	
	@JsonIgnore
	public boolean isApproved(){
		return this.status.equalsIgnoreCase("Approved");
	}

	@JsonProperty("_approvalIDDisplay")
	public String getApprovalIDDisplay() {
		if (approvalID != null)
			return approvalID;
		SubstanceReference subRef = getParentSubstanceReference();
		if (subRef != null) {
			return subRef.approvalID;
		}
		if(!isApproved()){
			return this.status + " record";
		}
		return "NO APPROVAL ID";
	}

	@JsonIgnore
	public List<SubstanceReference> getChildConceptReferences() {
		List<SubstanceReference> subConcepts = new ArrayList<SubstanceReference>();
		for (Relationship r : relationships) {
			if (r.type.equals("SUB_CONCEPT->SUBSTANCE")) {
				subConcepts.add(r.relatedSubstance);
			}
		}
		return subConcepts;
	}
	
	@JsonIgnore
	public List<SubstanceReference> getAlternativeDefinitionReferences() {
		List<SubstanceReference> subConcepts = new ArrayList<SubstanceReference>();
		for (Relationship r : relationships) {
			if (r.type.equals(ALTERNATE_SUBSTANCE_REL)) {
				subConcepts.add(r.relatedSubstance);
			}
		}
		return subConcepts;
	}
	@JsonIgnore
	public List<Relationship> getAlternativeDefinitionRelationships() {
		List<Relationship> subConcepts = new ArrayList<Relationship>();
		for (Relationship r : relationships) {
			if (r.type.equals(ALTERNATE_SUBSTANCE_REL)) {
				subConcepts.add(r);
			}
		}
		return subConcepts;
	}
	
	@JsonIgnore
	public SubstanceReference getPrimaryDefinitionReference() {
		for (Relationship r : relationships) {
			if (r.type.equals(PRIMARY_SUBSTANCE_REL)) {
				return r.relatedSubstance;
			}
		}
		return null;
	}
	
	public SubstanceReference asSubstanceReference(){
		SubstanceReference subref=new SubstanceReference();
		subref.refPname=this.getName();
		subref.refuuid=this.getOrGenerateUUID().toString();
		subref.approvalID=this.approvalID;
		return subref;
	}
	
	@JsonIgnore
	public boolean addAlternativeSubstanceDefinitionRelationship(Substance sub) {
		
		for(Relationship sref:getAlternativeDefinitionRelationships()){
			if(sref.relatedSubstance.refuuid.equals(sub.uuid.toString())){
				return true;
			}
		}
		
		Relationship r = new Relationship();
		r.relatedSubstance=sub.asSubstanceReference();
		r.type=ALTERNATE_SUBSTANCE_REL;
		r.addReference(Reference.SYSTEM_GENERATED(),this);
		
		return false;
	}

	@JsonIgnore
	public boolean hasChildConceptReferences() {
		if (getChildConceptReferences().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@JsonIgnore
	public boolean hasModifications() {
		if (this.modifications != null) {
			if (this.modifications.agentModifications.size() > 0
					|| this.modifications.physicalModifications.size() > 0
					|| this.modifications.structuralModifications.size() > 0) {
				return true;
			}
		}
		return false;

	}

	@JsonIgnore
	public int getModificationCount() {
		int ret = 0;
		if (this.modifications != null) {
			ret += this.modifications.agentModifications.size();
			ret += this.modifications.physicalModifications.size();
			ret += this.modifications.structuralModifications.size();
		}
		return ret;
	}

	
	public Modifications getModifications() {
		return modifications;
	}

	public Reference getReferenceByUUID(String uuid) {
		for (Reference r : this.references) {
			if (r.uuid.toString().equals(uuid))
				return r;
		}
		return null;
	}

	public void addImportReference(ProcessingJob p) {
		Reference r = new Reference();
		r.docType = Substance.DOC_TYPE_BATCH_IMPORT;
		r.citation = p.payload.name;
		r.documentDate = new Date();
		//r.tags.add(new Keyword(p.getClass().getName(), p.id + ""));
		r.tags.add(new Keyword(GinasRecordProcessorPlugin.class.getName(), 
				p.getKeyMatching(GinasRecordProcessorPlugin.class.getName())
		));
		this.references.add(r);
	}
	public Note addPropertyNote(String note, String property){
		Note n = new Note();
		n.note=note;
		Reference r = new Reference();
		r.docType = "PROPERTY_IMPORT";
		r.citation = property;
		r.documentDate = new Date();
		n.addReference(r);
		this.references.add(r);
		this.notes.add(n);
		return n;
	}
	public Note addNote(String note) {
		Note n = new Note();
		n.note=note;
		return n;
	}
	

	@JsonIgnore
	public String getDisplayStatus(){
		if("approved".equalsIgnoreCase(status)){
			return "Validated (UNII)";
		}
		return status;
	}
	
	@JsonIgnore
	 public String getLinkingID(){
	        if(approvalID!=null){
	                return approvalID;
	        }
	        if(uuid!=null){
	                return uuid.toString().split("-")[0];
	        }
	        return getName();
	}

	@PreUpdate
	public void updateVersion(){
		Integer i=0;
		try{
			i = Integer.parseInt(this.version);
		}catch(Exception e){
			
		}
		i++;
		this.version=i+"";
	}
	
	
	
	public static Class<?>[] getAllClasses() {
		return new Class<?>[]{
			Substance.class,
			ChemicalSubstance.class,
			ProteinSubstance.class,
			NucleicAcidSubstance.class,
			StructurallyDiverseSubstance.class,
			MixtureSubstance.class,
			PolymerSubstance.class,
			SpecifiedSubstanceGroup1Substance.class
		};
		
	}
	
}
