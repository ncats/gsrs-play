package ix.ginas.models;

import ix.core.models.Keyword;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.ReferenceListSerializer;

import java.util.Collection;
import java.util.HashMap;
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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@MappedSuperclass
public class GinasSubData extends GinasCommonData implements GinasData{
    @JsonIgnore
    @OneToOne(cascade=CascadeType.ALL)
    GinasReference recordReference;
    
   
    @JsonSerialize(using = ReferenceListSerializer.class)
    public Set<Keyword> getReferences(){
    	if(recordReference!=null){
    		return recordReference.references;
    	}
    	return null;
    }
    
    @JsonProperty("references")    
    public void setReferences(Collection<String> references){
    	ObjectMapper om = new ObjectMapper();
    	Map mm = new HashMap();
    	mm.put("references", references);
    	mm.put("entityType", this.getClass().getName());
    	JsonNode jsn=om.valueToTree(mm);
    	try {
    		recordReference= om.treeToValue(jsn, GinasReference.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	return;
    }
    
    public GinasSubData () {
    }
    
    
    public void addReference(String refUUID){
		if(this.recordReference==null){
			this.recordReference= new GinasReference(this);
		}
		this.recordReference.references.add(new Keyword("REFERENCE", 
				refUUID
		));
	}
	public void addReference(Reference r){
		addReference(r.uuid.toString());
	}
}
