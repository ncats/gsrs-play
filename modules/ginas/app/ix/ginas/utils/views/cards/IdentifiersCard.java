package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class IdentifiersCard extends CollectionDetailCard<Code>{
	Substance s;
	
	public IdentifiersCard(Substance s){
		
		super(s.getIdentifiers(),"Identifiers","identifiers");
		
		this.s=s;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.identifiercodes.render(c, s.uuid.toString());
	}
}
