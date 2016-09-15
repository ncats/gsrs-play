package ix.ginas.models;

import ix.core.GinasProcessingMessage;
import ix.core.models.Group;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface ValidationMessageHolder {
	void setValidationMessages(List<GinasProcessingMessage> messages);
	default List<GinasProcessingMessage> getValidationMessages(){
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
}
