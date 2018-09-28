package ix.core.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import ix.core.validator.ValidationMessage.MESSAGE_TYPE;
import ix.core.models.BeanViews;

public class ValidationResponse<T> {
	private final List<ValidationMessage> validationMessages = new ArrayList<>();
	private boolean valid = true;
	private T newObject;
	

	@JsonCreator
	public ValidationResponse(@JsonProperty("validationMessages") List<ValidationMessage> validationMessages,
	@JsonProperty("valid") boolean valid){
		this.valid = valid;
		this.validationMessages.addAll(validationMessages == null? Collections.emptyList() : validationMessages);
	}
	public ValidationResponse(T obj){
		setNewObject(obj);
	}
	
	public void setNewObject(T obj){
	    this.newObject=obj;
	}
	
	public void addValidationMessage(ValidationMessage vm){
		this.validationMessages.add(vm);
	}
	
	public List<ValidationMessage> getValidationMessages() {
			return validationMessages.stream()
					.sorted()
					.collect(Collectors.toList());
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
	
	public boolean hasProblem(){
	    return this.getValidationMessages()
	                .stream()
	                .anyMatch(vm->vm.getMessageType().isProblem());
	}
	
	public boolean hasError(){
        return this.getValidationMessages()
                    .stream()
                    .anyMatch(vm->vm.getMessageType()== MESSAGE_TYPE.ERROR);
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

	public ValidationResponse<T> merge(ValidationResponse<T> other){
		if(this.newObject !=other.newObject){
			throw new IllegalStateException("validated object must be the same");
		}
		ValidationResponse<T> merged = new ValidationResponse<>(this.newObject);

		for(ValidationMessage m : this.validationMessages){
			merged.addValidationMessage(m);
		}
		for(ValidationMessage m : other.validationMessages){
			merged.addValidationMessage(m);
		}

		merged.valid = this.valid && other.valid;

		return merged;
	}

	@Override
	public String toString() {
		return "ValidationResponse{" +
				"validationMessages=" + validationMessages +
				", valid=" + valid +
				", newObject=" + newObject +
				'}';
	}
}
