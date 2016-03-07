package ix.ginas.models;

import ix.core.models.Keyword;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@MappedSuperclass
public class GinasCommonSubData extends GinasCommonData implements GinasAccessReferenceControlled{
    @JsonIgnore
    @OneToOne(cascade=CascadeType.ALL)
	public GinasReferenceContainer recordReference;
    
   
    @JsonSerialize(using = ReferenceSetSerializer.class)
    public Set<Keyword> getReferences(){
    	if(recordReference!=null){
    		return recordReference.getReferences();
    	}
    	return new HashSet<Keyword>();
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
    
    public GinasCommonSubData () {
    }

	public GinasReferenceContainer getRecordReference() {
		return recordReference;
	}

	public void setRecordReference(GinasReferenceContainer recordReference) {
		this.recordReference = recordReference;
	}

	public void addReference(String refUUID){
		if(this.recordReference==null){
			this.recordReference= new GinasReferenceContainer(this);
		}
		this.recordReference.getReferences().add(new Keyword(GinasCommonSubData.REFERENCE,
				refUUID
		));
		
	}
	
	public void addReference(Reference r){
		addReference(r.getOrGenerateUUID().toString());
	}
	public void addReference(Reference r, Substance s){
		s.references.add(r);
		this.addReference(r);
	}
	
	public String toJson(){
		ObjectMapper om = new ObjectMapper();
		return om.valueToTree(this).toString();
	}

}
