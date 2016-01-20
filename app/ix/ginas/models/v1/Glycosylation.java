package ix.ginas.models.v1;


import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;


@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_glycosylation")
@JSONEntity(name = "glycosylation", title = "Glycosylation", isFinal = true)
public class Glycosylation extends GinasCommonSubData {
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_glyco_csite",
               joinColumns=@JoinColumn
               (name="ix_ginas_glyco_c_uuid",
                referencedColumnName="uuid"))
    public List<Site> CGlycosylationSites = new ArrayList<Site>();
    
    @JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
    SiteContainer getCGlycosylationSiteContainer;
    public List<Site> getCGlycosylationSites(){
    	System.out.println(getCGlycosylationSiteContainer);
    	if(getCGlycosylationSiteContainer!=null){
    		return getCGlycosylationSiteContainer.getSites();
    	}
    	return new ArrayList<Site>();
    }
    public void setCGlycosylationSites(List<Site> sites){
    	if(getCGlycosylationSiteContainer==null){
    		getCGlycosylationSiteContainer=new SiteContainer();
    	}
    	getCGlycosylationSiteContainer.setSites(sites);
    }
    
    @JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
    SiteContainer getNGlycosylationSiteContainer;
    public List<Site> getNGlycosylationSites(){
    	System.out.println(getNGlycosylationSiteContainer);
    	if(getNGlycosylationSiteContainer!=null){
    		return getNGlycosylationSiteContainer.getSites();
    	}
    	return new ArrayList<Site>();
    }
    public void setNGlycosylationSites(List<Site> sites){
    	if(getNGlycosylationSiteContainer==null){
    		getNGlycosylationSiteContainer=new SiteContainer();
    	}
    	getNGlycosylationSiteContainer.setSites(sites);
    }
    
    @JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
    SiteContainer getOGlycosylationSiteContainer;
    public List<Site> getOGlycosylationSites(){
    	System.out.println(getOGlycosylationSiteContainer);
    	if(getOGlycosylationSiteContainer!=null){
    		return getOGlycosylationSiteContainer.getSites();
    	}
    	return new ArrayList<Site>();
    }
    public void setOGlycosylationSites(List<Site> sites){
    	if(getOGlycosylationSiteContainer==null){
    		getOGlycosylationSiteContainer=new SiteContainer();
    	}
    	getOGlycosylationSiteContainer.setSites(sites);
    }
    
    @Indexable(facet=true,name="Glycosylation Type")
    public String glycosylationType;

    public Glycosylation () {}

}
