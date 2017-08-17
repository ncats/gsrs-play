package ix.ginas.models.v1;

import ix.core.SingleParent;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.SingleParent;

@Entity
@Table(name="ix_ginas_structuralmod")
@JSONEntity(title = "Structural Modification", isFinal = true)
@SingleParent
public class StructuralModification extends GinasCommonSubData {
	
	@ManyToOne(cascade = CascadeType.PERSIST)
	private Modifications owner;
	
    @JSONEntity(title = "Modification Type", isRequired = true)
    public String structuralModificationType;
    
    @JSONEntity(title = "Modification Location Type")
    public String locationType;
    
    @JSONEntity(title = "Residue Modified")
    public String residueModified;
    
    @JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
    SiteContainer siteContainer;
    public List<Site> getSites(){
    	if(siteContainer!=null){
    		return siteContainer.getSites();
    	}
    	return new ArrayList<Site>();
    }
    
    public void setSites(List<Site> sites){
    	if(siteContainer==null){
    		siteContainer=new SiteContainer(this.getClass().getName());
    	}
    	siteContainer.setSites(sites);
    }
    
    @JSONEntity(title = "Extent", values = "JSONConstants.ENUM_EXTENT", isRequired = true)
    public String extent;

    @OneToOne(cascade=CascadeType.ALL)
    public Amount extentAmount;
    @OneToOne(cascade=CascadeType.ALL)
    public SubstanceReference molecularFragment;
    
    @JSONEntity(title = "Modified Fragment Role")
    public String moleculareFragmentRole;
    
    @JSONEntity(title = "Modification Group")
    public String modificationGroup = "1";
    
    
    public StructuralModification () {}
}
