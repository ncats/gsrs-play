package ix.ginas.utils.views.cards;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CollectionDetailCard<T> extends AngularCardCard {

	public Collection<T> c;
	
	
	public CollectionDetailCard(Collection<T> col, String title, String anchor){
		super(col.size(), title, anchor);
		this.c=col;
	}
	

	@Override
	public boolean isVisble() {
		return (c!=null && !c.isEmpty());
	}
	
	public List<T> asList(){
		if(c instanceof List)return (List<T>)c;
		return c.stream().collect(Collectors.toList());
	}
		

}
