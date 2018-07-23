package ix.ginas.utils.views.cards;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class RelationshipsCard extends CollectionDetailCard<Relationship>{
	Substance s;
	boolean full=true;
	
	public RelationshipsCard(Substance s, String title, String anchor, Predicate<Relationship> toKeep){
		this(s
			   ,title
			   ,anchor,
			   s.relationships
			   .stream()
			   .filter(toKeep)
			   .collect(Collectors.toList()));
	}
	
	public RelationshipsCard(Substance s, String title, String anchor, Collection<Relationship> rels){
		super(rels,title,anchor);
		this.s=s;
	}
	
	public RelationshipsCard(Substance s){
		this(s, "Relationships", "relationships", s.getFilteredRelationships());
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.relationships.render(this.asList(), this.title, s.uuid.toString(),full);
	}
	
	
	public RelationshipsCard collapsed(){
		this.full=false;
		return this;
	}
}
