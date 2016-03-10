package ix.core;

public class DefaultValidator<K> extends AbstractValidator<K>{

	
	@Override
	public ValidationResponse<K> validate(K objnew, K objold) {
		return ValidationResponse.VALID_VALIDATION_RESPONSE(objnew);
	}


}
