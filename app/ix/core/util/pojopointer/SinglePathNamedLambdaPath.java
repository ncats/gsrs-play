package ix.core.util.pojopointer;

public abstract class SinglePathNamedLambdaPath extends LambdaPath{
	private final PojoPointer field;
	public SinglePathNamedLambdaPath(final PojoPointer pp){
		this.field=pp;
	}
	public PojoPointer getField() {
		return this.field;
	}
	public abstract String name();

	@Override
	protected String thisURIPath() {
		return URIPojoPointerParser.LAMBDA_CHAR + name() + "(" + this.field.toURIpath() + ")";
	}
}