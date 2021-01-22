package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.validation.ValidationUtils;

import java.util.Iterator;

/**
 * Created by katzelda on 5/14/18.
 */
public class PrimaryRelationshipsValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {

        Iterator<Relationship> iter = s.relationships.iterator();
        while(iter.hasNext()){
            Relationship n = iter.next();
            if (ValidationUtils.isEffectivelyNull(n)) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "Null relationship objects are not allowed")
                        .appliableChange(true);
                callback.addMessage(mes, () -> iter.remove());

            }
            if (ValidationUtils.isEffectivelyNull(n.relatedSubstance)
               || (
		      ValidationUtils.isEffectivelyNull(n.relatedSubstance.refuuid) &&
		           (ValidationUtils.isEffectivelyNull(n.relatedSubstance.refPname) || n.relatedSubstance.refPname.equals("NO_NAME"))
		  )
               ) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .ERROR_MESSAGE(
                                "Relationships must specify a related substance");
                callback.addMessage(mes);
            }
            if(ValidationUtils.isEffectivelyNull(n.type)){
                GinasProcessingMessage mes = GinasProcessingMessage
                        .ERROR_MESSAGE(
                                "Relationships must specify a type");
                callback.addMessage(mes);
            }
            if (!ValidationUtils.validateReference(s, n, callback, ValidationUtils.ReferenceAction.ALLOW)) {
                //TODO should we return early here or keep going and validate the rest?
                return ;
            }
        }

        long parentList=s.relationships.stream()
                .filter(r->"SUBSTANCE->SUB_CONCEPT".equals(r.type))
                .count();

        if(parentList>1){
            GinasProcessingMessage mes = GinasProcessingMessage
                    .ERROR_MESSAGE(
                            "Variant concepts may not specify more than one parent record");
            callback.addMessage(mes);
        }else if(parentList==1 && (s.substanceClass != Substance.SubstanceClass.concept)){
            GinasProcessingMessage mes = GinasProcessingMessage
                    .ERROR_MESSAGE(
                            "Non-concepts may not be specified as subconcepts.");
            callback.addMessage(mes);
        }


    }
}
