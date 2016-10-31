package ix.ginas.models;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.Keyword;
import ix.ginas.models.serialization.ReferenceSetDeserializer;
import ix.ginas.models.serialization.ReferenceSetSerializer;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

@MappedSuperclass
public class GinasCommonSubData extends GinasCommonData implements GinasAccessReferenceControlled{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//@JsonIgnore
	private EmbeddedKeywordList internalReferences = new EmbeddedKeywordList();
	
    public GinasCommonSubData () {
    }
    
    @JsonProperty("references")    
    @JsonSerialize(using = ReferenceSetSerializer.class)
    public Set<Keyword> getReferences(){
    	return new LinkedHashSet<Keyword>(internalReferences);
    }

    
    @JsonProperty("references")
    @JsonDeserialize(using = ReferenceSetDeserializer.class)
	@Override
	public void setReferences(Set<Keyword> references) {
    	this.internalReferences = new EmbeddedKeywordList(references);
	}
    
   

	public void addReference(String refUUID){
		this.internalReferences.add(new Keyword(GinasCommonSubData.REFERENCE,
				refUUID
		));
		setReferences(new LinkedHashSet<Keyword>(this.internalReferences));
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
	

	/**
	 * This is needed to ensure that any pieces marked as immutable
	 * are properly re-initialized
	 */
	@PreUpdate
	public void updateImmutables(){
		this.internalReferences = new EmbeddedKeywordList(internalReferences);
	}
}