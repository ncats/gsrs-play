package ix.ginas.utils.views.cards;

import java.util.ArrayList;
import java.util.Optional;

import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class ModificationsCard extends CollectionDetailCard<GinasCommonSubData>{
	Substance s;
	
	public ModificationsCard(Substance s){
		super(Optional.ofNullable(s.getModifications())
				.map(m->m.allModifications())
				.orElse(new ArrayList<GinasCommonSubData>())
				,"Modifications","modifications");
		this.s=s;
	}

	@Override
	public boolean isVisble() {
		return s.hasModifications();
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.modifications.render(s);
	}
}
