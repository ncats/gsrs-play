package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Subunit;
import play.twirl.api.Html;

public class SubunitsCard extends CollectionDetailCard<Subunit>{

	Substance ps;
	
	public SubunitsCard(ProteinSubstance ps){
		super(ps.protein.getSubunits(), "Subunits", "subunits");
		this.ps=ps;
	}
	public SubunitsCard(NucleicAcidSubstance ns){
		super(ns.nucleicAcid.getSubunits(), "Subunits", "subunits");
		this.ps=ns;
	}

	@Override
	public Html innerContent() {
		if(ps instanceof ProteinSubstance){
			return ix.ginas.views.html.details.properties.subunits.render(ps, asList(), "protein");
		}else{
			return ix.ginas.views.html.details.properties.subunits.render(ps, asList(), "nucleicAcid");
		}
	}
	
	

}
