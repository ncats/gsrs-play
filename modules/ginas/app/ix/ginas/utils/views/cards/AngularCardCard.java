package ix.ginas.utils.views.cards;

import play.twirl.api.Html;

public abstract class AngularCardCard implements DetailCard{

	String title;
	String anchor;
	int count=0;
	
	public AngularCardCard(int count,String title, String anchor){
		this.count=count;
		this.title=title;
		this.anchor=anchor;
	}
	
	@Override
	public Html getBreadCrumb() {
		return this.getDefaultBreadCrumb(count, anchor, title);
	}
	
	@Override
	public Html getContent() {
		String cardhtml ="<card eid='" + this.anchor + "' card-title='" + this.title + "' count='" + this.count + "' audit='audit'>";
		return new Html(cardhtml + innerContent().body() + "</card>");
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public abstract Html innerContent();
}
