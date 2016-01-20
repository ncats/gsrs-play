package ix.ginas.models.v1;


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
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.models.Indexable;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;


@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_glycosylation")
@JSONEntity(name = "glycosylation", title = "Glycosylation", isFinal = true)
public class Glycosylation extends GinasCommonSubData {
    
    @JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
    SiteContainer _CGlycosylationSiteContainer;
    public List<Site> getCGlycosylationSites(){
    	if(_CGlycosylationSiteContainer!=null){
    		return _CGlycosylationSiteContainer.getSites();
    	}
    	return new ArrayList<Site>();
    }
    @JsonProperty("CGlycosylationSites")
    public void setCGlycosylationSites(List<Site> sites){
    	if(_CGlycosylationSiteContainer==null){
    		_CGlycosylationSiteContainer=new SiteContainer(this.getClass().getName());
    	}
    	_CGlycosylationSiteContainer.setSites(sites);
    }
    
    @JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
    SiteContainer _NGlycosylationSiteContainer;
    public List<Site> getNGlycosylationSites(){
    	if(_NGlycosylationSiteContainer!=null){
    		return _NGlycosylationSiteContainer.getSites();
    	}
    	return new ArrayList<Site>();
    }
    
    @JsonProperty("NGlycosylationSites")
    public void setNGlycosylationSites(List<Site> sites){
    	if(_NGlycosylationSiteContainer==null){
    		_NGlycosylationSiteContainer=new SiteContainer(this.getClass().getName());
    		
    	}
    	_NGlycosylationSiteContainer.setSites(sites);
    }
    
    @JsonIgnore
	@OneToOne(cascade=CascadeType.ALL)
    SiteContainer _OGlycosylationSiteContainer;
    public List<Site> getOGlycosylationSites(){
    	if(_OGlycosylationSiteContainer!=null){
    		return _OGlycosylationSiteContainer.getSites();
    	}
    	return new ArrayList<Site>();
    }
    @JsonProperty("OGlycosylationSites")
    public void setOGlycosylationSites(List<Site> sites){
    	if(_OGlycosylationSiteContainer==null){
    		_OGlycosylationSiteContainer=new SiteContainer(this.getClass().getName());
    	}
    	_OGlycosylationSiteContainer.setSites(sites);
    }
    
    @Indexable(facet=true,name="Glycosylation Type")
    public String glycosylationType;

    public Glycosylation () {}

}
