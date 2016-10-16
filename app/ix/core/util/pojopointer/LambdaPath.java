package ix.core.util.pojopointer;

public abstract class LambdaPath extends AbstractPath{

	@Override
	protected String thisJsonPointerString() {
		throw new UnsupportedOperationException("JsonPointer unsupported for function paths");
	}
}