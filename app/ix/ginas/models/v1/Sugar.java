package ix.ginas.models.v1;

import ix.ginas.models.GinasCommonSubData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Sugar extends GinasCommonSubData {
	
	String sugar;

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
    		siteContainer=new SiteContainer(this.getClass().getName());
    	}
    	siteContainer.setSites(sites);
    }

	public String getSugar() {
		return sugar;
	}

	public void setSugar(String sugar) {
		this.sugar = sugar;
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
