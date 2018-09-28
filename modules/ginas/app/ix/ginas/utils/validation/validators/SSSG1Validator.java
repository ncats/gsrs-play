package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.validation.ValidationUtils;

/**
 * Created by katzelda on 5/14/18.
 */
public class SSSG1Validator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {
        SpecifiedSubstanceGroup1Substance cs = (SpecifiedSubstanceGroup1Substance) objnew;
        if (cs.specifiedSubstance == null) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Specified substance must have a specified substance component"));
            return;
        }

            if (cs.specifiedSubstance.constituents== null || cs.specifiedSubstance.constituents.isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Specified substance must have at least 1 constituent"));
            } else {
                cs.specifiedSubstance.constituents.stream()
                        .filter(c->c.substance==null)
                        .findAny()
                        .ifPresent(missingSubstance->{
                            callback.addMessage(GinasProcessingMessage
                                    .ERROR_MESSAGE("Specified substance constituents must have an associated substance record"));
                        });
            ValidationUtils.validateReference(cs, cs.specifiedSubstance, callback, ValidationUtils.ReferenceAction.FAIL);

            }


    }
}
