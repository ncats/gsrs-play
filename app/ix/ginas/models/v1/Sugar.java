package ix.ginas.models.v1;

import ix.ginas.models.Ginas;

import java.util.List;
import java.util.Map;

public class Sugar extends Ginas {
	List<Site> sites;
	String sugar;

	public List<Site> getSites() {
		return sites;
	}

	public void setSites(List<Site> sites) {
		this.sites = sites;
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
