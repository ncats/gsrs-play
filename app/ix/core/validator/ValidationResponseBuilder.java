package ix.core.validator;

import java.util.function.Predicate;

/**
 * Created by katzelda on 7/17/18.
 */
public class ValidationResponseBuilder<T> implements ValidatorCallback {

    private final ValidationResponse<T> resp;

    private final Predicate<GinasProcessingMessage> shouldApplyPredicate;

    private boolean allowDuplicates;

    private volatile boolean halted = false;
    public ValidationResponseBuilder(T o, Predicate<GinasProcessingMessage> shouldApplyPredicate) {
        resp = new ValidationResponse<T>(o);
        this.shouldApplyPredicate = shouldApplyPredicate == null ? m -> true : shouldApplyPredicate;
    }


    @Override
    public void addMessage(ValidationMessage message) {
        addMessage(message, null);
    }

    @Override
    public void addMessage(ValidationMessage message, Runnable appyAction) {
        if(halted){
            return;
        }
        resp.addValidationMessage(message);
        if (appyAction != null && message instanceof GinasProcessingMessage) {
            GinasProcessingMessage gpm = (GinasProcessingMessage) message;
            if (gpm.suggestedChange && shouldApplyPredicate.test(gpm)) {
                try {
                    appyAction.run();
                    gpm.appliedChange = true;
                    //downgrade applied change from error to warning
                    //TODO or should we make the lambda do it ?
                    if (gpm.isError()) {
                        gpm.messageType = GinasProcessingMessage.MESSAGE_TYPE.WARNING;
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void setValid() {
        resp.setValid();
    }

    @Override
    public void setInvalid() {
        resp.setInvalid();
    }

    @Override
    public void haltProcessing() {
        halted = true;
    }

    public ValidationResponse<T> buildResponse() {
        if (!allowDuplicates) {
            for (ValidationMessage m : resp.getValidationMessages()) {
                if (m instanceof GinasProcessingMessage) {
                    GinasProcessingMessage gpm = (GinasProcessingMessage) m;
                    if (gpm.isProblem() && gpm.isPossibleDuplicate()) {
                        gpm.makeError();
                    }
                }
            }
        }


        return resp;
    }

    public void allowPossibleDuplicates(boolean allowPossibleDuplicates) {
        allowDuplicates = allowPossibleDuplicates;
    }

}
