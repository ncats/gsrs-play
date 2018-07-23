package ix.ginas.utils.validation.validators;

import ix.core.validator.ValidatorCallback;
import ix.ginas.initializers.LoadValidatorInitializer;
import ix.ginas.models.v1.Substance;

/**
 * Validator that is usually the only validator that supports
 * {@link ix.ginas.initializers.LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE#IGNORE}
 *
 * Created by katzelda on 5/15/18.
 */
public class IgnoreValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {
        callback.setValid();
    }

    @Override
    public boolean supports(Substance newValue, Substance oldValue, LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE methodType) {
        return methodType == LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE.IGNORE;
    }
}
