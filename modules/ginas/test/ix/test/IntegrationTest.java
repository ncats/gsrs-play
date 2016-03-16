package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.controllers.EntityFactory;
import ix.ginas.models.v1.Substance;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {
	
	@Rule
	public GinasTestServer ts = new GinasTestServer(9001);


    @Test
    public void testRestAPISubstance() throws Exception {
        JsonNode substances = ts.getNotLoggedInSession().urlJSON("http://localhost:9001/ginas/app/api/v1/substances");
        assertTrue(!substances.isNull());
    }
    
    
    @Test 
    public void ensureSetupUsers(){
        GinasTestServer.UserSession user = ts.loginFakeUser1();
        assertEquals(GinasTestServer.FAKE_USER_1, user.getUserName());

    }
    
    @Test
    public void testFakeUserLoginPassword() throws Exception {
        try(GinasTestServer.UserSession session = ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1)) {
            session.withPasswordAuth();

            assertEquals(GinasTestServer.FAKE_USER_1, session.whoamiUsername());
        }
    }
    
    @Test
    public void testFakeUserLoginKey() throws Exception {
        try(GinasTestServer.UserSession session = ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1)) {
            session.withKeyAuth();

            assertEquals(GinasTestServer.FAKE_USER_1, session.whoamiUsername());
        }
    }
    

    @Test
    public void testFakeUserLoginToken() throws Exception {
        try(GinasTestServer.UserSession session = ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1)) {
            session.withTokenAuth();

            assertEquals(GinasTestServer.FAKE_USER_1, session.whoamiUsername());
        }
    	
    }
    
    @Test
    public void testFakeUserLoginNone() throws Exception {
        try(GinasTestServer.UserSession session = ts.login(GinasTestServer.FAKE_USER_1, GinasTestServer.FAKE_PASSWORD_1,
                GinasTestServer.AUTH_TYPE.NONE)) {
            session.whoamiFail();
        }
    	
    }
    

    @Test
    public void testRestAPIVocabularies()  throws Exception {
        try(GinasTestServer.UserSession session = ts.getNotLoggedInSession()) {
            JsonNode vacabs = session.vocabulariesJSON();
            JsonNode content = vacabs.at("/content");
            assertTrue("There should be content found in CV response", !content.isNull());
            assertTrue("There should be more than 0 CVs loaded, found (" + content.size() + ")", content.size() >= 1);
            assertTrue("There should be more than 0 CVs listed in total, found (" + vacabs.at("/total").asText() + ")", vacabs.at("/total").asInt() >= 1);
        }
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
    	
    	JsonNode jsdiff = PojoDiff.getEnhancedJsonDiff(s1a, s1b);
    	
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
