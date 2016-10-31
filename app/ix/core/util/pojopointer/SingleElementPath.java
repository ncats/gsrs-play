package ix.core.util.pojopointer;

public abstract class SingleElementPath<T> extends LambdaPath{
	T t;
	public SingleElementPath(final T t){
		this.t=t;
	}

	public T getValue(){
		return this.t;
	}

	public abstract String name();

	public void setValue(final T t){
		this.t=t;
	}

	@Override
	protected String thisURIPath() {
		return URIPojoPointerParser.LAMBDA_CHAR + name() + "(" + this.t + ")";
	}
}