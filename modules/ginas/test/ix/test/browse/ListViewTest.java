package ix.test.browse;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import ix.core.util.RunOnly;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.ginas.controllers.GinasApp;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Substance;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;

public class ListViewTest  extends AbstractGinasServerTest {
	static List<String> codeOrder = new ArrayList<String>();
	static{
		codeOrder.add("ZZZZ");
		codeOrder.add("BBB");
		codeOrder.add("DDD");
	}
	
	
	@Override
	public GinasTestServer createGinasTestServer(){
		return new GinasTestServer("ix.ginas.codes.order = "+ codeOrder);
	}

	@Test
	public void nonAmplifiedCodeSystemsShowInAlphabeticalOrder() throws Exception {
		
		// JsonNode entered = parseJsonFile(resource);
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			String theName = "ANYTHING";
			SubstanceAPI api = new SubstanceAPI(session);

			new SubstanceBuilder()
				.addName(theName)
				.addCode("A_AA1", "ANYCODE")
				.addCode("A_AA2", "ANYCODE")
				.addCode("A_AA3", "ANYCODE")
				.addCode("A_AA4", "ANYCODE")
				.addCode("A_AA4", "ANYCODE")
				.addCode("A_AA5", "ANYCODE")
				.addCode("A_AA6", "ANYCODE")
				.addCode("A_AA7", "ANYCODE")
				.addCode("A_AA8", "ANYCODE")
				.addCode("A_AA9", "ANYCODE")
				.addCode("A_AB1", "ANYCODE")
				.addCode("A_AB2", "ANYCODE")
				.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			System.out.println("=====================");
			String html = api.getTextSearchHTML(theName);
//			System.out.println(html);
			
			assertTrue(html.contains("A_AA1:"));
			assertTrue(html.contains("A_AA2:"));
			assertTrue(html.contains("A_AA3:"));
			assertTrue(html.contains("A_AA4:"));
			assertFalse(html.contains("A_AB2:"));
			
		}
	}
	
	@Test
	public void amplifiedCodeSystemShowsFirstInListView() throws Exception {
		// JsonNode entered = parseJsonFile(resource);
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			String theName = "ANYTHING";
			SubstanceAPI api = new SubstanceAPI(session);
			new SubstanceBuilder()
				.addName(theName)
				.addCode("A_AA1", "ANYCODE")
				.addCode("A_AA2", "ANYCODE")
				.addCode("A_AA3", "ANYCODE")
				.addCode("A_AA4", "ANYCODE")
				.addCode("A_AA4", "ANYCODE")
				.addCode("A_AA5", "ANYCODE")
				.addCode("A_AA6", "ANYCODE")
				.addCode("A_AA7", "ANYCODE")
				.addCode("A_AA8", "ANYCODE")
				.addCode("A_AA9", "ANYCODE")
				.addCode("A_AB1", "ANYCODE")
				.addCode("A_AB2", "ANYCODE")
				.addCode("ZZZZ", "ANYCODE")
				.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			String html = api.getTextSearchHTML(theName);
			
			assertTrue(html.contains("ZZZZ:"));
			assertFalse(html.contains("A_AB2:"));
			
		}catch(Throwable t){
			t.printStackTrace();
			throw t;
		}
	}
	@Test
	public void nonAmplifiedCodeSystemDoesntShowIfLowAlphabetical() throws Exception {
		// JsonNode entered = parseJsonFile(resource);
		try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
			String theName = "ANYTHING";
			SubstanceAPI api = new SubstanceAPI(session);
			new SubstanceBuilder()
				.addName(theName)
				.addCode("A_AA1", "ANYCODE")
				.addCode("A_AA2", "ANYCODE")
				.addCode("A_AA3", "ANYCODE")
				.addCode("A_AA4", "ANYCODE")
				.addCode("A_AA4", "ANYCODE")
				.addCode("A_AA5", "ANYCODE")
				.addCode("A_AA6", "ANYCODE")
				.addCode("A_AA7", "ANYCODE")
				.addCode("A_AA8", "ANYCODE")
				.addCode("A_AA9", "ANYCODE")
				.addCode("A_AB1", "ANYCODE")
				.addCode("A_AB2", "ANYCODE")
				.addCode("ZZZZZ", "ANYCODE")
				.buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));

			String html = api.getTextSearchHTML(theName);
			
			assertFalse(html.contains("ZZZZZ:"));
			
		}catch(Throwable t){
			t.printStackTrace();
			throw t;
		}
	}
	@Test
	public void sortedCodesShouldBeInOrderProvided() throws Exception {
			String theName = "ANYTHING";
			SubstanceBuilder sb=new SubstanceBuilder()
				.addName(theName);
			for(String c1: codeOrder){
				sb=sb.addCode(c1, "ANYCODE");
			}
			Substance s= sb.build();
			
			List<String> codesystems=s.getOrderedCodes(GinasApp.getCodeSystemOrder()).stream().map(c->c.codeSystem).collect(Collectors.toList());
			
			assertEquals(codeOrder,codesystems);
	}
	@Test
	public void sortedGroupedCodesShouldBeLimitedInOrderProvided() throws Exception {
			String theName = "ANYTHING";
			SubstanceBuilder sb=new SubstanceBuilder()
				.addName(theName);
			for(String c1: codeOrder){
				sb=sb.addCode(c1, "ANYCODE");
				sb=sb.addCode(c1, "ANYCODE-2");
			}
			Substance s= sb.build();
			Collection<List<Code>> clistGrouped=GinasApp.getOrderedGroupedCodes(s, 2);
			
			assertEquals(2,clistGrouped.size());
			List<Integer> sizelist=clistGrouped
										.stream()
										.map(cl->cl.size())
										.collect(Collectors.toList());
			for(Integer i: sizelist){
				assertEquals(2,i.intValue());
			}
			List<String> csystems=clistGrouped
										.stream()
										.map(l->l.get(0).codeSystem)
										.collect(Collectors.toList());
			
			
			assertEquals(codeOrder.subList(0, 2),csystems);
	}
}
