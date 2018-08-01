package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.Unit;
import play.twirl.api.Html;

public class SRUsCard extends CollectionDetailCard<Unit>{

	PolymerSubstance ps;
	
	public SRUsCard(PolymerSubstance ps){
		super(ps.polymer.structuralUnits, "Structural Units", "srus");
		this.ps=ps;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.polymersrus.render(ps);
	}
	
	

}
