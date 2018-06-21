package ix.test.chem;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.models.Structure;
import ix.ginas.utils.ChemUtils;

public class ChiralFlagFixerTest {

	@Test  
	public void testAssignChiralFlagWhenMissingAndAbsolute() throws Exception {
		String structureJson = "{\n" + 
				"    \"opticalActivity\": \"UNSPECIFIED\",\n" + 
				"    \"molfile\": \"\\n   JSDraw206211811082D\\n\\n 10 10  0  0  0  0              0 V2000\\n   26.8335  -10.0360    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   25.4825   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   25.4825   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   26.8335   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   28.1845   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   28.1845   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   29.5355   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   29.5355   -5.3560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   30.8865   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   32.2375   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n  1  2  1  0  0  0  0\\n  2  3  1  0  0  0  0\\n  3  4  1  0  0  0  0\\n  4  5  1  0  0  0  0\\n  5  6  1  0  0  0  0\\n  6  1  1  0  0  0  0\\n  5  7  1  0  0  0  0\\n  7  8  1  1  0  0  0\\n  7  9  1  0  0  0  0\\n  9 10  1  0  0  0  0\\nM  END\",\n" + 
				"    \"stereoCenters\": 1,\n" + 
				"    \"definedStereo\": 1,\n" + 
				"    \"ezCenters\": 0,\n" + 
				"    \"charge\": 0,\n" + 
				"    \"mwt\": 140.2658,\n" + 
				"    \"count\": 1,\n" + 
				"    \"stereochemistry\": \"ABSOLUTE\"\n" + 
				"}\n" + 
				"";
		ObjectMapper om = new ObjectMapper();
		Structure s = om.readValue(structureJson, Structure.class);
		ChemUtils.fixChiralFlag(s, new ArrayList<>());
		assertEquals(s.molfile.split("\n")[3], " 10 10  0  0  1  0              0 V2000");
	}
	
	@Test  
	public void testRemoveChiralFlagWhenPresentAndRacemic() throws Exception {
		String structureJson = "{\n" + 
				"    \"opticalActivity\": \"UNSPECIFIED\",\n" + 
				"    \"molfile\": \"\\n   JSDraw206211811182D\\n\\n 11 11  0  0  1  0              0 V2000\\n   26.8335  -10.0360    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   25.4825   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   25.4825   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   26.8335   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   28.1845   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   28.1845   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   29.5355   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   29.5355   -5.3560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   30.8865   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   32.2375   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   30.8865   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n  1  2  1  0  0  0  0\\n  2  3  1  0  0  0  0\\n  3  4  1  0  0  0  0\\n  4  5  1  0  0  0  0\\n  5  6  1  0  0  0  0\\n  6  1  1  0  0  0  0\\n  5  7  1  0  0  0  0\\n  7  8  1  1  0  0  0\\n  7  9  1  0  0  0  0\\n  9 10  1  0  0  0  0\\n  9 11  1  1  0  0  0\\nM  END\",\n" + 
				"    \"stereoCenters\": 1,\n" + 
				"    \"definedStereo\": 1,\n" + 
				"    \"ezCenters\": 0,\n" + 
				"    \"charge\": 0,\n" + 
				"    \"mwt\": 154.2924,\n" + 
				"    \"count\": 1,\n" + 
				"    \"stereochemistry\": \"RACEMIC\"\n" + 
				"  }";
		ObjectMapper om = new ObjectMapper();
		Structure s = om.readValue(structureJson, Structure.class);
		ChemUtils.fixChiralFlag(s, new ArrayList<>());
		assertEquals(s.molfile.split("\n")[3], " 11 11  0  0  0  0              0 V2000");
	}
	
	@Test  
	public void testStandardizeChiralFlagWhenAlignedPoorly() throws Exception {
		String structureJson = "{\n" + 
				"    \"opticalActivity\": \"UNSPECIFIED\",\n" + 
				"    \"molfile\": \"\\n   JSDraw206211811182D\\n\\n 11 11  0  0 0  0              0 V2000\\n   26.8335  -10.0360    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   25.4825   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   25.4825   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   26.8335   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   28.1845   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   28.1845   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   29.5355   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   29.5355   -5.3560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   30.8865   -7.6960    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   32.2375   -6.9160    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   30.8865   -9.2560    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n  1  2  1  0  0  0  0\\n  2  3  1  0  0  0  0\\n  3  4  1  0  0  0  0\\n  4  5  1  0  0  0  0\\n  5  6  1  0  0  0  0\\n  6  1  1  0  0  0  0\\n  5  7  1  0  0  0  0\\n  7  8  1  1  0  0  0\\n  7  9  1  0  0  0  0\\n  9 10  1  0  0  0  0\\n  9 11  1  1  0  0  0\\nM  END\",\n" + 
				"    \"stereoCenters\": 1,\n" + 
				"    \"definedStereo\": 1,\n" + 
				"    \"ezCenters\": 0,\n" + 
				"    \"charge\": 0,\n" + 
				"    \"mwt\": 154.2924,\n" + 
				"    \"count\": 1,\n" + 
				"    \"stereochemistry\": \"RACEMIC\"\n" + 
				"  }";
		ObjectMapper om = new ObjectMapper();
		Structure s = om.readValue(structureJson, Structure.class);
		ChemUtils.fixChiralFlag(s, new ArrayList<>());
		assertEquals(s.molfile.split("\n")[3], " 11 11  0  0  0  0              0 V2000");
	}
	
}
