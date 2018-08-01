package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.MixtureSubstance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class MixtureSourceCard extends AngularCardCard{
	MixtureSubstance s;
	
	public MixtureSourceCard(MixtureSubstance s){
		super(0, "Source Material", "mat");
		this.s=s;
		
	}

	@Override
	public boolean isVisble() {
		return s.mixture.getParentSubstance()!=null;
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.subref.render(s.mixture.getParentSubstance());
	}
	

}
