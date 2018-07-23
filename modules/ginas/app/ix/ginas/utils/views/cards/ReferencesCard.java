package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class ReferencesCard extends CollectionDetailCard<Reference>{
	Substance s;
	
	public ReferencesCard(Substance s){
		super(s.references,"References","references");
		this.s=s;
		
	}

	@Override
	public boolean isVisble() {
		return s.references !=null && !s.references.isEmpty();
	}

	@Override
	public Html innerContent() {
//		String ref="<referencesmanager reftable substance =\"'" + s.uuid.toString() + "'\"></referencesmanager>";
//		return new Html(ref);
		return  ix.ginas.views.html.details.properties.references.render(s.references);

	}
}
