package ix.ginas.utils.validation;

import java.util.List;

import ix.core.AbstractValidator;
import ix.core.UserFetcher;
import ix.core.ValidationMessage;
import ix.core.ValidationResponse;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasProcessingMessage;
import ix.ginas.utils.GinasProcessingStrategy;

public class DefaultSubstanceValidator extends AbstractValidator<Substance>{
	GinasProcessingStrategy _strategy;
	private static enum METHOD_TYPE{
		CREATE,
		UPDATE,
		APPROVE
	}
	METHOD_TYPE method=null;
	
	private UserProfile getCurrentUser(){
		return UserFetcher.getActingUserProfile(true);
	}
	
	public DefaultSubstanceValidator(GinasProcessingStrategy strategy, METHOD_TYPE method){
		_strategy=strategy;
		this.method=method;
	}
	
	public DefaultSubstanceValidator(GinasProcessingStrategy strategy){
		_strategy=strategy;
	}
	
	public static DefaultSubstanceValidator NEW_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.CREATE);
	}
	
	public static DefaultSubstanceValidator UPDATE_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.UPDATE);
	}
	
	@Override
	public ValidationResponse<Substance> validate(Substance objnew, Substance objold) {
		ValidationResponse<Substance> vr=new ValidationResponse<Substance>(objnew);
		vr.setInvalid();
		List<GinasProcessingMessage> vlad =Validation.validateAndPrepare(objnew, _strategy);			
		
		UserProfile up=getCurrentUser();
		if(up==null){
			up=UserProfile.GUEST();
		}
		
		if(objold!=null){
			if( objnew.getAccess().isEmpty() &&
			   !objold.getAccess().isEmpty()
					){
				if(
					   !(   up.hasRole(Role.Admin) ||
							up.hasRole(Role.SuperUpdate)
						)
				  ){
					vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Only superUpdate users can make a substance public"));
				}
			}
			
			if(objold.getApprovalID()!=null){
				if(!objold.getApprovalID().equals(objnew.getApprovalID())){
					//Can't change approvalID!!! (unless admin)
					if(up.hasRole(Role.Admin)){
						vlad.add(GinasProcessingMessage
								.WARNING_MESSAGE(
										"The approvalID for the record has changed. Was ('" +
										objold.getApprovalID() +
										"') but now is ('" + 
										objnew.getApprovalID() +
										"'). This is strongly discouraged.")
								);
					}else{
						vlad.add(GinasProcessingMessage
								.ERROR_MESSAGE(
										"The approvalID for the record has changed. Was ('" +
										objold.getApprovalID() +
										"') but now is ('" + 
										objnew.getApprovalID() +
										"'). This is not allowed, except by an admin.")
								);
					}
					
				}
			}
			
		}else{
			if (objnew.getAccess().isEmpty()) {
				if (!(up.hasRole(Role.Admin) || up.hasRole(Role.SuperDataEntry))) {
					vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Only superDataEntry users can make a substance public"));
				}
			}
		}
		
		if(vlad!=null){
			for(ValidationMessage gpm:vlad){
				vr.addValidationMessage(gpm);
			}
		}
		
		if(_strategy.handleMessages(objnew, vlad)){
			vr.setValid();
		}
		_strategy.addWarnings(objnew, vlad);
		return vr;
	}
	
	

	
}
