package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class ClassificationsCard extends CollectionDetailCard<Code>{
	Substance s;
	
	public ClassificationsCard(Substance s){
		super(s.getClassifications(),"Classification","classification");
		this.s=s;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.classifications.render(c, s);
	}
}
