package ix.ginas.models.v1;

import ix.core.models.Group;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.ginas.models.GinasAccessContainer;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasReferenceContainer;
import ix.ginas.models.GroupListSerializer;
import ix.ginas.models.ReferenceListSerializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@DiscriminatorValue("GSRS")
public class GinasChemicalStructure extends Structure implements GinasAccessReferenceControlled {

	public GinasChemicalStructure(){
		
	}
	
	public GinasChemicalStructure(Structure s){
		this.atropisomerism=s.atropisomerism;
		this.charge=s.charge;
		this.count=s.count;
		//this.created=s.created;
		this.definedStereo=s.definedStereo;
		this.deprecated=s.deprecated;
		this.digest=s.digest;
		this.ezCenters=s.ezCenters;
		this.formula=s.formula;
		this.id=s.id;
		this.lastEdited=s.lastEdited;
		this.links=s.links;
		this.molfile=s.molfile;
		this.mwt=s.mwt;
		this.opticalActivity=s.opticalActivity;
		this.properties=s.properties;
		this.smiles=s.smiles;
		this.stereoCenters=s.stereoCenters;
		this.stereoComments=s.stereoComments;
		this.version=s.version;
	}
	
	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL)
	GinasAccessContainer recordAccess;

	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL)
	public GinasReferenceContainer recordReference;

	@JsonProperty("access")
	public void setAccess(Collection<String> access) {
		ObjectMapper om = new ObjectMapper();
		Map mm = new HashMap();
		mm.put("access", access);
		mm.put("entityType", this.getClass().getName());
		JsonNode jsn = om.valueToTree(mm);
		try {
			recordAccess = om.treeToValue(jsn, GinasAccessContainer.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return;
	}

	@JsonSerialize(using = GroupListSerializer.class)
	public Set<Group> getAccess() {
		if (recordAccess != null) {
			return recordAccess.access;
		}
		return null;
	}

	@JsonSerialize(using = ReferenceListSerializer.class)
	public Set<Keyword> getReferences() {
		if (recordReference != null) {
			return recordReference.references;
		}
		return null;
	}

	@JsonProperty("references")
	public void setReferences(Collection<String> references) {
		ObjectMapper om = new ObjectMapper();
		Map mm = new HashMap();
		mm.put("references", references);
		mm.put("entityType", this.getClass().getName());
		JsonNode jsn = om.valueToTree(mm);
		
		try {
			recordReference = om.treeToValue(jsn, GinasReferenceContainer.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return;
	}

	public Set<Group> getAccessGroups() {
		return getAccess();
	}
	public void addReference(String refUUID){
		if(this.recordReference==null){
			this.recordReference= new GinasReferenceContainer(this);
		}
		this.recordReference.references.add(new Keyword("REFERENCE", 
				refUUID
		));
	}
	public void addReference(Reference r){
		addReference(r.uuid.toString());
	}
	public void addReference(Reference r, Substance s){
		s.references.add(r);
		this.addReference(r);
	}
}
