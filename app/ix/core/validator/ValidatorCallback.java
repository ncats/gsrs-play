package ix.core.validator;

/**
 * Created by katzelda on 7/17/18.
 */
public interface ValidatorCallback {
    void addMessage(ValidationMessage message);

    void addMessage(ValidationMessage message, Runnable applyAction);

    void setInvalid();

    void haltProcessing();

    void setValid();
}
