package ix.core.util.pojopointer;

import com.fasterxml.jackson.core.JsonPointer;

public final class IdentityPath extends AbstractPath{
	final static JsonPointer jsonIdentity=JsonPointer.compile("");
	final static String uriIdentity="";
	public IdentityPath(){}

	@Override
	protected String thisJsonPointerString() {
		throw new UnsupportedOperationException("`thisJsonPointerString` on IdentityPath should not be called");
	}
	@Override
	protected String thisURIPath() {
		throw new UnsupportedOperationException("`thisURIPath` on IdentityPath should not be called");
	}

	@Override
	public JsonPointer toJsonPointer(){
		if(this.hasTail()){
			return this.tail().toJsonPointer();
		}
		return IdentityPath.jsonIdentity;
	}

	@Override
	public String toURIpath(){
		if(this.hasTail()){
			return this.tail().toURIpath();
		}
		return IdentityPath.uriIdentity;
	}
}