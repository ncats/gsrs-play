package ix.core.util.pojopointer;

public class ObjectPath extends AbstractPath{
	private final String f;
	public ObjectPath(final String field){
		this.f=field;
	}

	public String getField(){
		return this.f;
	}

	@Override
	protected String thisJsonPointerString() {
		return "/" + this.f;
	}

	@Override
	protected String thisURIPath() {
		return thisJsonPointerString();
	}
}