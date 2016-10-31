package ix.core.util.pojopointer;

public class MapPath extends SinglePathNamedLambdaPath{
	public MapPath(final PojoPointer field){
		super(field);
	}
	@Override
	public String name() {
		return "map";
	}
}