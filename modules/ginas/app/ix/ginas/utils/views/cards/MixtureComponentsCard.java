package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Component;
import ix.ginas.models.v1.MixtureSubstance;
import play.twirl.api.Html;

public class MixtureComponentsCard extends CollectionDetailCard<Component>{

	MixtureSubstance ms;
	
	public MixtureComponentsCard(MixtureSubstance ms){
		super(ms.mixture.components, "Mixture Components", "components");		
		this.ms=ms;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.components.render(asList());
	}
	
	

}
