package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.validation.ValidationUtils;

import ix.core.validator.ValidatorCallback;
import java.util.Iterator;
import java.util.List;

/**
 * Created by katzelda on 5/14/18.
 */
public class CodesValidator extends AbstractValidatorPlugin<Substance> {
    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {

        Iterator<Code> codesIter = s.codes.iterator();
        while(codesIter.hasNext()){
            Code cd = codesIter.next();
            if (cd == null) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Null code objects are not allowed")
                        .appliableChange(true);
                callback.addMessage(mes, ()->codesIter.remove());
                continue;
            }
                if (ValidationUtils.isEffectivelyNull(cd.code)) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .ERROR_MESSAGE(
                                    "'Code' should not be null in code objects")
                            .appliableChange(true);
                    callback.addMessage(mes, ()-> cd.code="<no code>");

                }

                if (ValidationUtils.isEffectivelyNull(cd.codeSystem)) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .ERROR_MESSAGE(
                                    "'Code System' should not be null in code objects")
                            .appliableChange(true);
                    callback.addMessage(mes, ()->cd.codeSystem="<no system>");

                }

                if (ValidationUtils.isEffectivelyNull(cd.type)) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .WARNING_MESSAGE(
                                    "Must specify a code type for each name. Defaults to \"PRIMARY\" (PRIMARY)")
                            .appliableChange(true);
                    callback.addMessage(mes, ()-> cd.type="PRIMARY");

                }


            if (!ValidationUtils.validateReference(s, cd, callback, ValidationUtils.ReferenceAction.ALLOW)) {
                return;
            }

        }
        for (Code cd : s.codes) {
            try {
                List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory
                        .getSubstancesWithExactCode(100, 0, cd.code, cd.codeSystem);
                if (sr != null && !sr.isEmpty()) {
                    //TODO we only check the first hit?
                    //would be nice to say instead of possible duplciate hit say we got X hits
                    Substance s2 = sr.iterator().next();

                    if (s2.getUuid() != null && !s2.getUuid().equals(s.getUuid())) {
                        GinasProcessingMessage mes = GinasProcessingMessage
                                .WARNING_MESSAGE(
                                        "Code '"
                                                + cd.code
                                                + "'[" +cd.codeSystem
                                                + "] collides (possible duplicate) with existing code & codeSystem for substance:")
                                . addLink(GinasUtils.createSubstanceLink(s2));
                        callback.addMessage(mes);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
