package ix.ginas.models;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.Keyword;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

@MappedSuperclass
public class GinasCommonSubData extends GinasCommonData implements GinasAccessReferenceControlled{
    @JsonIgnore
    //@OneToOne(cascade=CascadeType.ALL)
	public GinasReferenceContainer recordReference;
    
    
//    
//    @Embeddable
//    public static class Test1 implements Serializable{
//    	public String inside="inside1";
//    }
//    
//    @Embedded
//    @Lob
//    public Test1 test= new Test1();
    
    public GinasCommonSubData () {
    }
    
    @JsonSerialize(using = ReferenceSetSerializer.class)
    public Set<Keyword> getReferences(){
    	if(recordReference!=null){
    		return recordReference.getReferences();
    	}
    	return new LinkedHashSet<Keyword>();
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
	
	public String toJson(){
		ObjectMapper om = new ObjectMapper();
		return om.valueToTree(this).toString();
	}

}
