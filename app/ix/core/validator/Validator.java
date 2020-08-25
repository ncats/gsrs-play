package ix.core.validator;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;


public interface Validator<T> {
	/**
	 * Create an empty validator that returns an Validation
	 * response that is marked as valid without
	 * any validation messages.
	 * @param <T>
	 * @return a new Validator will never be null and will always be valid.
	 */
	static <T> Validator<T> emptyValid(){
		return new Validator<T>() {
			@Override
			public void validate(T objnew, T objold, ValidatorCallback callback) {

			}
		};
	}

	static <T> ValidationResponseBuilder<T> newValidationResponseCallback(T obj){
		return new ValidationResponseBuilder<T>(obj, null);
	}

		static <T> ValidationResponseBuilder<T> newValidationResponseCallback(T obj, Predicate<GinasProcessingMessage> applyPredicate){
		return new ValidationResponseBuilder<T>(obj, applyPredicate);
	}
	/**
	 * Returns true if the object passes validation for submission
	 * or editing at the appropriate level. Warnings and other 
	 * conditions can be added to the validation messages.
	 * 
	 * If this method returns true, the last message should be a success 
	 * message.
	 * 
	 * 
	 * @param objnew the new version of the object to be validated.
	 * @param objold the previous version of the object to be validated;
	 *               or null if there wasn't a previous version (for example, if it's a new registration)
	 *
	 */
	void validate(T objnew, T objold, ValidatorCallback callback);
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
	 * @return
	 */
	default ValidationResponse<T> validate(T objnew, T objold){
		ValidationResponse response = new ValidationResponse(objnew);
//		ValidationPro
		validate(objnew, objold, new ValidatorCallback() {
			@Override
			public void addMessage(ValidationMessage message) {
				response.addValidationMessage(message);
	}
	
			@Override
			public void setInvalid() {
				response.setInvalid();
			}

			@Override
			public void setValid() {
				response.setValid();
			}

			@Override
			public void haltProcessing() {

			}

			@Override
			public void addMessage(ValidationMessage message, Runnable appyAction) {
				response.addValidationMessage(message);

			}
		});
		if(response.hasError()){
			response.setInvalid();
		}

		return response;
	}


	/**
	 * Combine this validator with the other provided validator
	 * so that calling validate is the same as calling
	 * both validators and returning a combined response.
	 *
	 * @param other the other validator to combine.
	 * @return a new Validator object that is the combined
	 * validation of this and the other.
	 *
	 */
	default Validator<T> combine(Validator<T> other){
		return (o, n, callback) -> {
			AtomicBoolean halt = new AtomicBoolean(false);
			//The spy is just used to note if haltProcessing() was
			//called on this validator so we know
			//whether or not to call the other validator next
			ValidatorCallback spy = new ValidatorCallback() {
				@Override
				public void addMessage(ValidationMessage message) {
					callback.addMessage(message);
				}

				@Override
				public void setInvalid() {
					callback.setInvalid();
				}

				@Override
				public void setValid() {
					callback.setValid();
				}

				@Override
				public void haltProcessing() {
					callback.haltProcessing();
					halt.set(true);
				}

				@Override
				public void addMessage(ValidationMessage message, Runnable appyAction) {
					callback.addMessage(message, appyAction);
				}
			};

			validate(o,n,spy);
			if(!halt.get()) {
				other.validate(o, n, callback);
			}

		};

	}
}


