package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Linkage;
import ix.ginas.models.v1.NucleicAcidSubstance;
import play.twirl.api.Html;

public class NaLinkagesCard extends CollectionDetailCard<Linkage>{

	NucleicAcidSubstance ns;
	
	public NaLinkagesCard(NucleicAcidSubstance ns){
		super(ns.nucleicAcid.getLinkages(), "Linkages", "linkage");
		this.ns=ns;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.nalinkages.render(ns);
		
	}
}
