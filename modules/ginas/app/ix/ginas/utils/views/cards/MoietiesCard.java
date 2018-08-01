package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Moiety;
import play.twirl.api.Html;

public class MoietiesCard extends CollectionDetailCard<Moiety>{

	ChemicalSubstance cs;
	
	public MoietiesCard(ChemicalSubstance cs){
		super(cs.moieties, "Moieties", "moieties");		
		this.cs=cs;
		//TODO: Find out why we need to do this
		this.c=cs.moieties;
		
		
	}

	@Override
	public boolean isVisble() {
		return super.isVisble();
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.moieties.render(cs);
	}
	
	

}
