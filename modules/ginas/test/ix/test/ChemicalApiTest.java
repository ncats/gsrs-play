package ix.test;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Reference;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import util.json.JsonUtil;

public class ChemicalApiTest {
	@Rule
    public GinasTestServer ts = new GinasTestServer(9001);
	
	final File resource=new File("test/testJSON/racemic-unicode.json");
	
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };
    
    @Test
    public void testMolfileMoietyDecomposeGetsCorrectCounts() throws Exception {
    	String molfile="\n" + 
    			"   JSDraw204021619552D\n" + 
    			"\n" + 
    			" 18 18  0  0  0  0              0 V2000\n" + 
    			"   15.1840   -9.1000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   13.8330   -8.3200    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   13.8330   -6.7600    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   16.5350   -8.3200    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   16.5350   -6.7600    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   15.1840   -5.9800    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   18.3040   -4.9400    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   16.9530   -4.1600    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   16.9530   -2.6000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   18.3040   -1.8200    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   19.6550   -2.6000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   19.6550   -4.1600    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   29.0160   -6.8640    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   27.6650   -6.0840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   27.6650   -4.5240    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   29.0160   -3.7440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   30.3670   -4.5240    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"   30.3670   -6.0840    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    			"  1  2  2  0  0  0  0\n" + 
    			"  2  3  1  0  0  0  0\n" + 
    			"  1  4  1  0  0  0  0\n" + 
    			"  4  5  2  0  0  0  0\n" + 
    			"  5  6  1  0  0  0  0\n" + 
    			"  6  3  2  0  0  0  0\n" + 
    			"  7  8  1  0  0  0  0\n" + 
    			"  8  9  1  0  0  0  0\n" + 
    			"  9 10  1  0  0  0  0\n" + 
    			" 10 11  1  0  0  0  0\n" + 
    			" 11 12  1  0  0  0  0\n" + 
    			" 12  7  1  0  0  0  0\n" + 
    			" 13 14  1  0  0  0  0\n" + 
    			" 14 15  1  0  0  0  0\n" + 
    			" 15 16  1  0  0  0  0\n" + 
    			" 16 17  1  0  0  0  0\n" + 
    			" 17 18  1  0  0  0  0\n" + 
    			" 18 13  1  0  0  0  0\n" + 
    			"M  END\n" + 
    			"";
        RestSession session = ts.newRestSession(ts.getFakeUser1());


        SubstanceAPI api = new SubstanceAPI(session);
        JsonNode jsn = api.fetchInstrumentJson(molfile);
        JsonNode structure=jsn.at("/structure");
        JsonNode moieties=jsn.at("/moieties");
        assertEquals(moieties.size(),2);
        JsonNode moi1=moieties.get(0);
        JsonNode moi2=moieties.get(1);
        
        if(moi1.at("/count").asInt()==1){
        	assertEquals(moi2.at("/count").asInt(),2);
        }else{
        	assertEquals(moi1.at("/count").asInt(),2);
        }
    }
    
    @Test
   	public void testFlexMatch() throws Exception {
        JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            ensurePass( api.submitSubstance(entered));
            String html=api.getFlexMatchHTML("ClCC1CO1");
            assertTrue("Should have some result for flex match, but couldn't find any",html.contains("<span class=\"label label-default\">1</span>"));
        }
   	}
    
    @Test
   	public void testFlexMatchWith2Moieties() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            ChemicalSubstance cs = makeChemicalSubstance("ClCC1CO1.O");
            EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
            JsonNode entered = em.valueToTree(cs);
            ensurePass( api.submitSubstance(entered));
            String html=api.getFlexMatchHTML("ClCC1CO1");
            assertTrue("Should have some result for flex match, but couldn't find any",html.contains("<span class=\"label label-default\">1</span>"));
        }
   	}
    
    @Test
   	public void testFlexMatchWithIons() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            String html=api.getFlexMatchHTML("OC1=CC=CC=C1");
            assertTrue("Should have 2 results for flex match, but found something else",html.contains("<span class=\"label label-default\">2</span>"));
        }
   	}
    @Test
   	public void testFlexMatchReturnsNothing() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            String html=api.getFlexMatchHTML("CCCOC1=CC=CC=C1");
            assertTrue("Should have no matches, but found some",html.contains("There are no results to show."));
            
        }
   	}
    
    @Test
   	public void testSubstructureSearchSimple() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            String html=api.getSubstructureMatchHTML("C1=CC=CC=C1");
            assertTrue("Should have 2 matches, but found something else",html.contains("<span class=\"label label-default\">2</span>"));
        }
   	}
    
    @Test
   	public void testSubstructureSearchSpecificity() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("CCCC");
            JsonNode form2 = makeChemicalSubstanceJSON("COC1=CC=CC=C1");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            String html=api.getSubstructureMatchHTML("C1=CC=CC=C1");
            assertTrue("Should have 1 match, but found something else:" + html,html.contains("<span class=\"label label-default\">1</span>"));
        }
   	}
    
    public JsonNode makeChemicalSubstanceJSON(String smiles){
    	ChemicalSubstance cs = makeChemicalSubstance(smiles);
        EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
        JsonNode entered = em.valueToTree(cs);
        return entered;
    }
    
    public ChemicalSubstance makeChemicalSubstance(String smiles){
    	ChemicalSubstance cs = new ChemicalSubstance();
    	cs.structure= new GinasChemicalStructure();
    	cs.structure.molfile=smiles;
    	Name n = new Name();
    	n.name=smiles + " name";
    	Reference r=Reference.SYSTEM_GENERATED();
    	n.addReference(r, cs);
    	cs.structure.addReference(r);
    	cs.names.add(n);
    	return cs;
    }
    
    
    public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	return SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));
    }
}
