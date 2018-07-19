package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;

/**
 * Created by katzelda on 5/14/18.
 */
public class PrimaryDefinitionValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        SubstanceReference sr = s.getPrimaryDefinitionReference();
        if (sr != null) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Primary definitions cannot be alternative definitions for other Primary definitions"));
        }
        for (SubstanceReference relsub : s
                .getAlternativeDefinitionReferences()) {
            Substance subAlternative = SubstanceFactory
                    .getFullSubstance(relsub);
            if (subAlternative.isPrimaryDefinition()) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Primary definitions cannot be alternative definitions for other Primary definitions"));
            }
        }

    }
}
