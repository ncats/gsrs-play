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
import ix.test.server.BrowserSession;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.utils.Util;
import util.json.JsonUtil;

public class StructurallyDiverseApiTest {


    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);
    final File resource=new File("test/testJSON/strucDivWithRefs.json");
	
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };
    
    @Test
   	public void testStructurallyDiverseReferencesShowOnJSON() throws Exception {
    	JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            ensurePass( api.submitSubstance(entered));
            String uuid = entered.at("/uuid").asText();
            JsonNode jsn = api.fetchSubstanceJsonByUuid(uuid);
            assertEquals(
            		entered.at("/structurallyDiverse/references").toString(),
            			jsn.at("/structurallyDiverse/references").toString());
            
        }
   	}
    @Test
   	public void testStructurallyDiverseReferencesShowOnView() throws Exception {
    	JsonNode entered = parseJsonFile(resource);
    	
    	try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            ensurePass( api.submitSubstance(entered));
            String uuid = entered.at("/uuid").asText();
            String html = api.fetchSubstance(uuid).getBody();
            assertTrue(html.contains("Definition References"));
            
        }
   	}
    
    public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	return SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
    }
    
    
}
