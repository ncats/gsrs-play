package ix.test;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import ix.test.util.TestNamePrinter;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.chem.Chem;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.ginas.controllers.GinasApp;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;
import ix.test.builder.AbstractSubstanceBuilder;
import ix.test.builder.SubstanceBuilder;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import util.json.JsonUtil;

public class ChemicalApiTest {


    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);
	
	final File resource=new File("test/testJSON/racemic-unicode.json");
	final File chemicalResource=new File("test/testJSON/editChemical.json");
	final File molformfile=new File("test/molforms.txt");
	
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };
    
    @Test @Ignore  
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
    
    @Test @Ignore  
   	public void testFlexMatch() throws Exception {
        JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            ensurePass( api.submitSubstance(entered));
            String html=api.getFlexMatchHTML("ClCC1CO1");
            assertTrue("Should have some result for flex match, but couldn't find any",html.contains("<span id=\"record-count\" class=\"label label-default\">1</span>"));
        }
   	}
    
    @Test @Ignore  
   	public void testFlexMatchWith2Moieties() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            ChemicalSubstance cs = makeChemicalSubstance("ClCC1CO1.O");
            EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
            JsonNode entered = em.valueToTree(cs);
            ensurePass( api.submitSubstance(entered));
            String html=api.getFlexMatchHTML("ClCC1CO1");
            assertTrue("Should have some result for flex match, but couldn't find any",html.contains("<span id=\"record-count\" class=\"label label-default\">1</span>"));
        }
   	}
    
    @Test @Ignore  
   	public void testFlexMatchWithIonsReturnsParents() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            String html=api.getFlexMatchHTML("OC1=CC=CC=C1");
            assertTrue("Should have 2 results for flex match, but found something else",html.contains("<span id=\"record-count\" class=\"label label-default\">2</span>"));
        }
   	}
    @Test @Ignore  
   	public void testBadFlexMatchReturnsNothing() throws Exception {
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
    
    @Test @Ignore  
   	public void testExactMatchReturnsOnlyExactMatches() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            JsonNode form3 = makeChemicalSubstanceJSON("OC1=CC=CC=C1");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            ensurePass( api.submitSubstance(form3));
            
            String html=api.getExactMatchHTML("OC1=CC=CC=C1");
            try{
            	assertTrue("Should have 1 match, but found something different",html.contains("<span id=\"record-count\" class=\"label label-default\">1</span>"));
            }catch(Throwable t){
            	//System.out.println(html);
            	t.printStackTrace();
            	throw t;
            }
            
        }
   	}
    
    @Test @Ignore  
   	public void substructureSearchSimple() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            String html=api.getSubstructureMatchHTML("C1=CC=CC=C1");
            assertTrue("Should have 2 matches, but found something else",html.contains("<span id=\"record-count\" class=\"label label-default\">2</span>"));
        }
   	}
    
    @Test @Ignore  
   	public void testSubstructureSearchSpecificity() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("CCCC");
            JsonNode form2 = makeChemicalSubstanceJSON("COC1=CC=CC=C1");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            String html=api.getSubstructureMatchHTML("C1=CC=CC=C1");
            assertTrue("Should have 1 match, but found something else:" + html,html.contains("<span id=\"record-count\" class=\"label label-default\">1</span>"));
        }
   	}
    
    @Test @Ignore  
    public void ensureWarningOnPentavalentCarbon(){
    	try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode pentaCarbon = makeChemicalSubstanceJSON("C(C)(C)(C)(C)C");
            
            String valmessage="NO VALANCE ERROR";
            JsonNode validation=api.validateSubstanceJson(pentaCarbon);
            for(JsonNode validationMessage:validation.at("/validationMessages")){
            	if(validationMessage.at("/messageType").asText().equals("WARNING")){
            		String msg=validationMessage.at("/message").asText();
            		if(msg.contains("Valance Error")){
            			valmessage=msg;
            		}
            	}
            }
            assertTrue("Pentavalent carbon should issue a warning message",valmessage.contains("Valance Error"));
            
            
        }
    }
    
    @Test @Ignore  
	public void testChemicalExportAsSDF() throws Exception {
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);
			JsonNode chemical = makeChemicalSubstanceJSON("COC1=CC=CC=C1");
            
            JsonNode entered= api.submitSubstanceJson(chemical);
            String displayName = entered.at("/_name").asText();
            String uuid=entered.at("/uuid").asText();
			String export=api.exportHTML(uuid,"sdf");
			assertTrue("Exported sdf should have $$$$ in it", export.contains("$$$$"));
			assertTrue("Exported sdf should have NON_OFFICIAL_NAMES in it", export.contains("NON_OFFICIAL_NAMES"));
			assertTrue("First line of sdf should have display name in it", export.startsWith(displayName));
			
		}
	}
    @Test @Ignore  
	public void testChemicalExportAsSmiles() throws Exception {
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);
			JsonNode chemical = makeChemicalSubstanceJSON("COC1=CC=CC=C1");
            
            JsonNode entered= api.submitSubstanceJson(chemical);
            String displayName = entered.at("/_name").asText();
            String uuid=entered.at("/uuid").asText();
			String export=api.exportHTML(uuid,"smiles");
			assertTrue("Exported smiles should be kekulized", export.contains("="));
			assertTrue("Exported smiles for 'COC1=CC=CC=C1' should have 'O'", export.contains("O"));
			assertTrue("Exported smiles for 'COC1=CC=CC=C1' should have 'C'", export.contains("C"));
			assertTrue("Exported smiles for 'COC1=CC=CC=C1' should have '1'", export.contains("1"));
			
		}
	}
    
    @Test @Ignore  
    public void testMolfileMoietyDecomposeDoesNotIncreaseStructureTotal() throws Exception {
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
        
        int oldCount=api.fetchStructureBrowseCount();
        
        
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
        int newCount=api.fetchStructureBrowseCount();
        
        assertEquals(oldCount, newCount);
    }
    
    @Test @Ignore  
    public void testExportTemporaryAfterMoietyDecompose() throws Exception {
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
        ensureExport(api,jsn.at("/structure"));
        ensureExport(api,jsn.at("/moieties/0"));
        ensureExport(api,jsn.at("/moieties/1"));
        //C1CCCCC1
    }

    
    
    
	@Test   
	public void testSubmitChemicalSubstanceTwice() throws Exception {

		String molfile1 = "\n   JSDraw206141615102D\n\n 14 14  0  0  0  0              0 V2000\n   26.8331   -7.2982    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   28.1674   -8.0599    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   25.4875   -8.0599    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   26.8331   -5.7326    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   28.1674   -9.6199    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   29.5328   -7.2672    0.0000 Se  0  0  0  0  0  0  0  0  0  0  0  0\n   25.4875   -9.6199    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   28.1674   -4.9766    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   25.4875   -4.9766    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   26.8331  -10.4154    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   30.8869   -8.0599    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   32.2325   -7.2672    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   30.8869   -9.5775    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   26.8442  -11.9754    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  1  2  2  0  0  0  0\n  1  3  1  0  0  0  0\n  1  4  1  0  0  0  0\n  2  5  1  0  0  0  0\n  2  6  1  0  0  0  0\n  3  7  2  0  0  0  0\n  4  8  1  0  0  0  0\n  4  9  2  0  0  0  0\n  5 10  2  0  0  0  0\n  6 11  1  0  0  0  0\n 11 12  1  0  0  0  0\n 11 13  2  0  0  0  0\n  7 10  1  0  0  0  0\n 10 14  1  0  0  0  0\nM  END";
		String molfile2 = "\n   JSDraw206141615142D\n\n 19 20  0  0  0  0              0 V2000\n   26.8331   -6.5182    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   28.1674   -7.2799    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   26.8331   -4.9526    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   29.5328   -6.4872    0.0000 Se  0  0  0  0  0  0  0  0  0  0  0  0\n   28.1674   -4.1966    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   25.4875   -4.1966    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   30.8869   -7.2799    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   32.2325   -6.4872    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   30.8869   -8.7975    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   28.1841   -8.8582    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   25.4821   -7.2982    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   25.4821   -8.8582    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   26.8331   -9.6382    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   26.8331  -11.1982    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   28.1841  -11.9782    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   28.1841  -13.5382    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   25.4821  -11.9782    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   25.4821  -13.5382    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   26.8331  -14.3182    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  1  3  1  0  0  0  0\n  2  4  1  0  0  0  0\n  3  5  1  0  0  0  0\n  3  6  2  0  0  0  0\n  4  7  1  0  0  0  0\n  7  8  1  0  0  0  0\n  7  9  2  0  0  0  0\n  1  2  2  0  0  0  0\n  2 10  1  0  0  0  0\n  1 11  1  0  0  0  0\n 11 12  2  0  0  0  0\n 12 13  1  0  0  0  0\n 13 10  2  0  0  0  0\n 13 14  1  0  0  0  0\n 14 15  2  0  0  0  0\n 15 16  1  0  0  0  0\n 14 17  1  0  0  0  0\n 17 18  2  0  0  0  0\n 18 19  1  0  0  0  0\n 19 16  2  0  0  0  0\nM  END";


		RestSession session = ts.newRestSession(ts.getFakeUser1());
		SubstanceAPI api = new SubstanceAPI(session);
		JsonNode entered= SubstanceJsonUtil
				.prepareUnapprovedPublic(JsonUtil.parseJsonFile(chemicalResource));
		String uuid=entered.get("uuid").asText();
		ensurePass(api.submitSubstance(entered));
		JsonNode fetched=api.fetchSubstanceJsonByUuid(uuid);
		String version = fetched.get("version").asText();
		System.out.println("version: " + version);
		JsonNode structure = fetched.at("/structure");
		String molFile = structure.get("molfile").asText();
		System.out.println("molfile: " + molFile);
		assertFalse(molFile.contains("26.8442  -11.9754    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"));
		assertEquals(version, "1");

		JsonNode updated1 = new JsonUtil
				.JsonNodeBuilder(fetched)
				.set("/structure/molfile", molfile1)
				.build();

		assertEquals(updated1.get("version").asText(), "1");

		ensurePass(api.updateSubstance(updated1));
		JsonNode fetched2nd=api.fetchSubstanceJsonByUuid(uuid);
		String version2 = fetched2nd.get("version").asText();
		System.out.println("version: " + version2);
		JsonNode structure2ndfetch = fetched2nd.at("/structure");
		String molFile2ndfetch = structure2ndfetch.get("molfile").asText();
		assertEquals(version2, "2");
		assertTrue(molFile2ndfetch.contains("26.8442  -11.9754    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"));

		JsonNode updated2 = new JsonUtil
				.JsonNodeBuilder(fetched2nd)
				.set("/structure/molfile", molfile2)
				.build();
		assertEquals(updated2.get("version").asText(), "2");

		ensurePass(api.updateSubstance(updated2));
		JsonNode fetched3rd=api.fetchSubstanceJsonByUuid(uuid);
		String version3 = fetched3rd.get("version").asText();
		System.out.println("version: " + version3);
		JsonNode structure3rdfetch = fetched3rd.at("/structure");
		String molFile3rdfetch = structure3rdfetch.get("molfile").asText();
		assertTrue(molFile3rdfetch.contains("26.8331  -11.1982    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"));
		assertEquals(version3, "3");
	}
	
	/**
	 * Tests that the canonicalization algorithm for the mol formulas
	 * returns the correct result after shuffling the order of the
	 * components 
	 * @throws Exception
	 */
	@Test @Ignore 
	public void testCanonicalMolForms() throws Exception {
		
		
		try (Stream<String> stream = Files.lines(molformfile.toPath())) {

			stream.forEach(s -> {
				List<String> shuffled= Arrays.asList(s.split("[.]"));
				Collections.shuffle(shuffled);
				String randStr = String.join(".",shuffled);
				assertEquals(s,ix.core.chem.FormulaInfo.toCanonicalString(randStr));
			});

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

	}
    
    public static void ensureExport(SubstanceAPI api, JsonNode structure){
    	String id = structure.at("/id").asText();
    	JsonNode uuidJSON=structure.at("/uuid");
    	if(
    			 uuidJSON!= null &&
    			!uuidJSON.isMissingNode() &&
    			!uuidJSON.isNull()
    			
    			){
    		id = uuidJSON.asText();
    	}
    	String export=api.exportHTML(id, "sdf");
    	String expectedSmiles=structure.at("/smiles").asText();
    	
    	assertTrue("Exported temporary structure sdf should have 'M  END' in it", export.contains("M  END"));
        assertTrue("Exported temporary structure sdf should have smiles string '" +expectedSmiles + "' in it", export.contains(expectedSmiles));
    }
    
    public static JsonNode makeChemicalSubstanceJSON(String smiles){
    	ChemicalSubstance cs = makeChemicalSubstance(smiles);
        EntityMapper em = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
        JsonNode entered = em.valueToTree(cs);
        return entered;
    }
    
    public static ChemicalSubstance makeChemicalSubstance(String smiles){
    	return new SubstanceBuilder()
    		.asChemical()
    		.setStructure(smiles)
    		.addName(smiles + " name")
    		.build();
    }
    
    
    public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	return SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
    }
    
    
}
