package ix.ginas.models.v1;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
import ix.core.models.Value;
import ix.core.util.TimeUtil;
import ix.ginas.models.GinasAccessContainer;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasReferenceContainer;
import ix.ginas.models.serialization.GroupDeserializer;
import ix.ginas.models.serialization.GroupSerializer;
import ix.ginas.models.serialization.PrincipalDeserializer;
import ix.ginas.models.serialization.PrincipalSerializer;
import ix.ginas.models.serialization.ReferenceSetDeserializer;
import ix.ginas.models.serialization.ReferenceSetSerializer;

@Entity
@DiscriminatorValue("GSRS")
public class GinasChemicalStructure extends Structure implements GinasAccessReferenceControlled {

	/**
	 
        if (struc == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }
        
        provider.defaultSerializeField("id", struc.id, jgen);
        provider.defaultSerializeField("created", struc.created, jgen);
        provider.defaultSerializeField("lastEdited", struc.lastEdited, jgen);
        provider.defaultSerializeField("deprecated", struc.deprecated, jgen);
        provider.defaultSerializeField("digest", struc.digest, jgen);
        provider.defaultSerializeField("molfile", struc.molfile, jgen);
        provider.defaultSerializeField("smiles", struc.smiles, jgen);
        provider.defaultSerializeField("formula", struc.formula, jgen);
        provider.defaultSerializeField
            ("stereochemistry", struc.stereoChemistry, jgen);
        provider.defaultSerializeField
            ("opticalActivity", struc.opticalActivity, jgen);
        provider.defaultSerializeField
            ("atropisomerism", struc.atropisomerism, jgen);
        provider.defaultSerializeField
            ("stereoComments", struc.stereoComments, jgen);
        provider.defaultSerializeField
            ("stereoCenters", struc.stereoCenters, jgen);
        provider.defaultSerializeField
            ("definedStereo", struc.definedStereo, jgen);
        provider.defaultSerializeField("ezCenters", struc.ezCenters, jgen);
        provider.defaultSerializeField("charge", struc.charge, jgen);
        provider.defaultSerializeField("mwt", struc.mwt, jgen);
        
        provider.defaultSerializeField("properties", struc.properties, jgen);
        if(struc.createdBy!=null)
        	provider.defaultSerializeField("createdBy", struc.createdBy.username, jgen);
        if(struc.lastEditedBy!=null)
        	provider.defaultSerializeField("lastEditedBy", struc.lastEditedBy.username, jgen);
        provider.defaultSerializeField("references", struc.getReferencesString(), jgen);      
        provider.defaultSerializeField("access", struc.getAccessString(), jgen);
        
        
       
        
        for (Value val : struc.properties) {
            if (Structure.H_LyChI_L4.equals(val.label)) {
                Keyword kw = (Keyword)val;
                provider.defaultSerializeField("hash", kw.term, jgen);
            }
        } 
	 */
	
    public Date created=null;
    
    @OneToOne()
    @JsonSerialize(using = PrincipalSerializer.class)
    @JsonDeserialize(using = PrincipalDeserializer.class)
    @Indexable(facet = true, name = "Created By")
    public Principal createdBy;
    
    @OneToOne()
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
//	@OneToOne(cascade = CascadeType.ALL)
	@Basic(fetch=FetchType.LAZY)
	GinasAccessContainer recordAccess;


	@JsonIgnore
	//@Basic(fetch=FetchType.LAZY)
	@OneToOne(cascade = CascadeType.ALL)
	private GinasReferenceContainer recordReference;


    @JsonIgnore
    public GinasAccessContainer getRecordAccess() {
    	return recordAccess;
    }

    @JsonIgnore
    public void setRecordAccess(GinasAccessContainer recordAccess) {
        this.recordAccess = new GinasAccessContainer(this);
        if(recordAccess!=null){
        this.recordAccess.setAccess(recordAccess.getAccess());
        }
    }

    @JsonProperty("access")
    @JsonDeserialize(contentUsing = GroupDeserializer.class)
    public void setAccess(Set<Group> access){
    	GinasAccessContainer recordAccess=this.getRecordAccess();
    	if(recordAccess==null){
    		recordAccess=new GinasAccessContainer(this);
    	}
    	recordAccess.setAccess(access);
		setRecordAccess(recordAccess);
    }
    
    @JsonProperty("access")
    @JsonSerialize(contentUsing = GroupSerializer.class)
    public Set<Group> getAccess(){
    	GinasAccessContainer gac=getRecordAccess();
    	if(gac!=null){
    		return gac.getAccess();
    	}
    	return new LinkedHashSet<Group>();
    }
    public Set<String> getAccessString(){
    	Set<String> keyset=new LinkedHashSet<String>();
    	for(Group k:getAccess()){
    		keyset.add(k.name);
    	}
    	return keyset;
    }

	public void addRestrictGroup(Group p){
		GinasAccessContainer gac=this.getRecordAccess();
		if(gac==null){
			gac=new GinasAccessContainer(this);
		}
		gac.add(p);
		this.setRecordAccess(gac);
	}
	
	public void addRestrictGroup(String group){
		addRestrictGroup(AdminFactory.registerGroupIfAbsent(new Group(group)));
	}


    @JsonSerialize(using = ReferenceSetSerializer.class)
    public Set<Keyword> getReferences(){
    	if(recordReference!=null){
    		return recordReference.getReferences();
    	}
    	return new LinkedHashSet<Keyword>();
    }
    
    @JsonIgnore
    public Set<String> getReferencesString(){
    	Set<String> keyset=new LinkedHashSet<String>();
    	for(Keyword k:getReferences()){
    		keyset.add(k.term);
    	}
    	return keyset;
    }

    @JsonProperty("references")    
    @JsonDeserialize(using = ReferenceSetDeserializer.class)
	@Override
	public void setReferences(Set<Keyword> references) {
    	GinasReferenceContainer grc=getRecordReference();
    	if(grc==null){
    		grc=new GinasReferenceContainer(this);
    	}
    	grc.setReferences(references);
    	setRecordReference(grc);
	}
    
   

    @JsonIgnore
	public GinasReferenceContainer getRecordReference() {
		return recordReference;
	}

    @JsonIgnore
	public void setRecordReference(GinasReferenceContainer recordReference) {
    	GinasReferenceContainer grc=new GinasReferenceContainer(this);
    	grc.setReferences(recordReference.references);
		this.recordReference = grc;
	}
    
    
    public void setId(UUID uuid){
    	this.id=uuid;
    }
    
    

	public void addReference(String refUUID){
		GinasReferenceContainer grc=getRecordReference();
    	if(grc==null){
    		grc=new GinasReferenceContainer(this);
    	}
    	grc.addReference(refUUID);
    	setRecordReference(grc);
	}
	
	public void addReference(Reference r){
		addReference(r.getOrGenerateUUID().toString());
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
