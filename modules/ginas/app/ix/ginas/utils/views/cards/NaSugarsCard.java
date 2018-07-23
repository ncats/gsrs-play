package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.Sugar;
import play.twirl.api.Html;

public class NaSugarsCard extends CollectionDetailCard<Sugar>{

	NucleicAcidSubstance ns;
	
	public NaSugarsCard(NucleicAcidSubstance ns){
		super(ns.nucleicAcid.getSugars(), "Sugars", "sugars");
		this.ns=ns;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.nasugars.render(ns);
		
	}
	
	

}
