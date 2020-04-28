package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Material;
import play.twirl.api.Html;

public class MonomersCard extends CollectionDetailCard<Material>{

	PolymerSubstance ps;
	
	public MonomersCard(PolymerSubstance ps){
		super(ps.polymer.monomers, "Monomers", "monomers");
		this.ps=ps;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.polymermonomers.render(ps);
	}
	
	

}
