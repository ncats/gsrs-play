package ix.ginas.models.v1;


import ix.ginas.models.GinasCommonSubData;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Linkage extends GinasCommonSubData {
	String linkage;
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

	public String getLinkage() {
		return linkage;
	}

	public void setLinkage(String linkage) {
		this.linkage = linkage;
	}

/*
	public void setFromMap(Map m) {
		super.setFromMap(m);
		linkage = (java.lang.String) (m.get("linkage"));
		sites = toDataHolderList(
				(List<Map>) m.get("sites"),
				new DataHolderFactory<gov.nih.ncats.informatics.ginas.shared.model.v1.NASite>() {
					@Override
					public gov.nih.ncats.informatics.ginas.shared.model.v1.NASite make() {
						return new gov.nih.ncats.informatics.ginas.shared.model.v1.NASite();
					}
				});
	}

	@Override
	public Map addAttributes(Map m) {
		super.addAttributes(m);

		m.put("linkage", linkage);
		m.put("sites", toMapList(sites));
		return m;
	}*/

}
