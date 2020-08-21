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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ix.core.util.RunOnly;
import ix.test.server.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.factories.SpecialFieldFactory;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.ginas.models.v1.Substance;
import ix.ginas.modelBuilders.SubstanceBuilder;
import play.Play;

public class SponsoredResultsTest extends AbstractGinasServerTest{
    static Set<String> specialFields;
		
	BrowserSession bsession;
	RestSession    rsession;
	RestSubstanceSubstanceSearcher    searcher;
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
				)
		        .stream()
		        .collect(Collectors.toSet());
		keyValue.put("fields",specialFields);
		special.add(keyValue);
		add.put(SpecialFieldFactory.IX_CORE_EXACTSEARCHFIELDS, special);
		ts.modifyConfig(add);
		ts.restart();
		bsession= ts.newBrowserSession(ts.getFakeUser1());
		rsession= ts.newRestSession(ts.getFakeUser1());
		searcher= new RestSubstanceSubstanceSearcher(rsession);
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
		Set<String> specialFound=new HashSet<>(SpecialFieldFactory.getInstance(Play.application())
		                .getRegisteredResourcesFor(sinfo));
		assertEquals(specialFields,specialFound);
	}
	
	@Test
	@RunOnly
    public void exactMatchOnSpecialFieldsShowFirst() throws IOException, InterruptedException{

	        String theName = "ANYTHING";
	        Set<String> uuids = new HashSet<String>();
	        int max=3;
            for(int i=0;i<max;i++){
                api.submitSubstance(new SubstanceBuilder()
                	.generateNewUUID() // need to explicitly put this here so we have a reference to it.
                	.andThen((Consumer <Substance>) s-> uuids.add(s.getUuid().toString()))
                    .addName(theName + " " + i)); // we need the space for lucene to correctly tokenize
            }
            Substance special =new SubstanceBuilder()
                .addName(theName)
                .generateNewUUID() // need to explicitly put this here so we have a reference to it.
				.andThen((Consumer <Substance>) s-> uuids.add(s.getUuid().toString()))
                .build();
            
            
            
            api.submitSubstance(special);
            SearchResult sr=searcher.query(theName);
            Set<String> searchUUIDs = new HashSet<>(sr.getUuids());
            searchUUIDs.removeAll(uuids);
            
            System.out.println("Extras");
            System.out.println(searchUUIDs);
            
            assertEquals(uuids,sr.getUuids());
            assertEquals(max+1,sr.getUuids().size());
            Set<String> specialUUIDs = sr.getSpecialUuids();
            assertEquals(special.getUuid(),
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
