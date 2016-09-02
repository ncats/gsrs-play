package ix.ginas.utils.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ix.core.AbstractValidator;
import ix.core.GinasProcessingMessage;
import ix.core.UserFetcher;
import ix.core.ValidationMessage;
import ix.core.ValidationResponse;
import ix.core.models.Role;
import ix.core.models.UserProfile;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasProcessingStrategy;
import ix.ginas.utils.GinasUtils;

public class DefaultSubstanceValidator extends AbstractValidator<Substance>{
	GinasProcessingStrategy _strategy;
	private static enum METHOD_TYPE{
		CREATE,
		UPDATE,
		APPROVE, 
		BATCH,
		IGNORE
	}
	METHOD_TYPE method=null;
	
	public static enum RECORD_CHANGES{
		
		
		
									// 1
		DEFINING_CHANGE,			//Something has changed (added / modified / removed) 
									//that's part of the "special" set of fields likely
									//to be defining
					
									// 2
		PUBLIC_CHANGE,				//Something has changed (added / modified / removed)
									//that's part of the public domain data,
									//therefore it would go down stream to NLM (and others)
		
									// 3
		APPROVAL_ID_GENERATED,		//An approvalID (UNII is generated)
		
								    // 4
		NEW_RECORD,					//A new record is put into the database

	    							// 5
		DEPRECATED,					//A record is being (effectively) removed from the
									//database 

									// 6
		INVOLVES_APPROVED_RECORD,	//Something has changed (added / modified / removed) 
									//on a record which has been approved
		
									// 7
		APPROVAL_ID_CHANGED_AFTER,	//The approval ID for an existing record
									//is changing (maybe a UNII switch, maybe 
									//manual override
		
									// 8
		CURRENT_USER_IS_PREVIOUS	//The current user making the change is also
									//the one responsible for the last change.
		
		
	}
	
	public Set<RECORD_CHANGES> change_types= new HashSet<RECORD_CHANGES>();
	
	
	
	//Is the change:
	// 1. Changing a defining element of a record?
	// 2. Causing a change in public visibility? (public data from report would change)
	// 3. Causing the generation of a new UNII / approvalID?
	// 4. Creating a new record?
	// 5. Changing an approved record?
	// 6. Deprecating an existing record?
//
//	
//	public static enum PRIVALEDGES{
//		QUERY,
//		REGISTER_NON_PUBLIC, //1,0,0,1
//		
//		EDIT_NON_APPROVED_NON_DEFINING_PUBLIC,//0,0,0,0
//		EDIT_NON_APPROVED_DEFINING_PUBLIC,    //0,0,0,1
//		EDIT_NON_APPROVED_NON_DEFINING_PUBLIC,//0,0,0,1
//		EDIT_NON_APPROVED_DEFINING_PUBLIC,
//		
//		EDIT_APPROVED_NON_DEFINING,
//		EDIT_APPROVED_DEFINING,
//		EDIT_PUBLIC,
//		
//		APPROVE_NEW,
//		CHANGE_APPROVAL_ID,
//		REGISTER_PUBLIC,
//
//
//		DEPRECATE_NON_APPROVED,
//		DEPRECATE_APPROVED,
//		DEPRECATE_PUBLIC,
//		DEPRECATE_NON_PUBLIC,
//		BULK_UPLOAD,
//		ADD_USERS,
//		CHANGE_USER_ROLES,
//		ADD_CV_TERM
////		APPROVE_EDITS_NON_DEFINING
////		APPROVE_EDITS_DEFINING
////		APPROVE_OWN_EDITS
////		MERGE,
//		
//		
//	}
//	
//	
	
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
	public static DefaultSubstanceValidator IGNORE_SUBSTANCE_VALIDATOR() {
		return new DefaultSubstanceValidator(null,METHOD_TYPE.IGNORE);
	}
	public static DefaultSubstanceValidator UPDATE_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.UPDATE);
	}
	public static DefaultSubstanceValidator BATCH_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.BATCH);
	}
	
	
