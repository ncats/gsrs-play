package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Substance;

/**
 * Created by katzelda on 5/7/18.
 */
public class SubstanceStatusValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        if(s.status == null){
            GinasProcessingMessage mes = GinasProcessingMessage
                    .WARNING_MESSAGE(
                            "No status specified for substance, defaulting to 'pending'")
                    .appliableChange(true);
            callback.addMessage(mes, ()-> s.status=Substance.STATUS_PENDING);
        }
    }
}
