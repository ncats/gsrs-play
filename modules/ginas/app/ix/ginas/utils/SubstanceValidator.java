package ix.ginas.utils;

import java.util.ArrayList;
import java.util.List;

import ix.core.ValidationMessage;
import ix.core.Validator;
import ix.ginas.models.v1.Substance;

public class SubstanceValidator implements Validator<Substance>{
	GinasProcessingStrategy _strategy;
	
	public SubstanceValidator(GinasProcessingStrategy strategy){
		_strategy=strategy;
	}
	
	@Override
	public boolean validate(Substance s, List<ValidationMessage> validation) {
		List<GinasProcessingMessage> vlad =Validation.validateAndPrepare(s, _strategy);			
		
		if(validation!=null && vlad!=null){
			for(ValidationMessage gpm:vlad){
				validation.add(gpm);
			}
		}
		boolean allow=_strategy.handleMessages(s, vlad);
		_strategy.addWarnings(s, vlad);
		return allow;
	}

	@Override
	public List<? extends ValidationMessage> getValidationMessageContainer() {
		return new ArrayList<GinasProcessingMessage>();
	}

}