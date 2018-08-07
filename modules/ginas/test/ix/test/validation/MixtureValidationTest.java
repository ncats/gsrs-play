package ix.test.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ix.AbstractGinasServerTest;
import ix.core.util.RunOnly;
import ix.core.validator.ValidationMessage;
import ix.core.validator.ValidationResponse;
import ix.ginas.modelBuilders.MixtureSubstanceBuilder;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Substance;
import ix.test.SubstanceJsonUtil;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Before;
import org.junit.Test;
import util.json.JsonUtilTest;

import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by katzelda on 7/20/18.
 */
public class MixtureValidationTest extends AbstractGinasServerTest{

    Substance s1, s2, s3;
    @Before
    public void loadReferenceSubstances(){
        try(RestSession restSession = ts.newRestSession(ts.getAdmin())){
            SubstanceAPI api = restSession.newSubstanceAPI();
            s1 = new SubstanceBuilder()
                    .addName("sub1")
                    .generateNewUUID()
                    .build();
            api.submitSubstance(s1);

            s2 = new SubstanceBuilder()
                    .addName("sub2")
                    .generateNewUUID()
                    .build();
            api.submitSubstance(s2);

            s3 = new SubstanceBuilder()
                    .addName("sub3")
                    .generateNewUUID()
                    .build();
            api.submitSubstance(s3);
        }
    }
    @Test
    public void mixtureWith2OneOfsIsOK(){

        try(RestSession restSession = ts.newRestSession(ts.getAdmin())) {
            SubstanceAPI api = restSession.newSubstanceAPI();

            JsonNode toSubmit = new MixtureSubstanceBuilder()
                    .addName("foo")
                    .addComponents("MAY_BE_PRESENT_ONE_OF", s1, s2)
                    .buildJson();

            SubstanceJsonUtil.ensurePass(api.submitSubstance(toSubmit));

        }
    }

    @Test
    public void refUnregisteredSubstanceIsWarning() throws Exception{

        try(RestSession restSession = ts.newRestSession(ts.getAdmin())) {
            SubstanceAPI api = restSession.newSubstanceAPI();

            Substance unregistered = new SubstanceBuilder()
                    .addName("unreg")
                    .generateNewUUID()
                    .build();

            //didn't submit
            JsonNode toSubmit = new MixtureSubstanceBuilder()
                    .addName("foo")
                    .addComponents("MAY_BE_PRESENT_ONE_OF", s1, unregistered)
                    .buildJson();

            //can't actually submit this because currently the submission of
            //a passing substance doesn't have a way to easily expose the validation
            //messages.  looks like they are added as notes but the format is different
            //and some information is lost.  could possibly be able to write a lossy converter...
            JsonNode result =  api.validateSubstance(toSubmit).asJson(); /*SubstanceJsonUtil.ensurePass(api.submitSubstance(toSubmit));*/
            System.out.println("submit result = " + result);
            ValidationResponse response = new ObjectMapper().treeToValue(
                    result, ValidationResponse.class);

            //this is split up and stored as a variable for java 8 type inference to work...
            Stream<ValidationMessage>s1 = response.getValidationMessages().stream();

            assertTrue(s1
                    .filter(m ->m.getMessageType() == ValidationMessage.MESSAGE_TYPE.WARNING)
                    .map(ValidationMessage::getMessage)
                    .filter(m-> m.contains("not yet registered"))
                    .findAny().isPresent());

        }
    }

    @Test
    public void mixtureMustHave2Components() throws Exception{

        try(RestSession restSession = ts.newRestSession(ts.getAdmin())) {
            SubstanceAPI api = restSession.newSubstanceAPI();

            JsonNode toSubmit = new MixtureSubstanceBuilder()
                    .addName("foo")
                    .addComponents("MUST_BE_PRESENT", s1)
                    .buildJson();

            JsonNode result = SubstanceJsonUtil.ensureFailure(api.submitSubstance(toSubmit));
            System.out.println(result);
            ValidationResponse response = new ObjectMapper().treeToValue(
                    result, ValidationResponse.class);



            assertFalse(response.isValid());
            //this is split up and stored as a variable for java 8 type inference to work...
            Stream<ValidationMessage>s1 = response.getValidationMessages().stream();

            assertTrue(s1
                    .filter(ValidationMessage::isError)
                    .map(ValidationMessage::getMessage)
                    .filter(m-> m.contains("at least 2"))
                    .findAny().isPresent());


        }
    }

    @Test
    public void cantRefSameSubstanceTwiceSameType() throws Exception{

        try(RestSession restSession = ts.newRestSession(ts.getAdmin())) {
            SubstanceAPI api = restSession.newSubstanceAPI();

            JsonNode toSubmit = new MixtureSubstanceBuilder()
                    .addName("foo")

                    .addComponents("MAY_BE_PRESENT_ONE_OF", s1, s1)
                    .buildJson();

            JsonNode result = SubstanceJsonUtil.ensureFailure(api.submitSubstance(toSubmit));
            System.out.println(result);
            ValidationResponse response = new ObjectMapper().treeToValue(
                    result, ValidationResponse.class);



            assertFalse(response.isValid());
            //this is split up and stored as a variable for java 8 type inference to work...
            Stream<ValidationMessage>s1 = response.getValidationMessages().stream();

            assertTrue(s1
                    .filter(ValidationMessage::isError)
                    .map(ValidationMessage::getMessage)
                    .filter(m-> m.contains("Cannot reference the same mixture substance twice"))
                    .findAny().isPresent());


        }
    }

    @Test
    public void mixtureMustAtLeast2OneOfComponents() throws Exception{

        try(RestSession restSession = ts.newRestSession(ts.getAdmin())) {
            SubstanceAPI api = restSession.newSubstanceAPI();

            JsonNode toSubmit = new MixtureSubstanceBuilder()
                    .addName("foo")
                    .addComponents("MUST_BE_PRESENT", s1)
                    .addComponents("MAY_BE_PRESENT_ONE_OF", s2)
                    .buildJson();

            JsonNode result = SubstanceJsonUtil.ensureFailure(api.submitSubstance(toSubmit));
            System.out.println(result);
            ValidationResponse response = new ObjectMapper().treeToValue(
                    result, ValidationResponse.class);



            assertFalse(response.isValid());
            //this is split up and stored as a variable for java 8 type inference to work...
            Stream<ValidationMessage>s1 = response.getValidationMessages().stream();

            assertTrue(s1
                    .filter(ValidationMessage::isError)
                    .map(ValidationMessage::getMessage)
                    .filter(m-> m.contains("Should have at least two \"One of\" components"))
                    .findAny().isPresent());


        }
    }
}
