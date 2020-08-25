package ix.test.validation;

import ix.AbstractGinasServerTest;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidationMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.validation.validators.AbstractValidatorPlugin;
import ix.test.server.GinasTestServer;
import ix.test.server.RestSession;
import ix.test.server.SubstanceAPI;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ValidatorWithPropertiesTest extends AbstractGinasServerTest {
    public enum MessageType {

        WARNING{
            @Override
            ValidationMessage createMessage(String message) {
                return GinasProcessingMessage.WARNING_MESSAGE(message);
            }
        },
        ERROR{
            @Override
            ValidationMessage createMessage(String message) {
                return GinasProcessingMessage.ERROR_MESSAGE(message);
            }
        },
        INFO{
            @Override
            ValidationMessage createMessage(String message) {
                return GinasProcessingMessage.INFO_MESSAGE(message);
            }
        },
        ;
        abstract ValidationMessage createMessage(String message);
    }


    public static class MyValidator extends AbstractValidatorPlugin<Substance> {
        private Pattern codePattern;
        private String codeSystem;

        private MessageType messageType = MessageType.WARNING;

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

        public String getCodeSystem() {
            return codeSystem;
        }

        public void setCodeSystem(String codeSystem) {
            this.codeSystem = codeSystem;
        }

        public Pattern getCodePattern() {
            return codePattern;
        }

        public void setCodePattern(Pattern codePattern) {
            this.codePattern = codePattern;
        }

        @Override
        public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {

            boolean found = objnew.getCodes()
                    .stream()
                    .filter(c-> codeSystem.equals(c.codeSystem))
                    .anyMatch(c->
                            codePattern.matcher(c.code).find()
                    );
            if(!found){
                callback.addMessage(messageType.createMessage("could not find code system " + codeSystem + " with pattern : " + codePattern));
            }
        }
    }

    @Override
    public GinasTestServer createGinasTestServer() {
        GinasTestServer ts= super.createGinasTestServer();

        String newConf = "substance.validators=" + "[{\n" +
                "    \"validatorClass\" = \"" + MyValidator.class.getName() + "\",\n" +
                "        \"newObjClass\" = \"ix.ginas.models.v1.Substance\",\n" +
                "\"parameters\" : {\n" +
                "\t\t\t\t\t\t\t\"codeSystem\" : \"myCodeSys\",\n" +
                "\t\t\t\t\t\t\t\"codePattern\" : \"foo[0-9]+bar\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "}]";
        System.out.println(newConf);
        ts.modifyConfig(newConf,
                GinasTestServer.ConfigOptions.ALL_TESTS);
        return ts;
    }

    @Test
    public void substancesThatMatchPatternAreOK(){
        Substance s = new SubstanceBuilder()
                .addCode("myCodeSys", "foo123bar")
                .build();
        try(RestSession session = ts.newRestSession(ts.getAdmin())){
            SubstanceAPI api = session.newSubstanceAPI();
            SubstanceAPI.ValidationResponse resp = api.validateSubstance(s.toFullJsonNode());
            resp.assertValid();
            List<ValidationMessage> filteredMessages = removeSuccessMessage(resp.getMessages());

            System.out.println(filteredMessages);
            assertTrue(filteredMessages.isEmpty());
        }
    }

    @Test
    public void substancesThatDoesntHaveMatchPatternIsWarningByDefault(){
        Substance s = new SubstanceBuilder()
                .addCode("otherCodeSys", "foo123bar")
                .build();
        try(RestSession session = ts.newRestSession(ts.getAdmin())){
            SubstanceAPI api = session.newSubstanceAPI();
            SubstanceAPI.ValidationResponse resp = api.validateSubstance(s.toFullJsonNode());
            resp.assertValid();
            List<ValidationMessage> filteredMessages = removeSuccessMessage(resp.getMessages());

            System.out.println(filteredMessages);
            assertEquals(Arrays.asList(GinasProcessingMessage.WARNING_MESSAGE("could not find code system myCodeSys with pattern : foo[0-9]+bar").toString()), filteredMessages.stream()
                                                    .map(Object::toString)
                                                    .collect(Collectors.toList()));
        }
    }

    @Test
    public void overideMessateTypeToError(){
        String newConf = "substance.validators=" + "[{\n" +
                "    \"validatorClass\" = \"" + MyValidator.class.getName() + "\",\n" +
                "        \"newObjClass\" = \"ix.ginas.models.v1.Substance\",\n" +
                "\"parameters\" : {\n" +
                "\t\t\t\t\t\t\t\"codeSystem\" : \"myCodeSys\",\n" +
                "\t\t\t\t\t\t\t\"codePattern\" : \"foo[0-9]+bar\"\n" +
                "\t\t\t\t\t\t\t\"messageType\" : \"ERROR\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "}]";
        ts.stop();
        ts.modifyConfig(newConf, GinasTestServer.ConfigOptions.THIS_TEST_ONLY);
        ts.start();

        Substance s = new SubstanceBuilder()
                .addCode("otherCodeSys", "foo123bar")
                .build();
        try(RestSession session = ts.newRestSession(ts.getAdmin())){
            SubstanceAPI api = session.newSubstanceAPI();
            SubstanceAPI.ValidationResponse resp = api.validateSubstance(s.toFullJsonNode());
            assertFalse(resp.isValid());
            List<ValidationMessage> filteredMessages = removeSuccessMessage(resp.getMessages());

            System.out.println(filteredMessages);
            assertEquals(Arrays.asList(GinasProcessingMessage.ERROR_MESSAGE("could not find code system myCodeSys with pattern : foo[0-9]+bar").toString()), filteredMessages.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()));
        }
    }

    private List<ValidationMessage> removeSuccessMessage(List<ValidationMessage> messages){
       return  messages.stream()
//               .peek(m -> System.out.println(m.getMessage()))
                .filter(m-> !("Substance is valid".equals(m.getMessage()) && m.getMessageType() == ValidationMessage.MESSAGE_TYPE.SUCCESS))
                .collect(Collectors.toList());
    }
}
