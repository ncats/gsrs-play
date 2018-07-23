package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class StructureCard extends AngularCardCard{

	ChemicalSubstance s;
	public StructureCard(ChemicalSubstance s){
		super(0,"Structure","structure");
		this.s=s;
	}


	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.structure.render(s);
	}
	
	
}
