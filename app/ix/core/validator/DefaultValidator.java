package ix.core.validator;

public class DefaultValidator<K> extends AbstractValidator<K> {

	
	@Override
	public void validate(K objnew, K objold, ValidatorCallback callback) {

	}

	@Override
	public ValidationResponse<K> validate(K objnew, K objold) {
		return ValidationResponse.VALID_VALIDATION_RESPONSE(objnew);
	}


}
