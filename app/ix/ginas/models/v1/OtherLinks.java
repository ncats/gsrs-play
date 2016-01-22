package ix.ginas.models.v1;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;


@Entity
@Table(name="ix_ginas_otherlinks")
public class OtherLinks extends GinasCommonSubData {
   
    @Indexable(facet=true,name="Linkage Type")
    public String linkageType;
    
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

    public OtherLinks () {}
}
