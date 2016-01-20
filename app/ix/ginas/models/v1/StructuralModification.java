package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.utils.JSONEntity;
import ix.ginas.models.GinasCommonSubData;

@Entity
@Table(name="ix_ginas_structuralmod")
@JSONEntity(title = "Structural Modification", isFinal = true)
public class StructuralModification extends GinasCommonSubData {
    @JSONEntity(title = "Modification Type", isRequired = true)
    @Column(nullable=false)
    public String structuralModificationType;
    
    @JSONEntity(title = "Modification Location Type")
    public String locationType;
    
    @JSONEntity(title = "Residue Modified")
    public String residueModified;
    
    @JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
    SiteContainer siteContainer;
    public List<Site> getSites(){
    	System.out.println(siteContainer);
    	if(siteContainer!=null){
    		return siteContainer.getSites();
    	}
    	return new ArrayList<Site>();
    }
    
    public void setSites(List<Site> sites){
    	if(siteContainer==null){
    		siteContainer=new SiteContainer();
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
    String moleculareFragmentRole;
    
    @JSONEntity(title = "Modification Group")
        String modificationGroup = "1";
    public StructuralModification () {}
}
