package ix.utils.pojopatch;

import java.util.Stack;

public interface PojoPatch{
	public Stack apply(Object old) throws Exception;
}