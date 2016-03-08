package ix.utils.pojopatch;

import java.util.Stack;

public interface PojoPatch<T>{
	Stack apply(T old) throws Exception;
}