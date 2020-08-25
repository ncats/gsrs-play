package ix.ginas.utils.validation.validators;

import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.Substance;

public class PropertyValidator extends AbstractValidatorPlugin<Substance> {

	@Override
	public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {
		objnew.properties.stream().filter((property) -> ( property.getName() == null || property.getName().trim().length()==0)).forEachOrdered((_item) -> {
			callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("Property must have a name!"));
		});
	}
	
}