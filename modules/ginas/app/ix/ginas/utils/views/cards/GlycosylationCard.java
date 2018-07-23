package ix.ginas.utils.views.cards;

import java.util.ArrayList;
import java.util.Optional;

import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Site;
import play.twirl.api.Html;

public class GlycosylationCard extends CollectionDetailCard<Site>{

	ProteinSubstance ps;
	
	public GlycosylationCard(ProteinSubstance ps){
		super(Optional.ofNullable(ps.protein.glycosylation)
				.map(g->g.getAllSites())
				.orElse(new ArrayList<>())
				, "Glycosylation", "glyc");
		this.ps=ps;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.glycosylation.render(ps);
	}
	
	

}
