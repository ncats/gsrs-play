package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class ConceptDefinitionCard extends AngularCardCard{
	Substance s;
	
	public ConceptDefinitionCard(Substance s){
		super(0,"Concept Definition", "definition");
		this.s=s;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.conceptdefinition.render(s);
	}
	

}
