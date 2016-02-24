package ix.core;

import java.util.ArrayList;
import java.util.List;


public class DefaultValidator implements Validator{

	@Override
	public boolean validate(Object obj, List validation) {
		return true;
	}

	@Override
	public List getValidationMessageContainer() {
		return new ArrayList<ValidationMessage>();
	}

}
