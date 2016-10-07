package ix.test;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import ix.seqaln.SequenceIndexer.CutoffType;
import ix.test.util.TestNamePrinter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.ginas.controllers.GinasApp;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Subunit;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.utils.Util;
import util.json.JsonUtil;

public class PolymerApiTest extends AbstractGinasTest {


    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);
    final File resource=new File("test/testJSON/polyquotes.json");

	
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };
    
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
