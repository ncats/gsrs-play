package ix.ginas.models.v1;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;

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
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_disulfide")
    public List<DisulfideLink> disulfideLinks = new ArrayList<DisulfideLink>();

    @OneToOne(cascade=CascadeType.ALL)
    public Glycosylation glycosylation;

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
    	for(DisulfideLink dsl: this.disulfideLinks){
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
