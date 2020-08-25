package ix.ginas.models.v1;


import ix.core.models.Indexable;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

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
	Modifications modifications = new Modifications();
	
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
	@OrderBy("subunitIndex asc")
	public List<Subunit> subunits = new ArrayList<>();
	
	@JSONEntity(title = "Sugars", isRequired = true)
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    //@JoinTable(name="ix_ginas_nucleicacid_sugar")
	List<Sugar> sugars = new ArrayList<>();

	@Indexable
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
	@Indexable
	public String getNucleicAcidType() {
		return nucleicAcidType;
	}

	public void setNucleicAcidType(String nucleicAcidType) {
		this.nucleicAcidType = nucleicAcidType;
	}


	@JsonProperty("nucleicAcidSubType")
	@Indexable(facet=true,name="Nucleic Acid Subtype")
	public List<String> getNucleicAcidSubType() {
		if( this.nucleicAcidSubType != null && this.nucleicAcidSubType.length() > 0){
			String[] type = this.nucleicAcidSubType.split(";");
			return new ArrayList<String>(Arrays.asList(type));
		}else {
			return new ArrayList<String>();
		}
	}

	@JsonProperty("nucleicAcidSubType")
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

	@Indexable
	public String getSequenceOrigin() {
		return sequenceOrigin;
	}

	public void setSequenceOrigin(String sequenceOrigin) {
		this.sequenceOrigin = sequenceOrigin;
	}

	@Indexable
	public String getSequenceType() {
		return sequenceType;
	}

	public void setSequenceType(String sequenceType) {
		this.sequenceType = sequenceType;
	}
	@Indexable
	public List<Subunit> getSubunits() {
		Collections.sort(subunits, new Comparator<Subunit>() {
			@Override
			public int compare(Subunit o1, Subunit o2) {
				if(o1.subunitIndex ==null){
//					System.out.println("null subunit index");
					if(o2.subunitIndex ==null){
						return Integer.compare(o2.getLength(), o1.getLength());
					}else{
						return 1;
					}
				}
				return o1.subunitIndex - o2.subunitIndex;
			}
		});
		adoptChildSubunits();
		return this.subunits;
	}

	public void setSubunits(List<Subunit> subunits) {
		this.subunits = subunits;
		adoptChildSubunits();
	}


	@Indexable
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

	/**
	 * mark our child subunits as ours.
	 * Mostly used so we know what kind of type
	 * this subunit is by walking up the tree
	 * to inspect its parent (us).
	 */
	@PreUpdate
	@PrePersist
	public void adoptChildSubunits(){
		List<Subunit> subunits=this.subunits;
		for(Subunit s: subunits){
			s.setParent(this);
		}
	}

	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		if(this.linkages!=null){
			for(Linkage l : this.linkages){
				temp.addAll(l.getAllChildrenAndSelfCapableOfHavingReferences());
			}
		}
		if(this.sugars!=null){
			for(Sugar s : this.sugars){
				temp.addAll(s.getAllChildrenAndSelfCapableOfHavingReferences());
			}
		}
		if(this.subunits!=null){
			for(Subunit s : this.subunits){
				temp.addAll(s.getAllChildrenAndSelfCapableOfHavingReferences());
			}
		}
		if(this.modifications!=null){
			temp.addAll(modifications.getAllChildrenAndSelfCapableOfHavingReferences());
		}

		return temp;
	}
}
