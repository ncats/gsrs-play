package ix.ginas.utils.validation.validators;

import ix.core.models.DefinitionalElement;
import ix.core.models.DefinitionalElements;
import ix.core.models.DefinitionalElements.DefinitionalElementDiff.OP;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.core.util.LogUtil;
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
//				System.out.println("in def hash Validator with substance of type " + objnew.substanceClass.name());
		LogUtil.trace(()->"in def hash Validator with substance of type " + objnew.substanceClass.name());

        /*if(objold ==null || objnew.getApprovalID() ==null || (objnew.getApprovalID() !=null && objold.getApprovalID() ==null)){
            //new substance or not approved don't validate
            return;
        }*/
        DefinitionalElements newDefinitionalElements = objnew.getDefinitionalElements();
        DefinitionalElements oldDefinitionalElements = new DefinitionalElements(new ArrayList<DefinitionalElement>());
				try	{
					oldDefinitionalElements =objold.getDefinitionalElements();
				}
				catch(Exception e){
						Logger.warn("Unable to access definitional elements for old substance");
					return;
				}

        if(!Arrays.equals(newDefinitionalElements.getDefinitionalHash(),
                oldDefinitionalElements.getDefinitionalHash())){
            //we have changed something "definitional"

            List<DefinitionalElements.DefinitionalElementDiff> diff = newDefinitionalElements.diff(oldDefinitionalElements);
            //quick hack in case the definitional elements are in a different order,
            //Arrays.equals() won't cut it. so need to do the more involved diff...
            if(!diff.isEmpty()) {
								if( changesContainLayer(diff, 1) && objnew.status.equals("approved")) {
										Logger.trace("approved substance with change to layer 1 ");
										// only for approved substances
										//confirm can be a new warning that can be dismissed
										UserProfile up=getCurrentUser();
										if(!up.hasRole(Role.Admin)) {
											/*
											This section related to GSRS-1347 (March 2020)
											When a user makes a change to an approved sustance (with a UNII) and the user is _not_ an admin
											-- but _is_ a super updated because regular updaters are not allowed to update approved substances
											we display a strong warning.
											Test this by making these types of changes:
											1) To a substance's level 1 hash (for example, by changing the structure of a Chemical)
												-> we expect a non-admin user to get the warning below
												-> we expect an admin user to get a warning about the specific changes to the def hash
											2) To a substance's level 2 hash (for example, by changing the stereochemistry field of a Chemical)
												-> we expect both types of user to get a warning about the specific changes to the def hash
											3) To a field outside of the def hash (for example, adding a name or code)
												-> no warning.
											*/
												String message =
													"WARNING! You have made a change to the fundamental definition of a validated substance. Are you sure you want to proceed with this change?";
												callback.addMessage(GinasProcessingMessage
													.WARNING_MESSAGE(message)
													.addNote(this.addNote));
												return;
										}
								}
								String message= createDiffMessage(diff);
								callback.addMessage(GinasProcessingMessage
										.WARNING_MESSAGE(message)
										.addNote(this.addNote));
								Logger.trace("in DefinitionalHashValidator, apending message " + message);
						} else {
								Logger.trace("diffs empty ");
						}
				} else {
					Logger.trace("Arrays equal");
				}
    }

		private boolean changesContainLayer(List<DefinitionalElements.DefinitionalElementDiff> changes, int layer) {
			Logger.trace("changed: ");

			boolean result= changes.stream().anyMatch(c-> (c.getOp().equals(OP.ADD) && c.getNewValue().getLayer()==layer)
							|| (c.getOp().equals(OP.REMOVED) && (c.getOldValue().getLayer() == layer))
							|| (c.getOp().equals(OP.CHANGED) && (c.getNewValue().getLayer() == layer || c.getOldValue().getLayer() == layer))
							);
			Logger.trace("changesContainLayer to return " + result);
			return result;
		}

		private String createDiffMessage(List<DefinitionalElements.DefinitionalElementDiff> diffs) {
			List<String> messageParts = new ArrayList();
			for(DefinitionalElements.DefinitionalElementDiff d : diffs){
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

			return message;
    }
}
