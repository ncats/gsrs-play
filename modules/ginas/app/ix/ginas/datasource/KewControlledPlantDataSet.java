package ix.ginas.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.Play;

public class KewControlledPlantDataSet implements DataSet<String>{
	private LinkedHashSet<String> controlledList = new LinkedHashSet<String>();
	
	public KewControlledPlantDataSet(String filename) throws JsonProcessingException, IOException{
		InputStream is=Play.application().resourceAsStream(filename);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode tree = mapper.readTree(is);
		
		for(JsonNode jsn:tree.at("/substanceNames")){
			String unii=jsn.at("/externalIdentifier").asText();
			controlledList.add(unii);
		}
		is.close();
	
	}
	
	
	
	@Override
	public Iterator<String> iterator() {
		return controlledList.iterator();
	}



	@Override
	public boolean contains(String k) {
		return controlledList.contains(k);
	}

}
