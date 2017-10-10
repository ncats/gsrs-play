package ix.ginas.models.v1;


import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import play.Logger;

@JSONEntity(title = "Nucleic Acid", isFinal = true)
@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_nucleicacid")
public class NucleicAcid extends GinasCommonSubData {
	
	@JSONEntity(title = "Linkages")
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	List<Linkage> linkages;
	
	
	@JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
	Modifications modifications;
	
	@JSONEntity(title = "Nucleic Acid Type", format = JSONConstants.CV_NUCLEIC_ACID_TYPE)
	String nucleicAcidType;
	
	@JSONEntity(title = "Nucleic Acid Subtypes", isUniqueItems = true, format = "table", itemsTitle = "Subtype", itemsFormat = JSONConstants.CV_NUCLEIC_ACID_SUBTYPE)
	@JsonIgnore
	
	private String nucleicAcidSubType;
	
	
    @Indexable(facet=true,name="Sequence Origin")
    String sequenceOrigin;
    
    @Indexable(facet=true,name="Sequence Type")
    String sequenceType;
	
	@JSONEntity(name = "subunits", title = "Subunits")
	@ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_nucleicacid_subunits")
	public List<Subunit> subunits;
	
	@JSONEntity(title = "Sugars", isRequired = true)
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    //@JoinTable(name="ix_ginas_nucleicacid_sugar")
	List<Sugar> sugars;

	public List<Linkage> getLinkages() {
		return linkages;
	}

	public void setLinkages(List<Linkage> linkages) {
		this.linkages = linkages;
	}

	
	@JsonIgnore
	public Modifications getModifications() {
		return modifications;
	}

	public void setModifications(Modifications modifications) {
		this.modifications = modifications;
	}

	public String getNucleicAcidType() {
		return nucleicAcidType;
	}

	public void setNucleicAcidType(String nucleicAcidType) {
		this.nucleicAcidType = nucleicAcidType;
	}

	public List<String> getNucleicAcidSubType() {
		if( this.nucleicAcidSubType != null){
			String[] type = this.nucleicAcidSubType.split(";");
			return Arrays.asList(type);
		}else {
			return null;
		}
	}

	@Indexable(facet=true,name="Nucleic Acid Subtype")
	public void setNucleicAcidSubType(List<String> nucleicAcidSubType) {
		StringBuilder sb = new StringBuilder();
		if(nucleicAcidSubType!=null){
			for(String s:nucleicAcidSubType){
				if(sb.length()>0){
					sb.append(";");
				}
				sb.append(s);
				
			}
		}
		this.nucleicAcidSubType = sb.toString();
	}

	public String getSequenceOrigin() {
		return sequenceOrigin;
	}

	public void setSequenceOrigin(String sequenceOrigin) {
		this.sequenceOrigin = sequenceOrigin;
	}

	public String getSequenceType() {
		return sequenceType;
	}

	public void setSequenceType(String sequenceType) {
		this.sequenceType = sequenceType;
	}

	public List<Subunit> getSubunits() {
		return subunits;
	}

	public void setSubunits(List<Subunit> subunits) {
		this.subunits = subunits;
	}

	public List<Sugar> getSugars() {
		return sugars;
	}

	public void setSugars(List<Sugar> sugars) {
		this.sugars = sugars;
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
    	if(this.sugars!=null){
    		int sugIndex=1;
	    	for(Sugar sug : this.sugars){
	    		if(sug.getSites()!=null){
	    			for(Site s: sug.getSites()){
	    				_modifiedCache.put(s.toString(),"sugar-" + sugIndex);
	    	    	}
	    		}
	    		sugIndex++;
	    	}
    	}
    	if(this.linkages!=null){
    		int linkIndex=1;
	    	for(Linkage sug : this.linkages){
	    		if(sug.getSites()!=null){
	    			for(Site s: sug.getSites()){
	    				_modifiedCache.put(s.toString(),"linkage-" + linkIndex);
	    	    	}
	    		}
	    		linkIndex++;
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
