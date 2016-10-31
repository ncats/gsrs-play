package ix.core.util.pojopointer;

import com.fasterxml.jackson.core.JsonPointer;

public abstract class AbstractPath implements PojoPointer{
	private PojoPointer child=null;
	private boolean isRaw=false;
	@Override
	public boolean isRaw(){
		return this.isRaw;
	}
	@Override
	public void setRaw(final boolean r){
		this.isRaw=r;
	}
	@Override
	public void tail(final PojoPointer child){
		this.child=child;
	}
	@Override
	public PojoPointer tail(){
		return this.child;
	}

	protected abstract String thisJsonPointerString();

	protected abstract String thisURIPath();

	@Override
	public JsonPointer toJsonPointer(){
		String s=thisJsonPointerString();
		if(this.child!=null){
			final JsonPointer jp = this.child.toJsonPointer();
			s+=jp.toString();
		}
		return JsonPointer.compile(s);
	}
	@Override
	public String toURIpath(){
		String uri=thisURIPath();
		if(this.child!=null){
			uri+= this.child.toURIpath();
		}
		return uri;
	}
}