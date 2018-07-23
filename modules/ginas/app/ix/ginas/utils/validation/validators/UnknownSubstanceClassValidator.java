package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.*;

/**
 * Created by katzelda on 5/16/18.
 */
public class UnknownSubstanceClassValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        switch (s.substanceClass) {
            case chemical:
            case concept:
            case mixture:
            case nucleicAcid:
            case polymer:
            case protein:
            case structurallyDiverse:
            case reference:
            case specifiedSubstanceG1:
            case specifiedSubstanceG2:
            case specifiedSubstanceG3:
            case specifiedSubstanceG4:
            case unspecifiedSubstance:
                break;
            default:
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Substance class \"" + s.substanceClass
                                + "\" is not valid"));
                break;
        }
    }
}
