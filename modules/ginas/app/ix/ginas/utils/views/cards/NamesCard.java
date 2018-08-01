package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class NamesCard extends CollectionDetailCard<Name>{
	Substance s;
	
	public NamesCard(Substance s){
		super(s.getAllNames(),"Names","names");
		this.s=s;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.names.render(asList(), s);
	}
}
