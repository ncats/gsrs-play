package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class VariantConceptsCard extends CollectionDetailCard<SubstanceReference>{
	Substance s;
	
	public VariantConceptsCard(Substance s){
		super(s.getChildConceptReferences(),"Variant Concepts","variants");
		this.s=s;
	}
	
	
	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.variantconcepts.render(s);
	}
	
	

}
