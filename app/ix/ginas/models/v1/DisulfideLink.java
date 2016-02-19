package ix.ginas.models.v1;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.ginas.models.GinasCommonSubData;


@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_disulfide")
public class DisulfideLink extends GinasCommonSubData {
//    @ManyToMany(cascade=CascadeType.ALL)
//    @JoinTable(name="ix_ginas_disulfide_site")
//    public List<Site> sites = new ArrayList<Site>();
	
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
    
    
    public DisulfideLink () {}
}
