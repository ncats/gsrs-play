package ix.test.validation;

import com.fasterxml.jackson.databind.JsonNode;
import ix.AbstractGinasServerTest;
import ix.ginas.modelBuilders.ChemicalSubstanceBuilder;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.Substance;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

public class NoteValidationTest extends AbstractGinasServerTest {

    @Test
    public void nullNotesAreRemoved(){
        try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);

            JsonNode json = new SubstanceBuilder()
                    .addName("aName", name -> name.addLanguage("en"))
                    .addNote(null)
                    .buildJson();
            SubstanceAPI.ValidationResponse response = api.validateSubstance(json);

            assertTrue(response.getMessages().stream()
                    .filter(m -> "Null note objects are not allowed".equals(m.getMessage()))
                    .findAny()
                    .isPresent());

            Substance submittedSubstance =SubstanceBuilder.from(api.submitSubstanceJson(json)).build();

            assertFalse(submittedSubstance.notes.stream().filter(Objects::isNull).findAny().isPresent());

        }
    }

    @Test
    public void nullNotesStringChangedToEmptyString(){
        try (RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);

            Note note = new Note();
            note.note= null;
            JsonNode json = new SubstanceBuilder()
                    .addName("aName", name -> name.addLanguage("en"))
                    .addNote(note)
                    .buildJson();
            SubstanceAPI.ValidationResponse response = api.validateSubstance(json);

            assertTrue(response.getMessages().stream()
                    .filter(m -> "Note objects must have a populated note field. Setting to empty String".equals(m.getMessage()))
                    .findAny()
                    .isPresent());

            Substance submittedSubstance =SubstanceBuilder.from(api.submitSubstanceJson(json)).build();

            String noteString =submittedSubstance.notes.get(0).note;
            assertEquals("", noteString);

        }
    }
}
