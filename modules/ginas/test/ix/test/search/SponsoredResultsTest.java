package ix.test.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ix.AbstractGinasClassServerTest;
import ix.core.factories.SpecialFieldFactory;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.core.util.RunOnly;
import ix.ginas.models.v1.Substance;
import ix.test.builder.SubstanceBuilder;
import ix.test.server.BrowserSession;
import ix.test.server.BrowserSubstanceSearcher;
import ix.test.server.RestSession;
import ix.test.server.SearchResult;
import ix.test.server.SubstanceAPI;
import play.Play;

public class SponsoredResultsTest extends AbstractGinasClassServerTest{
    static List<String> specialFields;
		
	BrowserSession bsession;
	RestSession    rsession;
	BrowserSubstanceSearcher    searcher;
	SubstanceAPI api;
	
	@Before
	public void buildup(){
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
		searcher= new BrowserSubstanceSearcher(bsession);
		api = new SubstanceAPI(rsession);
		
	}
	@After
	public void breakdown(){
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
		List<String> specialFound=new ArrayList<>(SpecialFieldFactory.getInstance(Play.application())
		                .getRegisteredResourcesFor(sinfo));
		assertEquals(specialFields,specialFound);
	}
	
	@Test
    public void exactMatchOnSpecialFieldsShowFirst() throws IOException{

	        String theName = "ANYTHING";
            for(int i=0;i<50;i++){
                api.submitSubstance(new SubstanceBuilder()
                    .addName(theName + " " + i)); // we need the space for lucene to correctly tokenize
            }
            Substance special =new SubstanceBuilder()
                .addName(theName)
					.generateNewUUID() // need to explicitly put this here so we have a reference to it.
                .build();
            
            api.submitSubstance(special);

            SearchResult sr=searcher.query(theName);

            assertEquals(51,sr.getUuids().size());
            Set<String> specialUUIDs = sr.getSpecialUuids();
            assertEquals(special.getUuid().toString().split("-")[0],
                         specialUUIDs.iterator().next());
    }
	
	@Test
    public void allMatchesAreShownOnSpecialMatch() throws Exception{

			Set<String> uuids = new HashSet<String>();
		
	        String theName = "ANYTHING";
	        Substance special =new SubstanceBuilder()
	                .addName(theName)
					.generateNewUUID() // need to explicitly put this here so we have a reference to it.
					.andThen(s->{uuids.add(s.getUuid().toString().split("-")[0]);})
	                .build();
	            api.submitSubstance(special);
	            
	        api.submitSubstance(new SubstanceBuilder()
	        		.generateNewUUID()
	        		.andThen(s->{uuids.add(s.getUuid().toString().split("-")[0]);})
                    .addName(theName + " WITH MORE THINGS")); // we need the space for lucene to correctly tokenize
            
	     

            SearchResult sr=searcher.query(theName);
            

            assertEquals(2,sr.getUuids().size());
            assertEquals(uuids,sr.getUuids());
            Set<String> specialUUIDs = sr.getSpecialUuids();
            assertEquals(special.getUuid().toString().split("-")[0],
                         specialUUIDs.iterator().next());
    }
}
