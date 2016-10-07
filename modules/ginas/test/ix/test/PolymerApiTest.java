package ix.test;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import util.json.JsonUtil;

public class PolymerApiTest extends AbstractGinasServerTest {

    final File resource=new File("test/testJSON/polyquotes.json");
    
    @Test
   	public void testSubmitPolymerWithStringAmounts() throws Exception {
    	 JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	 SubstanceAPI api = new SubstanceAPI(session);
             ensurePass(api.submitSubstance(entered));
             JsonNode roundTrip=api.fetchSubstanceJsonByUuid(entered.at("/uuid").asText());
             
             assertEquals("123.0",roundTrip.at("/polymer/monomers/0/amount/average").toString());
             assertEquals("1123.0",roundTrip.at("/polymer/monomers/0/amount/high").toString());
             assertEquals("22.0",roundTrip.at("/polymer/monomers/0/amount/low").toString());
        }
   	}
    
    public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	return SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
    }
    
    
}
