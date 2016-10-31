package ix.core.util.pojopointer;

public class LimitPath extends SingleElementPath<Long>{
	public LimitPath(final Long l){
		super(l);
	}
	
	@Override
	public String name() {
		return "limit";
	}
}