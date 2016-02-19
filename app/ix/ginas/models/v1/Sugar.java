package ix.ginas.models.v1;

import ix.core.models.BeanViews;
import ix.ginas.models.GinasCommonSubData;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@SuppressWarnings("serial")
@Entity
@Table(name="ix_ginas_sugar")
public class Sugar extends GinasCommonSubData {
	
	String sugar;

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
    
   
    public void setSitesShorthand(String sites){
    	if(siteContainer==null){
    		siteContainer=new SiteContainer(this.getClass().getName());
    	}
    	siteContainer.setShorthand(sites);
    }

	public String getSugar() {
		return sugar;
	}

	public void setSugar(String sugar) {
		this.sugar = sugar;
	}
	
	// @JsonView(BeanViews.Internal.class)
	public String getSitesShorthand(){
		if(siteContainer!=null){
    		return siteContainer.getShorthand();
    	}
    	return "";
	}

	/*public void setFromMap(Map m) {
		super.setFromMap(m);
		sites = toDataHolderList(
				(List<Map>) m.get("sites"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.NASite>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.NASite make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.NASite();
					}
				});
		sugar = (java.lang.String) (m.get("sugar"));
	}

	@Override
	public Map addAttributes(Map m) {
		super.addAttributes(m);

		m.put("sites", toMapList(sites));
		m.put("sugar", sugar);
		return m;
	}*/

}
