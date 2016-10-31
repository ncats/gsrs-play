package ix.core.util.pojopointer;

/**
 * {@link LambdaPath} which is used to group elements together based on
 * a matching element found at {@link #getField()}. The key to be used for
 * the mapping should be the same values found.
 * @author peryeata
 *
 */
public class GroupPath extends SinglePathNamedLambdaPath{

	public GroupPath(final PojoPointer field){
		super(field);
	}

	@Override
	public String name() {
		return "group";
	}
}