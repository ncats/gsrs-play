package ix.ginas.models.v1;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.models.BeanViews;
import ix.core.models.Indexable;
import ix.core.util.ModelUtils;
import ix.ginas.models.GinasCommonSubData;
import ix.utils.Global;
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
    @Indexable(indexed=false)
    public String disulfJSON=null;
    
    @OneToOne(mappedBy="protein")
    private ProteinSubstance proteinSubstance;
    

	@Transient
	protected transient ObjectMapper mapper = new ObjectMapper();
    
    @Transient
    List<DisulfideLink> tmpDisulfides=null;
    
    @JsonView(BeanViews.Full.class)
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
	    				System.err.println(e.getMessage());
	    				Logger.trace("Error parsing disulfides", e);
	    			}
	    		}
    		}catch(Exception e){
    			Logger.trace("Error parsing disulfides", e);
    		}
    		
    	}
    	tmpDisulfides=rolekinds;
        return tmpDisulfides;
    }
    
    @JsonView(BeanViews.Compact.class)
	@JsonProperty("_disulfideLinks")
	public JsonNode getJsonDisulfideLinks() {
		JsonNode node = null;
		List<DisulfideLink> links=this.getDisulfideLinks();
		if (links.size()>0) {
			try {
				ObjectNode n = mapper.createObjectNode();
				n.put("count", links.size());
				n.put("href", Global.getRef(proteinSubstance.getClass(), proteinSubstance.getUuid()) + "/protein/disulfideLinks");
				
				n.put("shorthand", ModelUtils.shorthandNotationForLinks(links));
				
				node = n;
			} catch (Exception ex) {
				ex.printStackTrace();
				node = mapper.valueToTree(links);
			}
		}
		return node;
	}
    
    @JsonView(BeanViews.Compact.class)
	@JsonProperty("_glycosylation")
	public JsonNode getJsonGlycosylation() {
		JsonNode node = null;
		Glycosylation glyc=this.glycosylation;
		if (glyc!=null) {
			try {
				ObjectNode n = mapper.createObjectNode();
				if(glyc.glycosylationType!=null){
					n.put("type",   glyc.glycosylationType);
				}
				n.put("nsites", glyc._NGlycosylationSiteContainer.siteCount);
				n.put("osites", glyc._OGlycosylationSiteContainer.siteCount);
				n.put("csites", glyc._CGlycosylationSiteContainer.siteCount);
				n.put("href", Global.getRef(proteinSubstance.getClass(), proteinSubstance.getUuid()) + "/protein/glycosylation");
				node = n;
			} catch (Exception ex) {
				ex.printStackTrace();
				node = mapper.valueToTree(glyc);
			}
		}
		return node;
	}
    
    
    public void setDisulfideLinks(List<DisulfideLink> links){
    	System.out.println("Setting disulf links" + links.size());
    	ObjectMapper om = new ObjectMapper();
    	disulfJSON=om.valueToTree(links).toString();
    	tmpDisulfides=null;
    }
    
    
    @JsonView(BeanViews.Full.class)
    @OneToOne(cascade=CascadeType.ALL)
    public Glycosylation glycosylation;

    @JsonView(BeanViews.Internal.class)
    @OneToOne(cascade=CascadeType.ALL)
    public Modifications modifications;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_protein_subunit")
    @OrderBy("subunitIndex asc")
    public List<Subunit> subunits = new ArrayList<Subunit>();
    
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<OtherLinks> otherLinks = new ArrayList<OtherLinks>();


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
    
//    @PostLoad
//    public void sortSubunits(){
////    	System.out.println("called" + this.uuid);
//    	int i = (int)(Math.random() * 1000);
//    	System.out.println("starting");
//    	System.out.println("start sort" + this.uuid + " " + subunits.size() + " " + i);
////    	List<Subunit> mysubunits=this.subunits;
////    	Collections.sort(mysubunits, new Comparator<Subunit>(){
////			@Override
////			public int compare(Subunit o1, Subunit o2) {
////				return o1.subunitIndex-o2.subunitIndex;
////			}
////    	});
////    	System.out.println("end sort" + this.uuid + " " + subunits.size() + " " + i);
//    }
    
    public List<Subunit> getSubunits(){
    	Collections.sort(subunits, new Comparator<Subunit>(){
			@Override
			public int compare(Subunit o1, Subunit o2) {
				return o1.subunitIndex-o2.subunitIndex;
			}
    	});
    	return this.subunits;
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
    
    /**
     * Get the residue string at the specified site. Returns null if it does not exist.
     * @param site
     * @return
     */
    public String getResidueAt(Site site){
    	Integer i=site.subunitIndex;
    	Integer j=site.residueIndex;
    	
    	try{
	    	for(Subunit su: this.subunits){
	    		if(su.subunitIndex.equals(i)){
	    			if(j-1>=su.sequence.length()|| j-1<0)return null;
	    			char res=su.sequence.charAt(j-1);
	    			return res + "";
	    		}
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return null;
    }
    
}
