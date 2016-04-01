package ix.utils.pojopatch;

import java.util.List;
import java.util.Stack;

import com.fasterxml.jackson.databind.JsonNode;

public interface PojoPatch<T>{
	public Stack apply(T old) throws Exception;
	public List<Change> getChanges();
	
	public static class Change{
		public String path;
		public String op;
		public String oldValue;
		public String newValue;
		public Change(String path, String op, String oldValue, String newValue){
			this.path=path;
			this.op=op;
			this.oldValue=oldValue;
			this.newValue=newValue;
		}
		public Change(JsonNode jsn){
			this(	jsn.at("/path").asText(),
					jsn.at("/op").asText(),
					null,
					jsn.at("/value").asText()
					);
		}
		public String toString(){
			return this.op + "\t" + this.path + "\t" + this.newValue;
		}
	}
}