package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Component;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.SpecifiedSubstanceComponent;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import play.twirl.api.Html;

public class Group1SpecifiedSubstanceComponentsCard extends CollectionDetailCard<SpecifiedSubstanceComponent>{

	SpecifiedSubstanceGroup1Substance ms;
	
	public Group1SpecifiedSubstanceComponentsCard(SpecifiedSubstanceGroup1Substance ms){
		super(ms.specifiedSubstance.constituents, "Specified Substance Constituents", "constituents");		
		this.ms=ms;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.specsubcomponents.render(asList());
	}
	
	

}
