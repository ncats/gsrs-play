package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalFactory;
import gov.nih.ncgc.jchemical.JchemicalReader;
import ix.core.GinasProcessingMessage;
import ix.core.GinasProcessingMessage.Link;
import ix.core.models.Backup;
import ix.core.models.BeanViews;
import ix.core.models.ChangeReason;
import ix.core.models.DataValidated;
import ix.core.models.DataVersion;
import ix.core.models.Edit;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Principal;
import ix.core.models.ProcessingJob;
import ix.core.plugins.GinasRecordProcessorPlugin;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.TimeUtil;
import ix.ginas.models.GinasCommonData;
import ix.ginas.models.ValidationMessageHolder;
import ix.ginas.models.serialization.DateDeserializer;
import ix.ginas.models.serialization.KeywordDeserializer;
import ix.ginas.models.serialization.KeywordListSerializer;
import ix.ginas.models.serialization.PrincipalDeserializer;
import ix.ginas.models.serialization.PrincipalSerializer;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Global;
import ix.utils.Util;
import play.Logger;
import play.Play;

@Backup
@JSONEntity(name = "substance", title = "Substance")
@Entity
@Table(name = "ix_ginas_substance")
@Inheritance
@DiscriminatorValue("SUB")
public class Substance extends GinasCommonData implements ValidationMessageHolder {


	public static final String VALIDATION_REFERENCE_TYPE = "VALIDATION_MESSAGE";

	public static final String GROUP_ADMIN = "admin";

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

	private static ChemicalFactory DEFAULT_READER_FACTORY = new JchemicalReader();
	private static String NULL_MOLFILE = "\n\n\n  0  0  0     0  0            999 V2000\nM  END\n\n$$$$";

	



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

	@Indexable(facet=true, name="Definition Type")
	public SubstanceDefinitionType definitionType = SubstanceDefinitionType.PRIMARY;

	@Indexable(facet=true, name="Definition Level")
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
	@Indexable(facet = true, name = "Approved By", sortable=true, recurse=false)
	public Principal approvedBy;

	@Indexable(facet = true, name = "Approved Date", sortable=true)
	@JsonDeserialize(using = DateDeserializer.class)
	public Date approved;

	@Indexable(name="Change Reason", suggest=true)
	@ChangeReason
	public String changeReason;


	public Date getApproved(){
		return this.approved;
	}

	@JSONEntity(title = "Names", minItems = 1, isRequired = true)
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
	public List<Name> names = new ArrayList<Name>();

	// TOOD original schema has superfluous 
	// name = codes in the schema here and
	// in all of Code's properties
	@JSONEntity(title = "Codes")
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
	public List<Code> codes = new ArrayList<Code>();

	@OneToOne(cascade = CascadeType.ALL)
	@JsonView(BeanViews.Full.class)
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
	@Indexable(facet = true, suggest = true, name = "Approval ID", sortable=true)
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

	public List<Code> getCodes(){
		return this.codes;
	}

