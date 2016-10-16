package ix.core.util.pojopointer;

public class SortPath extends SinglePathNamedLambdaPath{
	boolean rev=false;

	public SortPath(final PojoPointer field, final boolean rev){
		super(field);
		this.rev=rev;
	}

	public boolean isReverse(){
		return this.rev;
	}

	@Override
	public String name() {
		if(!this.rev) {
			return "sort";
		} else {
			return "revsort";
		}
	}
}