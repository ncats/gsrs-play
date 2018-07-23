package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class PropertiesCard extends CollectionDetailCard<Property>{
	Substance s;
	
	public PropertiesCard(Substance s){
		super(s.properties,"Characteristic Attributes","properties");
		this.s=s;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.properties.render(asList(), s.uuid.toString());
	}
}
