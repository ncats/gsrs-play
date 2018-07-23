package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Substance;

import java.util.UUID;

/**
 * Created by katzelda on 5/7/18.
 */
public class AutoGenerateUuidIfNeeded extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        if (s.getUuid() == null) {
        	UUID uuid = UUID.randomUUID();
            callback.addMessage(GinasProcessingMessage.INFO_MESSAGE("Substance has no UUID, will generated uuid:\"" + uuid + "\""),
                    ()->s.setUuid(uuid));
        }
    }
}
