package ix.ginas.models.v1;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.models.Role;
import ix.ginas.models.GinasCommonSubData;
import play.Logger;

@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_protein")
public class Protein extends GinasCommonSubData {
    @Indexable(facet=true,name="Protein Type")
    public String proteinType;
    
    @Indexable(facet=true,name="Protein Subtype")
    public String proteinSubType;
    
    @Indexable(facet=true,name="Sequence Origin")
    public String sequenceOrigin;
    
    @Indexable(facet=true,name="Sequence Type")
    public String sequenceType;
    
    @Lob
    @JsonIgnore
    public String disulfJSON=null;
    
    
    
//    @ManyToMany(cascade=CascadeType.ALL)
//    @JoinTable(name="ix_ginas_protein_disulfide")
//    public List<DisulfideLink> disulfideLinks = new ArrayList<DisulfideLink>();
//    
//    public List<DisulfideLink> getDisulfideLinks(){
//    	
//    	return this.disulfideLinks;
//    }
    @Transient
    List<DisulfideLink> tmpDisulfides=null;
    
    public List<DisulfideLink> getDisulfideLinks(){
    	if(tmpDisulfides!=null)return tmpDisulfides;
    	List<DisulfideLink> rolekinds=new ArrayList<DisulfideLink>();
    	if(this.disulfJSON!=null){
    		try{
	    		ObjectMapper om = new ObjectMapper();
	    		List l=om.readValue(disulfJSON, List.class);
	    		for(Object o:l){
	    			try{
	    				rolekinds.add(om.treeToValue(om.valueToTree(o), DisulfideLink.class));
	    			}catch(Exception e){
	    				e.printStackTrace();
	    			}
	    		}
    		}catch(Exception e){
    			Logger.trace("Error parsing disulfides", e);
    		}
    		
    	}
    	tmpDisulfides=rolekinds;
        return tmpDisulfides;
    }
    public void setDisulfideLinks(List<DisulfideLink> rolekinds){
    	ObjectMapper om = new ObjectMapper();
    	disulfJSON=om.valueToTree(rolekinds).toString();
    	tmpDisulfides=null;
    }

    @OneToOne(cascade=CascadeType.ALL)
    public Glycosylation glycosylation;

    @JsonView(BeanViews.Internal.class)
    @OneToOne(cascade=CascadeType.ALL)
    public Modifications modifications;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_subunit")
    public List<Subunit> subunits = new ArrayList<Subunit>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_otherlinks")
    public List<OtherLinks> otherLinks = new ArrayList<OtherLinks>();

//    @ManyToMany(cascade=CascadeType.ALL)
//    @JoinTable(name="ix_ginas_protein_reference")
//    @JsonSerialize(using=ReferenceListSerializer.class)
//    @JsonDeserialize(using=ReferenceListDeserializer.class)
//    public List<Value> references = new ArrayList<Value>();

    public Protein () {}

    
    public void setModifications(Modifications mod){
    	if(mod==null){
    		return;
    	}
    	this.modifications=mod;
    }
    
    @JsonIgnore
    @Transient
    private Map<String, String> _modifiedCache=null;
  
    @JsonIgnore
    public Map<String, String> getModifiedSites(){
    	if(_modifiedCache!=null){
    		return _modifiedCache;
    	}
    	
    	_modifiedCache =  new HashMap<String,String>();
    	//disulfides
    	for(DisulfideLink dsl: this.getDisulfideLinks()){
    		for(Site s:dsl.getSites()){
    			_modifiedCache.put(s.toString(),"disulfide");
    		}
    	}
    	//glycosylation
    	if(this.glycosylation!=null){
	    	for(Site s: this.glycosylation.getNGlycosylationSites()){
	    		_modifiedCache.put(s.toString(),"nglycosylation");
	    	}
	    	for(Site s: this.glycosylation.getOGlycosylationSites()){
				_modifiedCache.put(s.toString(),"oglycosylation");
	    	}
	    	for(Site s: this.glycosylation.getCGlycosylationSites()){
				_modifiedCache.put(s.toString(),"cglycosylation");
	    	}    	
    	}
    	if(modifications!=null){
    		//modifications
	    	for(StructuralModification sm : this.modifications.structuralModifications){
	    		if(sm.getSites()!=null){
	    			for(Site s: sm.getSites()){
	    				_modifiedCache.put(s.toString(),"structuralModification");
	    	    	}
	    		}
	    	}
    	}
    	if(this.otherLinks!=null){
    		//modifications
	    	for(OtherLinks sm : this.otherLinks){
	    		if(sm.getSites()!=null){
	    			for(Site s: sm.getSites()){
	    				_modifiedCache.put(s.toString(),"otherLinkage");
	    	    	}
	    		}
	    	}
    	}
    	return _modifiedCache;
    }
    
    @Override
    public void update(){
    	
    	System.out.println((new ObjectMapper()).valueToTree(subunits));
    	super.update();
    	System.out.println((new ObjectMapper()).valueToTree(subunits));
    }
    
    
    /**
     * Returns a string to describe any modification that happens at the specified 
     * site. Returns null if there is no modification.
     * @param subunitIndex
     * @param residueIndex
     * @return
     */
    public String getSiteModificationIfExists(int subunitIndex, int residueIndex){
    	return getModifiedSites().get(subunitIndex + "_" + residueIndex);    	
    }
    
}
