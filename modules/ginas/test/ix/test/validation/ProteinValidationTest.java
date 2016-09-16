package ix.test.validation;

import static ix.test.SubstanceJsonUtil.ensurePass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import ix.core.ValidationMessage;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Protein;
import ix.test.SubstanceJsonUtil;
import ix.test.builder.SubstanceBuilder;
import ix.test.ix.test.server.GinasTestServer;
import ix.test.ix.test.server.RestSession;
import ix.test.ix.test.server.SubstanceAPI;
import ix.test.ix.test.server.SubstanceAPI.ValidationResponse;
import ix.test.util.TestNamePrinter;
import util.json.JsonUtil;

public class ProteinValidationTest {


    @Rule
    public TestNamePrinter printer = new TestNamePrinter();

    @Rule
    public GinasTestServer ts = new GinasTestServer(9001);
	

    
    @Test   
   	public void testProteinWithNoSubunitsShouldFailValidation() throws Exception {
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            
           new SubstanceBuilder()
        			.asProtein()
        			.setProtein(new Protein())
        			.addName("Just a test")
        			.buildJsonAnd(js->{
        				ValidationResponse vr=api.validateSubstance(js);
        				assertFalse(vr.isValid());
        				 SubstanceJsonUtil.ensureFailure( api.submitSubstance(js));
        			});
        }
   	}
    
   
    
}
