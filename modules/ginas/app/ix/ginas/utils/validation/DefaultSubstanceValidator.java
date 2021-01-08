package ix.ginas.utils.validation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ix.core.*;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.core.validator.*;
import ix.core.initializers.LoadValidatorInitializer;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasProcessingStrategy;
import ix.ginas.utils.GinasUtils;

public class DefaultSubstanceValidator extends AbstractValidator<Substance> {
	GinasProcessingStrategy _strategy;
	private static enum METHOD_TYPE{
		CREATE,
		UPDATE,
		APPROVE, 
		BATCH,
		IGNORE
	}
	METHOD_TYPE method=null;
	
	public boolean allowNonTaggedPublicRecords= false;
	
	
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
		if(this.method== METHOD_TYPE.BATCH){
			allowNonTaggedPublicRecords=true;
		}
	}
	
	public DefaultSubstanceValidator(GinasProcessingStrategy strategy){
		_strategy=strategy;
	}
	
	public static DefaultSubstanceValidator NEW_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.CREATE);
	}
	public static DefaultSubstanceValidator IGNORE_SUBSTANCE_VALIDATOR() {
		return new DefaultSubstanceValidator(null,METHOD_TYPE.IGNORE);
	}
	public static DefaultSubstanceValidator UPDATE_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.UPDATE);
	}
	public static DefaultSubstanceValidator BATCH_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.BATCH);
	}
	
	@Override
	public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {

		Validator<Substance> validator = ValidatorFactory.getInstance().createValidatorFor(objnew, objold,
				this.method ==null? null: LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE.valueOf(this.method.name())
		);
		validator.validate(objnew, objold, callback);



//		//Some users can put in records flagged as possible duplicates
//		//some can't. We change some warnings to errors
//		boolean allowPossibleDuplicates=false;
//
//
//		if(		getCurrentUser().hasRole(Role.SuperUpdate) ||
//				getCurrentUser().hasRole(Role.SuperDataEntry) ||
//				getCurrentUser().hasRole(Role.Admin) ||
//				this.method==METHOD_TYPE.BATCH){
//			allowPossibleDuplicates=true;
//		}
//
//
//
//		ValidationResponse<Substance> vr=new ValidationResponse<Substance>(objnew);
//		if(this.method==METHOD_TYPE.IGNORE){
//			callback.setValid();
//			return;
//		}
//		callback.setInvalid();
//		try{
//			List<GinasProcessingMessage> vlad =ValidationUtils.validateAndPrepare(objnew, objold, _strategy);
//
//			//only for non-batch loads
//			if(this.method!=METHOD_TYPE.BATCH){
//				if(objold!=null){
//					changeSubstanceValidation(objnew,objold,vlad);
//				}else{
//					addNewSubstanceValidation(objnew,vlad);
//				}
//
//			}
//			if (objnew.isPublic()){
//				boolean allowed = objnew.references.stream()
//						.filter(Reference::isPublic)
//						.filter(Reference::isPublicDomain)
//						.filter(Reference::isPublicReleaseReference)
//						.findAny()
//						.isPresent();
//				if (!allowed) {
//					if(!allowNonTaggedPublicRecords){
//						vlad.add(GinasProcessingMessage
//								.ERROR_MESSAGE("Public records must have a PUBLIC DOMAIN reference with a '"
//										+ Reference.PUBLIC_DOMAIN_REF + "' tag"));
//					}
//				}
//			}
//
//			if(vlad!=null){
//				for(GinasProcessingMessage gpm:vlad){
//					if(gpm.isProblem() && gpm.isPossibleDuplicate()){
//						if(!allowPossibleDuplicates){
//							gpm.makeError();
//						}
//					}
//					callback.addMessage(gpm);
//				}
//			}
//
//			if(_strategy.handleMessages(objnew, vlad)){
//				vr.setValid();
//			}
//			_strategy.addProblems(objnew, vlad);
//
//
//
//			if(GinasProcessingMessage.ALL_VALID(vlad)){
//				vlad.add(GinasProcessingMessage.SUCCESS_MESSAGE("Substance is valid"));
//			}
//
//		}catch(Exception e){
//			throw e;
//		}finally{
//			//TimeProfiler.stopGlobalTime(TIME_KEY);
//		}
	}

	@Override
	public ValidationResponse<Substance> validate(Substance objnew, Substance objold) {
		
		
		ValidationResponseBuilder callback = new ValidationUtils.GinasValidationResponseBuilder(objnew, _strategy);

		//turn off duplicate checking in batch mode
		if(this.method == METHOD_TYPE.BATCH){
			callback.allowPossibleDuplicates(true);
		}
		
		this.validate(objnew, objold, callback);
		ValidationResponse<Substance> resp =  callback.buildResponse();

		List<GinasProcessingMessage> messages = resp.getValidationMessages()
													.stream()
													.filter(m-> m instanceof GinasProcessingMessage)
													.map(m ->(GinasProcessingMessage)m)
													.collect(Collectors.toList());
		messages.stream().forEach( _strategy::processMessage);
		if(_strategy.handleMessages(objnew, messages)){
			resp.setValid();
		}
		_strategy.addProblems(objnew, messages);


			
		if(GinasProcessingMessage.ALL_VALID(messages)){
			resp.addValidationMessage(GinasProcessingMessage.SUCCESS_MESSAGE("Substance is valid"));
						}
		return resp;

	}

	//TODO: All of this is ad-hoc, and needs to be moved to a more generic framework.
	
	// only for old
	private void changeSubstanceValidation(Substance objnew,Substance objold, List<GinasProcessingMessage> vlad) {
		UserProfile up = getCurrentUser();
		if(!objnew.getClass().equals(objold.getClass())){
			vlad.add(GinasProcessingMessage.WARNING_MESSAGE("Substance class should not typically be changed"));
		}
		
		if(!objold.version.equals(objnew.version)){
			vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Substance version '" + objnew.version +  "', does not match the stored version '" +  objold.version +"', record may have been changed while being updated"));
		}
		
		if (objnew.isPublic() && !objold.isPublic()) {
			if (!(up.hasRole(Role.Admin) || up.hasRole(Role.SuperUpdate))) {
				vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Only superUpdate users can make a substance public"));
			}
		}
		

		//Making a change to a validated record
		if (objnew.isValidated()) {
			if (!(up.hasRole(Role.Admin) || up.hasRole(Role.SuperUpdate))) {
				vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Only superUpdate users can update approved substances"));
			}
		}
		
		
		//Changed approvalID
		if (objold.approvalID != null) {
			if (!Objects.equals(objold.approvalID,objnew.approvalID)) {
				// Can't change approvalID!!! (unless admin)
				if (up.hasRole(Role.Admin)) {
					if("".equals(objnew.approvalID)){
						objnew.approvalID=null;
					}
					if(objnew.approvalID == null){
						vlad.add(GinasProcessingMessage
								.WARNING_MESSAGE("The approvalID for the record has been removed. The approvalID was \"" + objold.approvalID
										+ "\". Removing an approvalID is strongly discouraged."));
					}else{
						if(!GinasUtils.getApprovalIdGenerator().isValidId(objnew.approvalID)){
							vlad.add(GinasProcessingMessage
									.ERROR_MESSAGE("The approvalID for the record has changed. Was ('" + objold.approvalID
											+ "') but now is ('" + objnew.approvalID + "'). This approvalID is either a duplicate or invalid."));
						}else{
							vlad.add(GinasProcessingMessage
								.WARNING_MESSAGE("The approvalID for the record has changed. Was ('" + objold.approvalID
										+ "') but now is ('" + objnew.approvalID + "'). This is strongly discouraged."));
						}
					}
				} else {
					vlad.add(GinasProcessingMessage.ERROR_MESSAGE(
							"The approvalID for the record has changed. Was ('" + objold.approvalID + "') but now is ('"
									+ objnew.approvalID + "'). This is not allowed, except by an admin."));
				}
			}
		}
	}
	
	
	//only for new
	private void addNewSubstanceValidation(Substance objnew,List<GinasProcessingMessage> vlad){
		
		UserProfile up=getCurrentUser();
		if (objnew.isPublic()) {
			if (!(up.hasRole(Role.Admin) || up.hasRole(Role.SuperDataEntry))) {
				vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Only superDataEntry users can make a substance public"));
			}
		}
		if(objnew.approvalID!=null){
				vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Cannot give an approvalID to a new substance"));
		}
	}


	

	
}
