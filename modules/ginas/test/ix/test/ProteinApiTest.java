package ix.test;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasServerTest;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Subunit;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.utils.Util;
import util.json.JsonUtil;

public class ProteinApiTest extends AbstractGinasServerTest {
    
    @Test
   	public void testSubmitGeneratedProtein() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            String seq="ABCDEFGHI";
            ProteinSubstance ps = makeProteinSubstance(seq, 0);
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
        	String seq="ABCDEFGHIABCDEFGHI";
        	SubstanceAPI api = new SubstanceAPI(session);
        	int totsize=101;
            for(int i=0;i<totsize;i++){
	            ProteinSubstance ps = makeProteinSubstance(seq + seq.charAt(i%seq.length()), i);
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
    
    
//    public static JsonNode makeProteinSubstanceJSON(String seq){
//    	ProteinSubstance cs = makeProteinSubstance(seq);
//        EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
//        JsonNode entered = em.valueToTree(cs);
//        return entered;
//    }
    
    public static ProteinSubstance makeProteinSubstance(String seq, int i){
    	ProteinSubstance cs = new ProteinSubstance();
    	//cs.getOrGenerateUUID();
    	Subunit su1 = new Subunit();
    	su1.sequence=seq;
    	su1.subunitIndex=1;
    	cs.protein= new Protein();
    	cs.protein.subunits.add(su1);
    	Name n = new Name();
    	n.name = "name"+ i;
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
