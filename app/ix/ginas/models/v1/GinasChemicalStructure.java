package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.UserFetcher;
import ix.core.controllers.AdminFactory;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.models.Group;
import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Principal;
import ix.core.models.Structure;
import ix.core.util.TimeUtil;
import ix.ginas.models.GinasAccessContainer;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.GinasReferenceContainer;
import ix.ginas.models.GroupListDeserializer;
import ix.ginas.models.GroupListSerializer;
import ix.ginas.models.PrincipalDeserializer;
import ix.ginas.models.PrincipalSerializer;
import ix.ginas.models.ReferenceSetDeserializer;
import ix.ginas.models.ReferenceSetSerializer;

@Entity
@DiscriminatorValue("GSRS")
public class GinasChemicalStructure extends Structure implements GinasAccessReferenceControlled {

	
    public Date created=null;
    
    @OneToOne()
    @JsonSerialize(using = PrincipalSerializer.class)
    @JsonDeserialize(using = PrincipalDeserializer.class)
    @Indexable(facet = true, name = "Created By")
    public Principal createdBy;
    
    @OneToOne(cascade=CascadeType.ALL)
    @JsonSerialize(using = PrincipalSerializer.class)
    @JsonDeserialize(using = PrincipalDeserializer.class)
    @Indexable(facet = true, name = "Last Edited By")
    public Principal lastEditedBy;
	
	public GinasChemicalStructure(){
		
	}
	
	public GinasChemicalStructure(Structure s){
		this.atropisomerism=s.atropisomerism;
		this.charge=s.charge;
		this.count=s.count;
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
		this.stereoChemistry=s.stereoChemistry;
		this.version=s.version;
	}
	
	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL)
	GinasAccessContainer recordAccess;

	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL)
	public GinasReferenceContainer recordReference;

	@JsonProperty("access")
    @JsonDeserialize(using = GroupListDeserializer.class)
    public void setAccess(Set<Group> access){
    	List<String> accessGroups=new ArrayList<String>();
    	for(Group g: access){
    		accessGroups.add(g.name);
    	}
    	
    	ObjectMapper om = new ObjectMapper();
    	Map mm = new HashMap();
    	mm.put("access", accessGroups);
    	mm.put("entityType", this.getClass().getName());
    	JsonNode jsn=om.valueToTree(mm);
    	try {
			recordAccess= om.treeToValue(jsn, GinasAccessContainer.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    }

	public void addRestrictGroup(Group p){
		if(this.recordAccess==null){
			this.recordAccess=new GinasAccessContainer();
		}
		this.recordAccess.add(p);
	}
	public void addRestrictGroup(String group){
		addRestrictGroup(AdminFactory.registerGroupIfAbsent(new Group(group)));
	}



	
	@JsonProperty("access")
    @JsonSerialize(using = GroupListSerializer.class)
    public Set<Group> getAccess(){
    	if(recordAccess!=null){
    		return recordAccess.access;
    	}
    	return new LinkedHashSet<Group>();
    }

	
	
	@JsonSerialize(using = ReferenceSetSerializer.class)
	public Set<Keyword> getReferences() {
		if (recordReference != null) {
			return recordReference.getReferences();
		}
		return new LinkedHashSet<Keyword>();
	}
	
	@JsonProperty("references")    
    @JsonDeserialize(using = ReferenceSetDeserializer.class)
	@Override
	public void setReferences(Set<Keyword> references) {
    	if(this.recordReference==null){
    		this.recordReference=new GinasReferenceContainer(this);
    	}
    	this.recordReference.setReferences(references);
	}

	public Set<Group> getAccessGroups() {
		return getAccess();
	}
	
	public void addReference(String refUUID){
		if(this.recordReference==null){
			this.recordReference= new GinasReferenceContainer(this);
		}
		this.recordReference.references.add(new Keyword(GinasCommonSubData.REFERENCE,
				refUUID
		));
	}
	
	public void addReference(Reference r){
		addReference(r.getUuid().toString());
	}
	
	public void addReference(Reference r, Substance s){
		s.references.add(r);
		this.addReference(r);
	}
	
    @PrePersist
    @PreUpdate
    public void modifiedV2() {
    	if(created==null){
    		created= TimeUtil.getCurrentDate();
    	}
    	Principal p1=UserFetcher.getActingUser();
    	if(p1!=null){
    		lastEditedBy=p1;
    		if(this.createdBy==null){
    			createdBy=p1;
        	}
    	}
    }

	public GinasChemicalStructure copy() throws Exception {
		EntityMapper em=EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
		JsonNode jsn=em.valueToTree(this);
		GinasChemicalStructure gcs=em.treeToValue(jsn, GinasChemicalStructure.class);
		gcs.id=null;
		return gcs;
	}
}
