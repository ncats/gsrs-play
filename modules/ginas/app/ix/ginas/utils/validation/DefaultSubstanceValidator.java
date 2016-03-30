package ix.ginas.utils.validation;

import java.util.List;

import ix.core.AbstractValidator;
import ix.core.UserFetcher;
import ix.core.ValidationMessage;
import ix.core.ValidationResponse;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasProcessingMessage;
import ix.ginas.utils.GinasProcessingStrategy;
import ix.utils.TimeProfiler;
import play.Play;

public class DefaultSubstanceValidator extends AbstractValidator<Substance>{
	GinasProcessingStrategy _strategy;
	private static enum METHOD_TYPE{
		CREATE,
		UPDATE,
		APPROVE, 
		BATCH
	}
	METHOD_TYPE method=null;
	
	private UserProfile getCurrentUser(){
		UserProfile up= UserFetcher.getActingUserProfile(true);
		if(up==null){
			up=UserProfile.GUEST();
		}
		return up;
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
	public static DefaultSubstanceValidator BATCH_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.BATCH);
	}
	
	@Override
	public ValidationResponse<Substance> validate(Substance objnew, Substance objold) {
		String TIME_KEY="validating";
		TimeProfiler.addGlobalTime(TIME_KEY);
		ValidationResponse<Substance> vr=new ValidationResponse<Substance>(objnew);
		vr.setInvalid();
		try{
			List<GinasProcessingMessage> vlad =Validation.validateAndPrepare(objnew, _strategy);			
			
			if(this.method!=METHOD_TYPE.BATCH){
				if(objold!=null){
					changeSubstanceValation(objnew,objold,vlad);
				}else{
					addNewSubstanceValation(objnew,vlad);
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
	
	        if(GinasProcessingMessage.ALL_VALID(vlad)){
	        	vlad.add(GinasProcessingMessage.SUCCESS_MESSAGE("Substance is valid"));
	        }
	
			return vr;
		}catch(Exception e){
			throw e;
		}finally{
			TimeProfiler.stopGlobalTime(TIME_KEY);
		}
	}

	// only for old
	private void changeSubstanceValation(Substance objnew,Substance objold, List<GinasProcessingMessage> vlad) {
		UserProfile up = getCurrentUser();
		if(!objnew.getClass().equals(objold.getClass())){
			vlad.add(GinasProcessingMessage.WARNING_MESSAGE("Substance class should not typically be changed"));
		}
		
		if(!objold.version.equals(objnew.version)){
			vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Substance version '" + objnew.version +  "', does not match the stored version '" +  objold.version +"', record may have been changed while being updated"));
		}
		
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
				
				if(objold.approvalID!=null){
					if(!objold.approvalID.equals(objnew.approvalID)){
						//Can't change approvalID!!! (unless admin)
						if(up.hasRole(Role.Admin)){
							vlad.add(GinasProcessingMessage
									.WARNING_MESSAGE(
											"The approvalID for the record has changed. Was ('" +
											objold.approvalID +
											"') but now is ('" + 
											objnew.approvalID +
											"'). This is strongly discouraged.")
									);
						}else{
							vlad.add(GinasProcessingMessage
									.ERROR_MESSAGE(
											"The approvalID for the record has changed. Was ('" +
											objold.approvalID +
											"') but now is ('" + 
											objnew.approvalID +
											"'). This is not allowed, except by an admin.")
									);
						}
						
					}
				}
	}
	//only for new
	private void addNewSubstanceValation(Substance objnew,List<GinasProcessingMessage> vlad){
		
		UserProfile up=getCurrentUser();
		if (objnew.getAccess().isEmpty()) {
			if (!(up.hasRole(Role.Admin) || up.hasRole(Role.SuperDataEntry))) {
				vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Only superDataEntry users can make a substance public"));
			}
		}
		if(objnew.approvalID!=null){
				vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Cannot give an approvalID to a new substance"));
		}
	}
	

	
}
