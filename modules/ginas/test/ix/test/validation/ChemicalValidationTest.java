package ix.test.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.AbstractGinasServerTest;
import ix.core.util.RunOnly;
import ix.core.validator.ValidationMessage;
import ix.core.validator.ValidationResponse;
import ix.ginas.modelBuilders.ChemicalSubstanceBuilder;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;
import ix.test.SubstanceJsonUtil;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 8/27/18.
 */
public class ChemicalValidationTest extends AbstractGinasServerTest {


    private final ObjectMapper mapper = new ObjectMapper();
    @Test
    public void errorIfMissingStructureObj() {


        try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);

            SubstanceAPI.ValidationResponse response = api.validateSubstance(
                    new ChemicalSubstanceBuilder()
                            .addName("aName")
                            .buildJson());

            assertTrue(response.getMessages().stream()
                    .filter(m -> m.isError() && "Chemical substance must have a chemical structure".equals(m.getMessage()))
                    .findAny()
                    .isPresent());
        }
    }

    @Test
    public void smilesStructureShouldWarnAndConvertToMol() throws Exception{


        try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode toSubmit =
                    new ChemicalSubstanceBuilder()
                            .addName("aName")
                            .generateNewUUID()
                            .setStructure("c1ccccc1")
                            .buildJson();

            SubstanceAPI.ValidationResponse response = api.validateSubstance(toSubmit);




            assertTrue(response.isValid());
            //this is split up and stored as a variable fo

            List<ValidationMessage> messages = response.getMessages();

            assertTrue(messages.stream()
                    .filter(m -> m.getMessage().contains("should always be specified as mol"))
                    .findAny()
                    .isPresent());

            //now actually submit
            api.submitSubstance(toSubmit);
            JsonNode n = api.fetchSubstanceJsonByUuid(toSubmit.at("/uuid").textValue());

            String fetchedMol = ((ChemicalSubstance)SubstanceBuilder.from(n).build())
                    .structure.molfile;

            assertTrue(fetchedMol, fetchedMol.contains("M  END"));
        }
    }

    @Test
    public void definitionalHashChangeStructureOKAsFirstSubmission() throws Exception{


        try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode toSubmit =
                    new ChemicalSubstanceBuilder()
                            .addName("aName")
                            .generateNewUUID()
                            .setStructure("c1ccccc1")
                            .buildJson();

            SubstanceAPI.ValidationResponse response = api.validateSubstance(toSubmit);




            assertTrue(response.isValid());
            //this is split up and stored as a variable fo

            List<ValidationMessage> messages = response.getMessages();

            assertFalse(messages.stream()
                    .filter(m -> m.getMessage().contains("Definitional change"))
                    .findAny()
                    .isPresent());

        }
    }


}
