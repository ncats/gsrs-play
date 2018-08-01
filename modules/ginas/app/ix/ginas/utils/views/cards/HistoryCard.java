package ix.ginas.utils.views.cards;

import ix.core.models.Edit;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class HistoryCard extends CollectionDetailCard<Edit>{
	Substance s;
	
	public HistoryCard(Substance s){
		super(s.getEdits(),"History","history");
		
		this.s=s;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.history.render(s);
	}
}
