package ix.core.util.pojopointer;

public class ArrayPath extends AbstractPath{
	private int index=0;
	public ArrayPath(final int i){
		this.index=i;
	}
	public int getIndex(){
		return this.index;
	}
	@Override
	protected String thisJsonPointerString() {
		return "/" + this.index;
	}

	@Override
	protected String thisURIPath() {
		return "(" + URIPojoPointerParser.ARRAY_CHAR + this.index + ")";
	}
}