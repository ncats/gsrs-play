package ix.test.search;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ix.AbstractGinasClassServerTest;
import ix.core.factories.SpecialFieldFactory;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.ginas.models.v1.Substance;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.BrowserSession;
import ix.test.server.RestSession;
import ix.test.server.SearchResult;
import ix.test.server.SubstanceAPI;
import ix.test.server.SubstanceSearcher;
import play.Play;

public class SponsoredResultsTest extends AbstractGinasClassServerTest{
    static List<String> specialFields;
		
	static BrowserSession bsession;
	static RestSession    rsession;
	static SubstanceSearcher    searcher;
	static SubstanceAPI api;
	
	@BeforeClass
	public static void buildup(){
		Map<String,Object> add=new HashMap<>();
		List<Map> special=new ArrayList<>();
		Map<String,Object> keyValue=new HashMap<>();
		keyValue.put("class",Substance.class.getName());
		specialFields= Arrays.asList("root_names_name",
				"root_codes_code",
				"root_approvalID",
				"not_actually_present"
				);
		System.out.println("Setting:" + specialFields.toString());
		keyValue.put("fields",specialFields);
		special.add(keyValue);
		add.put(SpecialFieldFactory.IX_CORE_EXACTSEARCHFIELDS, special);
		ts.modifyConfig(add);
		ts.restart();
		bsession= ts.newBrowserSession(ts.getFakeUser1());
		rsession= ts.newRestSession(ts.getFakeUser1());
		searcher= new SubstanceSearcher(bsession);
		api = new SubstanceAPI(rsession);
		
	}
	@AfterClass
	public static void breakdown(){
	    bsession.close();
	    rsession.close();
	}
	
	@Test
	public void specialFieldsAreRegisteredForEntity(){
		EntityInfo<Substance> sinfo=EntityUtils.getEntityInfoFor(Substance.class);
		assertEquals(specialFields,sinfo.getSponsoredFields());
	}
	

	@Test
	public void specialFieldsAreRegisteredInFactoryForEntity(){
		EntityInfo<Substance> sinfo=EntityUtils.getEntityInfoFor(Substance.class);
		List<String> specialFound=SpecialFieldFactory.getInstance(Play.application())
		                .getRegisteredResourcesFor(sinfo);
		assertEquals(specialFields,specialFound);
	}
	
	@Test
    public void exactMatchOnSpecialFieldsShowFirst() throws IOException{
	        String theName = "ANYTHING";
            for(int i=0;i<50;i++){
                new SubstanceBuilder()
                    .addName(theName + " some extra stuff " + i)
                    .buildJsonAnd(j -> ensurePass(api.submitSubstance(j)));            
            }
            Substance special =new SubstanceBuilder()
                .addName(theName)
                .generateNewUUID()
                .build();
            ensurePass(api.submitSubstance(EntityWrapper.of(special).toFullJsonNode()));
            SearchResult sr=searcher.query(theName);
            assertEquals(51,sr.getUuids().size());
            Set<String> specialUUIDs = sr.getSpecialUuids();
            assertEquals(special.getUuid().toString().split("-")[0],
                         specialUUIDs.iterator().next());
            
    }
	
	
}
