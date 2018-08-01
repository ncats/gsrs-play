package ix.ginas.utils.views.cards;

import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

/**
 * Card view for detail overview of substance record
 * @author tyler
 *
 */
public class NotesCard extends CollectionDetailCard<Note>{
	Substance s;
	
	public NotesCard(Substance s){
		super(s.getDisplayNotes(),"Notes","notes");
		this.s=s;
		
	}

	@Override
	public Html innerContent() {
		return ix.ginas.views.html.details.properties.notes.render(asList(), s.uuid.toString());
	}
}
