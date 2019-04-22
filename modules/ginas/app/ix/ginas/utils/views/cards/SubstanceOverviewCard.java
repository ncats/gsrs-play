package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.PolymerSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * card title: Record Details
 * @author tyler
 *
 */
public class SubstanceOverviewCard extends AngularCardCard{

	Substance s;
	public SubstanceOverviewCard(Substance s){
		//super(0,DetailCard.titleCase(s.substanceClass+"") + " Details","details");
		super(0,"Overview","details");

		this.s=s;
	}


	@Override
	public Html innerContent() {
		if(s instanceof ProteinSubstance){
			return ix.ginas.views.html.details.proteinoverview.render((ProteinSubstance)s);
		}else if(s instanceof NucleicAcidSubstance){
			return ix.ginas.views.html.details.nucleicacidoverview.render((NucleicAcidSubstance)s);
		}else if(s instanceof StructurallyDiverseSubstance){
			return ix.ginas.views.html.details.diverseoverview.render((StructurallyDiverseSubstance)s);
		}else if(s instanceof ChemicalSubstance){
			return ix.ginas.views.html.details.chemicaloverview.render((ChemicalSubstance)s);
		}else if(s instanceof MixtureSubstance){
			return ix.ginas.views.html.details.mixtureoverview.render((MixtureSubstance)s);
		}else if(s instanceof PolymerSubstance){
			return ix.ginas.views.html.details.polymeroverview.render((PolymerSubstance)s);
		}else if(s instanceof SpecifiedSubstanceGroup1Substance){
            return ix.ginas.views.html.details.properties.ssg.ssgmainoverview.render(s);
		}else{
			//all others should be concepts (?)
			//return ix.ginas.views.html.details.overview.render(s);
			return ix.ginas.views.html.details.conceptoverview.render(s);
		}
	}
	
	
}
