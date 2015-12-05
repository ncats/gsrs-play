package ix.ginas.models.v1;


import ix.ginas.models.GinasSubData;

import java.util.List;

public class Linkage extends GinasSubData {
	String linkage;
	List<Site> sites;

	public String getLinkage() {
		return linkage;
	}

	public void setLinkage(String linkage) {
		this.linkage = linkage;
	}

	public List<Site> getSites() {
		return sites;
	}

	public void setSites(List<Site> sites) {
		this.sites = sites;
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
