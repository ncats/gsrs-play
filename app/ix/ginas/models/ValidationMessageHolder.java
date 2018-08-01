package ix.ginas.models;

import ix.core.validator.GinasProcessingMessage;

import java.util.List;


public interface ValidationMessageHolder {
	void setValidationMessages(List<GinasProcessingMessage> messages);
	default List<GinasProcessingMessage> getValidationMessages(){
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
}
