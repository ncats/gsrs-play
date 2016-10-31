package ix.core.util.pojopointer;

public class FieldPath extends SinglePathNamedLambdaPath{
	public FieldPath(final PojoPointer field){
		super(field);
	}

	@Override
	public String name() {
		return URIPojoPointerParser.OBJECT_FUNCTION_CHAR + "fields";
	}
}