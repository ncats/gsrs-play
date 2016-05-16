package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

import ix.core.models.Backup;
import ix.core.models.BeanViews;
import ix.core.models.DataVersion;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Principal;
import ix.core.models.ProcessingJob;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.util.TimeUtil;
import ix.ginas.models.EmbeddedKeywordList;
import ix.ginas.models.GinasCommonData;
import ix.ginas.models.serialization.DateDeserializer;
import ix.ginas.models.serialization.KeywordDeserializer;
import ix.ginas.models.serialization.KeywordListSerializer;
import ix.ginas.models.serialization.PrincipalDeserializer;
import ix.ginas.models.serialization.PrincipalSerializer;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Global;
import play.Logger;

@Backup
@JSONEntity(name = "substance", title = "Substance")
@Entity
@Table(name = "ix_ginas_substance")
@Inheritance
@DiscriminatorValue("SUB")
public class Substance extends GinasCommonData {

	public static final boolean REMOVE_INVALID_RELATIONSHIPS = false;
	private static final String DEFAULT_NO_NAME = "NO_NAME";
	
	private static final String DOC_TYPE_PROPERTY_IMPORT = "PROPERTY_IMPORT";
	public static final String ALTERNATE_SUBSTANCE_REL = "SUBSTANCE->SUB_ALTERNATE";
	public static final String PRIMARY_SUBSTANCE_REL = "SUB_ALTERNATE->SUBSTANCE";
	
	private static final String DOC_TYPE_BATCH_IMPORT = "BATCH_IMPORT";
	public static final String STATUS_APPROVED = "approved";
	public static final String STATUS_PENDING = "pending";
	public static final String STATUS_ALTERNATIVE = "alternative";
	public static final String DEFAULT_ALTERNATIVE_NAME = "ALTERNATIVE DEFINITION";

	
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

	public enum SubstanceDefinitionLevel{
		COMPLETE,
		INCOMPLETE,
		INVALID,
		REPRESENTATIVE
	}
	
	@Indexable(facet=true)
	public SubstanceDefinitionType definitionType = SubstanceDefinitionType.PRIMARY;
	
	@Indexable(facet=true)
	public SubstanceDefinitionLevel definitionLevel = SubstanceDefinitionLevel.COMPLETE;
	
	
	@JSONEntity(title = "Substance Type", values = "JSONConstants.ENUM_SUBSTANCETYPES", isRequired = true)
	@Indexable(suggest = true, facet = true, name = "Substance Class")
	@Column(name = "class")
	public SubstanceClass substanceClass;

	
	@Indexable(suggest = true, facet = true, name = "Record Status")
	public String status = STATUS_PENDING;
	
	@DataVersion
	public String version = "1";
	
	
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JsonSerialize(using = PrincipalSerializer.class)
	@JsonDeserialize(using = PrincipalDeserializer.class)
	@Indexable(facet = true, name = "Approved By")
	public Principal approvedBy;

	@Indexable(facet = true, name = "Approved Date")
	@JsonDeserialize(using = DateDeserializer.class)
	public Date approved;
	
	
	public Date getApproved(){
		return this.approved;
	}

	@JSONEntity(title = "Names", minItems = 1, isRequired = true)
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
	public List<Name> names = new ArrayList<Name>();

	// TOOD original schema has superfluous name = codes in the schema here and
	// in all of Code's properties
	@JSONEntity(title = "Codes")
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
	public List<Code> codes = new ArrayList<Code>();

	@OneToOne(cascade = CascadeType.ALL)
	public Modifications modifications;

	@JSONEntity(title = "Notes")
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
	public List<Note> notes = new ArrayList<Note>();

	@JSONEntity(title = "Properties")
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
	public List<Property> properties = new ArrayList<Property>();

	@JSONEntity(title = "Relationships")
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
	public List<Relationship> relationships = new ArrayList<Relationship>();

	@JSONEntity(title = "References", minItems = 1, isRequired = true)
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
	public List<Reference> references = new ArrayList<Reference>();

	@JSONEntity(title = "Approval ID", isReadOnly = true)
	@Column(length = 10)
	@Indexable(facet = true, suggest = true, name = "Approval ID")
	public String approvalID;

