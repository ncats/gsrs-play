package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.validation.ValidationUtils;

/**
 * Created by katzelda on 5/14/18.
 */
public class StructurallyDiverseValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {
        StructurallyDiverseSubstance cs = (StructurallyDiverseSubstance)objnew;
        if (cs.structurallyDiverse == null) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Structurally diverse substance must have a structurally diverse element"));
            return;
        }

            if (cs.structurallyDiverse.sourceMaterialClass == null
                    || cs.structurallyDiverse.sourceMaterialClass.equals("")) {

                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Structurally diverse substance must specify a sourceMaterialClass"));
            } else {
                if (cs.structurallyDiverse.sourceMaterialClass
                        .equals("ORGANISM")) {
                    boolean hasParent = false;
                    boolean hasTaxon = false;
                    if (cs.structurallyDiverse.parentSubstance != null) {
                        hasParent = true;
                    }
                    if (cs.structurallyDiverse.organismFamily != null
                            && !cs.structurallyDiverse.organismFamily
                            .equals("")) {
                        hasTaxon = true;
                    }
                    if (cs.structurallyDiverse.part == null
                            || cs.structurallyDiverse.part.isEmpty()) {
                        callback.addMessage(GinasProcessingMessage
                                .ERROR_MESSAGE("Structurally diverse organism substance must specify at least one (1) part"));
                    }
                    if (!hasParent && !hasTaxon) {
                        callback.addMessage(GinasProcessingMessage
                                .ERROR_MESSAGE("Structurally diverse organism substance must specify a parent substance, or a family"));
                    }
                    if (hasParent && hasTaxon) {
                        callback.addMessage(GinasProcessingMessage
                                .WARNING_MESSAGE("Structurally diverse organism substance typically should not specify both a parent and taxonomic information"));
            }
            if (cs.structurallyDiverse.sourceMaterialType == null
                    || cs.structurallyDiverse.sourceMaterialType.equals("")) {
                callback.addMessage(GinasProcessingMessage
                            .ERROR_MESSAGE("Organism Structurally diverse substance must specify a sourceMaterialType"));
            }
            }

        }

        if(!cs.structurallyDiverse.part.isEmpty()) {
            ValidationUtils.validateReference(cs, cs.structurallyDiverse, callback, ValidationUtils.ReferenceAction.FAIL);
        }


    }
}
