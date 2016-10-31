package ix.core.util.pojopointer;

public class CountPath extends SinglePathNamedLambdaPath{
	public CountPath(final PojoPointer field){
		super(field);
	}
	@Override
	public String name() {
		return "count";
	}
}