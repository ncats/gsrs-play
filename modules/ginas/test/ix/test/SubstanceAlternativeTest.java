package ix.test;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.diff.JsonDiff;

import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import ix.test.util.TestNamePrinter;
import util.json.JsonUtil;

public class SubstanceAlternativeTest {

    @Rule
    public TestNamePrinter printer = new TestNamePrinter();


    File resource ;
    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);
    private SubstanceAPI api;
    private RestSession session;

    @Before
    public void login(){
        //TODO do we need to specify token type?
        session = ts.newRestSession(ts.getFakeUser1(), RestSession.AUTH_TYPE.TOKEN);

        api = new SubstanceAPI(session);
    }

    @After
    public void logout(){
        session.logout();
    }

    @Test
    public void testAPIAlternativeSubstanceSubmitValidate()   throws Exception {
        //submit primary
        resource = new File("test/testJSON/alternative/Prim1.json");
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        String uuid = js.get("uuid").asText();
        JsonNode validationResult = api.validateSubstanceJson(js);
        SubstanceJsonUtil.ensureIsValid(validationResult);
        ensurePass(api.submitSubstance(js));

        //submit alternative
        resource = new File("test/testJSON/alternative/PostAlt.json");
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        String uuidA = jsA.get("uuid").asText();
        JsonNode validationResultA = api.validateSubstanceJson(jsA);
        SubstanceJsonUtil.ensureIsValid(validationResultA);
        ensurePass(api.submitSubstance(jsA));

        //check alternative relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuid(fetchedA);
        assertTrue(refUuidA.equals(uuid));

        //check primary relationship with alternative
        JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
        String refUuid = SubstanceJsonUtil.getRefUuid(fetched);
        assertTrue(refUuid.equals(uuidA));
    }

    @Test
    public void testAPIAlternativeSubstanceUpdate()   throws Exception {
        //submit primary
        resource = new File("test/testJSON/alternative/Prim1.json");
        JsonNode js = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        String uuid = js.get("uuid").asText();
        JsonNode validationResult = api.validateSubstanceJson(js);
        SubstanceJsonUtil.ensureIsValid(validationResult);
        ensurePass(api.submitSubstance(js));

        //submit alternative
        resource = new File("test/testJSON/alternative/PostAlt.json");
        JsonNode jsA = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
       
        String uuidA = jsA.get("uuid").asText();
        JsonNode validationResultA = api.validateSubstanceJson(jsA);
        SubstanceJsonUtil.ensureIsValid(validationResultA);
        ensurePass(api.submitSubstance(jsA));

        //check alternative relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJsonByUuid(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuid(fetchedA);
        assertTrue(refUuidA.equals(uuid));

        //check primary relationship with alternative
        JsonNode fetched = api.fetchSubstanceJsonByUuid(uuid);
        String refUuid = SubstanceJsonUtil.getRefUuid(fetched);
        assertTrue(refUuid.equals(uuidA));

        //submit new primary
        resource = new File("test/testJSON/alternative/Prim2.json");
        JsonNode jsNew = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        String uuidNew = jsNew.get("uuid").asText();
        JsonNode validationResultNew = api.validateSubstanceJson(jsNew);
        SubstanceJsonUtil.ensureIsValid(validationResultNew);
        ensurePass(api.submitSubstance(jsNew));

        //update alternative
        resource = new File("test/testJSON/alternative/PutAlt.json");
        JsonNode jsAUpdate = SubstanceJsonUtil.prepareUnapprovedPublic(JsonUtil.parseJsonFile(resource));
        JsonPatch jsp = JsonDiff.asJsonPatch(jsA,jsAUpdate);
        JsonNode newAVersion =jsp.apply(fetchedA);
        String uuidAUpdate = newAVersion.get("uuid").asText();
        JsonNode validationResultAUpdate = api.validateSubstanceJson(newAVersion);
        SubstanceJsonUtil.ensureIsValid(validationResultAUpdate);
        ensurePass(api.updateSubstance(newAVersion));

        //check primary has no relationships after alternative update
        JsonNode fetchedPrim = api.fetchSubstanceJsonByUuid(uuid);
        assertNull(fetchedPrim.get("relationships").get(0));

         //check alternative relationship with New primary
        JsonNode fetchedAUpdate = api.fetchSubstanceJsonByUuid(uuidAUpdate);
        String refUuidAUpdate = SubstanceJsonUtil.getRefUuid(fetchedAUpdate);
        assertTrue(refUuidAUpdate.equals(uuidNew));

        //check New primary relationship with alternative
        JsonNode fetchedNew = api.fetchSubstanceJsonByUuid(uuidNew);
        String refUuidNew = SubstanceJsonUtil.getRefUuid(fetchedNew);
        assertTrue(refUuidNew.equals(uuidAUpdate));
    }
}
