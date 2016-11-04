package ix.core.util.pojopointer;

public class ArrayPath extends AbstractPath{
	private int index=0;
	public ArrayPath(final int i){
		this.index=i;
	}
	public int getIndex(){
		return this.index;
	}
	
	public boolean isVirtualEnd(){
	    return (this.index==-1);
	}
	@Override
	protected String thisJsonPointerString() {
	    if(isVirtualEnd()){
	        return "/" + "-";
	    }
		return "/" + this.index;
	}

	@Override
	protected String thisURIPath() {
		return "(" + URIPojoPointerParser.ARRAY_CHAR + this.index + ")";
	}
}