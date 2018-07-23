package ix.test.chem;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ix.utils.FortranLikeParserHelper.LineParser;

public class FortranLikeParserTest {


	@Test  
	public void testStandardizeNonRightAlignedLine() throws Exception {
		LineParser lineFormat=new LineParser("aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv");
		String poorlyAligned=" 11 11  0 0  0  0              0 V2000";
		String properlyAligned=" 11 11  0  0  0  0              0 V2000";
		
		assertEquals(properlyAligned, lineFormat.standardize(poorlyAligned));
	}
	
	@Test  
	public void testGetSingleSectionOfLine() throws Exception {
		LineParser lineFormat=new LineParser("aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv");
		String properlyAligned=" 11 11  0  0  0  0              0 V2000";
		String expected=" 11";
		String found=lineFormat.parseOnly(properlyAligned, "bbb").get().getStandardForm();
		assertEquals(expected,found);
	}
	
	@Test  
	public void testGetSingleSectionOfLineAtEnd() throws Exception {
		LineParser lineFormat=new LineParser("aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv");
		String properlyAligned=" 11 11  0  0  0  0              0 V2000";
		String expected=" V2000";
		String found=lineFormat.parseOnly(properlyAligned, "vvvvvv").get().getStandardForm();
		assertEquals(expected,found);
	}
}
