package ix.core.validator;

import ix.core.initializers.LoadValidatorInitializer;

/**
 * Created by katzelda on 5/7/18.
 */
public interface ValidatorPlugin<T> extends Validator<T>{

    boolean supports(T newValue, T oldValue, LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE methodType);




}
