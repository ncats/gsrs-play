package ix.core.util.pojopointer;

public class SkipPath extends SingleElementPath<Long>{
	public SkipPath(final Long l){
		super(l);
	}
	@Override
	public String name() {
		return "skip";
	}
}