package ix.ginas.models.v1;

import java.util.LinkedHashSet;
import java.util.Map;

import javax.persistence.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.VIntArray;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.IntArrayDeserializer;
import ix.ginas.models.IntArraySerializer;

@Entity
@Table(name="ix_ginas_unit")
public class Unit extends GinasCommonSubData {
    @OneToOne(cascade=CascadeType.ALL)
    @JsonSerialize(using = IntArraySerializer.class)
    @JsonDeserialize(using = IntArrayDeserializer.class)
    public VIntArray amap;

    @OneToOne(cascade=CascadeType.ALL)
    public Amount amount;
    public Integer attachmentCount;
    public String label;
    @Lob
    @Basic(fetch=FetchType.EAGER)
    
    //TODO: should be changed to be a structure
    public String structure;
    
    public String type;
    
    @Lob
    @Column(name = "attachment_Map")
    private String _attachmentMap;
    
    public Map<String,LinkedHashSet<String>> getAttachmentMap(){
    	ObjectMapper om = new ObjectMapper();
    	Map<String, LinkedHashSet<String>> amap=null;
		try {
			amap = om.readValue(_attachmentMap, new TypeReference<Map<String, LinkedHashSet<String>>>(){});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	return amap;
    }
    
    public void setAttachmentMap(Map<String,LinkedHashSet<String>> amap){
    	ObjectMapper om = new ObjectMapper();
    	_attachmentMap=null;
    	try {
			_attachmentMap=om.writeValueAsString(amap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    }
    /*
    public Map<String,LinkedHashSet<String>> getAttachmentMap(){
    	ObjectMapper om = new ObjectMapper();
    	Map<String, LinkedHashSet<String>> amap=null;
    	
		try {
			amap = om.readValue(_attachmentMap, new TypeReference<Map<String, LinkedHashSet<String>>>(){});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	return amap;
    }
    
    public void setAttachementMap(Map<String,LinkedHashSet<String>> amap){
    	ObjectMapper om = new ObjectMapper();
    	_attachmentMap=null;
    	try {
			_attachmentMap=om.writeValueAsString(amap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    }
    */
    

    public Unit () {}
}
