package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.PolymerSubstance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class PolymerStructureCard extends AngularCardCard{

	PolymerSubstance s;
	public PolymerStructureCard(PolymerSubstance s){
		super(0,"Display Structure","dstructure");
		this.s=s;
	}


	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.polymerstructure.render(s);
	}
	
	
}
