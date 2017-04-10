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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasServerTest;
import ix.core.ValidationMessage;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.util.RunOnly;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.BrowserSession;
import ix.test.server.BrowserSubstanceSearcher;
import ix.test.server.RestSession;
import ix.test.server.RestSubstanceSearcher;
import ix.test.server.SearchResult;
import ix.test.server.SubstanceAPI;
import ix.test.server.SubstanceSearcher;
import util.json.JsonUtil;

public class ChemicalApiTest extends AbstractGinasServerTest {


	
	final File resource=new File("test/testJSON/racemic-unicode.json");
	final File chemicalResource=new File("test/testJSON/editChemical.json");
	final File molformfile=new File("test/molforms.txt");
	
	
	
    
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
            
            
            SubstanceSearcher searcher = new RestSubstanceSearcher(session);
            SearchResult sresult = searcher.flex("ClCC1CO1");
            assertTrue("Should have some result for flex match, but couldn't find any",sresult.getUuids().size()==1);
        }
   	}
    
    @Test   
   	public void testSgroupNonAtomListRegistration() throws Exception {
    	String molfileWith0AtomsInBrackets="\n" + 
    			"  Symyx   03161717412D 1   1.00000     0.00000     0\n" + 
    			"\n" + 
    			" 94105  0     1  0            999 V2000\n" + 
    			"   17.0912   -8.1355    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
    			"   19.7204   -7.0647    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   18.1496  -10.7814    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   19.1329  -14.3189    0.0000 N   0  0  3  0  0  0           0  0  0\n" + 
    			"   18.4746  -14.9856    0.0000 C   0  0  2  0  0  0           0  0  0\n" + 
    			"   17.9621   -7.9688    0.0000 N   0  0  0  0  0  4           0  0  0\n" + 
    			"   18.0162   -9.9272    0.0000 N   0  5  0  0  0  3           0  0  0\n" + 
    			"   18.1496   -7.0772    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   19.7204  -10.8022    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   19.8871   -7.9813    0.0000 N   0  0  0  0  0  4           0  0  0\n" + 
    			"   19.8746   -9.9105    0.0000 N   0  0  0  0  0  4           0  0  0\n" + 
    			"   17.1079   -9.7730    0.0000 C   0  0  3  0  0  0           0  0  0\n" + 
    			"   20.7704   -9.7064    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   18.9454   -6.6438    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   16.7080   -7.3522    0.0000 C   0  0  2  0  0  0           0  0  0\n" + 
    			"   18.9454  -11.2272    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   20.5454   -6.6314    0.0000 C   0  0  2  0  0  0           0  0  0\n" + 
    			"   17.3204  -11.2480    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
    			"   17.5620  -14.8355    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
    			"   20.7245   -8.1023    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   17.1412  -15.6771    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
    			"   18.6204  -15.9272    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   16.6996  -10.6314    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
    			"   14.5371  -16.1188    0.0000 P   0  0  3  0  0  0           0  0  0\n" + 
    			"   20.0371  -14.5022    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.1745   -8.9189    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   17.3454   -6.6980    0.0000 C   0  0  2  0  0  0           0  0  0\n" + 
    			"   19.0161  -13.3814    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.1954  -10.5939    0.0000 C   0  0  3  0  0  0           0  0  0\n" + 
    			"   20.5870  -11.2188    0.0000 C   0  0  2  0  0  0           0  0  0\n" + 
    			"   19.8538  -12.9856    0.0000 N   0  0  0  0  0  4           0  0  0\n" + 
    			"   21.1704   -7.2480    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
    			"   17.7870  -16.3480    0.0000 C   0  0  2  0  0  0           0  0  0\n" + 
    			"   20.4912  -13.6772    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   16.3287  -16.1438    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   20.5162  -15.3022    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   16.8496  -12.0563    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   15.9078   -6.8772    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   20.5454   -5.6939    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.4163  -13.6523    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.4454  -15.2938    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   17.3370   -5.7564    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   20.5870  -12.1604    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   15.7746  -10.6314    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.8955  -14.4689    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   22.0828   -7.2563    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.9788   -6.8731    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.3412   -5.2230    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.5288  -17.0564    0.0000 O   0  5  0  0  0  0           0  0  0\n" + 
    			"   14.9788  -10.1480    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.0871  -15.2897    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   15.4621  -12.8689    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   13.7246  -16.5814    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   22.2995  -12.6231    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   23.4662   -8.0647    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.5496  -12.8606    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.3412   -4.2855    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.9788   -9.2065    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.5120   -6.0647    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   15.9246  -13.6813    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   17.0995  -14.0147    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   22.7454  -11.8147    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   23.9287   -7.2438    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   15.9329  -12.0563    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.3788  -12.6231    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   22.5412   -8.0647    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   16.2913   -8.6064    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.5371  -14.4939    0.0000 C   0  0  1  0  0  0           0  0  0\n" + 
    			"   18.9454   -5.7147    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   18.9454  -12.1813    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   15.9078   -7.8230    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.4663   -6.6314    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.1870  -10.6314    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.5120   -7.6980    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
    			"   22.1412   -5.6939    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
    			"   23.9287   -8.8856    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
    			"   22.7454  -13.4314    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
    			"   14.0829  -13.6689    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   17.7787  -12.0606    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   17.3204  -17.1605    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.9954  -10.1147    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.9954  -11.0563    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   17.7829  -17.9814    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   21.9038  -16.1188    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   22.8080  -14.4605    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   15.4621  -14.4939    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   16.5368   -5.2820    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   16.5287   -4.3398    0.0000 C   0  0  0  0  0  0           0  0  0\n" + 
    			"   17.3317   -3.8574    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"   15.7176   -3.8673    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
    			"   18.8787   -8.9439    0.0000 Co  0  1  0  0  0  0           0  0  0\n" + 
    			"   19.9454   -4.8439    0.0000 O   0  5  0  0  0  2           0  0  0\n" + 
    			"   19.5963   -3.2620    0.0000 N   0  0  0  0  0  0           0  0  0\n" + 
    			"   18.4826   -2.6389    0.0000 O   0  0  0  0  0  0           0  0  0\n" + 
    			"  5  4  1  1     0  0\n" + 
    			"  6  1  1  0     0  0\n" + 
    			"  3  7  1  0     0  0\n" + 
    			"  8  6  2  0     0  0\n" + 
    			" 10  2  1  0     0  0\n" + 
    			" 11  9  2  0     0  0\n" + 
    			"  7 12  1  0     0  0\n" + 
    			" 12  1  1  0     0  0\n" + 
    			" 13 11  1  0     0  0\n" + 
    			" 14  8  1  0     0  0\n" + 
    			"  2 14  2  0     0  0\n" + 
    			" 15  1  1  0     0  0\n" + 
    			"  9 16  1  0     0  0\n" + 
    			" 16  3  2  0     0  0\n" + 
    			" 17  2  1  0     0  0\n" + 
    			"  3 18  1  0     0  0\n" + 
    			"  5 19  1  0     0  0\n" + 
    			" 20 10  2  0     0  0\n" + 
    			" 19 21  1  0     0  0\n" + 
    			"  5 22  1  0     0  0\n" + 
    			" 18 23  1  0     0  0\n" + 
    			" 23 12  1  0     0  0\n" + 
    			" 25  4  1  0     0  0\n" + 
    			" 26 20  1  0     0  0\n" + 
    			" 26 13  2  0     0  0\n" + 
    			" 27 15  1  0     0  0\n" + 
    			"  8 27  1  0     0  0\n" + 
    			" 28  4  1  0     0  0\n" + 
    			" 13 29  1  0     0  0\n" + 
    			" 29 30  1  0     0  0\n" + 
    			" 30  9  1  0     0  0\n" + 
    			" 31 28  2  0     0  0\n" + 
    			" 32 17  1  0     0  0\n" + 
    			" 20 32  1  0     0  0\n" + 
    			" 22 33  1  0     0  0\n" + 
    			" 33 21  1  0     0  0\n" + 
    			" 34 25  2  0     0  0\n" + 
    			" 31 34  1  0     0  0\n" + 
    			" 21 35  1  1     0  0\n" + 
    			" 35 24  1  0     0  0\n" + 
    			" 36 25  1  0     0  0\n" + 
    			" 18 37  1  6     0  0\n" + 
    			" 15 38  1  1     0  0\n" + 
    			" 17 39  1  1     0  0\n" + 
    			" 40 34  1  0     0  0\n" + 
    			" 41 36  2  0     0  0\n" + 
    			" 27 42  1  6     0  0\n" + 
    			" 30 43  1  6     0  0\n" + 
    			" 23 44  1  1     0  0\n" + 
    			" 45 41  1  0     0  0\n" + 
    			" 40 45  2  0     0  0\n" + 
    			" 32 46  1  6     0  0\n" + 
    			" 47 38  1  0     0  0\n" + 
    			" 48 39  1  0     0  0\n" + 
    			" 49 24  1  0     0  0\n" + 
    			" 50 44  1  0     0  0\n" + 
    			" 24 51  1  0     0  0\n" + 
    			" 53 24  2  0     0  0\n" + 
    			" 56 52  1  0     0  0\n" + 
    			" 57 48  2  0     0  0\n" + 
    			" 58 50  2  0     0  0\n" + 
    			" 59 47  2  0     0  0\n" + 
    			" 60 52  2  0     0  0\n" + 
    			" 19 61  1  1     0  0\n" + 
    			" 62 54  2  0     0  0\n" + 
    			" 63 55  2  0     0  0\n" + 
    			" 52 64  1  0     0  0\n" + 
    			" 64 37  1  0     0  0\n" + 
    			" 54 65  1  0     0  0\n" + 
    			" 65 43  1  0     0  0\n" + 
    			" 55 66  1  0     0  0\n" + 
    			" 66 46  1  0     0  0\n" + 
    			"  1 67  1  6     0  0\n" + 
    			" 51 68  1  0     0  0\n" + 
    			" 69 14  1  0     0  0\n" + 
    			" 70 16  1  0     0  0\n" + 
    			" 15 71  1  6     0  0\n" + 
    			" 17 72  1  6     0  0\n" + 
    			" 73 50  1  0     0  0\n" + 
    			" 74 47  1  0     0  0\n" + 
    			" 75 48  1  0     0  0\n" + 
    			" 76 55  1  0     0  0\n" + 
    			" 77 54  1  0     0  0\n" + 
    			" 68 78  1  0     0  0\n" + 
    			" 78 56  1  0     0  0\n" + 
    			" 18 79  1  1     0  0\n" + 
    			" 33 80  1  6     0  0\n" + 
    			" 81 29  1  0     0  0\n" + 
    			" 82 29  1  0     0  0\n" + 
    			" 83 80  1  0     0  0\n" + 
    			" 84 41  1  0     0  0\n" + 
    			" 85 45  1  0     0  0\n" + 
    			" 68 86  1  1     0  0\n" + 
    			" 42 87  1  0     0  0\n" + 
    			" 87 88  1  0     0  0\n" + 
    			" 88 89  2  0     0  0\n" + 
    			" 88 90  1  0     0  0\n" + 
    			"  7 91  1  0     0  0\n" + 
    			" 11 91  1  0     0  0\n" + 
    			" 91 10  1  0     0  0\n" + 
    			" 91  6  1  0     0  0\n" + 
    			" 31 91  1  0     0  0\n" + 
    			" 91 92  1  0     0  0\n" + 
    			" 93 92  1  0     0  0\n" + 
    			" 93 94  2  0     0  0\n" + 
    			"M  CHG  4   7  -1  49  -1  91   3  92  -1\n" + 
    			"M  STY  2   1 GEN   2 DAT\n" + 
    			"M  SLB  2   1   1   2   2\n" + 
    			"M  SAL   1 15   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15\n" + 
    			"M  SAL   1 15  16  17  18  19  20  21  22  23  24  25  26  27  28  29  30\n" + 
    			"M  SAL   1 15  31  32  33  34  35  36  37  38  39  40  41  42  43  44  45\n" + 
    			"M  SAL   1 15  46  47  48  49  50  51  52  53  54  55  56  57  58  59  60\n" + 
    			"M  SAL   1 15  61  62  63  64  65  66  67  68  69  70  71  72  73  74  75\n" + 
    			"M  SAL   1 15  76  77  78  79  80  81  82  83  84  85  86  87  88  89  90\n" + 
    			"M  SAL   1  4  91  92  93  94\n" + 
    			"M  SDI   1  4   13.2787  -18.3855   13.2787   -3.4055\n" + 
    			"M  SDI   1  4   24.6287   -3.4055   24.6287  -18.3855\n" + 
    			"M  SDT   2 FDAREG_SGROUP                 F\n" + 
    			"M  SDD   2    24.9287   -3.8555    DA    ALL  1       5\n" + 
    			"M  SED   2 OC-6\n" + 
    			"M  SPL  1   2   1\n" + 
    			"M  END";
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            UUID uuid = UUID.randomUUID();
            
            JsonNode entered=new SubstanceBuilder()
									.asChemical()
									.addName("Just a test")
									.setStructure(molfileWith0AtomsInBrackets)
									.setUUID(uuid)
									.buildJson();
            ensurePass( api.submitSubstance(entered));
            
            String fetchedLychi = api.fetchSubstanceLychiv4ByUuid(uuid.toString());
            
            assertEquals("GSV5NCTZX3GG", fetchedLychi);
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
            
            
            SubstanceSearcher searcher = new RestSubstanceSearcher(session);
            SearchResult sresult = searcher.flex("ClCC1CO1");
            assertTrue("Should have some result for flex match, but couldn't find any",sresult.getUuids().size()==1);
        }
   	}
    
    @Test   
   	public void testFlexMatchWithIonsReturnsParents() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            SubstanceSearcher searcher = new RestSubstanceSearcher(session);
            SearchResult sresult = searcher.flex("OC1=CC=CC=C1");
            assertEquals("Should have 2 results for flex match, but found something else",2,sresult.getUuids().size());
        }
   	}
    @Test
   	public void testBadFlexMatchReturnsNothing() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            

            SubstanceSearcher searcher = new RestSubstanceSearcher(session);
            SearchResult sresult = searcher.flex("CCCOC1=CC=CC=C1");
            assertEquals("Should have no matches, but found some",0,sresult.getUuids().size());
            
            
        }
   	}
    
    @Test   
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
            
            

            SubstanceSearcher searcher = new RestSubstanceSearcher(session);
            SearchResult sresult = searcher.exact("OC1=CC=CC=C1");
            assertEquals("Should have 1 match, but found something else",1,sresult.getUuids().size());
            
        }
   	}
    
    @Test   
   	public void substructureSearchSimple() throws Exception {
        //JsonNode entered = parseJsonFile(resource);
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode form1 = makeChemicalSubstanceJSON("[O-]C1=CC=CC=C1.[Cl-]");
            JsonNode form2 = makeChemicalSubstanceJSON("OC1=CC=CC=C1.O");
            
            ensurePass( api.submitSubstance(form1));
            ensurePass( api.submitSubstance(form2));
            
            SubstanceSearcher searcher = new RestSubstanceSearcher(session);
            SearchResult result=searcher.substructure("C1=CC=CC=C1");
            assertEquals(2, result.getUuids().size());
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
            
            
            SubstanceSearcher searcher = new RestSubstanceSearcher(session);
            SearchResult result=searcher.substructure("C1=CC=CC=C1");
            assertEquals(1, result.getUuids().size());
        }
   	}

	@Test
	public void testQueryAtomCachingAndSpecificity() throws Exception {
		// JsonNode entered = parseJsonFile(resource);
		try (RestSession session = ts.newRestSession(ts.getFakeUser1());
				BrowserSession bsession = ts.newBrowserSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);

			ensurePass(api.submitSubstance(makeChemicalSubstanceJSON("S(=O)(=O)(O)OCCC")));
			ensurePass(api.submitSubstance(makeChemicalSubstanceJSON("S(=O)(=O)(O)OC1CC1")));
			ensurePass(api.submitSubstance(makeChemicalSubstanceJSON("S(=O)(=O)(O)Oc1ccccc1")));
			ensurePass(api.submitSubstance(makeChemicalSubstanceJSON("S(=O)(=O)(O)Oc1ccc(C)cc1")));
			ensurePass(api.submitSubstance(makeChemicalSubstanceJSON("S(=O)(=O)(O)Oc1ccc(O)cc1")));

			RestSubstanceSearcher searcher = new RestSubstanceSearcher(session);
			BrowserSubstanceSearcher bsearcher = new BrowserSubstanceSearcher(bsession);

			SearchResult result = bsearcher.substructure(searcher.getStructureAsUUID("S(=O)(=O)(O)OC[#6]"));
			assertEquals(2, result.getUuids().size());

			result = bsearcher.substructure(searcher.getStructureAsUUID("S(=O)(=O)(O)OC@:[#6]"));
			assertEquals(3, result.getUuids().size());

			result = bsearcher.substructure(searcher.getStructureAsUUID("S(=O)(=O)(O)OC@-[#6]"));
			assertEquals(1, result.getUuids().size());
		}
	}
	
	@Test
	public void testCarbonSubstructureSearchDoesNotLimitToAliphaticCarbon() throws Exception {
		// JsonNode entered = parseJsonFile(resource);
		
		String searchFor="\n" + 
				"   JSDraw204101717192D\n" + 
				"\n" + 
				"  1  0  0  0  0  0              0 V2000\n" + 
				"  409.0000 -142.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"M  END";
		
		try (RestSession session = ts.newRestSession(ts.getFakeUser1());
				BrowserSession bsession = ts.newBrowserSession(ts.getFakeUser1())) {
			SubstanceAPI api = new SubstanceAPI(session);

			ensurePass(api.submitSubstance(makeChemicalSubstanceJSON("c1ccccc1")));
			ensurePass(api.submitSubstance(makeChemicalSubstanceJSON("Cc1ccccc1")));

			RestSubstanceSearcher searcher = new RestSubstanceSearcher(session);
			BrowserSubstanceSearcher bsearcher = new BrowserSubstanceSearcher(bsession);

			SearchResult result = bsearcher.substructure(searcher.getStructureAsUUID(searchFor));
			assertEquals(2, result.getUuids().size());
		}
	}
	

    @Test   
    public void ensureWarningOnPentavalentCarbon(){
    	try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode pentaCarbon = makeChemicalSubstanceJSON("C(C)(C)(C)(C)C");

			SubstanceAPI.ValidationResponse validationResponse = api.validateSubstance(pentaCarbon);

			List<ValidationMessage> messages = validationResponse.getMessages();

			Optional<ValidationMessage>  desiredWarning = messages.stream()
															.filter(msg -> msg.getMessageType() == ValidationMessage.MESSAGE_TYPE.WARNING &&  msg.getMessage().contains("Valence Error"))
															.findAny();

			assertTrue("Pentavalent carbon should issue a warning message", desiredWarning.isPresent());
            
        }
    }
    
    @Test   
    public void ensureWarningOnNetChargeStructure(){
    	try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode pentaCarbon = makeChemicalSubstanceJSON("[CH3+]");

			SubstanceAPI.ValidationResponse validationResponse = api.validateSubstance(pentaCarbon);

			List<ValidationMessage> messages = validationResponse.getMessages();

			Optional<ValidationMessage>  desiredWarning = messages.stream()
															.filter(msg -> msg.getMessageType() == ValidationMessage.MESSAGE_TYPE.WARNING &&  msg.getMessage().contains("charged balanced"))
															.findAny();

			assertTrue("Net charged structure should issue a warning message", desiredWarning.isPresent());
            
        }
    }

    @Test
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
    @Test   
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
    
    @Test   
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
    
    @Test   
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
		JsonNode structure = fetched.at("/structure");
		String molFile = structure.get("molfile").asText();
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
	@Test  
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
	
	/*
	 * TODO:
	 * Here's a test.
	 * 
	 * How about adding 10,000 substances. Then, go to the browse page.
	 * 
	 * Click the last page.
	 * 
	 * Here's what I want to know.
	 * 
	 * #1. How long does it take to go the last page?
	 * #2. How much memory is being taken up at this time?
	 * 
	 * 
	 */
    
    public static void ensureExport(SubstanceAPI api, JsonNode structure){
    	String id = structure.at("/id").asText();
    	JsonNode uuidJSON=structure.at("/uuid");
    	if(uuidJSON!= null &&
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
				.addName(smiles + " name")
    			.setStructure(smiles)
    			.build();
    }
    
    
    public JsonNode parseJsonFile(String path){
		return parseJsonFile(new File(path));
	}
    public JsonNode parseJsonFile(File resource){
    	return SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
    }
    
    
}
