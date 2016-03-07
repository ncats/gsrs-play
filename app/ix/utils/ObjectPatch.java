package ix.utils;

import java.util.Stack;

public interface ObjectPatch{
	public Stack apply(Object old) throws Exception;
}