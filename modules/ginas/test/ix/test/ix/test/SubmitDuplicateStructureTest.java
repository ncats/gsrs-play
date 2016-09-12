package ix.test.ix.test;

import com.fasterxml.jackson.databind.JsonNode;
import ix.test.SubstanceJsonUtil;
import ix.test.builder.ChemicalSubstanceBuilder;
import ix.test.builder.SubstanceBuilder;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;

import static ix.test.SubstanceJsonUtil.ensurePass;

/**
 * Created by katzelda on 9/9/16.
 */
public class SubmitDuplicateStructureTest {

    @Rule
    public GinasTestServer ts = new GinasTestServer();

    private SubstanceAPI api;
    private RestSession session;

    @Before
    public void login(){
        session = ts.newRestSession(ts.getFakeUser1());

        api = new SubstanceAPI(session);
    }

    @Test
    public void submitChemicalSubstanceWithStructureOnce() throws IOException{

        ChemicalSubstanceBuilder builder = SubstanceBuilder.from(new File("test/testJSON/pass/2moities.json"));

        JsonNode js = builder.buildJson();
        SubstanceAPI.ValidationResponse validationResponse = api.validateSubstance(js);

        assertTrue(validationResponse.isValid());

      //  ensurePass(api.submitSubstance(js));
    }
}
