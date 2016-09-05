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
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import ix.utils.Util;
import util.json.JsonUtil;

public class ProteinApiTest {


    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);
	
	
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };
    
    @Test
   	public void testSubmitGeneratedProtein() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            String seq="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            ProteinSubstance ps = makeProteinSubstance(seq);
            String uuid = ps.getOrGenerateUUID().toString();
            EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
            JsonNode entered = em.valueToTree(ps);
            ensurePass( api.submitSubstance(entered));
            JsonNode fetched=api.fetchSubstanceJsonByUuid(uuid);
            assertEquals(uuid,fetched.at("/uuid").asText());
            assertEquals(seq,fetched.at("/protein/subunits/0/sequence").asText());
        }
   	}
    
    @Test
   	public void globalSequenceSimilaritySearchShouldReturnAllSlightlyModifiedSequences() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
        	String seq="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        	SubstanceAPI api = new SubstanceAPI(session);
        	int totsize=101;
            for(int i=0;i<totsize;i++){
            	
	            ProteinSubstance ps = makeProteinSubstance(seq + seq.charAt(i%seq.length()));
	            String uuid = ps.getOrGenerateUUID().toString();
	            EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
	            JsonNode entered = em.valueToTree(ps);
	            ensurePass( api.submitSubstance(entered));
            }
            String oldCount=null;
            String total=null;
            long timeoutTime = System.currentTimeMillis() + 20000;
            for(;;){
	            String body = api.getSequenceSearchHTML(seq, CutoffType.GLOBAL, .9);
	            System.out.println("####################Body:");
	            total=body.split("<nav>")[1].split("<h3><span id=\"record-count\" class=\"label label-default\">")[1].split("<")[0];
	           // System.out.println(body);
	            System.out.println("Found:" + total + " of " + totsize);
	            if(
	            		System.currentTimeMillis()> timeoutTime ||
	            		body.contains("\"status\":\"Done\"")
	            		){
	            	break;
	            }
	            oldCount=total;
	            
	            Util.debugSpin(100);
            }
            assertEquals(totsize+"",total);
        }
   	}
    
    
    public static JsonNode makeProteinSubstanceJSON(String seq){
    	ProteinSubstance cs = makeProteinSubstance(seq);
        EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
        JsonNode entered = em.valueToTree(cs);
        return entered;
    }
    
    public static ProteinSubstance makeProteinSubstance(String seq){
    	ProteinSubstance cs = new ProteinSubstance();
    	//cs.getOrGenerateUUID();
    	Subunit su1 = new Subunit();
    	su1.sequence=seq;
    	su1.subunitIndex=1;
    	cs.protein= new Protein();
    	cs.protein.subunits.add(su1);
    	Name n = new Name();
    	n.name=seq + " name";
    	Reference r=Reference.SYSTEM_GENERATED();
    	r.addTag(Reference.PUBLIC_DOMAIN_REF);
    	r.publicDomain=true;
    	n.addReference(r, cs);
    	cs.protein.addReference(r);
    	cs.names.add(n);
    	return cs;
    }
    
    
    public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	return SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
    }
    
    
}
