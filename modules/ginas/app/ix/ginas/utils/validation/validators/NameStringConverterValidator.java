package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import ix.utils.Util;

public class NameStringConverterValidator extends AbstractValidatorPlugin<Substance> {

    boolean markAsError = false;


    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        for (Name n : s.names) {
            if (n == null) {
                continue;
            }
            String name = n.getName();
            for (String errMsg : Util.getStringConverter().validationErrors(name)) {
                GinasProcessingMessage mes;

                if (markAsError) {
                    mes = GinasProcessingMessage
                            .ERROR_MESSAGE("Name '" + name + "' " + errMsg + ".");
                } else {
                    mes = GinasProcessingMessage
                            .WARNING_MESSAGE("Name '" + name + "' " + errMsg + ".");
                }
                callback.addMessage(mes);
            }
        }
    }

    public boolean isMarkAsError() {
        return markAsError;
    }

    public void setMarkAsError(boolean markAsError) {
        this.markAsError = markAsError;
    }
}