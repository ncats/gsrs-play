package ix.ginas.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.Play;

public class CodeSystemURLGenerator implements DataSet<CodeSystemMeta>{
	
	
	private Map<String,CodeSystemMeta> controlledList = new LinkedHashMap<String, CodeSystemMeta>();
	
	public CodeSystemURLGenerator(String filename) throws JsonProcessingException, IOException{
		InputStream is=Play.application().resourceAsStream(filename);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode tree = mapper.readTree(is);
		
		for(JsonNode jsn:tree){
			String cs=jsn.at("/codeSystem").asText();
			String url=jsn.at("/url").asText();
			CodeSystemMeta csmap=new CodeSystemMeta(cs,url);
			controlledList.put(csmap.codeSystem, csmap);
		}
		is.close();
	}
	
	@Override
	public Iterator<CodeSystemMeta> iterator() {
		return controlledList.values().iterator();
	}

	@Override
	public boolean contains(CodeSystemMeta k) {
		return controlledList.containsKey(k.codeSystem);
	}
	
	public CodeSystemMeta fetch(String cs){
		return this.controlledList.get(cs);
	}
	
	

}
