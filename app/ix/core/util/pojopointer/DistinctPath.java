package ix.core.util.pojopointer;

public class DistinctPath extends SinglePathNamedLambdaPath{
	public DistinctPath(final PojoPointer field){
		super(field);
	}
	@Override
	public String name() {
		return "distinct";
	}
}