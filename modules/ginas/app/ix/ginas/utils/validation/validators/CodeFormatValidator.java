package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.controllers.v1.ControlledVocabularyFactory;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.VocabularyTerm;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author Mitch Miller
 */
public class CodeFormatValidator extends AbstractValidatorPlugin<Substance>
{

    @Override
    public void validate(Substance s, Substance oldSubstance, ValidatorCallback callback)
    {
        Map<String, List<Code>> codesBySystem = s.getCodes().stream()
								.filter(c-> c.codeSystem != null)
                                .collect(Collectors.groupingBy(c-> c.codeSystem,
                                                Collectors.toList()));

        ControlledVocabulary cvv = ControlledVocabularyFactory.getControlledVocabulary("CODE_SYSTEM");
        for(VocabularyTerm vt1 : cvv.terms){

            String codeSystemRegex = vt1.regex;
            //codes will be null if does not exist in map
            List<Code> codes =  codesBySystem.get(vt1.value);
            if(codeSystemRegex != null && !codeSystemRegex.isEmpty() && codes !=null){
                Pattern codePattern = Pattern.compile(codeSystemRegex);
                for(Code c : codes){
                    Matcher matcher = codePattern.matcher(c.getCode());
                    //find ? or matches?
                    if(!matcher.find()){
                        callback.addMessage(GinasProcessingMessage
                                .WARNING_MESSAGE(String.format("Code %s does not match pattern %s for system %s", c.getCode(), codeSystemRegex, vt1.value)));
                    }
                }
            }
        }
    }

}
