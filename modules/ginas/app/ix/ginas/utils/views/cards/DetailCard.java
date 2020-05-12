package ix.ginas.utils.views.cards;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ix.ginas.controllers.ViewType;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;


/**
 * Information in a "card" section should implement this interface.
 * This gives a view for data, having both a "breadcrumb" header location
 * as well as the actual content.
 * @author tyler
 *
 */
public interface DetailCard extends CardConsumer{
	/**
	 * Returns the {@link Html} for the breadcrumb location
	 * 
	 * @return
	 */
	public Html getBreadCrumb();
	public Html getContent();
	
	/**
	 * Returns true if the card is to be rendered
	 * @return
	 */
	public default boolean isVisble(){
		return true;
	}
	

	public default String getTitle(){
		return "UNTITLED";
	}
	
	
	
	
	
	public default Html getDefaultBreadCrumb(int size, String anchor, String title){
		
		return ix.ginas.views.html.details.breadlink.render(size, anchor, title);
	}
	
	
	public static DetailCard makeCard(Html h){
		return new DetailCard(){

			@Override
			public Html getBreadCrumb() {
				
				return this.getDefaultBreadCrumb(2, "whatever", "whatever");
			}

			@Override
			public Html getContent() {
				return h;
			}
			
		};
	}
	public static DetailCard makeCard(String html){
		return makeCard(new Html(html));
		
	}
	
	
	

	static String titleCase(String s){
		
		String snew= s.chars()
		 .skip(1)
		 .mapToObj(c-> {
			 if(c>='A' && c<='Z'){
				 return (" " + (char)c);
			 }else{
				 return (char)c+"";
			 }
		 })
		 .collect(Collectors.joining(""));
		String ret=s.substring(0,1).toUpperCase() + snew;
		return ret;
		
		
	}

	default void consumeCards(Substance s, Consumer<? super DetailCard> consumer){
		consumer.accept( this);
	}
	
}