//	public static enum RECORD_CHANGES{
//		DEFINING_CHANGE,          
//		PUBLIC_CHANGE,            // For now, just a public flag change?
//		APPROVAL_ID_GENERATED,    // 
//		NEW_RECORD,               // Easy
//		INVOLVES_APPROVED_RECORD, // Easy
//      
//		DEPRECATED
//	}
	
	@Override
	public ValidationResponse<Substance> validate(Substance objnew, Substance objold) {
		//Set<RECORD_CHANGES> categories = new HashSet<RECORD_CHANGES>();
		
		if(objold==null){
			change_types.add(RECORD_CHANGES.NEW_RECORD);
		}
		
		// PUBLIC CHANGE: (simple is did the main record pd change?)
		// 1. Find full set of elements in old object
		// 2. Find full set of elements in new object
		// 3. Restrict the elements from each list to only
		//    those that are public
		// 4. If the disjoint of the 2 sets is non-empty
		//    then there's a public change
		// 5. Then the tricky part of actually determining
		//    something else... TODO
		
		// DEFINING CHANGE:
		// Some extra subset from above? TODO
		
		
		
		// 
		
		
		
		// APPROVAL_ID_GENERATED (can't do it here)
		// 
		
		
		
		//TimeProfiler.addGlobalTime(TIME_KEY);
		ValidationResponse<Substance> vr=new ValidationResponse<Substance>(objnew);
		if(this.method==METHOD_TYPE.IGNORE){
			vr.setValid();
			return vr;
		}
		vr.setInvalid();
		try{
			List<GinasProcessingMessage> vlad =ValidationUtils.validateAndPrepare(objnew, _strategy);			
			
			//only for non-batch loads
			if(this.method!=METHOD_TYPE.BATCH){
				if(objold!=null){
					changeSubstanceValation(objnew,objold,vlad);
				}else{
					addNewSubstanceValation(objnew,vlad);
				}
				
			}
			if (objnew.isPublic()){
				boolean allowed = objnew.references.stream()
					.filter(Reference::isPublic)
					.filter(Reference::isPublicDomain)
					.filter(Reference::isPublicReleaseReference)
					.findAny().isPresent();
				
				if (!allowed) {
					if(this.method!=METHOD_TYPE.BATCH){
						vlad.add(GinasProcessingMessage
								.ERROR_MESSAGE("Public records must have a PUBLIC DOMAIN reference with a '"
										+ Reference.PUBLIC_DOMAIN_REF + "' tag"));
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
	
	        if(GinasProcessingMessage.ALL_VALID(vlad)){
	        	vlad.add(GinasProcessingMessage.SUCCESS_MESSAGE("Substance is valid"));
	        }
	
			return vr;
		}catch(Exception e){
			throw e;
		}finally{
			//TimeProfiler.stopGlobalTime(TIME_KEY);
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
		
		
		if (objnew.isPublic() && !objold.isPublic()) {
			if (!(up.hasRole(Role.Admin) || up.hasRole(Role.SuperUpdate))) {
				vlad.add(GinasProcessingMessage.ERROR_MESSAGE("Only superUpdate users can make a substance public"));
			}
		}
		
		

		if (objold.approvalID != null) {
			if (!objold.approvalID.equals(objnew.approvalID)) {
				
				
				
				// Can't change approvalID!!! (unless admin)
				if (up.hasRole(Role.Admin)) {
					if(!GinasUtils.getAPPROVAL_ID_GEN().isValidId(objnew.approvalID)){
						vlad.add(GinasProcessingMessage
								.ERROR_MESSAGE("The approvalID for the record has changed. Was ('" + objold.approvalID
										+ "') but now is ('" + objnew.approvalID + "'). This approvalID is either a duplicate or invalid."));
					}else{
						vlad.add(GinasProcessingMessage
							.WARNING_MESSAGE("The approvalID for the record has changed. Was ('" + objold.approvalID
									+ "') but now is ('" + objnew.approvalID + "'). This is strongly discouraged."));
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
	private void addNewSubstanceValation(Substance objnew,List<GinasProcessingMessage> vlad){
		
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
