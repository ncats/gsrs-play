package ix.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.diff.JsonDiff;

import play.Logger;
import util.json.JsonUtil;


import static ix.test.SubstanceJsonUtil.*;

public class SubstanceAlternativeTest {

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + getClass().getName() + " . " + description.getMethodName());
        }
    };

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
        JsonNode js = SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));
        String uuid = js.get("uuid").asText();
        JsonNode validationResult = api.validateSubstanceJson(js);
        SubstanceJsonUtil.ensureIsValid(validationResult);
        ensurePass(api.submitSubstance(js));

        //submit alternative
        resource = new File("test/testJSON/alternative/PostAlt.json");
        JsonNode jsA = SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));
        String uuidA = jsA.get("uuid").asText();
        JsonNode validationResultA = api.validateSubstanceJson(jsA);
        SubstanceJsonUtil.ensureIsValid(validationResultA);
        ensurePass(api.submitSubstance(jsA));

        //check alternative relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJson(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuid(fetchedA);
        assertTrue(refUuidA.equals(uuid));

        //check primary relationship with alternative
        JsonNode fetched = api.fetchSubstanceJson(uuid);
        String refUuid = SubstanceJsonUtil.getRefUuid(fetched);
        assertTrue(refUuid.equals(uuidA));
    }

    @Test
    public void testAPIAlternativeUpdatePrimarySubstance()   throws Exception {

        //submit primary
        resource = new File("test/testJSON/alternative/Prim1.json");
        JsonNode js = SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));
        String uuid = js.get("uuid").asText();
        JsonNode validationResult = api.validateSubstanceJson(js);
        SubstanceJsonUtil.ensureIsValid(validationResult);
        ensurePass(api.submitSubstance(js));

        //submit alternative
        resource = new File("test/testJSON/alternative/PostAlt.json");
        JsonNode jsA = SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));
        String uuidA = jsA.get("uuid").asText();
        JsonNode validationResultA = api.validateSubstanceJson(jsA);
        SubstanceJsonUtil.ensureIsValid(validationResultA);
        ensurePass(api.submitSubstance(jsA));

        /*//check alternative relationship with primary
        JsonNode fetchedA = api.fetchSubstanceJson(uuidA);
        String refUuidA = SubstanceJsonUtil.getRefUuid(fetchedA);
        assertTrue(refUuidA.equals(uuid));

        //check primary relationship with alternative
        JsonNode fetched = api.fetchSubstanceJson(uuid);
        String refUuid = SubstanceJsonUtil.getRefUuid(fetched);
        assertTrue(refUuid.equals(uuidA));*/

        //submit new primary
        resource = new File("test/testJSON/alternative/Prim2.json");
        JsonNode jsNew = SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));
        String uuidNew = js.get("uuid").asText();
        JsonNode validationResultNew = api.validateSubstanceJson(jsNew);
        SubstanceJsonUtil.ensureIsValid(validationResultNew);
        ensurePass(api.submitSubstance(jsNew));

        //update alternative
        resource = new File("test/testJSON/alternative/PutAlt.json");
        JsonNode jsAUpdate = SubstanceJsonUtil.toUnapproved(JsonUtil.parseJsonFile(resource));
        String uuidAUpdate = jsAUpdate.get("uuid").asText();
        JsonNode validationResultAUpdate = api.validateSubstanceJson(jsAUpdate);
        SubstanceJsonUtil.ensureIsValid(validationResultAUpdate);
        ensurePass(api.updateSubstance(jsAUpdate));

        /*//check primary has no relationship with alternate
        JsonNode fetched = api.fetchSubstanceJson(uuid);
        String refUuid = SubstanceJsonUtil.getRefUuid(fetched);
        assertFalse(refUuid.equals(uuidAUpdate));

        //check alternative relationship with New primary
        JsonNode fetchedAUpdate = api.fetchSubstanceJson(uuidAUpdate);
        String refUuidAUpdate = SubstanceJsonUtil.getRefUuid(fetchedAUpdate);
        assertTrue(refUuidAUpdate.equals(uuidNew));

        //check New primary relationship with alternative
        JsonNode fetchedNew = api.fetchSubstanceJson(uuidNew);
        String refUuidNew = SubstanceJsonUtil.getRefUuid(fetchedNew);
        assertTrue(refUuidNew.equals(uuidAUpdate)); */
    }
}
