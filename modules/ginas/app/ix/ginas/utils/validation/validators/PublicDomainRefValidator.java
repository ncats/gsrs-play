package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.initializers.LoadValidatorInitializer;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;

/**
 * Created by katzelda on 5/16/18.
 */
public class PublicDomainRefValidator extends AbstractValidatorPlugin<Substance>{
    @Override
    public boolean supports(Substance newValue, Substance oldValue, LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE methodType) {
        return methodType != LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE.BATCH;
    }

    @Override
    public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {
        if (objnew.isPublic()){
            boolean allowed = objnew.references.stream()
                    .filter(Reference::isPublic)
                    .filter(Reference::isPublicDomain)
                    .filter(Reference::isPublicReleaseReference)
                    .findAny()
                    .isPresent();
            if (!allowed) {
                    callback.addMessage(GinasProcessingMessage
                            .ERROR_MESSAGE("Public records must have a PUBLIC DOMAIN reference with a '"
                                    + Reference.PUBLIC_DOMAIN_REF + "' tag"));

            }
            objnew.getDisplayName().ifPresent(dn->{
                if(!dn.isPublic()){
                    callback.addMessage(GinasProcessingMessage
                            .ERROR_MESSAGE("Display name \"" + dn.getName() + "\""
                                    + " must be public if the full record is public."));
                }
            });
        }
    }
}
