package ix.core;

public interface Validator<T> {
	
	
	/**
	 * Returns true if the object passes validation for submission
	 * or editing at the appropriate level. Warnings and other 
	 * conditions can be added to the validation messages.
	 * 
	 * If this method returns true, the last message should be a success 
	 * message.
	 * 
	 * 
	 * @param objnew
	 * @param objold
	 * @param validation
	 * @return
	 */
	public ValidationResponse<T> validate(T objnew, T objold);
	
	/**
	 * Returns true if the object passes validation for submission.
	 * 
	 * This should be the same as calling 
	 * 
	 * validate(objnew,null,validation);
	 * 
	 * @param objnew
	 * @param validation
	 * @return
	 */
	public ValidationResponse<T> validate(T objnew);
	
	
}