	@JsonIgnore
	public List<Code> getOrderedCodes(Map<String, Integer> codeSystemOrder){

		List<Code> nlist = new ArrayList<Code>(this.codes);

		nlist.sort(new Comparator<Code>(){
			@Override
			public int compare(Code c1, Code c2) {
				if(c1.codeSystem==null){
					if(c2.codeSystem==null){
						return 0;
					}
					return 1;
				}
				if(c2.codeSystem==null)return -1;
				Integer i1=codeSystemOrder.get(c1.codeSystem);
				Integer i2=codeSystemOrder.get(c2.codeSystem);

				if(i1!=null && i2!=null){
					return i1-i2;
				}
				if(i1!=null && i2==null)return -1;
				if(i1==null && i2!=null)return 1;
				return c1.codeSystem.compareTo(c2.codeSystem);
			}
		}
				);

		return nlist;
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
	@JsonProperty("_modifications")
	public JsonNode getJsonModifications() {
		JsonNode node = null;
		if (this.getModifications()!=null) {
			try {
				ObjectNode n = mapper.createObjectNode();
				n.put("count", getModificationCount());
				n.put("href", Global.getRef(getClass(), getUuid()) + "/modifications");
				node = n;
			} catch (Exception ex) {
				ex.printStackTrace();
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

	@Indexable(suggest = true, facet = true, name = "Display Name", sortable=true)
	@JsonProperty("_name")
	public String getName() {
		if(names!=null){
			for (Name n : names) {
				if (n.preferred) {
					return n.name;
				}
			}
		}
		if(names!=null && names.size()>0){
			return names.get(0).name;	
		}
		if(this.isAlternativeDefinition()){
			SubstanceReference subref=this.getPrimaryDefinitionReference();
			if(subref!=null){
				String name1=subref.getName();
				if(name1!=null){
					return Substance.DEFAULT_ALTERNATIVE_NAME + " for [" + name1 + "]";
				}
				return Substance.DEFAULT_ALTERNATIVE_NAME;
			}
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
			if (r.type.toUpperCase().contains(Relationship.ACTIVE_MOIETY_RELATIONSHIP_TYPE)) {
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
		ret.removeAll(getAlternativeDefinitionRelationships());
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
		if ("concept".equals(this.substanceClass.toString())) {
			for (Relationship r : relationships) {
				if ("SUBSTANCE->SUB_CONCEPT".equals(r.type)) {
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
	@DataValidated
	public boolean isValidated(){
		return this.status.equalsIgnoreCase(STATUS_APPROVED);
	}

	//probably should be turned off
	@JsonProperty("_approvalIDDisplay")
	public String getApprovalIDDisplay() {

		if (approvalID != null)
			return approvalID;
		SubstanceReference subRef = getParentSubstanceReference();
		if (subRef != null) {
			return subRef.approvalID;
		}
		if(this.isAlternativeDefinition()){
			SubstanceReference subRef2 = this.getPrimaryDefinitionReference();
			if(subRef2!=null){

				if(subRef2.approvalID!=null){
					return subRef2.approvalID;
				}
			}
		}

		if(!isValidated()){
			return this.status + " record";
		}
		return "NO APPROVAL ID";
	}

	@JsonIgnore
	public String getBestId() {
		if (approvalID != null)
			return approvalID;
		SubstanceReference subRef = getParentSubstanceReference();
		if (subRef != null) {
			return subRef.approvalID + "_sub" + this.uuid;
		}
		if(this.isAlternativeDefinition()){
			SubstanceReference subRef2 = this.getPrimaryDefinitionReference();
			if(subRef2!=null){
				if(subRef2.approvalID!=null){
					return subRef2.approvalID + "_alt" + this.uuid;
				}
			}
		}
		return this.uuid.toString();
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

	/**
	 * Returns this substance as a SubstanceReference
	 * for linking.
	 * @return
	 */
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

	/**
	 * Temporary measure to persist validation messages without the use
	 * of a data model change
	 * 
	 * @param note
	 * @param property
	 * @return
	 */
	public Note addValidationNote(GinasProcessingMessage gpm, Reference r){
		Note n = new Note();
		n.note="[Validation]" + gpm.getMessageType() + ":" + gpm.getMessage();
		if(gpm.hasLinks()){
			for(Link mes : gpm.links){
				n.note += "\n" + mes.text;
			}
		}
		n.addReference(r);
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
		return String.valueOf(this.deprecated);
	}

	/**
	 * removes keywords with matching value to input
	 * 
	 * @param tag
	 * @return List of Keywords matching criteria
	 */
	public List<Keyword> removeTagString(String tag) {
		List<Keyword> removed = new ArrayList<Keyword>();

		Iterator<Keyword> iter = tags.iterator();
		while(iter.hasNext()){
			Keyword k = iter.next();
			if(k.getValue().equals(tag)){
				iter.remove();
				removed.add(k);
			}
		}
		return removed;
	}

	/**
	 * Returns references which are associated with the act of
	 * making the record "public".
	 * 
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<Reference> getPublicReleaseReferences(){
		List<Reference> rlist = new ArrayList<Reference>();
		for(Reference r: this.references){
			if(r.isPublicReleaseReference()){
				rlist.add(r);
			}
		}
		return rlist;
	}

	@JsonIgnore
	@Indexable(facet = true, name = "Name Count", sortable=true)
	public int getNameCount(){
		return names.size();
	}

	@JsonIgnore
	@Indexable(facet = true, name = "Reference Count", sortable=true)
	public int getReferenceCount(){
		return names.size();
	}

	@JsonIgnore
	public boolean hasCodes(){
		return !this.codes.isEmpty();
	}
	@JsonIgnore
	public boolean hasNames(){
		return !this.names.isEmpty();
	}

	@JsonIgnore
	public boolean hasRelationships(){
		return !this.relationships.isEmpty();
	}





	/**
	 * Create a new {@link Chemical} object for this Substance and ignore
	 * any errors or warnings.
	 *
	 * @return a new {@link Chemical} object, should never be null.
	 *
	 */
	@JsonIgnore
	@Transient
	public Chemical toChemical() {
		return toChemical(new ArrayList<>());
	}

	/**
	 * Create a new {@link Chemical} object for this Substance and report any
	 * errors or warnings to the passed in {@link GinasProcessingMessage} parameter.
	 *
	 * @param messages the list of {@link GinasProcessingMessage}s to add
	 *                 errors/warnings to if there are problems.
	 * @return a new {@link Chemical} object, should never be null.
	 *
	 * @throws NullPointerException if messages is null
	 */
	@JsonIgnore
	@Transient
	public Chemical toChemical(List<GinasProcessingMessage> messages) {
		Objects.requireNonNull(messages);
		Chemical c = getChemicalImpl(messages);

		if (c.getDim() < 2) {
			c.clean2D();
		}
		if (approvalID != null) {
			c.setProperty("APPROVAL_ID", approvalID);
		}
		c.setProperty("NAME", getName());
		c.setName(getName());
		StringBuilder sb = new StringBuilder();

		for (Name n : getOfficialNames()) {
			String name = n.name;
			sb.append(name + "\n");

			for (String loc : n.getLocators(this)) {
				sb.append(name + " [" + loc + "]\n");
			}

		}
		if (sb.length() > 0) {
			c.setProperty("OFFICIAL_NAMES", sb.toString());
		}
		// clear builder
		sb.setLength(0);
		for (Name n : getNonOfficialNames()) {
			String name = n.name;
			sb.append(name + "\n");

			for (String loc : n.getLocators(this)) {
				sb.append(name + " [" + loc + "]\n");
			}
		}
		if (sb.length() > 0) {
			c.setProperty("NON_OFFICIAL_NAMES", sb.toString());
		}
		// clear builder
		sb.setLength(0);

		for (Code cd : codes) {
			String codesset = c.getProperty(cd.codeSystem);

			StringBuilder codeBuilder;
			if (codesset == null || codesset.trim().equals("")) {
				codeBuilder = new StringBuilder();
			} else {
				codeBuilder = new StringBuilder(codesset).append('\n');
			}
			codeBuilder.append(cd.code);
			if (!"PRIMARY".equals(cd.type)) {
				codeBuilder.append(" [").append(cd.type).append("]");
			}
			c.setProperty(cd.codeSystem, codeBuilder.toString());
		}
		for (GinasProcessingMessage gpm : messages) {
			String codesset = c.getProperty("EXPORT-WARNINGS");

			StringBuilder codeBuilder;
			if (codesset == null || codesset.trim().equals("")) {
				codeBuilder = new StringBuilder();
			} else {
				codeBuilder = new StringBuilder(codesset).append('\n');
			}
			codeBuilder.append(gpm.message);
			c.setProperty("EXPORT-WARNINGS", codeBuilder.toString());
		}
		return c;
	}

	/**
	 * Create a new {@link Chemical} object for this Substance.
	 * By default, this will create an empty Chemical, subclasses
	 * that are actually Chemicals should override this method
	 * to return something meaningful.
	 *
	 * @param messages the list of {@link GinasProcessingMessage}s to add
	 *                 errors/warnings to if there are problems.
	 * @return a new {@link Chemical} object, should never be null.
	 */
	protected Chemical getChemicalImpl(List<GinasProcessingMessage> messages) {
		messages.add(GinasProcessingMessage
				.WARNING_MESSAGE("Structure is non-chemical. Structure format is largely meaningless."));
		return DEFAULT_READER_FACTORY.createChemical(NULL_MOLFILE, Chemical.FORMAT_SDF);
	}



	@JsonIgnore
	public List<Edit> getEdits(){
		//this is not entirely necessary, and could be done
		//more explicitly
		return EntityWrapper.of(this).getEdits(); 
	}

	/**
	 * This is not yet implemented, so it always returns an empty list.
	 * @return
	 */
	@JsonIgnore
	public List<GinasProcessingMessage> getValidationMessages(){
		return new ArrayList<GinasProcessingMessage>();
	}


	//TODO: make this better, maybe multiple keywords?
	/**
	 * Used specifically for faceting, right now it's not
	 * very useful
	 * @return
	 */
	@Indexable(name="Validation", facet=true)
	@JsonIgnore
	public List<String> getValidation(){
		List<String> validationTypes = new ArrayList<String>();
		String vprefix="[Validation]";
		for(Note n: this.notes){
			if(n.note.startsWith(vprefix)){
				String vtype = n.note.substring(vprefix.length()).split(":")[0];
				validationTypes.add(vtype);
				if(n.note.contains("duplicate") && n.note.contains("name")){
					validationTypes.add("Name Collision");
				}
				if(n.note.contains("duplicate") && n.note.contains("code")){
					validationTypes.add("Code Collision");
				}
				if(n.note.contains("duplicate") && n.note.contains("structure")){
					validationTypes.add("Structure Collision");
				}
			}

		}
		return validationTypes;
	}




	/**
	 * Store the validation message on the record.
	 * 
	 * Currently, these are stored as notes.
	 * 
	 * @param gpm
	 * 
	 */
	@Override
	public void setValidationMessages(List<GinasProcessingMessage> gpm) {
		Reference r = new Reference();
		r.docType = VALIDATION_REFERENCE_TYPE;
		r.citation = "GSRS System-generated Validation messages";
		r.addRestrictGroup(GROUP_ADMIN);
		r.documentDate = TimeUtil.getCurrentDate();
		this.references.add(r);
		for(GinasProcessingMessage message: gpm){
			Note n=this.addValidationNote(message, r);
			if(n!=null){
				n.addRestrictGroup(GROUP_ADMIN);
			}
		}
	}

}
