package ix.ginas.utils.validation.validators;

import ix.core.models.DefinitionalElement;
import ix.core.models.DefinitionalElements;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Substance;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import play.Logger;

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

        /*if(objold ==null || objnew.getApprovalID() ==null || (objnew.getApprovalID() !=null && objold.getApprovalID() ==null)){
            //new substance or not approved don't validate
            return;
        }*/
//        System.out.println("still in in def hash Validator");
        //if we're here, we are changing an approved substance
        DefinitionalElements newDefinitionalElements = objnew.getDefinitionalElements();
        DefinitionalElements oldDefinitionalElements = new DefinitionalElements(new ArrayList<DefinitionalElement>());
				try
				{
					oldDefinitionalElements =objold.getDefinitionalElements();
				}
				catch(NullPointerException npe)
				{
					System.out.println("Old substance has no DefinitionalElements");
					return;
				}


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
							List<String> messageParts = new ArrayList();
							for(DefinitionalElements.DefinitionalElementDiff d : diff){
								switch(d.getOp()) {
									case CHANGED :
										messageParts.add( String.format("definitional element %s changed from '%s' to '%s'",
												d.getNewValue().getKey(), d.getOldValue().getValue(), d.getNewValue().getValue()));
										break;
									case ADD :
										messageParts.add( String.format("definitional element %s added with value '%s'",
												d.getNewValue().getKey(), d.getNewValue().getValue()));
										break;
									case REMOVED :
										messageParts.add(String.format("definitional element %s with value '%s' was removed",
												d.getOldValue().getKey(), d.getOldValue().getValue()));
										break;
								}
							}
							String message;
							if(messageParts.size() == 1) {
								message ="A definitional change has been made: " +
															messageParts.get(0) +" please reaffirm.  ";
							} else {
								message ="Definitional changes have been made: " +
															String.join("; ", messageParts) +"; please reaffirm.  ";
							}
                callback.addMessage(GinasProcessingMessage
                      .WARNING_MESSAGE(message));
							Logger.debug("in DefinitionalHashValidator, apending message " + message);
						} else {
							Logger.debug("diffs empty ");
            }

        } else {
					Logger.debug("Arrays equal");
        }

    }
}
