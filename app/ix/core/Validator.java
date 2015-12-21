package ix.core;

import java.util.List;

public interface Validator<T> {
	/**
	 * Returns true if the object passes validation for submission
	 * at the appropriate level. Warnings and other conditions can be added to the validation
	 * messages.
	 * 
	 * If the validator returns true, the last message should be a success message.
	 * 
	 * 
	 * @param obj
	 * @return
	 */
	public boolean validate(T obj, List<ValidationMessage> validation);
	
	
	
	public List<? extends ValidationMessage> getValidationMessageContainer();
	
}


