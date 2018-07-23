package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.core.validator.ValidatorCallback;
import ix.ginas.initializers.LoadValidatorInitializer;
import ix.ginas.models.v1.Substance;

/**
 * Created by katzelda on 5/14/18.
 */
public class NewSubstanceNonBatchLoadValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {
        UserProfile up=getCurrentUser();
        if (objnew.isPublic()) {
            if (!(up.hasRole(Role.Admin) || up.hasRole(Role.SuperDataEntry))) {
                callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("Only superDataEntry users can make a substance public"));
            }
        }
        if(objnew.approvalID!=null){
            callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("Cannot give an approvalID to a new substance"));
        }
    }

    @Override
    public boolean supports(Substance newValue, Substance oldValue, LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE methodType) {
        return methodType != LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE.BATCH && oldValue ==null;
    }

}
