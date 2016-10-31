package ix.utils.pojopatch;

import java.util.List;
import java.util.Stack;

import com.fasterxml.jackson.databind.JsonNode;

public interface PojoPatch<T>{
	//public Stack apply(T old) throws Exception;
	public Stack<?> apply(T old, ChangeEventListener ... changeListener) throws Exception;
	
	
	public List<Change> getChanges();
	
	
}