package ix.ginas.utils.validation;

import java.util.ArrayList;
import java.util.List;

import ix.core.ValidationMessage;
import ix.core.Validator;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.GinasProcessingMessage;
import ix.ginas.utils.GinasProcessingStrategy;

public class DefaultSubstanceValidator implements Validator<Substance>{
	GinasProcessingStrategy _strategy;
	private static enum METHOD_TYPE{
		CREATE,
		UPDATE,
		APPROVE
	}
	METHOD_TYPE method=null;
	
	public DefaultSubstanceValidator(GinasProcessingStrategy strategy, METHOD_TYPE method){
		_strategy=strategy;
		this.method=method;
	}
	
	public DefaultSubstanceValidator(GinasProcessingStrategy strategy){
		_strategy=strategy;
	}
	
	public static DefaultSubstanceValidator NEW_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.CREATE);
	}
	
	public static DefaultSubstanceValidator UPDATE_SUBSTANCE_VALIDATOR(GinasProcessingStrategy strategy){
		return new DefaultSubstanceValidator(strategy,METHOD_TYPE.UPDATE);
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
