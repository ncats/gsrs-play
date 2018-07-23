package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.DisulfideLink;
import ix.ginas.models.v1.ProteinSubstance;
import play.twirl.api.Html;

public class DisulfidesCard extends CollectionDetailCard<DisulfideLink>{

	ProteinSubstance ps;
	
	public DisulfidesCard(ProteinSubstance ps){
		super(ps.protein.getDisulfideLinks(), "Disulfide Links", "disulfides");
		this.ps=ps;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.disulfides.render(ps);
	}
	
	

}