	// TODO in original schema, this field is missing its items: String
	@JSONEntity(title = "Tags", format = "table", isUniqueItems = true)
	@JsonSerialize(using = KeywordListSerializer.class)
    @JsonDeserialize(contentUsing = KeywordDeserializer.TagDeserializer.class)
	@ManyToMany(cascade = CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    @JoinTable(name = "ix_ginas_substance_tags")
	public List<Keyword> tags = new ArrayList<Keyword>();
	
	public void addTag(Keyword tag){
		for(Keyword k:tags){
			if(k.getValue().equals(tag.getValue()))return;
		}
		tags.add(tag);
	}
	
	public void addTagString(String tag){
		this.addTag(new Keyword(GinasCommonData.TAG,tag));
	}
	
	public boolean hasTagString(String tag){
		for(Keyword k:tags){
			if(k.getValue().equals(tag))return true;
		}
		return false;
	}
	

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
				n.put("href", Global.getRef(getClass(), getUuid()) + "/names");
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
				n.put("href", Global.getRef(getClass(), getUuid()) + "/references");
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
				n.put("href", Global.getRef(getClass(), getUuid()) + "/codes");
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
				n.put("href", Global.getRef(getClass(), getUuid())
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
				n.put("href", Global.getRef(getClass(), getUuid()) + "/properties");
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
		if(this.isAlternativeDefinition()){
			String name1=this.getPrimaryDefinitionReference().getName();
			if(name1!=null){
				return Substance.DEFAULT_ALTERNATIVE_NAME + " for [" + name1 + "]";
			}
			return Substance.DEFAULT_ALTERNATIVE_NAME;
		}
		return Substance.DEFAULT_NO_NAME;
	}

	@JsonIgnore
	public List<Relationship> getMetabolites() {
		List<Relationship> ret = new ArrayList<Relationship>();
		for (Relationship r : this.relationships) {
			if (r.type.toLowerCase().contains("metabolite")) {
				ret.add(r);
			}
		}
		return ret;
	}

	@JsonIgnore
	public List<Relationship> getImpurities() {
		List<Relationship> ret = new ArrayList<Relationship>();
		for (Relationship r : this.relationships) {
			if (r.type.toLowerCase().contains("impurity")) {
				ret.add(r);
			}
		}
		return ret;
	}

	@JsonIgnore
	public List<Relationship> getActiveMoieties() {
		List<Relationship> ret = new ArrayList<Relationship>();
		for (Relationship r : this.relationships) {
			if (r.type.toLowerCase().contains("active moiety")) {
				ret.add(r);
			}
		}
		return ret;
	}

	@JsonIgnore
	public List<Relationship> getFilteredRelationships() {
		List<Relationship> ret = new ArrayList<Relationship>(this.relationships);
		ret.removeAll(getImpurities());
		ret.removeAll(getMetabolites());
		ret.removeAll(getActiveMoieties());
		return ret;
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
	public void fixstatus(){
		if(this.isAlternativeDefinition()){
			this.status=Substance.STATUS_ALTERNATIVE;
		}
	}

	@PrePersist
	@PreUpdate
	public void tidy() {
		if (!REMOVE_INVALID_RELATIONSHIPS){
			return;
		}
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
		return approvalID;
	}
	
	
	@JsonIgnore
	public boolean isValidated(){
		return this.status.equalsIgnoreCase(STATUS_APPROVED);
	}

	@JsonProperty("_approvalIDDisplay")
	public String getApprovalIDDisplay() {
		if (approvalID != null)
			return approvalID;
		SubstanceReference subRef = getParentSubstanceReference();
		if (subRef != null) {
			return subRef.approvalID;
		}
		if(!isValidated()){
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
			if(sref.relatedSubstance.refuuid.equals(sub.getUuid().toString())){
				return true;
			}
		}
		
		Relationship r = new Relationship();
		r.relatedSubstance=sub.asSubstanceReference();
		r.type=ALTERNATE_SUBSTANCE_REL;
		r.addReference(Reference.SYSTEM_GENERATED(),this);
		this.relationships.add(r);
		return false;
	}
	@JsonIgnore
	public List<Relationship> removeAlternativeSubstanceDefinitionRelationship(Substance sub) {
		List<Relationship> toRemove= new ArrayList<Relationship>();
		for(Relationship sref:getAlternativeDefinitionRelationships()){
			if(sref.relatedSubstance.refuuid.equals(sub.getUuid().toString())){
				toRemove.add(sref);
			}
		}
		this.relationships.removeAll(toRemove);
		return toRemove;
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
			if (r.getUuid().toString().equals(uuid))
				return r;
		}
		return null;
	}

	//TODO: should be somewhere else
	public void addImportReference(ProcessingJob p) {
		Reference r = new Reference();
		r.docType = Substance.DOC_TYPE_BATCH_IMPORT;
		r.citation = p.payload.name;
		r.documentDate = TimeUtil.getCurrentDate();
		String processingKey=p.getKeyMatching(GinasRecordProcessorPlugin.class.getName());
		r.id=processingKey;
		this.references.add(r);
	}
	public Note addPropertyNote(String note, String property){
		Note n = new Note();
		n.note=note;
		Reference r = new Reference();
		r.docType = DOC_TYPE_PROPERTY_IMPORT;
		r.citation = property;
		r.documentDate = TimeUtil.getCurrentDate();
		n.addReference(r);
		for(Note oldNote:this.notes){
			if(oldNote.note.equals(n.note)){
				return null;
			}
		}
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
		if(STATUS_APPROVED.equalsIgnoreCase(status)){
			return "Validated (UNII)"; //TODO: move this elsewhere
		}
		return status;
	}
	
	@JsonIgnore
	 public String getLinkingID(){
	        if(approvalID!=null){
	                return approvalID;
	        }
			UUID uuid = getUuid();
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
	
	public List<Note> getNotes(){
		return this.notes;
	}
	
	/**
	 * These are the simple notes that we actually want to disaply,
	 * not the long complicated ones.
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<Note> getDisplayNotes(){
		List<Note> displayNotes = new ArrayList<Note>();
		for(Note n: this.notes){
			if(n.note.length() < 1500){
				displayNotes.add(n);
			}
		}
		return displayNotes;
	}
	
	@JsonIgnore
	public List<SubstanceReference> getDependsOnSubstanceReferences(){
		
		List<SubstanceReference> srefs=new ArrayList<SubstanceReference>();
		Modifications m=this.getModifications();
		if(m!=null){
			if(m.agentModifications!=null){
				for(AgentModification am:m.agentModifications){
					if(am.agentSubstance!=null)
						srefs.add(am.agentSubstance);
				}
			}
			if(m.structuralModifications!=null){
				for(StructuralModification sm:m.structuralModifications){
					if(sm.molecularFragment!=null)
						srefs.add(sm.molecularFragment);
				}
			}
		}
		return srefs;
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
	
	@Override
	public Class<?>[] fetchEquivalentClasses(){
		return getAllClasses();
	}
	
	
	public int hashCode(){
		return (this.getUuid() + this.version).hashCode();
	}
	public boolean equals(Object o){
		if(o == null)return false;
		if(!(o instanceof Substance))return false;
		
		String thisID= (this.getUuid() + this.version);
		String thatID= (((Substance)o).getUuid() + ((Substance)o).version);
		return thisID.equals(thatID);
	}
	
	@JsonIgnore
	@Indexable(facet=true, name="Modifications")
	public List<String> getModifiedCategory(){
		List<String> mods = new ArrayList<String>();
		if(this.getModificationCount()<=0){
			mods.add("No Modifications");
		}else{
			mods.add("Any Modification");
			if(!this.getModifications().structuralModifications.isEmpty()){
				mods.add("Structural Modification");
			}
			if(!this.getModifications().agentModifications.isEmpty()){
				mods.add("Agent Modification");
			}
			if(!this.getModifications().physicalModifications.isEmpty()){
				mods.add("Physical Modification");
			}
		}
		return mods;
	}
	
	
	
	@JsonIgnore
	@Indexable(facet=true)
	public String getSubstanceDeprecated(){
		//System.out.println("Found deprecated record");
		return ""+this.deprecated;
	}

	/**
	 * removes keywords with matching value to input
	 * 
	 * @param tag
	 * @return List of Keywords matching criteria
	 */
	public List<Keyword> removeTagString(String tag) {
		List<Keyword> toRemove = new ArrayList<Keyword>();
		for(Keyword k:tags){
			if(k.getValue().equals(tag)){
				toRemove.add(k);
			}
		}
		tags.removeAll(toRemove);
		return toRemove;
	}
	
}
