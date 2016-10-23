package ix.test.search;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.factories.SpecialFieldFactory;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityInfo;
import ix.ginas.models.v1.Substance;
import ix.test.server.GinasTestServer;
import play.Play;

public class SponsoredResultsTest extends AbstractGinasServerTest{
	List<String> specialFields;
		
	
	
	
	
	@Override
	public GinasTestServer createGinasTestServer(){
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
		return new GinasTestServer(add);
	}
	
	@Test
	public void specialFieldsAreRegisteredForEntity(){
		EntityInfo<Substance> sinfo=EntityUtils.getEntityInfoFor(Substance.class);
		assertEquals(specialFields,sinfo.getSponsoredFields());
	}
	

	@Test
	public void specialFieldsAreRegisteredInFactoryForEntity(){
		EntityInfo<Substance> sinfo=EntityUtils.getEntityInfoFor(Substance.class);
		List<String> specialFound=SpecialFieldFactory.getInstance(Play.application()).getRegisteredResourcesFor(sinfo);
		assertEquals(specialFields,specialFound);
	}
	
	
}
