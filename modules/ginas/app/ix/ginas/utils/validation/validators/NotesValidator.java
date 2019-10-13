package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.validation.ValidationUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by katzelda on 5/14/18.
 */
public class NotesValidator extends AbstractValidatorPlugin<Substance>{
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {

        Iterator<Note> iter = s.notes.iterator();
        while(iter.hasNext()){
            Note n = iter.next();
            if (n == null) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Null note objects are not allowed")
                        .appliableChange(true);
                callback.addMessage(mes, () -> {
                    iter.remove();
                });
                continue;
            }
            if (n.note == null) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Note objects must have a populated note field. Setting to empty String")
                        .appliableChange(true);
                callback.addMessage(mes, () -> n.note= "");
                continue;
            }
            if (!ValidationUtils.validateReference(s,n, callback, ValidationUtils.ReferenceAction.ALLOW)) {
                //TODO should we really return here and not check the others?
                break ;
            }
        }

        //don't know if we need this but this way a new reference is made so ebean knows we changed stuff
        s.notes = new ArrayList<>(s.notes);

    }
}
