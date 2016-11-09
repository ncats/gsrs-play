package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.SingleParent;
import ix.core.models.VIntArray;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.serialization.IntArrayDeserializer;
import ix.ginas.models.serialization.IntArraySerializer;

/**
 * A Chemical Structual Fragment of a {@link Polymer}, typically
 * it is the repeated portion, aka a Structural Repeat Unit (SRU). {@link Unit}s
 * but may only be a fragment of an SRU that doesn't repeat.
 */
@Entity
@Table(name="ix_ginas_unit")
@SingleParent
public class Unit extends GinasCommonSubData {
	@ManyToOne(cascade = CascadeType.PERSIST)
	private Polymer owner;
	
	
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
    public String structure;    //TODO: should be changed to be a structure
    
    public String type;
    
    @Lob
    @Column(name="attachmentMap")
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
    
    
    
    @JsonIgnore
    //TODO:Make this inspect the structure itself
    public List<String> getContainedConnections(){
    	//System.err.println("WARNING: SRU structure not validated to check for connection points");
    	List<String> contained=new ArrayList<String>();
    	
    	Pattern p = Pattern.compile("_(R[0-9][0-9]*)");
    	Matcher m = p.matcher(this.structure);

    	//System.out.println(this.structure);
    	while (m.find()) {
    		String rg=m.group(1);
    	    contained.add(rg);
    	   // System.out.println("Found contained:" + rg);
    	}
    	
    	return contained;
    }
    
    @JsonIgnore
    public List<String> getMentionedConnections(){
    	Map<String,LinkedHashSet<String>> mymap=this.getAttachmentMap();
    	List<String> conset=new ArrayList<String>();
		if(mymap!=null){
			for(String k:mymap.keySet()){
				conset.add(k);
				System.out.println("Found mentioned:" + k);
			}
		}
		return conset;
    }
    
    public void addConnection(String rgroup1, String rgroup2){
    	Map<String,LinkedHashSet<String>> amap=this.getAttachmentMap();
    	if(amap==null){
    		amap=new HashMap<String,LinkedHashSet<String>>();
    	}
    	LinkedHashSet<String> set1=amap.get(rgroup1);
    	if(set1==null){
    		set1=new LinkedHashSet<String>();
    		amap.put(rgroup1, set1);
    	}
    	set1.add(rgroup2);
    	setAttachmentMap(amap);
    }
    
    
    @JsonIgnore
    public Polymer getPolymer(){
        return this.owner;
    }
    

    public Unit () {}
}
