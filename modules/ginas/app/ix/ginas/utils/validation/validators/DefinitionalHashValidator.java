package ix.ginas.utils.validation.validators;

import ix.core.models.DefinitionalElements;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidationMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Substance;

import java.util.Arrays;
import java.util.List;

/**
 * Created by katzelda on 2/11/19.
 */
public class DefinitionalHashValidator  extends AbstractValidatorPlugin<Substance>{

    /*
    When changing a structure or defining information the application should require
    you to enter a new reference or reaffirm the reference or references for the structure
    or defining information for an approved substance
     */
    @Override
    public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {

//        System.out.println("in def hash Validator");

        if(objold ==null || objnew.getApprovalID() ==null || (objnew.getApprovalID() !=null && objold.getApprovalID() ==null)){
            //new substance or not approved don't validate
            return;
        }
//        System.out.println("still in in def hash Validator");
        //if we're here, we are changing an approved substance
        DefinitionalElements newDefinitionalElements = objnew.getDefinitionalElements();
        DefinitionalElements oldDefinitionalElements = objold.getDefinitionalElements();

//        System.out.println("new def elements =\n " + newDefinitionalElements);
//        System.out.println("===========\nold def elements =\n " + oldDefinitionalElements);

//        System.out.println(Substance.toHexString(newDefinitionalElements.getDefinitionalHash()));
//        System.out.println(Substance.toHexString(oldDefinitionalElements.getDefinitionalHash()));
        if(!Arrays.equals(newDefinitionalElements.getDefinitionalHash(),
                oldDefinitionalElements.getDefinitionalHash())){
            //we have changed something "definitional"

            // only for approved substances
            //re-affirm can be a new warning that can be dismissed

            List<DefinitionalElements.DefinitionalElementDiff> diff = newDefinitionalElements.diff(oldDefinitionalElements);
            //quick hack in case the definitional elements are in a different order,
            //Arrays.equals() won't cut it. so need to do the more involved diff...
            if(!diff.isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .WARNING_MESSAGE("Definitional change(s) has been made: please re-affirm.  " + diff));
            }
        }

    }
}
