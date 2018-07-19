package ix.core.validator;

public abstract class AbstractValidator<T> implements Validator<T> {

	public ValidationResponse<T> validate(T objnew){
		return this.validate(objnew, null);
	}
	
}
