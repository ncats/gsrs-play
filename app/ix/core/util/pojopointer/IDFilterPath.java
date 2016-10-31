package ix.core.util.pojopointer;

import java.util.Objects;

public class IDFilterPath extends LambdaPath{
	private final String id;
	public IDFilterPath(final String id){
		Objects.requireNonNull(id);
		this.id=id;
	}

	public String getId(){
		return this.id;
	}

	@Override
	protected String thisURIPath() {
		return "(" + this.id + ")";
	}
}