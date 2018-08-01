package ix.ginas.utils.views.cards;

import java.util.stream.Collectors;

import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class PrimaryDefinitionCard extends AngularCardCard{
	Substance s;
	
	public PrimaryDefinitionCard(Substance s){
		super(0,"Primary Definition","primary-def");
		this.s=s;
		
	}

	@Override
	public boolean isVisble() {
		return s.getPrimaryDefinitionReference()!=null;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.primarydefinition.render(s.getPrimaryDefinitionReference());
	}
	

}
