package ix.test.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.AbstractGinasServerTest;
import ix.core.util.RunOnly;
import ix.core.validator.ValidationMessage;
import ix.ginas.modelBuilders.NucleicAcidSubstanceBuilder;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.Subunit;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;
/**
 * Created by katzelda on 8/9/18.
 */
public class NucleicAcidValidationTest extends AbstractGinasServerTest {

    @Test
    public void mustHaveSubunit(){
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode sub = new NucleicAcidSubstanceBuilder()
                    .addName("name")
                    .buildJson();

            SubstanceAPI.ValidationResponse response = api.validateSubstance(sub);
//            System.out.println(response.getMessages());
            assertFalse(response.isValid());

            assertTrue(response.getMessages().stream()
                    .filter(m->m.getMessageType() == ValidationMessage.MESSAGE_TYPE.ERROR &&  m.getMessage().contains("must have at least 1 subunit"))
                    .findAny().isPresent());
        }


    }

    private void assertArrayNotEquals(byte[] expecteds, byte[] actuals) {
        try {
            assertArrayEquals(expecteds, actuals);
        } catch (AssertionError e) {
            return;
        }
        fail("The arrays are equal");
    }

    @Test
    public void addingSubunitChangesDefinitionalHash(){
        String sequence = "ACGTACGTACGT";

        NucleicAcidSubstance sub = new NucleicAcidSubstanceBuilder()
                .addName("aName")
                .addDnaSubunit(sequence)
                .build();

        byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();


        byte[] newHash = new NucleicAcidSubstanceBuilder(sub).addDnaSubunit("AAAAAAA").build().getDefinitionalElements().getDefinitionalHash();

        assertArrayNotEquals(oldHash, newHash);
    }

    @Test
    public void changingSubunitChangesDefinitionalHash(){
        String sequence = "ACGTACGTACGT";

        NucleicAcidSubstance sub = new NucleicAcidSubstanceBuilder()
                .addName("aName")
                .addDnaSubunit(sequence)
                .build();

        byte[] oldHash = sub.getDefinitionalElements().getDefinitionalHash();

        sub.nucleicAcid.getSubunits().get(0).sequence = sequence + "NN";

        byte[] newHash = sub.getDefinitionalElements().getDefinitionalHash();

        assertArrayNotEquals(oldHash, newHash);
    }

    @Test
    public void dnaSeqMustReference() throws Exception{
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);


            NucleicAcidSubstance sub = new NucleicAcidSubstanceBuilder()
                    .addName("name")
                    .addDnaSubunit("ACGTACGT")
                    .build();


            sub.nucleicAcid.setReferences(Collections.emptySet());
            sub.references.clear();

            SubstanceAPI.ValidationResponse response = api.validateSubstance(sub.toFullJsonNode());
            assertFalse(response.isValid());

            assertTrue(response.getMessages().toString(), response.getMessages().stream()
                    .filter(m->m.getMessageType() == ValidationMessage.MESSAGE_TYPE.ERROR &&  m.getMessage().contains("needs at least 1 reference"))
                    .findAny().isPresent());
        }


    }

    @Test
    public void validDna() throws Exception{
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);


            NucleicAcidSubstance sub = new NucleicAcidSubstanceBuilder()
                    .addName("name")
                    .addDnaSubunit("ACGTACGT")
                    .build();



            SubstanceAPI.ValidationResponse response = api.validateSubstance(sub.toFullJsonNode());
            assertTrue(response.getMessages().toString(), response.isValid());

            assertFalse(response.getMessages().toString(), response.getMessages().stream()
                    .filter(m-> m.getMessage().contains("needs at least 1 reference"))
                    .findAny().isPresent());
        }


    }
    @Test
    public void invalidDnaSequence() throws Exception{
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode sub = new NucleicAcidSubstanceBuilder()
                    .addName("name")
                    .addDnaSubunit("ACGT&&&NNT")
                    .buildJson();


            SubstanceAPI.ValidationResponse response = api.validateSubstance(sub);
            assertFalse(response.isValid());

            assertTrue(response.getMessages().stream()
                    .filter(m->m.getMessageType() == ValidationMessage.MESSAGE_TYPE.ERROR &&  m.getMessage().contains("invalid character"))
                    .findAny().isPresent());
        }


    }
    @Test
    public void invalidRnaSequence() throws Exception{
        try( RestSession session = ts.newRestSession(ts.getFakeUser1())) {
            SubstanceAPI api = new SubstanceAPI(session);
            JsonNode sub = new NucleicAcidSubstanceBuilder()
                    .addName("name")
                    .addRnaSubunit("ACGU&&&NNU")
                    .buildJson();


            SubstanceAPI.ValidationResponse response = api.validateSubstance(sub);
            assertFalse(response.isValid());

            assertTrue(response.getMessages().stream()
                    .filter(m->m.getMessageType() == ValidationMessage.MESSAGE_TYPE.ERROR &&  m.getMessage().contains("invalid character"))
                    .findAny().isPresent());
        }


    }
}
