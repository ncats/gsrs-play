package ix.core;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

import ix.core.models.BeanViews;

public class ValidationResponse<T> {
	private List<ValidationMessage> validationMessages;
	private boolean valid;
	private T newObject;
	
	public ValidationResponse(T obj){
		this.newObject=obj;
	}
	
	public void addValidationMessage(ValidationMessage vm){
		if(validationMessages==null){
			validationMessages=new ArrayList<ValidationMessage>();
		}
		this.validationMessages.add(vm);
	}
	
	public List<ValidationMessage> getValidationMessages(){
		return validationMessages;
	}
	
	public void setInvalid(){
		this.valid=false;
	}
	
	public void setValid(){
		this.valid=true;
	}
	
	public boolean isValid(){
		return valid;
	}
	
	
	@JsonView(BeanViews.Full.class)
	public T getNewObect(){
		return newObject;
	}
	
	
	public static <K> ValidationResponse<K> VALID_VALIDATION_RESPONSE(K obj){
		ValidationResponse<K> vr=new ValidationResponse<K>(obj);
		vr.setValid();
		vr.addValidationMessage(new ValidationMessage(){

			@Override
			public boolean isError() {
				return false;
			}

			@Override
			public String getMessage() {
				return "SUCCESS";
			}

			@Override
			public MESSAGE_TYPE getMessageType() {
				return MESSAGE_TYPE.SUCCESS;
			}
			
		});
		return vr;
	}
	
}
