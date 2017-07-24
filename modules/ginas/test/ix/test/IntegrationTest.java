package ix.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import ix.AbstractGinasServerTest;
import ix.core.controllers.EntityFactory;
import ix.core.models.Role;
import ix.core.util.RunOnly;
import ix.ginas.models.v1.Substance;
import ix.test.server.ControlledVocab;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.util.TestNamePrinter;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import play.Configuration;
import play.api.test.FakeApplication;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class IntegrationTest extends AbstractGinasServerTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();


	@Test
	public void stopServer(){
		ts.stop();
		//play will throw a runtime exception
		//we won't get a 404
		expectedException.expect(RuntimeException.class);
		expectedException.expectMessage("no started application");

		ts.notLoggedInRestSession().get("ginas/app");
	}

	@Test
	public void restartedServer(){
		ts.restart();
		JsonNode substances = ts.notLoggedInRestSession().getAsJson("ginas/app/api/v1/substances");
		assertFalse( SubstanceJsonUtil.isLiteralNull(substances));

	}

    @Test
    public void testRestAPISubstance() throws Exception {
        JsonNode substances = ts.notLoggedInRestSession().getAsJson("ginas/app/api/v1/substances");
        assertFalse( SubstanceJsonUtil.isLiteralNull(substances));
    }


    @Test
	public void testRestAPIBDNUMProcessor() throws Exception {

		ts.stop(true);


		List<Map<String, ?>> processors = new ArrayList<>();

		processors.add(new HashMap<String, Object>(){{
				put("class", "ix.ginas.models.v1.Substance");
				put("processor", "ix.ginas.processors.SubstanceProcessor");
		}});
		processors.add( new HashMap<String, Object>(){{
			put("class", "ix.ginas.models.v1.Substance");
			put("processor", "ix.ginas.processors.UniqueCodeGenerator");
			put("with", new HashMap<String, Object>() {{

				put("codesystem","BDNUM");
				put("suffix","AB");
				put("length",10);
				put("padding",true);
			}});
		}});

		ts.modifyConfig("ix.core.entityprocessors", processors);
		ts.start();
		JsonNode result = ts.notLoggedInRestSession().getAsJson("ginas/app/api/v1/vocabularies/search?q=terms_value:BDNUM");
		assertFalse( SubstanceJsonUtil.isLiteralNull(result));
		assertEquals(1, result.get("total").asInt());
	}


    @Test
    public void ensureSetupUsers() throws Exception{
        try(RestSession session = ts.newRestSession(ts.getFakeUser1())){

            assertEquals(ts.getFakeUser1().getUserName(), session.getUserName());
        }

    }
    
    @Test
    public void testFakeUserLoginPassword() throws Exception {
        GinasTestServer.User user = ts.createUser(Role.DataEntry);
        try(RestSession session = ts.newRestSession(user, RestSession.AUTH_TYPE.USERNAME_PASSWORD)){

            assertEquals(user.getUserName(), session.getUserName());
        }
    }
    
    @Test
    public void testFakeUserLoginKey() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1(), RestSession.AUTH_TYPE.USERNAME_KEY)){

            assertEquals(ts.getFakeUser1().getUserName(), session.getUserName());
        }
    }
    

    @Test
    public void loginToken() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1(), RestSession.AUTH_TYPE.TOKEN)){

            assertEquals(ts.getFakeUser1().getUserName(), session.getUserName());
        }
    	
    }
    
    @Test
    public void notPassingCredentialsShouldFailRestCalls() throws Exception {
        try(RestSession session = ts.newRestSession(ts.getFakeUser1(), RestSession.AUTH_TYPE.NONE)){
           assertNull(session.getUserName());
        }

    }
    

    @Test
    public void testRestAPIVocabularies()  throws Exception {
        ControlledVocab cv = ts.notLoggedInRestSession().getControlledVocabulary();
        int total = cv.getTotalCount();
        int loaded = cv.getLoadedCount();

        assertTrue("There should be more than 0 CVs loaded, found (" + loaded + ")", loaded >= 1);
        assertTrue("There should be more than 0 CVs listed in total, found " + total, total >=1);


    }
    
    @Test
    public void pojoDiffSwitchOrderTest() throws Exception{
    	ObjectMapper om1=EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
    	JsonNode js1=om1.readTree("{\n" + 
    			"	\"nucleicAcid\": {\n" + 
    			"		\"subunits\": [{\n" + 
    			"			\"sequence\": \"CAAGAGGCAUACUCCUCAUAGGGAAAGAUCUUGACUGCCACUGUCUCAAACUGCUCUGAAGUGUUCUGCUUCAGCUUGGCCUUAUAGACCUCAGCAAAGCGACCUUUCCCCACCAGGGUGUCCAGCUCAAUGGGCAGCAGCUCUGUGUUGUGGUUGAUGUUGUUGGCACACGUGGAGCUGAUGUCAGAGCGGUCAUCUUCCAGGAUGAUGGCACAGUGCUCGCUGAACUCCAUGAGCUUCCGCGUCUUGCCGGUUUCCCAGGUUGAACUCAGCUUCUGCUGCCGGUUAACGCGGUAGCAGUAGAAGAUGAUGAUGACAGAUAUGGCAACUCCCAGUGGUGGCAGGAGGCUGAUGCCUGUCACUUGAAAUAUGACUAGCAACAAGUCAGGAUUGCUGGUGUUAUAUUCUUCUGAGAAGAUGAUGUUGUCAUUGCACUCAUCAGAGCUACAGGAACACAUGAAGAAAGUCUCACCAGGCUUUUUUUUUUCCUUCAUAAUGCACUUUGGAGAAGCAGCAUCUUCCAGAAUAAAGUCAUGGUAGGGGAGCUUGGGGUCAUGGCAAACUGUCUCUAGUGUUAUGUUCUCGUCAUUCUUUCUCCAUACAGCCACACAGACUUCCUGUGGCUUCUCACAGAUGGAGGUGAUGCUGCAGUUGCUCAUGCAGGAUUUCUGGUUGUCACAGGUGGAAAAUCUCACAUCACAAAAUUUACACAGUUGUGGAAACUUGACUGCACCGUUGUUGUCAGUGACUAUCAUGUCGUUAUUAAUAUGUCUCAGUGGAUGGGCAGUCCUAUUACAGCUGGGGCAGAUGAUUUCAUCUUUCUGGGCCUCCAUUUCCACAUCCGACUUCUGAACGUGCGGUGGGAUCGUGCUGGCGAUACGCGUCCACAGGACGAUGUGCAGCGGCCACAGGCCCCUGAGCAGCCCCCGA\",\n" + 
    			"			\"subunitIndex\": 1\n" + 
    			"		}],\n" + 
    			"		\"sequenceType\": \"ANTI-SENSE RNA\",\n" + 
    			"		\"linkages\": [{\n" + 
    			"			\"linkage\": \"P\",\n" + 
    			"			\"sitesShorthand\":\"1_2-1_930\" \n" + 
    			"		}],\n" + 
    			"		\"sugars\": [{\n" + 
    			"			\"sitesShorthand\": \"1_1-1_930\",\n" + 
    			"			\"sugar\": \"R\"\n" + 
    			"		}],\n" + 
    			"		\"modifications\": {\n" + 
    			"			\"structuralModifications\": [],\n" + 
    			"			\"physicalModifications\": [],\n" + 
    			"			\"agentModifications\": []\n" + 
    			"		},\n" + 
    			"		\"nucleicAcidSubType\": [\"\"],\n" + 
    			"		\"references\": [],\n" + 
    			"		\"access\": []\n" + 
    			"	},\n" + 
    			"	\"approvalID\": \"FUA17JE46Y\",\n" + 
    			"	\"status\": \"approved\",\n" + 
    			"	\"approvedBy\": \"FDA_SRS\",\n" + 
    			"	\"approved\": 1449802860146,\n" + 
    			"	\"substanceClass\": \"nucleicAcid\",\n" + 
    			"	\"names\": ["+
	    			"   {\n" + 
	    			"		\"name\": \"TGFF-2 ANTI-SENSE RNA (6-935)\",\n" + 
	    			"		\"type\": \"cn\",\n" + 
	    			"		\"domains\": [],\n" + 
	    			"		\"uuid\": \"99980b4b-ee4f-4aac-92dc-5a74387528a8\",\n" + 
	    			"		\"languages\": [\"en\"],\n" + 
	    			"		\"references\": [\"6dc80b4b-ee4f-4aac-92dc-5a74387528a8\"],\n" + 
	    			"		\"preferred\": true,\n" + 
	    			"		\"access\": []\n" + 
	    			"	},"+
	    			"   {\n" + 
	    			"		\"name\": \"TGFβ-2 ANTI-SENSE RNA (6-935)\",\n" + 
	    			"		\"type\": \"of\",\n" + 
	    			"		\"domains\": [],\n" + 
	    			"		\"uuid\": \"77780b4b-ee4f-4aac-92dc-5a74387528a8\",\n" + 
	    			"		\"languages\": [\"en\"],\n" + 
	    			"		\"references\": [\"6dc80b4b-ee4f-4aac-92dc-5a74387528a8\"],\n" + 
	    			"		\"preferred\": true,\n" + 
	    			"		\"access\": []\n" + 
	    			"	}"+
    			"],\n" + 
    			"	\"codes\": [],\n" + 
    			"	\"relationships\": [],\n" + 
    			"	\"references\": [{\n" + 
    			"		\"uuid\": \"6dc80b4b-ee4f-4aac-92dc-5a74387528a8\",\n" + 
    			"		\"citation\": \"J Clin Oncol 2006; 24: 4721?4730\",\n" + 
    			"		\"docType\": \"SRS\",\n" + 
    			"		\"publicDomain\": true,\n" + 
    			"		\"tags\": [\"NOMEN\"],\n" + 
    			"		\"access\": []\n" + 
    			"	}, {\n" + 
    			"		\"uuid\": \"3316964d-c2ce-45ce-9f08-813e1f23b457\",\n" + 
    			"		\"citation\": \"SRS import [FUA17JE46Y]\",\n" + 
    			"		\"docType\": \"SRS\",\n" + 
    			"		\"publicDomain\": true,\n" + 
    			"		\"documentDate\": \"2015-12-10T22:01:00.000-0500\",\n" + 
    			"		\"tags\": [\"NOMEN\"],\n" + 
    			"		\"url\": \"http://fdasis.nlm.nih.gov/srs/srsdirect.jsp?regno\\u003dFUA17JE46Y\",\n" + 
    			"		\"access\": []\n" + 
    			"	}],\n" + 
    			"	\"properties\": [],\n" + 
    			"	\"notes\": [],\n" + 
    			"	\"tags\": [],\n" + 
    			"	\"uuid\": \"95f72217-0244-4123-8c02-d6e313eb48b4\",\n" + 
    			"	\"access\": [],\n" + 
    			"	\"lastEditedBy\": \"FDA_SRS\"\n" + 
    			"}");
    	
    	JsonNode js2=om1.readTree("{\n" + 
    			"	\"nucleicAcid\": {\n" + 
    			"		\"subunits\": [{\n" + 
    			"			\"sequence\": \"CAAGAGGCAUACUCCUCAUAGGGAAAGAUCUUGACUGCCACUGUCUCAAACUGCUCUGAAGUGUUCUGCUUCAGCUUGGCCUUAUAGACCUCAGCAAAGCGACCUUUCCCCACCAGGGUGUCCAGCUCAAUGGGCAGCAGCUCUGUGUUGUGGUUGAUGUUGUUGGCACACGUGGAGCUGAUGUCAGAGCGGUCAUCUUCCAGGAUGAUGGCACAGUGCUCGCUGAACUCCAUGAGCUUCCGCGUCUUGCCGGUUUCCCAGGUUGAACUCAGCUUCUGCUGCCGGUUAACGCGGUAGCAGUAGAAGAUGAUGAUGACAGAUAUGGCAACUCCCAGUGGUGGCAGGAGGCUGAUGCCUGUCACUUGAAAUAUGACUAGCAACAAGUCAGGAUUGCUGGUGUUAUAUUCUUCUGAGAAGAUGAUGUUGUCAUUGCACUCAUCAGAGCUACAGGAACACAUGAAGAAAGUCUCACCAGGCUUUUUUUUUUCCUUCAUAAUGCACUUUGGAGAAGCAGCAUCUUCCAGAAUAAAGUCAUGGUAGGGGAGCUUGGGGUCAUGGCAAACUGUCUCUAGUGUUAUGUUCUCGUCAUUCUUUCUCCAUACAGCCACACAGACUUCCUGUGGCUUCUCACAGAUGGAGGUGAUGCUGCAGUUGCUCAUGCAGGAUUUCUGGUUGUCACAGGUGGAAAAUCUCACAUCACAAAAUUUACACAGUUGUGGAAACUUGACUGCACCGUUGUUGUCAGUGACUAUCAUGUCGUUAUUAAUAUGUCUCAGUGGAUGGGCAGUCCUAUUACAGCUGGGGCAGAUGAUUUCAUCUUUCUGGGCCUCCAUUUCCACAUCCGACUUCUGAACGUGCGGUGGGAUCGUGCUGGCGAUACGCGUCCACAGGACGAUGUGCAGCGGCCACAGGCCCCUGAGCAGCCCCCGA\",\n" + 
    			"			\"subunitIndex\": 1\n" + 
    			"		}],\n" + 
    			"		\"sequenceType\": \"ANTI-SENSE RNA\",\n" + 
    			"		\"linkages\": [{\n" + 
    			"			\"linkage\": \"P\",\n" + 
    			"			\"sitesShorthand\":\"1_2-1_930\" \n" + 
    			"		}],\n" + 
    			"		\"sugars\": [{\n" + 
    			"			\"sitesShorthand\": \"1_1-1_930\",\n" + 
    			"			\"sugar\": \"R\"\n" + 
    			"		}],\n" + 
    			"		\"modifications\": {\n" + 
    			"			\"structuralModifications\": [],\n" + 
    			"			\"physicalModifications\": [],\n" + 
    			"			\"agentModifications\": []\n" + 
    			"		},\n" + 
    			"		\"nucleicAcidSubType\": [\"\"],\n" + 
    			"		\"references\": [],\n" + 
    			"		\"access\": []\n" + 
    			"	},\n" + 
    			"	\"approvalID\": \"FUA17JE46Y\",\n" + 
    			"	\"status\": \"approved\",\n" + 
    			"	\"approvedBy\": \"FDA_SRS\",\n" + 
    			"	\"approved\": 1449802860146,\n" + 
    			"	\"substanceClass\": \"nucleicAcid\",\n" + 
    			"	\"names\": ["+
    			"   {\n" + 
    			"		\"name\": \"TGFF-2 ANTI-SENSE RNA (6-935)\",\n" + 
    			"		\"type\": \"cn\",\n" + 
    			"		\"domains\": [],\n" + 
    			"		\"uuid\": \"99980b4b-ee4f-4aac-92dc-5a74387528a8\",\n" + 
    			"		\"languages\": [\"en\"],\n" + 
    			"		\"references\": [\"6dc80b4b-ee4f-4aac-92dc-5a74387528a8\"],\n" + 
    			"		\"preferred\": true,\n" + 
    			"		\"access\": []\n" + 
    			"	},"+
    			"   {\n" + 
    			"		\"name\": \"TGFβ-2 ANTI-SENSE RNA (6-935)\",\n" + 
    			"		\"type\": \"of\",\n" + 
    			"		\"domains\": [],\n" + 
    			"		\"uuid\": \"77780b4b-ee4f-4aac-92dc-5a74387528a8\",\n" + 
    			"		\"languages\": [\"en\"],\n" + 
    			"		\"references\": [\"6dc80b4b-ee4f-4aac-92dc-5a74387528a8\"],\n" + 
    			"		\"preferred\": true,\n" + 
    			"		\"access\": []\n" + 
    			"	}"+
			"],\n" + 
    			"	\"codes\": [],\n" + 
    			"	\"relationships\": [],\n" + 
    			"	\"references\": ["+
    			
    			" {\n" + 
    			"		\"uuid\": \"3316964d-c2ce-45ce-9f08-813e1f23b457\",\n" + 
    			"		\"citation\": \"SRS import [FUA17JE46Y]\",\n" + 
    			"		\"docType\": \"SRS\",\n" + 
    			"		\"publicDomain\": true,\n" + 
    			"		\"documentDate\": \"2015-12-10T22:01:00.000-0500\",\n" + 
    			"		\"tags\": [\"NOMEN\"],\n" + 
    			"		\"url\": \"http://fdasis.nlm.nih.gov/srs/srsdirect.jsp?regno\\u003dFUA17JE46Y\",\n" + 
    			"		\"access\": []\n" + 
    			"	},"+
    			"{\n" + 
    			"		\"uuid\": \"6dc80b4b-ee4f-4aac-92dc-5a74387528a8\",\n" + 
    			"		\"citation\": \"J Clin Oncol 2006; 24: 4721?4730\",\n" + 
    			"		\"docType\": \"SRS\",\n" + 
    			"		\"publicDomain\": true,\n" + 
    			"		\"tags\": [\"NOMEN\"],\n" + 
    			"		\"access\": []\n" + 
    			"	}"+
    			"],\n" + 
    			"	\"properties\": [],\n" + 
    			"	\"notes\": [],\n" + 
    			"	\"tags\": [],\n" + 
    			"	\"uuid\": \"95f72217-0244-4123-8c02-d6e313eb48b4\",\n" + 
    			"	\"access\": [],\n" + 
    			"	\"lastEditedBy\": \"FDA_SRS\"\n" + 
    			"}");
    	Substance s1a=om1.treeToValue(js1, Substance.class);
    	Substance s1b=om1.treeToValue(js1, Substance.class);
    	Substance s2a=om1.treeToValue(js2, Substance.class);
    	
    	PojoPatch p1=     PojoDiff.getEnhancedDiff(s1a,s2a);
    	PojoPatch p2=     PojoDiff.getDiff(s1b,s2a);
    	
    	p1.apply(s1a);
    	p2.apply(s1b);
    	
    	JsonNode jsdiff = PojoDiff.getEnhancedJsonDiff(s1a, s1b,null);
    	
    	for(JsonNode jsd:jsdiff){
    		System.err.println("Diff is:" + jsd.toString());
    	}
    	assertTrue(jsdiff.size()<=0);
    	
    	JsonNode jsdiff2 = PojoDiff.getJsonDiff(s1a, s1b);
    	
    	for(JsonNode jsd:jsdiff2){
    		System.err.println("Lazy Diff is:" + jsd.toString());
    	}
    	
    }
}
