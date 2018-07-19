package ix.ginas.utils.validation;

import ix.core.validator.Validator;
import ix.ginas.initializers.LoadValidatorInitializer;

/**
 * Created by katzelda on 5/7/18.
 */
public interface ValidatorPlugin<T> extends Validator<T>{

    boolean supports(T newValue, T oldValue, LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE methodType);




}
