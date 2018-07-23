package ix.ginas.utils.views.cards;

import java.util.stream.Collectors;

import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class AlternativeDefinitionsCard extends CollectionDetailCard<Relationship>{
	Substance s;
	
	public AlternativeDefinitionsCard(Substance s){
		super(s.getAlternativeDefinitionRelationships(),"Alternative Definitions","altrelationships");
		this.s=s;
		
	}
	
	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.relationships.render(s.getAlternativeDefinitionRelationships(), this.title, s.uuid.toString(),false);
	}
	
	

}
