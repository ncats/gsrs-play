package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.OtherLinks;
import ix.ginas.models.v1.ProteinSubstance;
import play.twirl.api.Html;

public class OtherLinksCard extends CollectionDetailCard<OtherLinks>{

	ProteinSubstance ps;
	
	public OtherLinksCard(ProteinSubstance ps){
		super(ps.protein.otherLinks, "Other Links", "olinks");
		this.ps=ps;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.otherlinks.render(ps);
	}
	
	

}
