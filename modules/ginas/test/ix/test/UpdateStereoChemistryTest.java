package ix.test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasServerTest;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import ix.test.util.TestNamePrinter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import util.json.JsonUtil;

import java.io.File;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;

public class UpdateStereoChemistryTest  extends AbstractGinasServerTest {
    final File chemicalResource=new File("test/testJSON/editChemical.json");

    
    private GinasTestServer.User fakeUser1;

    @Before
    public void getUsers(){
        fakeUser1 = ts.getFakeUser1();
    }

    @Test
    public void testStereoChemistryUpdate() throws Exception {

        RestSession session = ts.newRestSession(fakeUser1);

        SubstanceAPI api = new SubstanceAPI(session);
        JsonNode entered= SubstanceJsonUtil
                .prepareUnapprovedPublic(JsonUtil.parseJsonFile(chemicalResource));
        String uuid=entered.get("uuid").asText();
        ensurePass(api.submitSubstance(entered));
        JsonNode fetched=api.fetchSubstanceJsonByUuid(uuid);
        JsonNode structure = fetched.at("/structure");
        String stereoChem = structure.get("stereochemistry").asText();
        String opticalAct = structure.get("opticalActivity").asText();
        String atropisomerism = structure.get("atropisomerism").asText();
        assertEquals(stereoChem, "ACHIRAL");
        assertEquals(opticalAct, "NONE");
        assertEquals(atropisomerism, "No");

        JsonNode updated = new JsonUtil
                .JsonNodeBuilder(fetched)
                .set("/structure/stereochemistry", "EPIMERIC")
                .set("/structure/opticalActivity", "( + )")
                .set("/structure/atropisomerism", "Unknown")
                .build();

        ensurePass(api.updateSubstance(updated));
        JsonNode fetchedagain=api.fetchSubstanceJsonByUuid(uuid);
        JsonNode newStructure = fetchedagain.at("/structure");
        String newStereochem = newStructure.get("stereochemistry").asText();
        String newOptact = newStructure.get("opticalActivity").asText();
        String newatropi = newStructure.get("atropisomerism").asText();
        assertEquals(newStereochem, "EPIMERIC");
        assertEquals(newOptact, "( + )");
        assertEquals(newatropi, "Unknown");
    }

    @Test
    public void testMoietyUpdate() throws Exception {

        RestSession session = ts.newRestSession(fakeUser1);

        SubstanceAPI api = new SubstanceAPI(session);
        JsonNode entered= SubstanceJsonUtil
                .prepareUnapprovedPublic(JsonUtil.parseJsonFile(chemicalResource));
        String uuid=entered.get("uuid").asText();
        ensurePass(api.submitSubstance(entered));
        JsonNode fetched=api.fetchSubstanceJsonByUuid(uuid);
        JsonNode moiety = fetched.at("/moieties/0");
        String stereoChem = moiety.get("stereochemistry").asText();
        String opticalAct = moiety.get("opticalActivity").asText();
        assertEquals(stereoChem, "ACHIRAL");
        assertEquals(opticalAct, "NONE");

        JsonNode updated = new JsonUtil
                .JsonNodeBuilder(fetched)
                .set("/moieties/0/stereochemistry", "EPIMERIC")
                .set("/moieties/0/opticalActivity", "( + )")
                .add("/moieties/0/atropisomerism", "Unknown")
                .build();

        ensurePass(api.updateSubstance(updated));
        JsonNode fetchedagain=api.fetchSubstanceJsonByUuid(uuid);
        JsonNode newMoiety = fetchedagain.at("/moieties/0");
        String newStereochem = newMoiety.get("stereochemistry").asText();
        String newOptact = newMoiety.get("opticalActivity").asText();
        String newatropi = newMoiety.get("atropisomerism").asText();
        assertEquals(newStereochem, "EPIMERIC");
        assertEquals(newOptact, "( + )");
        assertEquals(newatropi, "Unknown");
    }
}
