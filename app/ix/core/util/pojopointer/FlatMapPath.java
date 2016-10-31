package ix.core.util.pojopointer;

public class FlatMapPath extends SinglePathNamedLambdaPath{
	public FlatMapPath(final PojoPointer field){
		super(field);
	}
	@Override
	public String name() {
		return "flatmap";
	}
}